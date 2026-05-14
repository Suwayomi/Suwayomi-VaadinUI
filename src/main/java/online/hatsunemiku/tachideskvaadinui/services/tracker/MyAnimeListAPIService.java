/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import online.hatsunemiku.tachideskvaadinui.data.tracking.OAuthData;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.data.tracking.mal.MALManga;
import online.hatsunemiku.tachideskvaadinui.data.tracking.mal.MALMangaListEntry;
import online.hatsunemiku.tachideskvaadinui.data.tracking.mal.MALMangaListResponse;
import online.hatsunemiku.tachideskvaadinui.data.tracking.mal.MALMangaStatus;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import online.hatsunemiku.tachideskvaadinui.utils.PKCEUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The MyAnimeListAPIService class is responsible for interacting with the MyAnimeList API.
 *
 * <p>This class provides methods for authentication with MyAnimeList and retrieving manga/user
 * information from the API.
 */
@Service
public class MyAnimeListAPIService {

  private static final Logger log = LoggerFactory.getLogger(MyAnimeListAPIService.class);
  private static final String MAL_API_URL = "https://api.myanimelist.net/v2";
  private final String CLIENT_ID = "a039c56fb609cd33ebd59381a6e9b460";
  private final TrackingDataService tds;
  private final WebClient webClient;
  private final Cache<UUID, String> pkceCache;
  @Nullable
  private String accessToken;

  /**
   * Initializes an instance of the MyAnimeListAPIService class.
   *
   * @param tds The {@link TrackingDataService} used for storing tokens.
   * @param webClient The {@link WebClient} used for making requests to the MAL API.
   */
  public MyAnimeListAPIService(TrackingDataService tds, WebClient webClient) {
    this.tds = tds;
    this.webClient = webClient;
    this.pkceCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    if (!tds.getTokens().hasMalToken()) {
      return;
    }

    OAuthData data = tds.getTokens().getMalToken();

    if (data.getExpiresAsInstant().isBefore(Instant.now())) {
      try {
        var newData = refreshToken(data.getRefreshToken());
        tds.getTokens().setMalToken(newData);
        data = newData;
      } catch (Exception e) {
        log.error("Failed to refresh MAL token", e);
        return;
      }
    }

    authenticateMALWithToken(data);
  }

  /**
   * Authenticates the MAL API with the provided {@link OAuthData} object containing the access
   * token.
   *
   * @param data The {@link OAuthData} object containing the access token.
   */
  private void authenticateMALWithToken(OAuthData data) {
    this.accessToken = data.getAccessToken();
    if (this.accessToken != null && !this.accessToken.startsWith("Bearer")) {
      this.accessToken = "Bearer " + this.accessToken;
    }
  }

  /**
   * Generates the authorization URL for MyAnimeList. This URL is used to authenticate the user with
   * the MAL API.
   *
   * @return The authorization URL for MyAnimeList.
   */
  @NotNull
  public String getAuthUrl() {
    String baseUrl = "https://myanimelist.net/v1/oauth2/authorize";
    String responseType = "code";
    String codeChallenge = PKCEUtils.generateCodeVerifier(128);

    UUID pkceId = UUID.randomUUID();
    pkceCache.put(pkceId, codeChallenge);

    String stateParam = "{\"pkceId\":\"%s\"}";
    stateParam = URLEncoder.encode(stateParam.formatted(pkceId), StandardCharsets.UTF_8);

    String params = "response_type=%s&client_id=%s&code_challenge=%s&state=%s";
    params =
        params.formatted(responseType, CLIENT_ID, codeChallenge, stateParam);

    return "%s?%s".formatted(baseUrl, params);
  }

  /**
   * Checks if the user has a valid MAL token.
   *
   * @return {@code true} if the user has a valid MAL token, {@code false} otherwise.
   */
  public boolean hasMalToken() {
    return tds.getTokens().hasMalToken();
  }

