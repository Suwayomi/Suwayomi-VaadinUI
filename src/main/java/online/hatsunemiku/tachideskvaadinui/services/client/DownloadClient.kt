package online.hatsunemiku.tachideskvaadinui.services.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.DeleteChapterMutation
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.DownloadChaptersMutation
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.TrackDownloadsSubscription
import online.hatsunemiku.tachideskvaadinui.services.WebClientService
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class DownloadClient(private val clientService: WebClientService) {

    /**
     * Downloads the chapters specified by the given list of chapterIds.
     *
     * @param chapterIds The list of [online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter.getId] chapter IDs to download.
     * @return True if all chapters were successfully downloaded, false otherwise.
     */
    fun downloadChapters(chapterIds: List<Int>): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(DownloadChaptersMutation(chapterIds)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while downloading chapters: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while downloading chapters: No data")
                val downloadStatus = data.enqueueChapterDownloads.downloadStatus ?: throw RuntimeException("Error while downloading chapters: Null status")

                val newChapterIds = downloadStatus.queue.map { it.chapter.id }

                // check if newChapterIds contains all chapterIds
                chapterIds.all { newChapterIds.contains(it) }
            } catch (e: Exception) {
                throw RuntimeException("Error while downloading chapters", e)
            }
        }
    }

    /**
     * Deletes the chapter specified by the given chapterId.
     *
     * @param chapterId The [online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter.getId] chapter ID to delete.
     * @return True if the chapter was successfully deleted, false otherwise.
     */
    fun deleteChapter(chapterId: Int): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(DeleteChapterMutation(chapterId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while deleting chapter: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while deleting chapter: No data")
                val deletionFail = data.deleteDownloadedChapter.chapters.isDownloaded

                deletionFail != true
            } catch (e: Exception) {
                throw RuntimeException("Error while deleting chapter", e)
            }
        }
    }

    fun trackDownloads(): Flux<List<DownloadChangeEvent>> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return Flux.create<List<DownloadChangeEvent>> { sink ->
            val job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    apolloClient.subscription(TrackDownloadsSubscription())
                        .toFlow()
                        .collect { response ->
                            if (response.hasErrors()) {
                                sink.error(RuntimeException("Error in download subscription: " + response.errors))
                                return@collect
                            }
                            val data = response.data ?: return@collect
                            sink.next(data.downloadChanged.queue.map { node ->
                                DownloadChangeEvent(
                                    node.progress.toFloat(),
                                    node.state.toString(),
                                    EnqueuedChapter(node.chapter.id)
                                )
                            })
                        }
                } catch (e: Exception) {
                    sink.error(e)
                }
            }
            sink.onDispose {
                job.cancel()
            }
        }
    }

    class EnqueuedChapter(private val id: Int) {
        fun id(): Int = id
    }

    class DownloadChangeEvent(private val progress: Float, private val state: String, private val chapter: EnqueuedChapter) {
        fun progress(): Float = progress
        fun state(): String = state
        fun chapter(): EnqueuedChapter = chapter
    }
}
