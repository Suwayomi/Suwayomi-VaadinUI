package online.hatsunemiku.tachideskvaadinui.services.client

import kotlinx.coroutines.runBlocking
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SourceSearchResult
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.SearchSourceMutation
import online.hatsunemiku.tachideskvaadinui.services.WebClientService
import org.springframework.stereotype.Component

@Component
class SearchClient(private val webClientService: WebClientService) {

    fun search(searchQuery: String, page: Int, sourceId: String): SourceSearchResult {
        val apolloClient = webClientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(SearchSourceMutation(sourceId, page, searchQuery)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while searching: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while searching: No data")
                val result = data.fetchSourceManga ?: throw RuntimeException("Error while searching: No result data")

                val mangaList = result.mangas.map { node ->
                    Manga().apply {
                        id = node.id
                        thumbnailUrl = node.thumbnailUrl
                        title = node.title
                        isInLibrary = false
                    }
                }

                SourceSearchResult(mangaList, result.hasNextPage == true, page)
            } catch (e: Exception) {
                throw RuntimeException("Error while searching", e)
            }
        }
    }
}
