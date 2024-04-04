/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker;

import static online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus.CURRENT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tracking.OAuthData;
import online.hatsunemiku.tachideskvaadinui.data.tracking.TrackerTokens;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMangaListResponse;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMedia;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListScoreFormat;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.GraphQLRequest;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.MangaList;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.responses.AniListAddMangaResponse;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.responses.AniListChangePrivacyStatusResponse;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.responses.AniListChangeStatusResponse;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.responses.AniListMangaStatistics;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Is responsible for interacting with the AniList API. This class provides methods for retrieving
 * and updating AniList tokens, searching for manga, and managing manga lists.
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
  private final ObjectMapper mapper;
  private final WebClient webClient;

  /**
   * Constructs an AniListAPIService object with the given SettingsService and ObjectMapper
   * dependencies.
   *
   * @param dataService the TrackingDataService object to be used for retrieving and updating the
   *     AniList token and manga trackers
   * @param mapper the ObjectMapper object to be used for serializing and deserializing JSON data.
   */
  public AniListAPIService(TrackingDataService dataService, ObjectMapper mapper) {
    this.dataService = dataService;
    this.mapper = mapper;
    this.webClient = WebClient.create(ANILIST_API_URL);

    try {
      log.info("User ID: {}", getCurrentUserId());
    } catch (RuntimeException e) {
      log.info("No AniList token set yet");
    }
  }

  /**
   * Retrieves the AniList token from the settings.
   *
   * @return an Optional containing the AniList token if it is present, otherwise returns an empty
   *     Optional
   */
  private Optional<OAuthData> getAniListToken() {
    TrackerTokens trackerTokens = dataService.getTokens();

    if (!trackerTokens.hasAniListToken()) {
      return Optional.empty();
    }

    return Optional.of(trackerTokens.getAniListToken());
  }

  /**
   * Checks if there is an AniList token available.
   *
   * @return true if there is an AniList token available, false otherwise
   */
  public boolean hasAniListToken() {
    return getAniListToken().isPresent();
  }

  /**
   * Retrieves the AniList token header.
   *
   * @return the AniList token header as a string
   * @throws IllegalStateException if there is no AniList token available or if the token is not
   *     valid.
   */
  private String getAniListTokenHeader() {
    if (!hasAniListToken()) {
      throw new IllegalStateException("No AniList Token");
    }

    //noinspection OptionalGetWithoutIsPresent - hasAniListToken() is called before this method

    var token = getAniListToken().get();

    if (!token.getTokenType().equals("Bearer")) {
      throw new IllegalStateException("AniList token is not a Bearer token");
    }

    return "Bearer " + token.getAccessToken();
  }

  /**
   * Generates the authorization URL for the AniList API. The generated URL includes the OAuth code
   * pattern with the client ID and the redirect URI.
   *
   * @return The generated authorization URL.
   */
  public String getAniListAuthUrl() {
    return String.format(OAUTH_CODE_PATTERN, OAUTH_CLIENT_ID);
  }

  /**
   * Searches for manga with the given name on AniList. Returns a response object containing a list
   * of manga, which are possible matches for the given name.
   *
   * @param name The name of the manga to search for
   * @return An {@link AniListMangaListResponse} object representing the manga search results
   */
  public AniListMangaListResponse searchManga(String name) {
    String query =
        """
            query Search($search: String) {
                Page(perPage: 50) {
                  media(search: $search, type: MANGA, format_not_in: [NOVEL]) {
                    id
                    title {
                      romaji
                      english
                      native
                      userPreferred
                    }
                    coverImage {
                      large
                    }
                    format
                    status
                    chapters
                    description
                    startDate {
                      year
                      month
                      day
                    }
                  }
                }
              }
            """;

    String variables = """
        {
          "search": "%s"
        }
        """.formatted(name);

    GraphQLRequest request = new GraphQLRequest(query, variables);

    return webClient
        .post()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(request))
        .retrieve()
        .bodyToMono(AniListMangaListResponse.class)
        .block();
  }

  /**
   * Returns the current user's ID.
   *
   * @return The current user's ID
   * @throws RuntimeException If no AniList token is available or if there is an error retrieving
   *     the user ID
   */
  private int getCurrentUserId() {
    if (!hasAniListToken()) {
      throw new RuntimeException("No AniList Token");
    }

    String query =
        """
            query {
              Viewer {
                id
              }
            }
            """;

    String variables = "{}";

    String id = sendAuthGraphQLRequest(query, variables);

    if (id == null || id.isEmpty()) {
      throw new RuntimeException("No user ID");
    }

    // json
    JsonObject json = Json.parse(id);

    return (int) json.getObject("data").getObject("Viewer").getNumber("id");
  }

  /**
   * Checks whether the manga with the given ID is in the user's list on AniList.
   *
   * @param mangaId The ID of the manga to check
   * @return {@code true} if the manga is in the user's list, {@code false} otherwise
   * @throws RuntimeException If an error occurs while checking the manga
   */
  public boolean isMangaInList(int mangaId) {
    String query =
        """
            query ($userId: Int, $mangaId: Int) {
              MediaList(userId: $userId, type: MANGA, mediaId: $mangaId) {
                id
                status
              }
            }
            """;

    String variables =
        """
            {
              "userId": %s,
              "mangaId": %s
            }
            """
            .formatted(getCurrentUserId(), mangaId);

    GraphQLRequest request = new GraphQLRequest(query, variables);

    // false if 404. True if 200. Error if anything else
    var response =
        webClient
            .post()
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", getAniListTokenHeader())
            .bodyValue(request)
            .exchangeToMono(Mono::just)
            .block();

    if (response == null) {
      throw new RuntimeException("Response is null");
    }

    if (response.statusCode().is2xxSuccessful()) {
      return true;
    }

    if (response.statusCode().is4xxClientError()) {
      return false;
    }

    throw new RuntimeException("Unexpected response code: " + response.statusCode());
  }

  /**
   * Adds a manga to the user's list with the status "READING".
   *
   * @param mangaId The ID of the manga to be added
   * @throws RuntimeException If an error occurs while adding the manga to the list
   */
  public void addMangaToList(int mangaId, boolean isPrivate) {
    // language=graphql
    String query =
        """
            mutation($mangaId: Int, $status: MediaListStatus, $private: Boolean){
              SaveMediaListEntry(mediaId: $mangaId, status: $status, private: $private) {
                id
                status
                private
              }
            }
            """;

    String variables =
        """
            {
              "mangaId": %s,
              "status": "%s",
              "private": %s
            }
            """
            .formatted(mangaId, CURRENT.name(), isPrivate);

    GraphQLRequest request = new GraphQLRequest(query, variables);

    // false if 404. True if 200. Error if anything else
    var response =
        webClient
            .post()
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", getAniListTokenHeader())
            .bodyValue(request)
            .retrieve()
            .toEntity(String.class)
            .block();

    if (response == null) {
      throw new RuntimeException("Response is null");
    }

    if (response.getStatusCode().is2xxSuccessful()) {
      return;
    }

    if (response.getStatusCode().is4xxClientError()) {
      throw new RuntimeException("Manga not found");
    }

    throw new RuntimeException("Unexpected response code: " + response.getStatusCode());
  }

  /**
   * Retrieves Manga statistics from AniList given a manga ID.
   *
   * @param mangaId The ID of the manga.
   * @return The AniListMangaStatistics object containing the manga statistics.
   * @throws WebClientResponseException.NotFound If the manga with the specified ID is not found.
   * @throws RuntimeException If the response is null, or if there is an error parsing the JSON
   *     response.
   */
  public AniListMangaStatistics getMangaFromList(int mangaId) {
    String query =
        """
            query ($mangaId: Int, $userId: Int) {
              MediaList(mediaId: $mangaId, userId: $userId) {
                status
                progress
                score
                startedAt {
                  year
                  month
                  day
                }
                completedAt {
                  year
                  month
                  day
                }
              }
            }
            """;

    String variables =
        """
            {
              "mangaId": %s,
              "userId": %s
            }
            """
            .formatted(mangaId, getCurrentUserId());

    GraphQLRequest request = new GraphQLRequest(query, variables);

    String response;
    try {
      response =
          webClient
              .post()
              .contentType(MediaType.APPLICATION_JSON)
              .header("Authorization", getAniListTokenHeader())
              .bodyValue(request)
              .retrieve()
              .bodyToMono(String.class)
              .block();
    } catch (WebClientResponseException.NotFound e) {
      log.warn("Manga with ID {} not found", mangaId);
      throw e;
    }

    if (response == null) {
      throw new RuntimeException("Response is null");
    }

    JsonObject json = Json.parse(response);
    json = json.getObject("data").getObject("MediaList");

    try {
      var statistics = mapper.readValue(json.toJson(), AniListMangaStatistics.class);
      if (statistics == null) {
        throw new RuntimeException("Statistics is null");
      }

      return statistics;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public AniListScoreFormat getScoreFormat() {
    String query =
        """
            query {
              Viewer {
                mediaListOptions {
                  scoreFormat
                }
              }
            }
            """;

    String variables = "{}";

    String response = sendAuthGraphQLRequest(query, variables);

    if (response == null || response.isEmpty()) {
      throw new RuntimeException("Response is null");
    }

    JsonObject json = Json.parse(response);

    String scoreFormat =
        json.getObject("data")
            .getObject("Viewer")
            .getObject("mediaListOptions")
            .getString("scoreFormat");

    return AniListScoreFormat.valueOf(scoreFormat);
  }

  public Optional<Integer> getChapterCount(int mangaId) {
    String query =
        """
            query ($mangaId: Int) {
              Media(id: $mangaId) {
                chapters
              }
            }
            """;

    String variables =
        """
            {
              "mangaId": %s
            }
            """
            .formatted(mangaId);

    String response = sendAuthGraphQLRequest(query, variables);

    if (response == null || response.isEmpty()) {
      throw new RuntimeException("Response is null");
    }

    JsonObject json = Json.parse(response);

    JsonValue possibleInt = json.getObject("data").getObject("Media").get("chapters");

    if (possibleInt == null || possibleInt.jsEquals(Json.createNull())) {
      return Optional.empty();
    }

    return Optional.of((int) possibleInt.asNumber());
  }

  @Nullable
  private String sendAuthGraphQLRequest(String query, String variables) {
    GraphQLRequest request = new GraphQLRequest(query, variables);

    return webClient
        .post()
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", getAniListTokenHeader())
        .bodyValue(request)
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

  public void updateMangaProgress(int mangaId, float mangaProgress) {
    String query =
        """
            mutation ($mangaId: Int, $progress: Int) {
              SaveMediaListEntry (mediaId: $mangaId, progress: $progress) {
                id
                progress
              }
            }
            """;

    String variables =
        """
            {
              "mangaId": %s,
              "progress": %s
            }
            """
            .formatted(mangaId, mangaProgress);

    // {"data":{"SaveMediaListEntry":{"id":360194831,"progress":1}}}
    var json = Json.parse(sendAuthGraphQLRequest(query, variables));
    String data = json.getObject("data").getObject("SaveMediaListEntry").toJson();

    try {
      var response = mapper.readValue(data, AniListAddMangaResponse.class);
      if (response == null) {
        throw new RuntimeException("Response is null");
      }
      log.info("Updated manga with ID {} to progress {}", response.id(), response.progress());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public void updateMangaStatus(int aniListId, AniListStatus value) {
    String query =
        """
            mutation ($mangaId: Int, $status: MediaListStatus) {
              SaveMediaListEntry (mediaId: $mangaId, status: $status) {
                id
                status
              }
            }
            """;

    String variables =
        """
            {
              "mangaId": %s,
              "status": "%s"
            }
            """
            .formatted(aniListId, value.name());

    var json = Json.parse(sendAuthGraphQLRequest(query, variables));

    String data = json.getObject("data").getObject("SaveMediaListEntry").toJson();

    try {
      var response = mapper.readValue(data, AniListChangeStatusResponse.class);
      if (response == null) {
        throw new RuntimeException("Response is null");
      }
      log.info("Updated manga with ID {} to status {}", response.id(), response.status());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public void updateMangaScore(int aniListId, int value) {

    String query =
        """
            mutation ($mangaId: Int, $score: Float) {
              SaveMediaListEntry (mediaId: $mangaId, score: $score) {
                id
                score
              }
            }
            """;

    String variables =
        """
            {
              "mangaId": %s,
              "score": %s
            }
            """
            .formatted(aniListId, value);

    var json = Json.parse(sendAuthGraphQLRequest(query, variables));

    String data = json.getObject("data").getObject("SaveMediaListEntry").toJson();

    try {
      var response = mapper.readValue(data, AniListChangeStatusResponse.class);
      if (response == null) {
        throw new RuntimeException("Response is null");
      }
      log.info("Updated manga with ID {} to score {}", response.id(), response.status());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public void updateMangaStartDate(int aniListId, MediaDate date) {
    String query =
        """
            mutation ($mangaId: Int, $startDate: FuzzyDateInput) {
              SaveMediaListEntry (mediaId: $mangaId, startedAt: $startDate) {
                id
                startedAt {
                  year
                  month
                  day
                }
              }
            }
            """;

    String variables =
        """
            {
              "mangaId": %s,
              "startDate": {
                "year": %s,
                "month": %s,
                "day": %s
              }
            }
            """
            .formatted(aniListId, date.year(), date.month(), date.day());

    var json = Json.parse(sendAuthGraphQLRequest(query, variables));

    String data = json.getObject("data").getObject("SaveMediaListEntry").toJson();

    try {
      var response = mapper.readValue(data, AniListChangeStatusResponse.class);
      if (response == null) {
        throw new RuntimeException("Response is null");
      }
      log.info("Updated manga with ID {} to start date {}", response.id(), response.status());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public void updateMangaEndDate(int aniListId, MediaDate date) {
    String query =
        """
            mutation ($mangaId: Int, $endDate: FuzzyDateInput) {
              SaveMediaListEntry (mediaId: $mangaId, completedAt: $endDate) {
                id
                completedAt {
                  year
                  month
                  day
                }
              }
            }
            """;

    String variables =
        """
            {
              "mangaId": %s,
              "endDate": {
                "year": %s,
                "month": %s,
                "day": %s
              }
            }
            """
            .formatted(aniListId, date.year(), date.month(), date.day());

    var json = Json.parse(sendAuthGraphQLRequest(query, variables));

    String data = json.getObject("data").getObject("SaveMediaListEntry").toJson();

    try {
      var response = mapper.readValue(data, AniListChangeStatusResponse.class);
      if (response == null) {
        throw new RuntimeException("Response is null");
      }
      log.info("Updated manga with ID {} to end date {}", response.id(), response.status());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Retrieves the user's manga list.
   *
   * @return The manga list containing the user's reading, plan to read, completed, on hold, and
   *     dropped manga
   * @throws RuntimeException If an error occurs while retrieving the manga list
   */
  public MangaList getMangaList() {
    String query =
        """
            query ($userId: Int) {
              MediaListCollection(userId: $userId, type: MANGA) {
                lists {
                  entries {
                    id
                    mediaId
                    status
                    progress
                    score
                    startedAt {
                      year
                      month
                      day
                    }
                    completedAt {
                      year
                      month
                      day
                    }
                    media {
                      title {
                        romaji
                        english
                        native
                      }
                      coverImage {
                        large
                        medium
                      }
                    }
                  }
                }
              }
            }
            """;

    String variables =
        """
            {
              "userId": %s
            }
            """
            .formatted(getCurrentUserId());

    String response = sendAuthGraphQLRequest(query, variables);

    if (response == null || response.isEmpty()) {
      throw new RuntimeException("Response is null");
    }

    var lists = getListsFromResponse(response);

    int listSize = lists.length();

    List<AniListMedia> completed = new ArrayList<>();
    List<AniListMedia> reading = new ArrayList<>();
    List<AniListMedia> dropped = new ArrayList<>();
    List<AniListMedia> onHold = new ArrayList<>();
    List<AniListMedia> planToRead = new ArrayList<>();

    for (int i = 0; i < listSize; i++) {
      var list = lists.getObject(i).getArray("entries");

      var typeRef = new TypeReference<List<AniListMedia>>() {};
      try {
        for (int j = 0; j < list.length(); j++) {
          replaceMediaWithImageAndTitle(list, j);
        }

        String listJson = list.toJson();

        var tempList = mapper.readValue(listJson, typeRef);

        var status = AniListStatus.valueOf(tempList.get(0).status());

        switch (status) {
          case COMPLETED -> completed.addAll(tempList);
          case CURRENT, REPEATING -> reading.addAll(tempList);
          case DROPPED -> dropped.addAll(tempList);
          case PAUSED -> onHold.addAll(tempList);
          case PLANNING -> planToRead.addAll(tempList);
          default -> log.warn("Unknown status: {}", status);
        }

      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      } catch (IllegalArgumentException e) {
        log.warn("Unknown status: {}", e.getMessage());
      }
    }

    return new MangaList(reading, planToRead, completed, onHold, dropped);
  }

  /**
   * Replaces the "media" object in a JsonArray with "coverImage" and "title" objects.
   *
   * @param list The JsonArray containing the media object to be replaced
   * @param j The index of the media object within the JsonArray
   */
  private void replaceMediaWithImageAndTitle(JsonArray list, int j) {
    var media = list.getObject(j).getObject("media");
    var coverImage = media.getObject("coverImage");
    var title = media.getObject("title");

    // remove media from object
    list.getObject(j).remove("media");
    // add back the two keys
    list.getObject(j).put("coverImage", coverImage);
    list.getObject(j).put("title", title);
  }

  /**
   * Parses a JSON response and returns the "lists" array from the "MediaListCollection" object.
   *
   * @param response The JSON response to parse
   * @return The "lists" array from the "MediaListCollection" object
   */
  private JsonArray getListsFromResponse(String response) {
    JsonObject json = Json.parse(response);
    var data = json.getObject("data");
    var collection = data.getObject("MediaListCollection");
    return collection.getArray("lists");
  }

  public void updateMangaPrivacyStatus(int aniListId, boolean isPrivate) {
    // language=graphql
    String query =
        """
            mutation ($mangaId: Int, $private: Boolean) {
              SaveMediaListEntry(mediaId: $mangaId, private: $private) {
                id
                private
              }
            }
            """;

    String variables =
        """
            {
              "mangaId": %s,
              "private": %s
            }
            """
            .formatted(aniListId, isPrivate);

    var json = Json.parse(sendAuthGraphQLRequest(query, variables));
    String data = json.getObject("data").getObject("SaveMediaListEntry").toJson();

    try {
      var response = mapper.readValue(data, AniListChangePrivacyStatusResponse.class);
      if (response == null) {
        throw new RuntimeException("Response is null");
      }
      log.info(
          "Updated manga with ID {} to privacy status {}", response.id(), response.isPrivate());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private int getMangaListEntryId(int mangaId) {
    // language=graphql
    String query =
        """
            query ($mangaId: Int, $userId: Int) {
              MediaList(mediaId: $mangaId, userId: $userId) {
                id
              }
            }
            """;

    int userId = getCurrentUserId();

    String variables =
        """
            {
              "mangaId": %s,
              "userId": %s
            }
            """
            .formatted(mangaId, userId);

    var json = Json.parse(sendAuthGraphQLRequest(query, variables));
    return (int) json.getObject("data").getObject("MediaList").getNumber("id");
  }

  public void removeMangaFromList(int aniListId) {
    // language=graphql
    String query =
        """
            mutation ($entryId: Int) {
              DeleteMediaListEntry(id: $entryId) {
                deleted
              }
            }
            """;

    int entryId = getMangaListEntryId(aniListId);

    var variables = """
        {
          "entryId": %s
        }
        """.formatted(entryId);

    var json = Json.parse(sendAuthGraphQLRequest(query, variables));

    if (json == null) {
      throw new RuntimeException("Response is null");
    }

    boolean deleted =
        json.getObject("data").getObject("DeleteMediaListEntry").getBoolean("deleted");

    if (deleted) {
      log.info("Deleted manga with ID {}", aniListId);
    } else {
      throw new RuntimeException("Manga could not be deleted");
    }
  }
}