  /**
   * Exchanges the authorization code for an access and refresh token. Verifies the PKCE ID before
   * exchanging the code for tokens.
   *
   * @param code The authorization code to exchange for tokens.
   * @param pkceId The PKCE ID used for generating the code challenge.
   */
  public void exchangeCodeForTokens(String code, String pkceId) {
    String pkce = pkceCache.getIfPresent(UUID.fromString(pkceId));

    if (pkce == null) {
      throw new IllegalArgumentException("Invalid PKCE ID");
    }

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("client_id", CLIENT_ID);
    body.add("grant_type", "authorization_code");
    body.add("code", code);
    body.add("code_verifier", pkce);

    OAuthData data = webClient
        .post()
        .uri("https://myanimelist.net/v1/oauth2/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(body))
        .retrieve()
        .bodyToMono(OAuthData.class)
        .block();

    if (data == null) {
      throw new RuntimeException("Failed to exchange code for tokens");
    }

    tds.getTokens().setMalToken(data);
    authenticateMALWithToken(data);
  }

  /**
   * Refreshes the access token using the refresh token.
   *
   * @param refreshToken The refresh token to use for refreshing the access token.
   * @return An {@link OAuthData} object containing the new access token and refresh token.
   */
  private OAuthData refreshToken(String refreshToken) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("client_id", CLIENT_ID);
    body.add("grant_type", "refresh_token");
    body.add("refresh_token", refreshToken);

    return webClient
        .post()
        .uri("https://myanimelist.net/v1/oauth2/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(body))
        .retrieve()
        .bodyToMono(OAuthData.class)
        .block();
  }

  /**
   * Retrieves a list of {@link MALManga} objects with the specified status.
   *
   * @param status The {@link MALMangaStatus} enum value representing the status of the manga.
   * @return A List of {@link MALManga} objects with the specified status.
   * @throws IllegalStateException If not authenticated with MyAnimeList (MAL).
   */
  public List<MALManga> getMangaWithStatus(MALMangaStatus status) {
    if (accessToken == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    List<MALManga> allManga = new ArrayList<>();
    String nextUrl = MAL_API_URL + "/users/@me/mangalist?status=" + status.getApiValue() + "&limit=1000&fields=main_picture";

    while (nextUrl != null) {
      MALMangaListResponse response = webClient
          .get()
          .uri(nextUrl)
          .header("Authorization", accessToken)
          .retrieve()
          .bodyToMono(MALMangaListResponse.class)
          .block();

      if (response != null && response.data() != null) {
        allManga.addAll(response.data().stream().map(MALMangaListEntry::manga).toList());
        nextUrl = response.paging() != null ? response.paging().next() : null;
      } else {
        nextUrl = null;
      }
    }

    log.debug("Got {} manga with status {}", allManga.size(), status.name());
    return allManga;
  }

  /**
   * Updates the status of a manga on the user's list.
   *
   * @param id The MyAnimeList ID of the manga.
   * @param status The new status to be used.
   */
  public void updateMangaListStatus(int id, MALMangaStatus status) {
    if (accessToken == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("status", status.getApiValue());

    webClient
        .put()
        .uri(MAL_API_URL + "/manga/" + id + "/my_list_status")
        .header("Authorization", accessToken)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(body))
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  /**
   * Updates the score of a manga on the user's list.
   *
   * @param id The MyAnimeList ID of the manga.
   * @param score The new score of the manga.
   */
  public void updateMangaListScore(int id, double score) {
    if (accessToken == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("score", String.valueOf((int) score));

    webClient
        .put()
        .uri(MAL_API_URL + "/manga/" + id + "/my_list_status")
        .header("Authorization", accessToken)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(body))
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  /**
   * Updates the end date of a manga on the user's list.
   *
   * @param malId The MyAnimeList ID of the manga.
   * @param date The new end date of the manga.
   */
  public void updateMangaListEndDate(int malId, MediaDate date) {
    if (accessToken == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    if (date.year() == null || date.month() == null || date.day() == null) {
      return;
    }

    String formattedDate = LocalDate.of(date.year(), date.month(), date.day())
        .format(DateTimeFormatter.ISO_LOCAL_DATE);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("finish_date", formattedDate);

    webClient
        .put()
        .uri(MAL_API_URL + "/manga/" + malId + "/my_list_status")
        .header("Authorization", accessToken)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(body))
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  /**
   * Removes a manga from the user's list.
   *
   * @param malId The MyAnimeList ID of the manga to remove.
   */
  public void removeMangaFromList(int malId) {
    if (accessToken == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    webClient
        .delete()
        .uri(MAL_API_URL + "/manga/" + malId + "/my_list_status")
        .header("Authorization", accessToken)
        .retrieve()
        .toBodilessEntity()
        .block();
  }
}
