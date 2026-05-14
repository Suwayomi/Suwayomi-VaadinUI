/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.api.Optional;
import com.apollographql.apollo.api.http.HttpRequest;
import com.apollographql.apollo.api.http.HttpResponse;
import com.apollographql.java.client.ApolloCallback;
import com.apollographql.java.client.ApolloClient;
import com.apollographql.java.client.network.http.HttpCallback;
import com.apollographql.java.client.network.http.HttpInterceptor;
import com.apollographql.java.client.network.http.HttpInterceptorChain;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tracking.OAuthData;
import online.hatsunemiku.tachideskvaadinui.data.tracking.TrackerTokens;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMedia;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.MangaList;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.AniListMangaStatistics;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.*;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.type.FuzzyDateInput;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.type.MediaListStatus;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Is responsible for interacting with the AniList API using Apollo GraphQL client.
 */
@Service
@Slf4j
public class AniListAPIService {

  private static final String ANILIST_API_URL = "https://graphql.anilist.co";
  private static final String OAUTH_CLIENT_ID = "14576";
  public static final String OAUTH_URL = "https://anilist.co/api/v2/oauth";
  private static final String OAUTH_CODE_PATTERN =
      OAUTH_URL + "/authorize?client_id=%s&response_type=token";

  private final TrackingDataService dataService;
  private final ApolloClient apolloClient;

  public AniListAPIService(TrackingDataService dataService) {
    this.dataService = dataService;
    this.apolloClient = new ApolloClient.Builder()
        .serverUrl(ANILIST_API_URL)
        .addHttpInterceptor((httpRequest, httpInterceptorChain, httpCallback) -> {
            if (hasAniListToken()) {
                httpRequest = httpRequest.newBuilder()
                    .addHeader("Authorization", getAniListTokenHeader())
                    .build();
            }
            httpInterceptorChain.proceed(httpRequest, httpCallback);
        })
        .build();

    try {
      log.info("User ID: {}", getCurrentUserId());
    } catch (RuntimeException e) {
      log.info("No AniList token set yet");
    }
  }

  private java.util.Optional<OAuthData> getAniListToken() {
    TrackerTokens trackerTokens = dataService.getTokens();
    if (!trackerTokens.hasAniListToken()) {
      return java.util.Optional.empty();
    }
    return java.util.Optional.of(trackerTokens.getAniListToken());
  }

  public boolean hasAniListToken() {
    return getAniListToken().isPresent();
  }

  private String getAniListTokenHeader() {
    if (!hasAniListToken()) {
      throw new IllegalStateException("No AniList Token");
    }
    var token = getAniListToken().get();
    if (!token.getTokenType().equals("Bearer")) {
      throw new IllegalStateException("AniList token is not a Bearer token");
    }
    return "Bearer " + token.getAccessToken();
  }

  public String getAniListAuthUrl() {
    return String.format(OAUTH_CODE_PATTERN, OAUTH_CLIENT_ID);
  }

  private int getCurrentUserId() {
    CompletableFuture<ApolloResponse<GetViewerIdQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetViewerIdQuery()).enqueue(future::complete);

