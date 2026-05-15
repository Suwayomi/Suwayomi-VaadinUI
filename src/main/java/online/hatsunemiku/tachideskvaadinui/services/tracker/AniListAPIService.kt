/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import kotlinx.coroutines.runBlocking
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMedia
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.MangaList
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.AniListMangaStatistics
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.*
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.type.FuzzyDateInput
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.type.MediaListStatus
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Is responsible for interacting with the AniList API using Apollo GraphQL client.
 */
@Service
class AniListAPIService(private val dataService: TrackingDataService) {

    companion object {
        private val log = LoggerFactory.getLogger(AniListAPIService::class.java)
        private const val ANILIST_API_URL = "https://graphql.anilist.co"
        private const val OAUTH_CLIENT_ID = "14576"
        const val OAUTH_URL = "https://anilist.co/api/v2/oauth"
        private const val OAUTH_CODE_PATTERN = "$OAUTH_URL/authorize?client_id=%s&response_type=token"
    }

    private val apolloClient: ApolloClient = ApolloClient.Builder()
        .serverUrl(ANILIST_API_URL)
        .addHttpInterceptor(object : HttpInterceptor {
            override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain): HttpResponse {
                return if (hasAniListToken()) {
                    val authenticatedRequest = request.newBuilder()
                        .addHeader("Authorization", getAniListTokenHeader())
                        .build()
                    chain.proceed(authenticatedRequest)
                } else {
                    chain.proceed(request)
                }
            }
        })
        .build()

    init {
        try {
            log.info("User ID: {}", getCurrentUserId())
        } catch (e: Exception) {
            log.info("No AniList token set yet")
        }
    }

    private fun getAniListToken(): java.util.Optional<online.hatsunemiku.tachideskvaadinui.data.tracking.OAuthData> {
        val trackerTokens = (dataService as Any).javaClass.getMethod("getTokens").invoke(dataService) as online.hatsunemiku.tachideskvaadinui.data.tracking.TrackerTokens
        return if (!trackerTokens.hasAniListToken()) {
            java.util.Optional.empty()
        } else {
            java.util.Optional.of(trackerTokens.javaClass.getMethod("getAniListToken").invoke(trackerTokens) as online.hatsunemiku.tachideskvaadinui.data.tracking.OAuthData)
        }
    }

    fun hasAniListToken(): Boolean {
        return getAniListToken().isPresent
    }

    private fun getAniListTokenHeader(): String {
        if (!hasAniListToken()) {
            throw IllegalStateException("No AniList Token")
        }
        val token = getAniListToken().get()
        val tokenType = token.javaClass.getMethod("getTokenType").invoke(token) as String
        if (tokenType != "Bearer") {
            throw IllegalStateException("AniList token is not a Bearer token")
        }
        val accessToken = token.javaClass.getMethod("getAccessToken").invoke(token) as String
        return "Bearer " + accessToken
    }

    fun getAniListAuthUrl(): String {
        return String.format(OAUTH_CODE_PATTERN, OAUTH_CLIENT_ID)
    }

    private fun getCurrentUserId(): Int {
        return runBlocking {
            val response = apolloClient.query(GetViewerIdQuery()).execute()
            if (response.hasErrors()) {
                throw RuntimeException("Error retrieving user ID: " + response.errors)
            }
            response.data?.Viewer?.id ?: throw RuntimeException("No user ID found in viewer response")
        }
    }

    fun getMangaFromList(mangaId: Int): AniListMangaStatistics {
        return runBlocking {
            val response = apolloClient.query(GetMediaListQuery(Optional.present(mangaId), Optional.present(getCurrentUserId()))).execute()
            if (response.hasErrors()) {
                log.warn("Manga with ID {} not found or error occurred: {}", mangaId, response.errors)
                throw RuntimeException("Manga list entry not found")
            }
            val entry = response.data?.MediaList ?: throw RuntimeException("Manga list entry is null")

            AniListMangaStatistics(
                mapToInternalStatus(entry.status),
                entry.progress ?: 0,
                entry.score?.toInt() ?: 0,
                mapToInternalDate(entry.startedAt),
                mapToInternalDate(entry.completedAt)
            )
        }
    }

    fun updateMangaProgress(mangaId: Int, mangaProgress: Double) {
        val mutation = SaveMediaListEntryMutation(
            mangaId = Optional.present(mangaId),
            progress = Optional.present(mangaProgress.toInt())
        )
        executeMutation(mutation, "progress")
    }

    fun updateMangaStatus(aniListId: Int, value: AniListStatus) {
        val mutation = SaveMediaListEntryMutation(
            mangaId = Optional.present(aniListId),
            status = Optional.present(mapToApolloStatus(value))
        )
        executeMutation(mutation, "status")
    }

    fun updateMangaScore(aniListId: Int, value: Double) {
        val mutation = SaveMediaListEntryMutation(
            mangaId = Optional.present(aniListId),
            score = Optional.present(value)
        )
        executeMutation(mutation, "score")
    }

    fun updateMangaEndDate(aniListId: Int, date: MediaDate?) {
        val endDate = if (date == null) Optional.Absent else Optional.present(
            FuzzyDateInput(
                year = Optional.present(date.year()),
                month = Optional.present(date.month()),
                day = Optional.present(date.day())
            )
        )

        val mutation = SaveMediaListEntryMutation(
            mangaId = Optional.present(aniListId),
            completedAt = endDate
        )
        executeMutation(mutation, "end date")
    }

    fun updateMangaPrivacyStatus(aniListId: Int, isPrivate: Boolean) {
        val mutation = SaveMediaListEntryMutation(
            mangaId = Optional.present(aniListId),
            `private` = Optional.present(isPrivate)
        )
        executeMutation(mutation, "privacy status")
    }

    private fun executeMutation(mutation: SaveMediaListEntryMutation, fieldName: String) {
        runBlocking {
            val response = apolloClient.mutation(mutation).execute()
            if (response.hasErrors()) {
                throw RuntimeException("Error updating manga $fieldName: " + response.errors)
            }
            response.data?.SaveMediaListEntry?.let {
                log.info("Updated manga $fieldName for ID ${it.id}")
            }
        }
    }

    fun getMangaList(): MangaList {
        return runBlocking {
            val response = apolloClient.query(GetMediaListCollectionQuery(Optional.present(getCurrentUserId()))).execute()
            if (response.hasErrors()) {
                throw RuntimeException("Error retrieving manga list: " + response.errors)
            }
            val collection = response.data?.MediaListCollection ?: throw RuntimeException("Manga list response is empty")

            val completed = mutableListOf<AniListMedia>()
            val reading = mutableListOf<AniListMedia>()
            val dropped = mutableListOf<AniListMedia>()
            val onHold = mutableListOf<AniListMedia>()
            val planToRead = mutableListOf<AniListMedia>()

            collection.lists?.filterNotNull()?.forEach { list ->
                list.entries?.filterNotNull()?.forEach { entry ->
                    val media = mapToInternalMedia(entry)
                    val status = mapToInternalStatus(entry.status)

                    when (status) {
                        AniListStatus.COMPLETED -> completed.add(media)
                        AniListStatus.CURRENT, AniListStatus.REPEATING -> reading.add(media)
                        AniListStatus.DROPPED -> dropped.add(media)
                        AniListStatus.PAUSED -> onHold.add(media)
                        AniListStatus.PLANNING -> planToRead.add(media)
                    }
                }
            }

            MangaList(reading, planToRead, completed, onHold, dropped)
        }
    }

    fun removeMangaFromList(aniListId: Int) {
        val entryId = getMangaListEntryId(aniListId)
        runBlocking {
            val response = apolloClient.mutation(DeleteMediaListEntryMutation(Optional.present(entryId))).execute()
            if (response.hasErrors()) {
                throw RuntimeException("Error deleting manga: " + response.errors)
            }
            if (response.data?.DeleteMediaListEntry?.deleted == true) {
                log.info("Deleted manga with ID $aniListId")
            } else {
                throw RuntimeException("Manga could not be deleted")
            }
        }
    }

    private fun getMangaListEntryId(mangaId: Int): Int {
        return runBlocking {
            val response = apolloClient.query(GetMediaListEntryIdQuery(Optional.present(mangaId), Optional.present(getCurrentUserId()))).execute()
            if (response.hasErrors()) {
                throw RuntimeException("Error retrieving manga list entry ID: " + response.errors)
            }
            response.data?.MediaList?.id ?: throw RuntimeException("Manga list entry ID not found")
        }
    }

    fun addMangaToList(mangaId: Int, isPrivate: Boolean) {
        val mutation = SaveMediaListEntryMutation(
            mangaId = Optional.present(mangaId),
            status = Optional.present(MediaListStatus.CURRENT),
            `private` = Optional.present(isPrivate)
        )
        runBlocking {
            val response = apolloClient.mutation(mutation).execute()
            if (response.hasErrors()) {
                throw RuntimeException("Error adding manga to list: " + response.errors)
            }
        }
    }

    // Helper Mappers

    private fun mapToInternalStatus(apolloStatus: MediaListStatus?): AniListStatus {
        return when (apolloStatus) {
            MediaListStatus.CURRENT -> AniListStatus.CURRENT
            MediaListStatus.PLANNING -> AniListStatus.PLANNING
            MediaListStatus.COMPLETED -> AniListStatus.COMPLETED
            MediaListStatus.DROPPED -> AniListStatus.DROPPED
            MediaListStatus.PAUSED -> AniListStatus.PAUSED
            MediaListStatus.REPEATING -> AniListStatus.REPEATING
            else -> AniListStatus.PLANNING
        }
    }

    private fun mapToApolloStatus(internalStatus: AniListStatus): MediaListStatus {
        return when (internalStatus) {
            AniListStatus.CURRENT -> MediaListStatus.CURRENT
            AniListStatus.PLANNING -> MediaListStatus.PLANNING
            AniListStatus.COMPLETED -> MediaListStatus.COMPLETED
            AniListStatus.DROPPED -> MediaListStatus.DROPPED
            AniListStatus.PAUSED -> MediaListStatus.PAUSED
            AniListStatus.REPEATING -> MediaListStatus.REPEATING
        }
    }

    private fun mapToInternalDate(date: GetMediaListQuery.StartedAt?): MediaDate? {
        return date?.let { MediaDate(it.day, it.month, it.year) }
    }

    private fun mapToInternalDate(date: GetMediaListQuery.CompletedAt?): MediaDate? {
        return date?.let { MediaDate(it.day, it.month, it.year) }
    }

    private fun mapToInternalDate(date: GetMediaListCollectionQuery.StartedAt?): MediaDate? {
        return date?.let { MediaDate(it.day, it.month, it.year) }
    }

    private fun mapToInternalDate(date: GetMediaListCollectionQuery.CompletedAt?): MediaDate? {
        return date?.let { MediaDate(it.day, it.month, it.year) }
    }

    private fun mapToInternalMedia(entry: GetMediaListCollectionQuery.Entry): AniListMedia {
        val media = entry.media!!
        return AniListMedia(
            entry.mediaId,
            AniListMedia.MediaTitle(null, media.title?.romaji, media.title?.english, media.title?.native),
            AniListMedia.MediaCoverImage(media.coverImage?.large),
            null,
            entry.status?.rawValue,
            0,
            null,
            mapToInternalDate(entry.startedAt)
        )
    }
}
