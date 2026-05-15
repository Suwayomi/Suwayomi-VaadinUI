package online.hatsunemiku.tachideskvaadinui.services.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.event.MangaUpdateEvent
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.HasSkippedQuery
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.TrackMangaUpdateSubscription
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.UpdateLibraryMangaMutation
import online.hatsunemiku.tachideskvaadinui.services.WebClientService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Client responsible for any server communication related to manga library updates.
 *
 * @version 1.12.0
 * @since 0.9.0
 */
@Component
class LibUpdateClient(
    private val webClientService: WebClientService,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Starts the library update process on the server.
     *
     * @return `true` if the update process has started running, `false` otherwise
     */
    fun fetchUpdate(): Boolean {
        val apolloClient = webClientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(UpdateLibraryMangaMutation()).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while updating library: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while updating library: No data")
                var isRunning = data.updateLibraryManga.updateStatus.isRunning

                if (isRunning != true) {
                    val skippedResponse = apolloClient.query(HasSkippedQuery()).execute()
                    if (skippedResponse.hasErrors()) {
                        throw RuntimeException("Error while checking skipped jobs: " + skippedResponse.errors)
                    }
                    val skippedData = skippedResponse.data ?: throw RuntimeException("Error while updating library: No skipped data")
                    isRunning = skippedData.updateStatus.skippedJobs.mangas.nodes.isNotEmpty()
                }

                isRunning == true
            } catch (e: Exception) {
                throw RuntimeException("Error while updating library", e)
            }
        }
    }

    /** Opens a WebSocket connection to the server to track the update status of the manga library. */
    fun startUpdateTracking() {
        val apolloClient = webClientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        scope.launch {
            try {
                apolloClient.subscription(TrackMangaUpdateSubscription())
                    .toFlow()
                    .collectLatest { response ->
                        if (response.hasErrors()) {
                            throw RuntimeException("Error in update tracking subscription: " + response.errors)
                        }
                        val data = response.data ?: throw RuntimeException("Couldn't retrieve update run status")

                        val completedManga = data.updateStatusChanged.completeJobs.mangas.nodes.map { node ->
                            Manga().apply {
                                id = node.id
                                title = node.title
                            }
                        }

                        val event = MangaUpdateEvent(data.updateStatusChanged.isRunning == true, completedManga)
                        if (!event.isRunning) {
                            eventPublisher.publishEvent(event)
                        }
                    }
            } catch (e: Exception) {
                delay(Duration.ofSeconds(5))
                startUpdateTracking()
            }
        }
    }
}
