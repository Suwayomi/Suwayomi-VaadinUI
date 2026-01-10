/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker;

import com.apollographql.apollo.api.Operation.Data;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.client.ApolloClient;
import com.apollographql.java.rx3.Rx3Apollo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tracking.OAuthData;
import online.hatsunemiku.tachideskvaadinui.data.tracking.TrackerTokens;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMedia;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.GraphQLRequest;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.MangaList;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.AniListMangaStatistics;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.AddMangaToListMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.GetCurrentUserIdQuery;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.GetMangaFromUserQuery;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.GetMangaListEntryIdQuery;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.GetMangaListOfUserQuery;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.RemoveMangaFromListMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.UpdateMangaEndDateMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.UpdateMangaPrivacyStatusMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.UpdateMangaProgressMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.UpdateMangaProgressMutation.SaveMediaListEntry;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.UpdateMangaScoreMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.UpdateMangaStatusMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.type.MediaListStatus;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Is responsible for interacting with the AniList API. This class provides methods for retrieving
 * and updating AniList tokens, searching for manga, and managing manga lists.
 */
@Service
@Slf4j
public class AniListAPIService {

  public static final String OAUTH_URL = "https://anilist.co/api/v2/oauth";
  private static final String ANILIST_API_URL = "https://graphql.anilist.co";
  private static final String OAUTH_CLIENT_ID = "14576";
  private static final String OAUTH_CODE_PATTERN =
      OAUTH_URL + "/authorize?client_id=%s&response_type=token";

  private final TrackingDataService dataService;
  private final WebClient webClient;
  private final ApolloClient aniListClient;

  /**
   * Constructs an AniListAPIService object with the given SettingsService and ObjectMapper
   * dependencies.
   *
   * @param dataService the TrackingDataService object to be used for retrieving and updating the
   *                    AniList token and manga trackers
   * @param aniListClient the ApolloClient object to be used for sending GraphQL requests to the
   *                      AniList API
   */
  public AniListAPIService(TrackingDataService dataService, ApolloClient aniListClient) {
    this.dataService = dataService;
    this.webClient = WebClient.create(ANILIST_API_URL);
    this.aniListClient = aniListClient;

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

    var query = GetCurrentUserIdQuery.builder().build();
    var response = Rx3Apollo.single(authorized(aniListClient.query(query))).blockingGet();

    // Throws if AniList query returns errors
    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't get current user ID: {}", response.errors.getFirst().getMessage());
      throw new RuntimeException(response.errors.getFirst().getMessage());
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    return response.data.Viewer.id;
  }

  /**
   * Retrieves Manga statistics from AniList given a manga ID.
   *
   * @param mangaId The ID of the manga.
   * @return The AniListMangaStatistics object containing the manga statistics.
   * @throws WebClientResponseException.NotFound If the manga with the specified ID is not found.
   * @throws RuntimeException                    If the response is null, or if there is an error
   *                                             parsing the JSON response.
   */
  public AniListMangaStatistics getMangaFromList(int mangaId) {
    var query = GetMangaFromUserQuery.builder()
        .mangaId(mangaId)
        .userId(getCurrentUserId())
        .build();

    var response = Rx3Apollo.single(authorized(aniListClient.query(query))).blockingGet();

    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't get manga from list: {}", response.errors);
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    return new AniListMangaStatistics(response.data.MediaList);
  }

  /**
   * Sends a GraphQL request to the AniList API with the given query and variables. Additionally,
   * the request is authenticated with the user's AniList token, so the user must be logged in.
   *
   * @param query     The GraphQL query to send
   * @param variables The variables to send with the query
   * @return The response from the AniList API as a String, usually in JSON format
   */
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

  /**
   * Updates the progress of the manga with the given AniList ID.
   *
   * @param mangaId       The AniList ID of the manga to update
   * @param mangaProgress The new progress of the manga
   */
  public void updateMangaProgress(int mangaId, int mangaProgress) {
    var mutation = UpdateMangaProgressMutation.builder()
        .mangaId(mangaId)
        .progress(mangaProgress)
        .build();

    var response = Rx3Apollo.single(authorized(aniListClient.mutation(mutation))).blockingGet();

    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't update manga progress: {}", response.errors);
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    SaveMediaListEntry updatedEntry = response.data.SaveMediaListEntry;

    log.debug("Updated manga with ID {} to progress {}", updatedEntry.id, updatedEntry.progress);
  }

  /**
   * Updates the status of the manga with the given AniList ID.
   *
   * @param aniListId The AniList ID of the manga to update
   * @param value     The new status of the manga as an {@link AniListStatus} enum value
   */
  public void updateMangaStatus(int aniListId, AniListStatus value) {
    var mutation = UpdateMangaStatusMutation.builder()
        .mangaId(aniListId)
        .status(MediaListStatus.safeValueOf(value.name()))
        .build();

    var response = Rx3Apollo.single(authorized(aniListClient.mutation(mutation))).blockingGet();
    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't update manga status: {}", response.errors);
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    var updatedEntry = response.data.SaveMediaListEntry;

    log.debug("Updated Manga with ID {} to status {}", updatedEntry.id, updatedEntry.status);
  }

  /**
   * Updates the score of the manga with the given AniList ID.
   *
   * @param aniListId The AniList ID of the manga to update
   * @param value     The new score of the manga. Range depends on the user's score format.
   */
  public void updateMangaScore(int aniListId, double value) {

    var mutation = UpdateMangaScoreMutation.builder()
        .mangaId(aniListId)
        .score(value)
        .build();

    var response = Rx3Apollo.single(authorized(aniListClient.mutation(mutation))).blockingGet();

    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't update manga score: {}", response.errors);
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    var updatedEntry = response.data.SaveMediaListEntry;
    log.debug("Updated manga with ID {} to score {}", updatedEntry.id, updatedEntry.score);
  }

