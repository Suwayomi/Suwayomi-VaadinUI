/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi;

import com.jayway.jsonpath.TypeRef;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Status;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackRecord;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
  public SuwayomiTrackingClient(WebClientService clientService,
      SuwayomiMetaClient suwayomiMetaClient) {
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
    @Language("graphql")
    String query =
        """
            query IsTrackerLoggedIn($id: Int!) {
              tracker(id: $id) {
                isLoggedIn
                isTokenExpired
              }
            }
            """;

    Map<String, Integer> variables = Map.of("id", id);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while checking if tracker is logged in");
    }

    return response.extractValueAsObject("tracker.isLoggedIn", Boolean.class);
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
    @Language("graphql")
    String query =
        """
            query GetTrackerAuthUrl($id: Int!) {
              tracker(id: $id) {
                authUrl
              }
            }
            """;

    Map<String, Integer> variables = Map.of("id", id);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while getting tracker auth url");
    }

    return response.extractValueAsObject("tracker.authUrl", String.class);
  }

  /**
   * Logs in to a tracker using the provided redirect URL and tracker ID.
   *
   * @param url the redirect URL to log in to the tracker
   * @param id  the ID of the tracker to log in to
   */
  public void loginTracker(String url, int id) {
    @Language("graphql")
    String query =
        """
            mutation LoginTracker($url: String!, $trackerId: Int!) {
              loginTrackerOAuth(input: {callbackUrl: $url, trackerId: $trackerId}) {
                isLoggedIn
              }
            }
            """;

    Map<String, Object> variables = Map.of("url", url, "trackerId", id);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while logging in tracker");
    }

    if (!response.extractValueAsObject("loginTrackerOAuth.isLoggedIn", Boolean.class)) {
      log.error("Server returned false after logging in the tracker with id {}", id);
    }
  }

  /**
   * Searches for a manga on a tracker using the provided query and tracker ID.
   *
   * @param query the search query for the manga
   * @param id    the ID of the tracker to search on
   * @return a list of {@link TrackerSearchResult} objects representing the search results
   * @see online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService.TrackerType
   */
  @SuppressWarnings("JavadocReference")
  public List<TrackerSearchResult> searchTracker(String query, int id) {
    @Language("graphql")
    String graphQuery =
        """
            query searchTracker($query: String!, $id: Int!) {
              searchTracker(input: {query: $query, trackerId: $id}) {
                trackSearches {
                  coverUrl
                  id
                  publishingStatus
                  publishingType
                  remoteId
                  startDate
                  summary
                  title
                  totalChapters
                  trackingUrl
                }
              }
            }
            """;

    Map<String, Object> variables = Map.of("query", query, "id", id);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(graphQuery, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while searching tracker");
    }

    if (response.hasErrors()) {
      String errorText = "Error while searching tracker: " + response.getErrors();
      log.error(errorText);
      throw new RuntimeException(errorText);
    }

    TypeRef<List<TrackerSearchResult>> typeRef = new TypeRef<>() {
    };

    return response.extractValueAsObject("searchTracker.trackSearches", typeRef);
  }

  /**
   * Tracks a manga on a tracker using the provided manga ID, external ID, and tracker ID.
   *
   * @param mangaId    the Suwayomi ID of the manga to be tracked
   * @param externalId the external ID of the manga on the tracker. This is the ID of the manga on
   *                   the tracker's website.
   * @param trackerId  the ID of the tracker to track the manga on.
   * @see online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService.TrackerType
   */
  @SuppressWarnings("JavadocReference")
  public void trackMangaOnTracker(int mangaId, long externalId, int trackerId) {
    @Language("graphql")
    var query =
        """
            mutation TrackManga($mangaId: Int!, $remoteId: LongString!, $trackerId: Int!) {
              bindTrack(input: {mangaId: $mangaId, remoteId: $remoteId, trackerId: $trackerId}) {
                trackRecord {
                  id
                  trackerId
                  mangaId
                }
              }
            }
            """;

    String remoteId = String.valueOf(externalId);

    var variables = Map.of("mangaId", mangaId, "remoteId", remoteId, "trackerId", trackerId);

    var graphClient = clientService.getDgsGraphQlClient();

    Duration timeout = Duration.ofSeconds(60);
    var optional = graphClient.reactiveExecuteQuery(query, variables).blockOptional(timeout);

    if (optional.isEmpty()) {
      throw new RuntimeException(
          "Didn't receive a response from the server after trying to track the manga");
    }

    if (optional.get().hasErrors()) {
      throw new RuntimeException("Error while tracking manga: " + optional.get().getErrors());
    }
  }

  /**
   * Syncs the manga data on the server with the tracker.
   *
   * @param mangaId the ID of the manga to sync
   */
  public void trackProgress(int mangaId) {
    @Language("graphql")
    var query =
        """
            mutation TrackProgressOnTrackers($mangaId: Int!) {
              trackProgress(input: {mangaId: $mangaId}) {
                trackRecords {
                  id
                }
              }
            }
            """;

    var graphClient = clientService.getDgsGraphQlClient();

    var variables = Map.of("mangaId", mangaId);

    Duration timeout = Duration.ofSeconds(10);
    var optional = graphClient.reactiveExecuteQuery(query, variables).blockOptional(timeout);

    if (optional.isEmpty()) {
      throw new RuntimeException(
          "Didn't receive a response from the server after trying to track the manga");
    }

    log.info("Tracked progress on trackers");
  }

  /**
   * Checks if a manga is tracked on a tracker using the provided manga ID and tracker ID.
   *
   * @param mangaId   the ID of the manga to check
   * @param trackerId the ID of the tracker to check
   * @return {@code true} if the manga is tracked on the tracker, {@code false} otherwise
   */
  public boolean isMangaTracked(int mangaId, int trackerId) {
    @Language("graphql")
    var query =
        """
            query IsMangaTracked($mangaId: Int!) {
              manga(id: $mangaId) {
                trackRecords {
                  nodes {
                    trackerId
                  }
                }
              }
            }
            """;

    var variables = Map.of("mangaId", mangaId, "trackerId", trackerId);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while checking if manga is tracked");
    }

    TypeRef<List<TrackRecord>> typeRef = new TypeRef<>() {
    };

    var trackRecords = response.extractValueAsObject("manga.trackRecords.nodes", typeRef);

    return trackRecords.stream().anyMatch(record -> record.getTrackerId() == trackerId);
  }

  /**
   * Returns the track record of a manga for a specific tracker.
   *
   * @param mangaId   the ID of the
   *                  {@link online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga Manga} to get
   *                  the track record for
   * @param trackerId the ID of the tracker to get the track record for
   * @return the {@link TrackRecord} of the manga for the tracker or {@code null} if the manga is
   * not tracked on the tracker.
   */
  public TrackRecord getTrackRecord(long mangaId, int trackerId) {
    @Language("graphql")
    var query = """
        query GetMangaTrackRecords($mangaId: Int!) {
          manga(id: $mangaId) {
            trackRecords {
              nodes {
            		id
                libraryId
                mangaId
                remoteId
                trackerId
               \s
                remoteUrl
               \s
                title
                lastChapterRead
                totalChapters
                displayScore
               \s
                finishDate
                startDate
                score
                status
              }
            }
          }
        }
        """;

    var variables = Map.of("mangaId", mangaId);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while getting manga track records");
    }

    if (response.hasErrors()) {
      throw new RuntimeException(
          "Error while getting manga track records: " + response.getErrors());
    }

    TypeRef<List<TrackRecord>> typeRef = new TypeRef<>() {
    };

    var trackRecords = response.extractValueAsObject("manga.trackRecords.nodes", typeRef);

    return trackRecords.stream()
        .filter(record -> record.getTrackerId() == trackerId)
        .findFirst()
        .orElse(null);
  }

  /**
   * Updates the data of a track record on the server.
   *
   * @param trackRecord The {@link TrackRecord} object containing the data to be updated.
   * @throws RuntimeException If an error occurs while updating the track record, if the response
   *                          from the server contains errors, or if the updated data does not match
   *                          the expected data.
   */
  public void updateTrackerData(TrackRecord trackRecord) {
    @Language("graphql")
    var query = """
        mutation AllTheStuffForSuwayomiTracking(
          $recordId: Int!
          $finishDate: LongString!
          $lastChapterRead: Float!
          $startDate: LongString!
          $status: Int!
        ) {
          updateTrack(
            input: {
              recordId: $recordId
              finishDate: $finishDate
              lastChapterRead: $lastChapterRead
              startDate: $startDate
              status: $status
            }
          ) {
            trackRecord {
              id
              finishDate
              lastChapterRead
              startDate
              status
            }
          }
        }
        """;

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

    var variables = Map.of(
        "recordId", trackRecord.getId(),
        "finishDate", finishDate,
        "lastChapterRead", trackRecord.getLastChapterRead(),
        "startDate", startDate,
        "status", trackRecord.getStatus()
    );

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while updating track record");
    }

    if (response.hasErrors()) {
      throw new RuntimeException("Error while updating track record: " + response.getErrors());
    }

    var updatedRecord = response.extractValueAsObject("updateTrack.trackRecord", TrackRecord.class);

    //check if the new data is as expected

    if (!Objects.equals(updatedRecord.getFinishDate(), trackRecord.getFinishDate())) {
      throw new RuntimeException("Finish date was not updated correctly");
    }

    if (updatedRecord.getLastChapterRead() != trackRecord.getLastChapterRead()) {
      throw new RuntimeException("Last chapter read was not updated correctly");
    }

    if (!Objects.equals(updatedRecord.getStartDate(), trackRecord.getStartDate())) {
      throw new RuntimeException("Start date was not updated correctly");
    }

    if (updatedRecord.getStatus() != trackRecord.getStatus()) {
      throw new RuntimeException("Status was not updated correctly");
    }

    log.info("Updated track record with ID {}", updatedRecord.getId());
  }

  /**
   * Retrieves the statuses for a specific track record.
   *
   * @param trackRecordId The ID of the track record for which the statuses are to be retrieved.
   * @return A list of Status objects representing the statuses for the specified track record.
   * @throws RuntimeException If an error occurs while retrieving the statuses or if the response
   *                          from the server contains errors.
   */
  public List<Status> getStatuses(int trackRecordId) {
    @Language("graphql")
    var query = """
        query GetStatuses($trackRecordId: Int!) {
          tracker(id: $trackRecordId) {
            statuses {
              name
              value
            }
          }
        }
        """;

    var variables = Map.of("trackRecordId", trackRecordId);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while getting track statuses");
    }

    if (response.hasErrors()) {
      throw new RuntimeException("Error while getting track statuses: " + response.getErrors());
    }

    TypeRef<List<Status>> typeRef = new TypeRef<>() {
    };

    return response.extractValueAsObject("tracker.statuses", typeRef);
  }

  /**
   * Stops tracking a manga on a tracker.
   *
   * @param recordId     The ID of the track record to stop tracking.
   * @param deleteRemote A boolean indicating whether to delete the remote track record.
   * @throws RuntimeException If an error occurs while stopping tracking or if the response from the
   *                          server contains errors.
   */
  public void stopTracking(int recordId, boolean deleteRemote) {
    @Language("graphql")
    String query = getStopTrackingQuery();

    var variables = Map.of("recordId", recordId, "deleteRemote", deleteRemote);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while stopping tracking");
    }

    if (response.hasErrors()) {
      throw new RuntimeException("Error while stopping tracking: " + response.getErrors());
    }

    log.info("Stopped tracking manga with ID {}", recordId);
  }

  /**
   * Constructs the GraphQL mutation query for stopping the tracking of a manga. The query differs
   * based on the server version.
   *
   * @return The GraphQL mutation query as a string.
   */
  @Language("graphql")
  private @NotNull String getStopTrackingQuery() {
    @Language("graphql")
    String query;

    var version = suwayomiMetaClient.getServerVersion();

    if (version.getRevisionNumber() >= 1510) {
      query = """
          mutation StopTracking($recordId: Int!, $deleteRemote: Boolean!) {
            unbindTrack(input: { recordId: $recordId, deleteRemoteTrack: $deleteRemote }) {
              trackRecord {
                id
              }
            }
          }
          """;
    } else {
      query = """
              mutation StopTracking($recordId: Int!) {
                updateTrack(input: { recordId: $recordId, unbind: true }) {
                  trackRecord {
                    id
                  }
                }
              }
          """;
    }
    return query;
  }

  /**
   * Retrieves the tracking scores for a specific track record.
   *
   * @param recordId The ID of the track record for which the tracker scores are to be retrieved.
   * @return A list of strings representing the available scores for the tracker type of the track
   * record.
   * @throws RuntimeException If an error occurs while retrieving the tracking scores or if the
   *                          response contains errors.
   */
  public List<String> getTrackingScores(int recordId) {
    @Language("graphql")
    var query = """
        query GetTrackingScores($recordId: Int!) {
          trackRecord(id: $recordId) {
            tracker {
              scores
            }
          }
        }
        """;

    var variables = Map.of("recordId", recordId);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while getting tracking scores");
    }

    if (response.hasErrors()) {
      throw new RuntimeException("Error while getting tracking scores: " + response.getErrors());
    }

    TypeRef<List<String>> typeRef = new TypeRef<>() {
    };

    return response.extractValueAsObject("trackRecord.tracker.scores", typeRef);
  }

  /**
   * Updates the score of a track record.
   *
   * @param recordId The ID of the track record to be updated.
   * @param value    The new score value as a string.
   * @throws RuntimeException If an error occurs while updating the score or if the updated score
   *                          does not match the expected value.
   */
  public void updateScore(int recordId, String value) {
    @Language("graphql")
    String query = """
        mutation updateScore($score: String!, $recordId: Int!) {
          updateTrack(input: {scoreString: $score, recordId: $recordId}) {
            trackRecord {
              score
            }
          }
        }
        """;

    var variables = Map.of("score", value, "recordId", recordId);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while updating score");
    }

    if (response.hasErrors()) {
      throw new RuntimeException("Error while updating score: " + response.getErrors());
    }

    float score = response.extractValueAsObject("updateTrack.trackRecord.score", Float.class);

    if (score != Float.parseFloat(value)) {
      throw new RuntimeException("Score was not updated correctly");
    }

    log.info("Updated score for track record with ID {}", recordId);
  }
}
