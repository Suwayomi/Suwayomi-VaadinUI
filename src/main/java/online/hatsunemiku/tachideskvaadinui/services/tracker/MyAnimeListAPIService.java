/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.katsute.mal4j.AccessToken;
import dev.katsute.mal4j.Authorization;
import dev.katsute.mal4j.MyAnimeList;
import dev.katsute.mal4j.MyAnimeListAuthenticator;
import dev.katsute.mal4j.PaginatedIterator;
import dev.katsute.mal4j.manga.Manga;
import dev.katsute.mal4j.manga.MangaListStatus;
import dev.katsute.mal4j.manga.property.MangaSort;
import dev.katsute.mal4j.manga.property.MangaStatus;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import online.hatsunemiku.tachideskvaadinui.data.tracking.OAuthData;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
  private final String CLIENT_ID = "a039c56fb609cd33ebd59381a6e9b460";
  private final TrackingDataService tds;
  private final WebClient webClient;
  private final Cache<UUID, String> pkceCache;
  @Nullable
  private MyAnimeList mal;

  /**
   * Initializes an instance of the MyAnimeListAPIService class.
   *
   * @param tds       The {@link TrackingDataService} used for storing tokens.
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
      var newData = refreshToken(data.getRefreshToken());
      tds.getTokens().setMalToken(newData);

      data = newData;
    }

    authenticateMALWithToken(data);
  }

  /**
   * Authenticates the MAL API with the provided {@link OAuthData} object containing the access
   * token. The resulting {@link MyAnimeList} object is stored in {@link #mal}
   *
   * @param data The {@link OAuthData} object containing the access token.
   */
  private void authenticateMALWithToken(OAuthData data) {
    this.mal = MyAnimeList.withToken(data.getAccessToken());
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
    String codeChallenge = MyAnimeListAuthenticator.generatePKCE(128);

    UUID pkceId = UUID.randomUUID();

    pkceCache.put(pkceId, codeChallenge);

    String stateParam = "{\"pkceId\"=\"%s\"}";
    stateParam = URLEncoder.encode(stateParam.formatted(pkceId), StandardCharsets.UTF_8);

    String params = "response_type=%s&client_id=%s&code_challenge=%s&state=%s";
    params =
        params.formatted(responseType, CLIENT_ID, codeChallenge, pkceId.toString(), stateParam);

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
   * @param code   The authorization code to exchange for tokens.
   * @param pkceId The PKCE ID used for generating the code challenge.
   */
  public void exchangeCodeForTokens(String code, String pkceId) {
    String pkce = pkceCache.getIfPresent(UUID.fromString(pkceId));

    if (pkce == null) {
      throw new IllegalArgumentException("Invalid PKCE ID");
    }

    Authorization auth = new Authorization(CLIENT_ID, null, code, pkce);

    MyAnimeListAuthenticator oauth = new MyAnimeListAuthenticator(auth);
    AccessToken token = oauth.getAccessToken();

    OAuthData data = new OAuthData(token);

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
    body.add("grant_type", "refresh_token");
    body.add("refresh_token", refreshToken);

    return webClient
        .post()
        .uri("https://myanimelist.net/v1/oauth2/token")
        .headers(headers -> headers.setBasicAuth(CLIENT_ID, ""))
        .bodyValue(body)
        .retrieve()
        .bodyToMono(OAuthData.class)
        .block();
  }

  /**
   * Retrieves a list of {@link Manga} objects with the specified status.
   *
   * @param status The {@link MangaStatus} enum value representing the status of the manga.
   * @return A List of {@link Manga} objects with the specified status.
   * @throws IllegalStateException If not authenticated with MyAnimeList (MAL).
   */
  public List<Manga> getMangaWithStatus(MangaStatus status) {
    if (mal == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    PaginatedIterator<MangaListStatus> iter =
        mal.getUserMangaListing()
            .withStatus(status)
            .sortBy(MangaSort.Title)
            .includeNSFW()
            .searchAll();

    var list = new ArrayList<MangaListStatus>();

    while (iter.hasNext()) {
      list.add(iter.next());
    }

    log.debug("Got {} manga with status {}", list.size(), status.name());

    return list.stream().map(MangaListStatus::getManga).toList();
  }

  /**
   * Retrieves a {@link Manga} object with the specified ID from MyAnimeList.
   * @param id The MyAnimeList ID of the manga.
   * @return The {@link Manga} object containing the manga's information.
   */
  public Manga getManga(int id) {
    if (mal == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    return mal.getManga(id);
  }

  /**
   * Updates the status of a manga on the user's list.
   * @param id The MyAnimeList ID of the manga.
   * @param status The new status to be used.
   */
  public void updateMangaListStatus(int id, MangaStatus status) {
    if (mal == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    mal.updateMangaListing(id).status(status).update();
  }

  /**
   * Updates the score of a manga on the user's list.
   *
   * @param id    The MyAnimeList ID of the manga.
   * @param score The new score of the manga.
   */
  public void updateMangaListScore(int id, int score) {
    if (mal == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    mal.updateMangaListing(id).score(score).update();
  }

  /**
   * Updates the start date of a manga on the user's list.
   *
   * @param malId The MyAnimeList ID of the manga.
   * @param date  The new start date of the manga.
   */
  public void updateMangaListStartDate(int malId, MediaDate date) {
    if (mal == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    Instant instant =
        LocalDate.of(date.year(), date.month(), date.day())
            .atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant();
    Date startDate = Date.from(instant);

    mal.updateMangaListing(malId).startDate(startDate).update();
  }

  /**
   * Updates the end date of a manga on the user's list.
   *
   * @param malId The MyAnimeList ID of the manga.
   * @param date  The new end date of the manga.
   */
  public void updateMangaListEndDate(int malId, MediaDate date) {
    if (mal == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    if (date.year() == null || date.month() == null || date.day() == null) {
      return;
    }

    Instant instant =
        LocalDate.of(date.year(), date.month(), date.day())
            .atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant();
    Date endDate = Date.from(instant);

    mal.updateMangaListing(malId).finishDate(endDate).update();
  }

  /**
   * Removes a manga from the user's list.
   *
   * @param malId The MyAnimeList ID of the manga to remove.
   */
  public void removeMangaFromList(int malId) {
    if (mal == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    mal.deleteMangaListing(malId);
  }

  /**
   * Updates the progress (read chapter count) of a manga on the user's list.
   *
   * @param malId The MyAnimeList ID of the manga.
   * @param value The new progress value. This is the chapter number the user has read.
   */
  public void updateMangaListProgress(int malId, int value) {
    if (mal == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    mal.updateMangaListing(malId).chaptersRead(value).update();
  }
}
