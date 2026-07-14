package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi

import kotlinx.coroutines.runBlocking
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Status
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackRecord
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.*
import online.hatsunemiku.tachideskvaadinui.services.WebClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * The SuwayomiTrackingClient class provides methods to interact with a Suwayomi tracker through
 * GraphQL API requests.
 */
@Component
class SuwayomiTrackingClient(
    private val clientService: WebClientService
) {

    companion object {
        private val log = LoggerFactory.getLogger(SuwayomiTrackingClient::class.java)
    }

    /**
     * Checks if a tracker with the provided ID is logged in.
     *
     * @param id the ID of the tracker to check if it is logged in
     * @return `true` if the tracker is logged in, `false` otherwise
     */
    fun isTrackerLoggedIn(id: Int): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(IsTrackerLoggedInQuery(id)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while checking if tracker is logged in: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while checking if tracker is logged in: No data")
                data.tracker.isLoggedIn
            } catch (e: Exception) {
                throw RuntimeException("Error while checking if tracker is logged in", e)
            }
        }
    }

    /**
     * Gets the authentication URL for a tracker with the provided ID.
     *
     * @param id the ID of the tracker to get the authentication URL for
     * @return the authentication URL for the tracker
     */
    fun getTrackerAuthUrl(id: Int): String {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetTrackerAuthUrlQuery(id)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while getting tracker auth url: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while getting tracker auth url: No data")
                data.tracker.authUrl ?: ""
            } catch (e: Exception) {
                throw RuntimeException("Error while getting tracker auth url", e)
            }
        }
    }

    /**
     * Logs in to a tracker using the provided redirect URL and tracker ID.
     *
     * @param url the redirect URL to log in to the tracker
     * @param id the ID of the tracker to log in to
     */
    fun loginTracker(url: String, id: Int) {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        runBlocking {
            try {
                val response = apolloClient.mutation(LoginTrackerMutation(url, id)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while logging in tracker: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while logging in tracker: No data")
                if (data.loginTrackerOAuth.isLoggedIn != true) {
                    log.error("Server returned false after logging in the tracker with id {}", id)
                }
            } catch (e: Exception) {
                throw RuntimeException("Error while logging in tracker", e)
            }
        }
    }

    /**
     * Searches for a manga on a tracker using the provided query and tracker ID.
     *
     * @param query the search query for the manga
     * @param id the ID of the tracker to search on
     * @return a list of [TrackerSearchResult] objects representing the search results
     */
    fun searchTracker(query: String, id: Int): List<TrackerSearchResult> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(SearchTrackerQuery(query, id)).execute()
                if (response.hasErrors()) {
                    val errorText = "Error while searching tracker: " + response.errors
                    log.error(errorText)
                    throw RuntimeException(errorText)
                }

                val data = response.data ?: throw RuntimeException("Error while searching tracker: No data")
                data.searchTracker.trackSearches.map { node ->
                    TrackerSearchResult(
                        node.coverUrl,
                        node.id,
                        node.remoteId.toString().toInt(),
                        node.publishingStatus,
                        node.publishingType,
                        node.startDate,
                        node.summary,
                        node.title,
                        node.totalChapters,
                        node.trackingUrl
                    )
                }
            } catch (e: Exception) {
                throw RuntimeException("Error while searching tracker", e)
            }
        }
    }

    /**
     * Tracks a manga on a tracker using the provided manga ID, external ID, and tracker ID.
     *
     * @param mangaId the Suwayomi ID of the manga to be tracked
     * @param externalId the external ID of the manga on the tracker. This is the ID of the manga on
     *     the tracker's website.
     * @param trackerId the ID of the tracker to track the manga on.
     */
    fun trackMangaOnTracker(mangaId: Int, externalId: Long, trackerId: Int) {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        val remoteId = externalId.toString()

        runBlocking {
            try {
                val response = apolloClient.mutation(TrackMangaMutation(mangaId, remoteId, trackerId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while tracking manga: " + response.errors)
                }

                if (response.data?.bindTrack == null) {
                    throw RuntimeException("Didn't receive a response from the server after trying to track the manga")
                }
            } catch (e: Exception) {
                throw RuntimeException("Error while tracking manga", e)
            }
        }
    }

    /**
     * Syncs the manga data on the server with the tracker.
     *
     * @param mangaId the ID of the manga to sync
     */
    fun trackProgress(mangaId: Int) {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        runBlocking {
            try {
                val response = apolloClient.mutation(TrackProgressOnTrackersMutation(mangaId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while tracking manga progress: " + response.errors)
                }

                if (response.data?.trackProgress == null) {
                    throw RuntimeException("Didn't receive a response from the server after trying to track the manga")
                }

                log.info("Tracked progress on trackers")
            } catch (e: Exception) {
                throw RuntimeException("Error while tracking manga progress", e)
            }
        }
    }

    /**
     * Checks if a manga is tracked on a tracker using the provided manga ID and tracker ID.
     *
     * @param mangaId the ID of the manga to check
     * @param trackerId the ID of the tracker to check
     * @return `true` if the manga is tracked on the tracker, `false` otherwise
     */
    fun isMangaTracked(mangaId: Int, trackerId: Int): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(IsMangaTrackedQuery(mangaId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while checking if manga is tracked: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while checking if manga is tracked: No data")
                data.manga.trackRecords.nodes.any { it.trackerId == trackerId }
            } catch (e: Exception) {
                throw RuntimeException("Error while checking if manga is tracked", e)
            }
        }
    }

    /**
     * Returns the track record of a manga for a specific tracker.
     *
     * @param mangaId the ID of the [online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga] to get the track record for
     * @param trackerId the ID of the tracker to get the track record for
     * @return the [TrackRecord] of the manga for the tracker or `null` if the manga is
     *     not tracked on the tracker.
     */
    fun getTrackRecord(mangaId: Long, trackerId: Int): TrackRecord? {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetMangaTrackRecordsQuery(mangaId.toInt())).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while getting manga track records: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while getting manga track records: No data")
                data.manga.trackRecords.nodes
                    .find { it.trackerId == trackerId }
                    ?.let { node ->
                        TrackRecord().apply {
                            id = node.id
                            libraryId = node.libraryId?.toString()?.toLong() ?: 0L
                            this.mangaId = node.mangaId
                            remoteId = node.remoteId.toString().toLong()
                            this.trackerId = node.trackerId
                            remoteUrl = node.remoteUrl
                            title = node.title
                            lastChapterRead = node.lastChapterRead.toFloat()
                            totalChapters = node.totalChapters
                            displayScore = node.displayScore
                            score = node.score.toFloat()
                            status = node.status
                            startDate = node.startDate.toString().toLongOrNull()
                                ?.let { if (it == 0L) null else java.time.Instant.ofEpochMilli(it) }
                            finishDate = node.finishDate.toString().toLongOrNull()
                                ?.let { if (it == 0L) null else java.time.Instant.ofEpochMilli(it) }
                        }
                    }
            } catch (e: Exception) {
                throw RuntimeException("Error while getting manga track records", e)
            }
        }
    }

    /**
     * Updates the data of a track record on the server.
     *
     * @param trackRecord The [TrackRecord] object containing the data to be updated.
     * @throws RuntimeException If an error occurs while updating the track record, if the response
     *     from the server contains errors, or if the updated data does not match the expected data.
     */
    fun updateTrackerData(trackRecord: TrackRecord) {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        val startDate = trackRecord.startDate?.toEpochMilli()?.toString() ?: "0"
        val finishDate = trackRecord.finishDate?.toEpochMilli()?.toString() ?: "0"

        runBlocking {
            try {
                val response = apolloClient.mutation(AllTheStuffForSuwayomiTrackingMutation(
                    trackRecord.id,
                    finishDate,
                    trackRecord.lastChapterRead.toDouble(),
                    startDate,
                    trackRecord.status
                )).execute()
                
                if (response.hasErrors()) {
                    throw RuntimeException("Error while updating track record: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while updating track record: No data")
                val updatedRecord = data.updateTrack.trackRecord ?: throw RuntimeException("Error while updating track record: No updated data")

                if (updatedRecord.lastChapterRead.toFloat() != trackRecord.lastChapterRead) {
                    throw RuntimeException("Last chapter read was not updated correctly")
                }

                if (updatedRecord.status != trackRecord.status) {
                    throw RuntimeException("Status was not updated correctly")
                }

                log.info("Updated track record with ID {}", updatedRecord.id)
            } catch (e: Exception) {
                throw RuntimeException("Error while updating track record", e)
            }
        }
    }

    /**
     * Retrieves the statuses for a specific track record.
     *
     * @param trackRecordId The ID of the track record for which the statuses are to be retrieved.
     * @return A list of Status objects representing the statuses for the specified track record.
     * @throws RuntimeException If an error occurs while retrieving the statuses or if the response
     *     from the server contains errors.
     */
    fun getStatuses(trackRecordId: Int): List<Status> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetStatusesQuery(trackRecordId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while getting track statuses: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while getting track statuses: No data")
                data.tracker.statuses.map { node ->
                    Status().apply {
                        name = node.name
                        value = node.value
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException("Error while getting track statuses", e)
            }
        }
    }

    /**
     * Stops tracking a manga on a tracker.
     *
     * @param recordId The ID of the track record to stop tracking.
     * @param deleteRemote A boolean indicating whether to delete the remote track record.
     * @throws RuntimeException If an error occurs while stopping tracking or if the response from the
     *     server contains errors.
     */
    fun stopTracking(recordId: Int, deleteRemote: Boolean) {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        runBlocking {
            try {
                val response = apolloClient.mutation(StopTrackingNewMutation(recordId, deleteRemote)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while stopping tracking: " + response.errors)
                }

                log.info("Stopped tracking manga with ID {}", recordId)
            } catch (e: Exception) {
                throw RuntimeException("Error while stopping tracking", e)
            }
        }
    }

    /**
     * Retrieves the tracking scores for a specific track record.
     *
     * @param recordId The ID of the track record for which the tracker scores are to be retrieved.
     * @return A list of strings representing the available scores for the tracker type of the track
     *     record.
     * @throws RuntimeException If an error occurs while retrieving the tracking scores or if the
     *     response contains errors.
     */
    fun getTrackingScores(recordId: Int): List<String> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetTrackingScoresQuery(recordId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while getting tracking scores: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while getting tracking scores: No data")
                data.trackRecord.tracker.scores
            } catch (e: Exception) {
                throw RuntimeException("Error while getting tracking scores", e)
            }
        }
    }

    /**
     * Updates the score of a track record.
     *
     * @param recordId The ID of the track record to be updated.
     * @param value The new score value as a string.
     * @throws RuntimeException If an error occurs while updating the score or if the updated score
     *     does not match the expected value.
     */
    fun updateScore(recordId: Int, value: String) {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        runBlocking {
            try {
                val response = apolloClient.mutation(UpdateScoreMutation(value, recordId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while updating score: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while updating score: No data")
                val updatedRecord = data.updateTrack.trackRecord ?: throw RuntimeException("Error while updating score: No updated record")
                val score = updatedRecord.score.toFloat()

                if (score != value.toFloat()) {
                    throw RuntimeException("Score was not updated correctly")
                }

                log.info("Updated score for track record with ID {}", recordId)
            } catch (e: Exception) {
                throw RuntimeException("Error while updating score", e)
            }
        }
    }
}
