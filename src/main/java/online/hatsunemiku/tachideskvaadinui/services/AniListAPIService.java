package online.hatsunemiku.tachideskvaadinui.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import online.hatsunemiku.tachideskvaadinui.data.tracking.OAuthResponse;
import online.hatsunemiku.tachideskvaadinui.data.tracking.TrackerTokens;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMangaListResponse;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListScoreFormat;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.GraphQLRequest;
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
  private static final String OAUTH_CLIENT_ID = "13903";
  private static final String OAUTH_CLIENT_SECRET = "BNqU6CmatS5LgdJRYpNB3e0kgDdgupmoX8XU4PrG";
  private static final String OAUTH_REDIRECT_URI = "http://localhost:8080/validate/anilist";
  public static final String OAUTH_URL = "https://anilist.co/api/v2/oauth";
  private static final String OAUTH_CODE_PATTERN =
      OAUTH_URL + "/authorize?client_id=%s&redirect_uri=%s&response_type=code";
  private final OkHttpClient client;

  private final SettingsService settingsService;
  private final ObjectMapper mapper;
  private final WebClient webClient;

  /**
   * Constructs an AniListAPIService object with the given SettingsService and ObjectMapper
   * dependencies.
   *
   * @param settingsService the SettingsService object to be used for accessing user settings.
   * @param mapper the ObjectMapper object to be used for serializing and deserializing JSON data.
   */
  public AniListAPIService(SettingsService settingsService, ObjectMapper mapper) {
    this.settingsService = settingsService;
    this.client = new OkHttpClient();
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
  private Optional<String> getAniListToken() {
    String token = settingsService.getSettings().getTrackerTokens().getAniListToken();

    if (token == null || token.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(token);
  }

  /**
   * Checks if there is an AniList token available.
   *
   * @return true if there is an AniList token available, false otherwise
   */
  public boolean hasAniListToken() {
    return getAniListToken().isPresent();
  }

  private String getAniListTokenHeader() {
    if (!hasAniListToken()) {
      throw new IllegalStateException("No AniList Token");
    }

    //noinspection OptionalGetWithoutIsPresent - hasAniListToken() is called before this method
    return "Bearer " + getAniListToken().get();
  }

  /**
   * Generates the authorization URL for the AniList API. The generated URL includes the OAuth code
   * pattern with the client ID and the redirect URI.
   *
   * @return The generated authorization URL.
   */
  public String getAniListAuthUrl() {
    return String.format(OAUTH_CODE_PATTERN, OAUTH_CLIENT_ID, OAUTH_REDIRECT_URI);
  }

  /**
   * Requests an OAuth token using the given authorization code. The new OAuth Information is also
   * saved via {@link TrackerTokens#setAniListAuth(OAuthResponse)}
   *
   * @param code The authorization code
   * @throws RuntimeException If an error occurs while requesting the OAuth token
   */
  public void requestOAuthToken(String code) {

    String oauthTokenUrl = OAUTH_URL + "/token";

    // OAuth headers
    okhttp3.Headers headers =
        new okhttp3.Headers.Builder()
            .add("Content-Type", "application/json")
            .add("Accept", "application/json")
            .build();

    // OAuth json
    String json =
        """
        {
          "grant_type": "authorization_code",
          "client_id": "%s",
          "client_secret": "%s",
          "redirect_uri": "%s",
          "code": "%s"
        }
        """
            .formatted(OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_REDIRECT_URI, code);

    RequestBody body =
        RequestBody.create(json, okhttp3.MediaType.parse("application/json; charset=utf-8"));

    Request request = new Request.Builder().url(oauthTokenUrl).headers(headers).post(body).build();

    try (var response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new RuntimeException("Unexpected code " + response);
      }

      ResponseBody accessTokenBody = response.body();

      if (accessTokenBody == null) {
        throw new RuntimeException("Access token body is null");
      }

      String responseJson = accessTokenBody.string();

      OAuthResponse aniListAuth = mapper.readValue(responseJson, OAuthResponse.class);

      settingsService.getSettings().getTrackerTokens().setAniListAuth(aniListAuth);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
            .formatted(mangaId, AniListStatus.CURRENT.name());

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
        """.formatted(mangaId);

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
}