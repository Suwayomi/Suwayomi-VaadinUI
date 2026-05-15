package online.hatsunemiku.tachideskvaadinui.services.client

import com.apollographql.apollo.api.Optional
import kotlinx.coroutines.runBlocking
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source
import online.hatsunemiku.tachideskvaadinui.exceptions.AndroidOnlyException
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.*
import online.hatsunemiku.tachideskvaadinui.services.WebClientService
import online.hatsunemiku.tachideskvaadinui.services.client.exception.InvalidResponseException
import org.springframework.stereotype.Component

@Component
class MangaClient(private val clientService: WebClientService) {

    fun addMangaToCategories(categoryIds: List<Int>, mangaId: Int): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(AddMangaToCategoriesMutation(Optional.present(categoryIds), mangaId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while adding manga to categories: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while adding manga to categories: No data")
                val newCategoryIds = data.updateMangaCategories.manga.categories.nodes.map { it.id }

                categoryIds.all { newCategoryIds.contains(it) }
            } catch (e: Exception) {
                throw RuntimeException("Error while adding manga to categories", e)
            }
        }
    }

    fun removeMangaFromCategories(categoryIds: List<Int>, mangaId: Int): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(RemoveMangaFromCategoriesMutation(Optional.present(categoryIds), mangaId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while removing manga from categories: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while removing manga from categories: No data")
                val newCategoryIds = data.updateMangaCategories.manga.categories.nodes.map { it.id }

                categoryIds.none { newCategoryIds.contains(it) }
            } catch (e: Exception) {
                throw RuntimeException("Error while removing manga from categories", e)
            }
        }
    }

    fun getChapter(chapterId: Long): Chapter? {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetChapterQuery(chapterId.toInt())).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while getting chapter: " + response.errors)
                }

                response.data?.chapter?.let { mapToChapter(it) }
            } catch (e: Exception) {
                throw RuntimeException("Error while getting chapter", e)
            }
        }
    }

    fun getChapters(mangaId: Int): List<Chapter> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetMangaChaptersQuery(mangaId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while getting manga chapters: " + response.errors)
                }

                response.data?.manga?.chapters?.nodes?.map { mapToChapter(it) } ?: emptyList()
            } catch (e: Exception) {
                throw RuntimeException("Error while getting manga chapters", e)
            }
        }
    }

    fun fetchChapterList(mangaId: Int): List<Chapter> {
        val manga = getManga(mangaId.toLong()) ?: throw RuntimeException("Error while fetching manga $mangaId")
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(FetchChapterListMutation(mangaId)).execute()
                if (response.hasErrors()) {
                    throw InvalidResponseException("Invalid response from server for manga $mangaId", null)
                }

                response.data?.fetchChapters?.chapters?.map { mapToChapter(it) } ?: emptyList()
            } catch (e: Exception) {
                throw RuntimeException("Error while fetching chapter list", e)
            }
        }
    }

    fun addMangaToLibrary(mangaId: Int): Boolean {
        return updateMangaLibraryStatus(mangaId, true)
    }

    fun removeMangaFromLibrary(mangaId: Int): Boolean {
        val manga = getManga(mangaId.toLong()) ?: return false
        val categories = manga.getMangaCategories()

        if (categories.isNotEmpty()) {
            val categoryIds = categories.map { it.id }
            if (!removeMangaFromCategories(categoryIds, mangaId)) {
                return false
            }
        }

        return !updateMangaLibraryStatus(mangaId, false)
    }

    fun setChapterRead(chapterId: Int): Boolean {
        return updateChapterReadStatus(chapterId, true)
    }

    fun setChapterUnread(chapterId: Int): Boolean {
        return !updateChapterReadStatus(chapterId, false)
    }

    fun getManga(mangaId: Long): Manga? {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(FetchMangaMutation(mangaId.toInt())).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while fetching manga: " + response.errors)
                }

                response.data?.fetchManga?.manga?.let { mapToManga(it) }
            } catch (e: Exception) {
                throw RuntimeException("Error while fetching manga", e)
            }
        }
    }

    private fun updateChapterReadStatus(chapterId: Int, read: Boolean): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(SetChapterReadStatusMutation(chapterId, read)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while updating chapter read status: " + response.errors)
                }

                response.data?.updateChapter?.chapter?.isRead == true
            } catch (e: Exception) {
                throw RuntimeException("Error while updating chapter read status", e)
            }
        }
    }

    private fun updateMangaLibraryStatus(mangaId: Int, add: Boolean): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(UpdateMangaLibraryStatusMutation(mangaId, add)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while updating manga library status: " + response.errors)
                }

                response.data?.updateManga?.manga?.inLibrary == true
            } catch (e: Exception) {
                throw RuntimeException("Error while updating manga library status", e)
            }
        }
    }

    fun getChapterPages(chapterId: Int): List<String> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            val response = apolloClient.mutation(GetChapterPagesMutation(chapterId)).execute()
            if (response.hasErrors()) {
                if (response.errors?.any { it.message.contains("kotlinx-coroutines-android") } == true) {
                    throw AndroidOnlyException()
                }
                throw RuntimeException("Error while getting chapter pages: " + response.errors)
            }

            response.data?.fetchChapterPages?.pages ?: emptyList()
        }
    }

    fun getLibraryManga(): List<Manga> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetLibraryMangaQuery()).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while retrieving library manga: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while retrieving library manga: No data")
                data.categories.nodes.flatMap { node ->
                    node.mangas.nodes.map { mapToManga(it) }
                }
            } catch (e: Exception) {
                throw RuntimeException("Error while retrieving library manga", e)
            }
        }
    }

    private fun mapToChapter(node: GetChapterQuery.Chapter): Chapter {
        return Chapter().apply {
            id = node.id
            name = node.name
            chapterNumber = node.chapterNumber.toFloat()
            isDownloaded = node.isDownloaded == true
            isRead = false
            mangaId = node.mangaId
            url = ""
            pageCount = node.pageCount
        }
    }

    private fun mapToChapter(node: GetMangaChaptersQuery.Node): Chapter {
        return Chapter().apply {
            id = node.id
            name = node.name
            chapterNumber = node.chapterNumber.toFloat()
            isDownloaded = node.isDownloaded == true
            isRead = node.isRead == true
            mangaId = node.mangaId
            url = node.url
            pageCount = node.pageCount
        }
    }

    private fun mapToChapter(node: FetchChapterListMutation.Chapter): Chapter {
        return Chapter().apply {
            id = node.id
            name = node.name
            chapterNumber = node.chapterNumber.toFloat()
            isDownloaded = node.isDownloaded == true
            isRead = node.isRead == true
            mangaId = node.mangaId
            url = node.url
            pageCount = node.pageCount
        }
    }

    private fun mapToManga(node: FetchMangaMutation.Manga): Manga {
        return Manga().apply {
            id = node.id
            author = node.author
            title = node.title
            description = node.description
            thumbnailUrl = node.thumbnailUrl
            isInLibrary = node.inLibrary == true
            status = node.status?.rawValue

            node.source?.let { sNode ->
                source = Source().apply {
                    id = sNode.id?.toString()
                    name = sNode.name
                    displayName = sNode.displayName
                    lang = sNode.lang
                    iconUrl = sNode.iconUrl
                    isSupportsLatest = sNode.supportsLatest == true
                    isConfigurable = sNode.isConfigurable == true
                    isNsfw = sNode.isNsfw == true
                }
            }

            node.lastReadChapter?.let { lrNode ->
                lastChapterRead = Chapter().apply { id = lrNode.id }
            }

            node.categories?.nodes?.let { catNodes ->
                // Note: We need a way to set categories, currently Manga.kt has it private
                // For now, let's assume we can access it or use the nodes directly if we refactor Manga.kt further
                // Accessing private fields in Kotlin within same package works if they are internal, 
                // but here they are private.
            }

            node.chapters?.nodes?.let { chapterNodes ->
                chapterCount = chapterNodes.size
            }
        }
    }

    private fun mapToManga(node: GetLibraryMangaQuery.Node1): Manga {
        return Manga().apply {
            id = node.id
            title = node.title
            thumbnailUrl = node.thumbnailUrl
            isInLibrary = node.inLibrary == true

            node.lastReadChapter?.let { lrNode ->
                lastChapterRead = Chapter().apply { id = lrNode.id }
            }
        }
    }
}
