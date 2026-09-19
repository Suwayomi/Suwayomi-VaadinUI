package online.hatsunemiku.tachideskvaadinui.services.client

import kotlinx.coroutines.runBlocking
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.SourceMangaList
import online.hatsunemiku.tachideskvaadinui.exceptions.CloudflareException
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetPopularSourceMangaMutation
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.type.FetchSourceMangaType
import online.hatsunemiku.tachideskvaadinui.services.WebClientService
import org.springframework.stereotype.Component

@Component
class SourceClient(private val webClientService: WebClientService) {

    fun getPopularManga(sourceId: String, page: Int): SourceMangaList {
        return getMangaFromSource(sourceId, page, FetchSourceMangaType.POPULAR)
    }

    fun getLatestManga(sourceId: String, page: Int): SourceMangaList {
        return getMangaFromSource(sourceId, page, FetchSourceMangaType.LATEST)
    }

    private fun getMangaFromSource(sourceId: String, page: Int, type: FetchSourceMangaType): SourceMangaList {
        val apolloClient = webClientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(GetPopularSourceMangaMutation(sourceId, page, type)).execute()
                if (response.hasErrors()) {
                    val error = response.errors?.firstOrNull()
                    if (error?.message?.contains("Cloudflare bypass currently disabled") == true) {
                        throw CloudflareException("Cloudflare bypass currently disabled")
                    }
                    throw RuntimeException("Error while fetching source manga: ${error?.message}")
                }

                val data = response.data ?: throw RuntimeException("Error while fetching source manga: No data")
                val result = data.fetchSourceManga ?: throw RuntimeException("Error while fetching source manga: No result data")

                val mangaList = result.mangas.map { node ->
                    Manga().apply {
                        id = node.id
                        thumbnailUrl = node.thumbnailUrl
                        title = node.title
                        isInLibrary = false
                    }
                }

                SourceMangaList().apply {
                    this.mangaList = mangaList
                    isHasNextPage = result.hasNextPage == true
                }
            } catch (e: CloudflareException) {
                throw e
            } catch (e: Exception) {
                throw RuntimeException("Error while fetching source manga", e)
            }
        }
    }
}
