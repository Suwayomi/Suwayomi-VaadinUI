package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCallback;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Status;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackRecord;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.*;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * The SuwayomiTrackingClient class provides methods to interact with a Suwayomi tracker through
 * GraphQL API requests.
 */
@Component
public class SuwayomiTrackingClient {

  private static final Logger log = LoggerFactory.getLogger(SuwayomiTrackingClient.class);
  private final WebClientService clientService;
  private final SuwayomiMetaClient suwayomiMetaClient;

  /**
   * Creates a new instance of the {@link SuwayomiTrackingClient} class.
   *
   * @param clientService the {@link WebClientService} used for making API requests
   */
  public SuwayomiTrackingClient(
      WebClientService clientService, SuwayomiMetaClient suwayomiMetaClient) {
    this.clientService = clientService;
    this.suwayomiMetaClient = suwayomiMetaClient;
  }

  /**
   * Checks if a tracker with the provided ID is logged in.
   *
   * @param id the ID of the tracker to check if it is logged in
   * @return {@code true} if the tracker is logged in, {@code false} otherwise
   * @see online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService.TrackerType
   */
  @SuppressWarnings("JavadocReference")
  public boolean isTrackerLoggedIn(int id) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<IsTrackerLoggedInQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new IsTrackerLoggedInQuery(id)).enqueue(new ApolloCallback<IsTrackerLoggedInQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<IsTrackerLoggedInQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while checking if tracker is logged in: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.tracker == null) {
        throw new RuntimeException("Error while checking if tracker is logged in");
      }

      return Boolean.TRUE.equals(data.tracker.isLoggedIn);
    } catch (Exception e) {
      throw new RuntimeException("Error while checking if tracker is logged in", e);
    }
  }

  /**
   * Gets the authentication URL for a tracker with the provided ID.
   *
   * @param id the ID of the tracker to get the authentication URL for
   * @return the authentication URL for the tracker
   * @see online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService.TrackerType
   */
  @SuppressWarnings("JavadocReference")
  public String getTrackerAuthUrl(int id) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetTrackerAuthUrlQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetTrackerAuthUrlQuery(id)).enqueue(new ApolloCallback<GetTrackerAuthUrlQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetTrackerAuthUrlQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while getting tracker auth url: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.tracker == null) {
        throw new RuntimeException("Error while getting tracker auth url");
      }

      return data.tracker.authUrl;
    } catch (Exception e) {
      throw new RuntimeException("Error while getting tracker auth url", e);
    }
  }

  /**
   * Logs in to a tracker using the provided redirect URL and tracker ID.
   *
   * @param url the redirect URL to log in to the tracker
   * @param id the ID of the tracker to log in to
   */
  public void loginTracker(String url, int id) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<LoginTrackerMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new LoginTrackerMutation(url, id)).enqueue(new ApolloCallback<LoginTrackerMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<LoginTrackerMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while logging in tracker: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.loginTrackerOAuth == null) {
        throw new RuntimeException("Error while logging in tracker");
      }

      if (!Boolean.TRUE.equals(data.loginTrackerOAuth.isLoggedIn)) {
        log.error("Server returned false after logging in the tracker with id {}", id);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error while logging in tracker", e);
    }
  }

  /**
   * Searches for a manga on a tracker using the provided query and tracker ID.
   *
   * @param query the search query for the manga
   * @param id the ID of the tracker to search on
   * @return a list of {@link TrackerSearchResult} objects representing the search results
   * @see online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService.TrackerType
   */
  @SuppressWarnings("JavadocReference")
  public List<TrackerSearchResult> searchTracker(String query, int id) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<SearchTrackerQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new SearchTrackerQuery(query, id)).enqueue(new ApolloCallback<SearchTrackerQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<SearchTrackerQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        String errorText = "Error while searching tracker: " + response.errors;
        log.error(errorText);
        throw new RuntimeException(errorText);
      }

      var data = response.data;
      if (data == null || data.searchTracker == null) {
        throw new RuntimeException("Error while searching tracker");
      }

      return data.searchTracker.trackSearches.stream()
          .map(node -> new TrackerSearchResult(
              node.coverUrl,
              node.id,
              node.remoteId != null ? Integer.parseInt(node.remoteId.toString()) : 0,
              node.publishingStatus,
              node.publishingType,
              node.startDate,
              node.summary,
              node.title,
              node.totalChapters != null ? node.totalChapters : 0,
              node.trackingUrl
          ))
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error while searching tracker", e);
    }
  }

  /**
   * Tracks a manga on a tracker using the provided manga ID, external ID, and tracker ID.
   *
   * @param mangaId the Suwayomi ID of the manga to be tracked
   * @param externalId the external ID of the manga on the tracker. This is the ID of the manga on
   *     the tracker's website.
   * @param trackerId the ID of the tracker to track the manga on.
   * @see online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService.TrackerType
   */
  @SuppressWarnings("JavadocReference")
  public void trackMangaOnTracker(int mangaId, long externalId, int trackerId) {
    var apolloClient = clientService.getApolloClient();

    String remoteId = String.valueOf(externalId);

    CompletableFuture<ApolloResponse<TrackMangaMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new TrackMangaMutation(mangaId, remoteId, trackerId)).enqueue(new ApolloCallback<TrackMangaMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<TrackMangaMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while tracking manga: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.bindTrack == null) {
        throw new RuntimeException("Didn't receive a response from the server after trying to track the manga");
      }
    } catch (Exception e) {
      throw new RuntimeException("Error while tracking manga", e);
    }
  }

  /**
   * Syncs the manga data on the server with the tracker.
   *
   * @param mangaId the ID of the manga to sync
   */
  public void trackProgress(int mangaId) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<TrackProgressOnTrackersMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new TrackProgressOnTrackersMutation(mangaId)).enqueue(new ApolloCallback<TrackProgressOnTrackersMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<TrackProgressOnTrackersMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while tracking manga progress: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.trackProgress == null) {
        throw new RuntimeException("Didn't receive a response from the server after trying to track the manga");
      }

      log.info("Tracked progress on trackers");
    } catch (Exception e) {
      throw new RuntimeException("Error while tracking manga progress", e);
    }
  }

  /**
   * Checks if a manga is tracked on a tracker using the provided manga ID and tracker ID.
   *
   * @param mangaId the ID of the manga to check
   * @param trackerId the ID of the tracker to check
   * @return {@code true} if the manga is tracked on the tracker, {@code false} otherwise
   */
  public boolean isMangaTracked(int mangaId, int trackerId) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<IsMangaTrackedQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new IsMangaTrackedQuery(mangaId)).enqueue(new ApolloCallback<IsMangaTrackedQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<IsMangaTrackedQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while checking if manga is tracked: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.manga == null || data.manga.trackRecords == null) {
        throw new RuntimeException("Error while checking if manga is tracked");
      }

      return data.manga.trackRecords.nodes.stream()
          .anyMatch(record -> record.trackerId == trackerId);
    } catch (Exception e) {
      throw new RuntimeException("Error while checking if manga is tracked", e);
    }
  }

  /**
   * Returns the track record of a manga for a specific tracker.
   *
   * @param mangaId the ID of the {@link online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga
   *     Manga} to get the track record for
   * @param trackerId the ID of the tracker to get the track record for
   * @return the {@link TrackRecord} of the manga for the tracker or {@code null} if the manga is
   *     not tracked on the tracker.
   */
  public TrackRecord getTrackRecord(long mangaId, int trackerId) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetMangaTrackRecordsQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetMangaTrackRecordsQuery((int) mangaId)).enqueue(new ApolloCallback<GetMangaTrackRecordsQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetMangaTrackRecordsQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while getting manga track records: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.manga == null || data.manga.trackRecords == null) {
        throw new RuntimeException("Error while getting manga track records");
      }

      return data.manga.trackRecords.nodes.stream()
          .filter(record -> record.trackerId == trackerId)
          .map(node -> {
            TrackRecord record = new TrackRecord();
            record.setId(node.id);
            record.setLibraryId(node.libraryId != null ? Long.parseLong(node.libraryId.toString()) : 0L);
            record.setMangaId(node.mangaId);
            record.setRemoteId(node.remoteId != null ? Long.parseLong(node.remoteId.toString()) : 0L);
            record.setTrackerId(node.trackerId);
            record.setRemoteUrl(node.remoteUrl);
            record.setTitle(node.title);
            record.setLastChapterRead(node.lastChapterRead != null ? node.lastChapterRead.floatValue() : 0.0f);
            record.setTotalChapters(node.totalChapters != null ? node.totalChapters : 0);
            record.setDisplayScore(node.displayScore);
            // date conversion if necessary, TrackRecord expects Instant
            // record.setFinishDate(...);
            // record.setStartDate(...);
            record.setScore(node.score != null ? node.score.floatValue() : 0.0f);
            record.setStatus(node.status != null ? node.status : 0);
            return record;
          })
          .findFirst()
          .orElse(null);
    } catch (Exception e) {
      throw new RuntimeException("Error while getting manga track records", e);
    }
  }

  /**
   * Updates the data of a track record on the server.
   *
   * @param trackRecord The {@link TrackRecord} object containing the data to be updated.
   * @throws RuntimeException If an error occurs while updating the track record, if the response
   *     from the server contains errors, or if the updated data does not match the expected data.
   */
  public void updateTrackerData(TrackRecord trackRecord) {
    var apolloClient = clientService.getApolloClient();

    String startDate;
    if (trackRecord.getStartDate() == null) {
      startDate = "0";
    } else {
      startDate = String.valueOf(trackRecord.getStartDate().toEpochMilli());
    }

    String finishDate;
    if (trackRecord.getFinishDate() == null) {
      finishDate = "0";
    } else {
      finishDate = String.valueOf(trackRecord.getFinishDate().toEpochMilli());
    }

    CompletableFuture<ApolloResponse<AllTheStuffForSuwayomiTrackingMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new AllTheStuffForSuwayomiTrackingMutation(
        trackRecord.getId(),
        finishDate,
        (double) trackRecord.getLastChapterRead(),
        startDate,
        trackRecord.getStatus()
    )).enqueue(new ApolloCallback<AllTheStuffForSuwayomiTrackingMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<AllTheStuffForSuwayomiTrackingMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while updating track record: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.updateTrack == null || data.updateTrack.trackRecord == null) {
        throw new RuntimeException("Error while updating track record");
      }

      var updatedRecord = data.updateTrack.trackRecord;

      if (updatedRecord.lastChapterRead.floatValue() != trackRecord.getLastChapterRead()) {
        throw new RuntimeException("Last chapter read was not updated correctly");
      }

      if (updatedRecord.status != trackRecord.getStatus()) {
        throw new RuntimeException("Status was not updated correctly");
      }

      log.info("Updated track record with ID {}", updatedRecord.id);
    } catch (Exception e) {
      throw new RuntimeException("Error while updating track record", e);
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
  public List<Status> getStatuses(int trackRecordId) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetStatusesQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetStatusesQuery(trackRecordId)).enqueue(new ApolloCallback<GetStatusesQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetStatusesQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while getting track statuses: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.tracker == null || data.tracker.statuses == null) {
        throw new RuntimeException("Error while getting track statuses");
      }

      return data.tracker.statuses.stream()
          .map(node -> {
            Status status = new Status();
            status.setName(node.name);
            status.setValue(node.value);
            return status;
          })
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error while getting track statuses", e);
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
  public void stopTracking(int recordId, boolean deleteRemote) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<StopTrackingNewMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new StopTrackingNewMutation(recordId, deleteRemote)).enqueue(new ApolloCallback<StopTrackingNewMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<StopTrackingNewMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while stopping tracking: " + response.errors);
      }

      log.info("Stopped tracking manga with ID {}", recordId);
    } catch (Exception e) {
      throw new RuntimeException("Error while stopping tracking", e);
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
  public List<String> getTrackingScores(int recordId) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetTrackingScoresQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetTrackingScoresQuery(recordId)).enqueue(new ApolloCallback<GetTrackingScoresQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetTrackingScoresQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while getting tracking scores: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.trackRecord == null || data.trackRecord.tracker == null) {
        throw new RuntimeException("Error while getting tracking scores");
      }

      return data.trackRecord.tracker.scores;
    } catch (Exception e) {
      throw new RuntimeException("Error while getting tracking scores", e);
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
  public void updateScore(int recordId, String value) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<UpdateScoreMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new UpdateScoreMutation(value, recordId)).enqueue(new ApolloCallback<UpdateScoreMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<UpdateScoreMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while updating score: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.updateTrack == null || data.updateTrack.trackRecord == null) {
        throw new RuntimeException("Error while updating score");
      }

      float score = data.updateTrack.trackRecord.score.floatValue();

      if (score != Float.parseFloat(value)) {
        throw new RuntimeException("Score was not updated correctly");
      }

      log.info("Updated score for track record with ID {}", recordId);
    } catch (Exception e) {
      throw new RuntimeException("Error while updating score", e);
    }
  }
}
