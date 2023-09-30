package online.hatsunemiku.tachideskvaadinui.services;

import static online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus.CURRENT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
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
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.responses.AniListChangeStatusResponse;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.responses.AniListMangaStatistics;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
   *                    AniList token and manga trackers
   * @param mapper      the ObjectMapper object to be used for serializing and deserializing JSON
   *                    data.
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
   * Optional
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
   *                               valid.
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
   *                          the user ID
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
  public void addMangaToList(int mangaId) {
    String query =
        """
            mutation($mangaId: Int, $status: MediaListStatus){
              SaveMediaListEntry(mediaId: $mangaId, status: $status) {
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
            .formatted(mangaId, CURRENT.name());

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

    var response =
        webClient
            .post()
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", getAniListTokenHeader())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .block();

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

  public void updateMangaProgress(int mangaId, int mangaProgress) {
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

  public MangaList getMangaList() {
    String query = """
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

    String variables = """
        {
          "userId": %s
        }
        """.formatted(getCurrentUserId());

    String response = sendAuthGraphQLRequest(query, variables);

    if (response == null || response.isEmpty()) {
      throw new RuntimeException("Response is null");
    }

    JsonObject json = Json.parse(response);
    var data = json.getObject("data");
    var collection = data.getObject("MediaListCollection");
    var lists = collection.getArray("lists");

    int listSize = lists.length();

    List<AniListMedia> completed = null;
    List<AniListMedia> reading = null;
    List<AniListMedia> dropped = null;
    List<AniListMedia> onHold = null;
    List<AniListMedia> planToRead = null;

    for (int i = 0; i < listSize; i++) {
      var list = lists.getObject(i).getArray("entries");

      var typeRef = new TypeReference<List<AniListMedia>>() {
      };
      try {
        for (int j = 0; j < list.length(); j++) {
          var media = list.getObject(j).getObject("media");
          var coverImage = media.getObject("coverImage");
          var title = media.getObject("title");

          //remove media from object
          list.getObject(j).remove("media");
          //add back the two keys
          list.getObject(j).put("coverImage", coverImage);
          list.getObject(j).put("title", title);
        }

        String listJson = list.toJson();

        var tempList = mapper.readValue(listJson, typeRef);

        var status = AniListStatus.valueOf(tempList.get(0).status());

        switch (status) {
          case COMPLETED -> completed = tempList;
          case CURRENT -> reading = tempList;
          case DROPPED -> dropped = tempList;
          case PAUSED -> onHold = tempList;
          case PLANNING -> planToRead = tempList;
        }

      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      } catch (IllegalArgumentException e) {
        log.warn("Unknown status: {}", e.getMessage());
      }
    }

    if (completed == null) {
      completed = List.of();
    }

    if (reading == null) {
      reading = List.of();
    }

    if (dropped == null) {
      dropped = List.of();
    }

    if (onHold == null) {
      onHold = List.of();
    }

    if (planToRead == null) {
      planToRead = List.of();
    }

    return new MangaList(reading, planToRead, completed, onHold, dropped);
  }
}