    var response = future.join();
    if (response.hasErrors()) {
      throw new RuntimeException("Error retrieving user ID: " + response.errors);
    }
    if (response.data == null || response.data.Viewer == null) {
      throw new RuntimeException("No user ID found in viewer response");
    }
    return response.data.Viewer.id;
  }

  public AniListMangaStatistics getMangaFromList(int mangaId) {
    CompletableFuture<ApolloResponse<GetMediaListQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetMediaListQuery(Optional.present(mangaId), Optional.present(getCurrentUserId()))).enqueue(future::complete);

    var response = future.join();
    if (response.hasErrors()) {
      log.warn("Manga with ID {} not found or error occurred: {}", mangaId, response.errors);
      throw new RuntimeException("Manga list entry not found");
    }
    var data = response.data;
    if (data == null || data.MediaList == null) {
        throw new RuntimeException("Manga list entry is null");
    }

    var entry = data.MediaList;
    return new AniListMangaStatistics(
        mapToInternalStatus(entry.status),
        entry.progress != null ? entry.progress : 0,
        entry.score != null ? entry.score.intValue() : 0,
        mapToInternalDate(entry.startedAt),
        mapToInternalDate(entry.completedAt)
    );
  }

  public void updateMangaProgress(int mangaId, double mangaProgress) {
    SaveMediaListEntryMutation mutation = SaveMediaListEntryMutation.builder()
        .mangaId(mangaId)
        .progress((int) mangaProgress)
        .build();

    executeMutation(mutation, "progress");
  }

  public void updateMangaStatus(int aniListId, AniListStatus value) {
    SaveMediaListEntryMutation mutation = SaveMediaListEntryMutation.builder()
        .mangaId(aniListId)
        .status(mapToApolloStatus(value))
        .build();

    executeMutation(mutation, "status");
  }

  public void updateMangaScore(int aniListId, double value) {
    SaveMediaListEntryMutation mutation = SaveMediaListEntryMutation.builder()
        .mangaId(aniListId)
        .score(value)
        .build();

    executeMutation(mutation, "score");
  }

  public void updateMangaEndDate(int aniListId, MediaDate date) {
    FuzzyDateInput endDate = date == null ? null : FuzzyDateInput.builder()
        .year(date.year())
        .month(date.month())
        .day(date.day())
        .build();

    SaveMediaListEntryMutation mutation = SaveMediaListEntryMutation.builder()
        .mangaId(aniListId)
        .completedAt(endDate)
        .build();

    executeMutation(mutation, "end date");
  }

  public void updateMangaPrivacyStatus(int aniListId, boolean isPrivate) {
    SaveMediaListEntryMutation mutation = SaveMediaListEntryMutation.builder()
        .mangaId(aniListId)
        .private_(isPrivate)
        .build();

    executeMutation(mutation, "privacy status");
  }

  private void executeMutation(SaveMediaListEntryMutation mutation, String fieldName) {
    CompletableFuture<ApolloResponse<SaveMediaListEntryMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(mutation).enqueue(future::complete);

    var response = future.join();
    if (response.hasErrors()) {
      throw new RuntimeException("Error updating manga " + fieldName + ": " + response.errors);
    }
    if (response.data != null && response.data.SaveMediaListEntry != null) {
      log.info("Updated manga {} for ID {}", fieldName, response.data.SaveMediaListEntry.id);
    }
  }

  public MangaList getMangaList() {
    CompletableFuture<ApolloResponse<GetMediaListCollectionQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetMediaListCollectionQuery(Optional.present(getCurrentUserId()))).enqueue(future::complete);

    var response = future.join();
    if (response.hasErrors()) {
      throw new RuntimeException("Error retrieving manga list: " + response.errors);
    }
    if (response.data == null || response.data.MediaListCollection == null) {
      throw new RuntimeException("Manga list response is empty");
    }

    List<AniListMedia> completed = new ArrayList<>();
    List<AniListMedia> reading = new ArrayList<>();
    List<AniListMedia> dropped = new ArrayList<>();
    List<AniListMedia> onHold = new ArrayList<>();
    List<AniListMedia> planToRead = new ArrayList<>();

    for (var list : response.data.MediaListCollection.lists) {
        if (list == null || list.entries == null) continue;

        for (var entry : list.entries) {
            if (entry == null) continue;

            AniListMedia media = mapToInternalMedia(entry);
            AniListStatus status = mapToInternalStatus(entry.status);

            switch (status) {
                case COMPLETED -> completed.add(media);
                case CURRENT, REPEATING -> reading.add(media);
                case DROPPED -> dropped.add(media);
                case PAUSED -> onHold.add(media);
                case PLANNING -> planToRead.add(media);
            }
        }
    }

    return new MangaList(reading, planToRead, completed, onHold, dropped);
  }

  public void removeMangaFromList(int aniListId) {
    int entryId = getMangaListEntryId(aniListId);
    CompletableFuture<ApolloResponse<DeleteMediaListEntryMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new DeleteMediaListEntryMutation(Optional.present(entryId))).enqueue(future::complete);

    var response = future.join();
    if (response.hasErrors()) {
      throw new RuntimeException("Error deleting manga: " + response.errors);
    }
    if (response.data != null && response.data.DeleteMediaListEntry != null && Boolean.TRUE.equals(response.data.DeleteMediaListEntry.deleted)) {
      log.info("Deleted manga with ID {}", aniListId);
    } else {
      throw new RuntimeException("Manga could not be deleted");
    }
  }

  private int getMangaListEntryId(int mangaId) {
    CompletableFuture<ApolloResponse<GetMediaListEntryIdQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetMediaListEntryIdQuery(Optional.present(mangaId), Optional.present(getCurrentUserId()))).enqueue(future::complete);

    var response = future.join();
    if (response.hasErrors()) {
      throw new RuntimeException("Error retrieving manga list entry ID: " + response.errors);
    }
    if (response.data == null || response.data.MediaList == null) {
      throw new RuntimeException("Manga list entry ID not found");
    }
    return response.data.MediaList.id;
  }

  public void addMangaToList(int mangaId, boolean isPrivate) {
    SaveMediaListEntryMutation mutation = SaveMediaListEntryMutation.builder()
        .mangaId(mangaId)
        .status(MediaListStatus.CURRENT)
        .private_(isPrivate)
        .build();

    CompletableFuture<ApolloResponse<SaveMediaListEntryMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(mutation).enqueue(future::complete);

    var response = future.join();
    if (response.hasErrors()) {
        throw new RuntimeException("Error adding manga to list: " + response.errors);
    }
  }

  // Helper Mappers

  private AniListStatus mapToInternalStatus(MediaListStatus apolloStatus) {
    if (apolloStatus == null) return AniListStatus.PLANNING;
    if (MediaListStatus.CURRENT.equals(apolloStatus)) return AniListStatus.CURRENT;
    if (MediaListStatus.PLANNING.equals(apolloStatus)) return AniListStatus.PLANNING;
    if (MediaListStatus.COMPLETED.equals(apolloStatus)) return AniListStatus.COMPLETED;
    if (MediaListStatus.DROPPED.equals(apolloStatus)) return AniListStatus.DROPPED;
    if (MediaListStatus.PAUSED.equals(apolloStatus)) return AniListStatus.PAUSED;
    if (MediaListStatus.REPEATING.equals(apolloStatus)) return AniListStatus.REPEATING;
    return AniListStatus.PLANNING;
  }

  private MediaListStatus mapToApolloStatus(AniListStatus internalStatus) {
    return switch (internalStatus) {
      case CURRENT -> MediaListStatus.CURRENT;
      case PLANNING -> MediaListStatus.PLANNING;
      case COMPLETED -> MediaListStatus.COMPLETED;
      case DROPPED -> MediaListStatus.DROPPED;
      case PAUSED -> MediaListStatus.PAUSED;
      case REPEATING -> MediaListStatus.REPEATING;
    };
  }

  private MediaDate mapToInternalDate(GetMediaListQuery.StartedAt date) {
    if (date == null) return null;
    return new MediaDate(date.day, date.month, date.year);
  }

  private MediaDate mapToInternalDate(GetMediaListQuery.CompletedAt date) {
    if (date == null) return null;
    return new MediaDate(date.day, date.month, date.year);
  }

  private MediaDate mapToInternalDate(GetMediaListCollectionQuery.StartedAt date) {
    if (date == null) return null;
    return new MediaDate(date.day, date.month, date.year);
  }

  private MediaDate mapToInternalDate(GetMediaListCollectionQuery.CompletedAt date) {
    if (date == null) return null;
    return new MediaDate(date.day, date.month, date.year);
  }

  private AniListMedia mapToInternalMedia(GetMediaListCollectionQuery.Entry entry) {
      var media = entry.media;
      return new AniListMedia(
          entry.mediaId,
          new AniListMedia.MediaTitle(null, media.title.romaji, media.title.english, media.title.native_),
          new AniListMedia.MediaCoverImage(media.coverImage.large),
          null,
          entry.status != null ? entry.status.rawValue : null,
          0,
          null,
          mapToInternalDate(entry.startedAt)
      );
  }
}