  /**
   * Updates the end date of the manga with the given AniList ID.
   *
   * @param aniListId The AniList ID of the manga to update
   * @param date      The new end date of the manga as a {@link MediaDate} object
   */
  public void updateMangaEndDate(int aniListId, MediaDate date) {

    var mutation = UpdateMangaEndDateMutation.builder()
        .mangaId(aniListId)
        .endDate(date.toFuzzyDateInput())
        .build();

    var response = Rx3Apollo.single(authorized(aniListClient.mutation(mutation))).blockingGet();
    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't update manga end date: {}", response.errors);
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    log.debug("Updated manga with ID {} to end date {}", aniListId, date);
  }

  /**
   * Retrieves the user's manga list.
   *
   * @return The manga list containing the user's reading, plan to read, completed, on hold, and
   * dropped manga
   * @throws RuntimeException If an error occurs while retrieving the manga list
   */
  public MangaList getMangaList() {

    var query = GetMangaListOfUserQuery.builder()
        .userId(getCurrentUserId())
        .build();

    var response = Rx3Apollo.single(authorized(aniListClient.query(query))).blockingGet();

    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't get manga list: {}", response.errors);
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    var lists = response.data.MediaListCollection.lists;

    List<AniListMedia> completed = new ArrayList<>();
    List<AniListMedia> reading = new ArrayList<>();
    List<AniListMedia> dropped = new ArrayList<>();
    List<AniListMedia> onHold = new ArrayList<>();
    List<AniListMedia> planToRead = new ArrayList<>();

    for (var list : lists) {
      var entries = list.entries;

      if (entries.isEmpty()) {
        continue;
      }

      var converted = entries.stream()
          .map(AniListMedia::new)
          .toList();

      // Categorizes converted entries based on status
      switch (converted.getFirst().status()) {
        case CURRENT, REPEATING -> reading.addAll(converted);
        case COMPLETED -> completed.addAll(converted);
        case DROPPED -> dropped.addAll(converted);
        case PAUSED -> onHold.addAll(converted);
        case PLANNING -> planToRead.addAll(converted);
        default -> log.warn("Unknown status: {}", converted.getFirst().status());
      }
    }

    return new MangaList(reading, planToRead, completed, onHold, dropped);
  }

  /**
   * Updates the privacy status of the manga with the given AniList ID.
   *
   * @param aniListId The AniList ID of the manga to update
   * @param isPrivate The new privacy status of the manga. {@code true} if the manga is private,
   *                  {@code false} otherwise
   */
  public void updateMangaPrivacyStatus(int aniListId, boolean isPrivate) {
    // language=graphql
    var mutation = UpdateMangaPrivacyStatusMutation.builder()
        .mangaId(aniListId)
        .private_(isPrivate)
        .build();

    var response = Rx3Apollo.single(authorized(aniListClient.mutation(mutation))).blockingGet();

    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't update manga privacy status: {}", response.errors);
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    log.debug("Updated manga with ID {} to private status {}", aniListId, isPrivate);
  }

  /**
   * Retrieves the entry ID of the manga with the given AniList ID in the user's list.
   *
   * @param mangaId The AniList ID of the manga to retrieve
   * @return The entry ID of the manga in the user's list
   */
  private int getMangaListEntryId(int mangaId) {

    var query = GetMangaListEntryIdQuery.builder()
        .mangaId(mangaId)
        .userId(getCurrentUserId())
        .build();

    var response = Rx3Apollo.single(authorized(aniListClient.query(query))).blockingGet();
    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't get manga list entry ID: {}", response.errors);
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    return response.data.MediaList.id;
  }

  /**
   * Removes the manga with the given AniList ID from the user's list.
   *
   * @param aniListId The AniList ID of the manga to remove
   */
  public void removeMangaFromList(int aniListId) {

    var mutation = RemoveMangaFromListMutation.builder()
        .entryId(getMangaListEntryId(aniListId))
        .build();

    var response = Rx3Apollo.single(authorized(aniListClient.mutation(mutation))).blockingGet();

    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't remove manga from list: {}", response.errors);
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    boolean deleted = response.data.DeleteMediaListEntry.deleted;

    if (!deleted) {
      throw new RuntimeException("Couldn't remove manga from list");
    }
  }

  /**
   * Adds a manga to the user's list with the status "READING".
   *
   * @param mangaId The ID of the manga to be added
   * @throws RuntimeException If an error occurs while adding the manga to the list
   */
  public void addMangaToList(int mangaId, boolean isPrivate) {
    var mutation = AddMangaToListMutation.builder()
        .mangaId(mangaId)
        .private_(isPrivate)
        .status(MediaListStatus.CURRENT)
        .build();

    var response = Rx3Apollo.single(authorized(aniListClient.mutation(mutation))).blockingGet();

    if (response.hasErrors()) {
      assert response.errors != null;
      log.error("Couldn't add manga to list: {}", response.errors);
    }

    if (response.data == null) {
      throw new RuntimeException("No data in response");
    }

    log.debug("Added manga with ID {} to list", response.data.SaveMediaListEntry.id);
  }

  /**
   * Helper to attach the Authorization header to an Apollo Call.
   * <p>
   * Usage: {@code authorized(aniListClient.query(new MyQuery())).execute();}
   *
   * @param call The ApolloCall to authorize
   * @return The authorized ApolloCall
   */
  private <T extends Data> ApolloCall<T> authorized(ApolloCall<T> call) {
    if (hasAniListToken()) {
      return call.addHttpHeader("Authorization", getAniListTokenHeader());
    }
    return call;
  }
}
