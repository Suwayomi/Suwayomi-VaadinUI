/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.ServerVersion;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Status;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackRecord;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackerType;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.SuwayomiMangaStatistics;
import online.hatsunemiku.tachideskvaadinui.services.SuwayomiService;
import online.hatsunemiku.tachideskvaadinui.services.client.suwayomi.SuwayomiTrackingClient;
import org.springframework.stereotype.Service;

/**
 * Represents a Service for handling tracking requests to the Suwayomi Server. This service is a
 * wrapper around the {@link SuwayomiTrackingClient} class and provides methods to abstract the
 * inner workings of the client. Also provides convenience methods for handling tracking requests.
 */
@Slf4j
@Service
public class SuwayomiTrackingService {

  private final SuwayomiTrackingClient client;
  private final SuwayomiService suwayomiService;

  /**
   * Represents a Suwayomi Tracking Service.
   *
   * @param client the {@link SuwayomiTrackingClient} used for handling tracking requests to the
   *     Suwayomi Server.
   * @param suwayomiService the {@link SuwayomiService} used for getting meta-data about the
   *     Suwayomi Server.
   */
  public SuwayomiTrackingService(SuwayomiTrackingClient client, SuwayomiService suwayomiService) {
    this.client = client;
    this.suwayomiService = suwayomiService;
  }

  /**
   * Checks if the Suwayomi tracking service is authenticated with AniList.
   *
   * @return true if the Suwayomi tracking service is authenticated with AniList, false otherwise
   */
  public boolean isAniListAuthenticated() {
    int id = TrackerType.ANILIST.id;
    return client.isTrackerLoggedIn(id);
  }

  /**
   * Checks if the Suwayomi tracking service is authenticated with MyAnimeList (MAL).
   *
   * @return true if the Suwayomi tracking service is authenticated with MAL, false otherwise
   */
  public boolean isMALAuthenticated() {
    int id = TrackerType.MAL.id;
    return client.isTrackerLoggedIn(id);
  }

  /**
   * Constructs the AniList authentication URL. This URL is used to authenticate the User with the
   * AniList API.
   *
   * @return the AniList authentication URL as a string
   */
  public String getAniListAuthUrl() {
    int id = TrackerType.ANILIST.id;
    return client.getTrackerAuthUrl(id) + getStateAuthParam(id);
  }

  /**
   * Constructs the MyAnimeList (MAL) authentication URL. This URL is used to authenticate the User
   * with the MAL API.
   *
   * @return the MAL authentication URL as a string
   */
  public String getMALAuthUrl() {
    int id = TrackerType.MAL.id;
    return client.getTrackerAuthUrl(id) + getStateAuthParam(id);
  }

  /**
   * Searches for manga on AniList using the provided query.
   *
   * @param query the search query for manga
   * @return a list of {@link TrackerSearchResult} objects representing the search results
   */
  public List<TrackerSearchResult> searchAniList(String query) {
    int id = TrackerType.ANILIST.id;
    return client.searchTracker(query, id);
  }

  /**
   * Searches for manga on MyAnimeList (MAL) using the provided query.
   *
   * @param query the search query for manga
   * @return a list of {@link TrackerSearchResult} objects representing the search results
   */
  public List<TrackerSearchResult> searchMAL(String query) {
    int id = TrackerType.MAL.id;
    return client.searchTracker(query, id);
  }

  /**
   * Tracks a manga on AniList using the provided manga ID and external ID.
   *
   * @param mangaId the ID of the manga to be tracked
   * @param externalId the external ID of the manga on AniList
   */
  public void trackOnAniList(int mangaId, int externalId) {
    int id = TrackerType.ANILIST.id;
    client.trackMangaOnTracker(mangaId, externalId, id);
  }

  /**
   * Tracks a manga on MyAnimeList (MAL) using the provided manga ID and external ID.
   *
   * @param mangaId the ID of the manga to be tracked
   * @param externalId the external ID of the manga on MAL
   */
  public void trackOnMAL(int mangaId, int externalId) {
    int id = TrackerType.MAL.id;
    client.trackMangaOnTracker(mangaId, externalId, id);
  }

  /**
   * Checks if a manga is tracked on AniList.
   *
   * @param mangaId the ID of the manga to check
   * @return {@code true} if the manga is tracked on AniList, {@code false} otherwise
   */
  public boolean isMangaTrackedOnAniList(int mangaId) {
    int id = TrackerType.ANILIST.id;
    return client.isMangaTracked(mangaId, id);
  }

  /**
   * Checks if a manga is tracked on MyAnimeList (MAL).
   *
   * @param mangaId the ID of the manga to check
   * @return {@code true} if the manga is tracked on MAL, {@code false} otherwise
   */
  public boolean isMangaTrackedOnMAL(int mangaId) {
    int id = TrackerType.MAL.id;
    return client.isMangaTracked(mangaId, id);
  }

  /**
   * Logs in to the Suwayomi tracker with the specified URL and tracker ID.
   *
   * @param url the URL used for the login callback
   * @param trackerId the ID of the tracker to log in to
   */
  public void loginSuwayomi(String url, int trackerId) {
    client.loginTracker(url, trackerId);
  }

  /**
   * Retrieves the state authentication parameter for the specified tracker.
   *
   * @param id the ID of the tracker
   * @return the state authentication parameter as a formatted string
   */
  private String getStateAuthParam(int id) {

    String jsonTemplate =
        """
            {\
            "redirectUrl":"http://localhost:8080/validate/suwayomi",\
            "trackerId":%d,\
            "anyOtherInfo":"%s"\
            }\
            """
            .strip();

    String template = "&state=%s";

    TrackerType trackerType = TrackerType.fromId(id);

    if (trackerType == null) {
      throw new IllegalArgumentException("Invalid tracker ID");
    }

    String trackerName = trackerType.name();

    String json = jsonTemplate.formatted(id, trackerName);

    return template.formatted(json);
  }

  /**
   * Syncs the progress of a manga from the Suwayomi server to the tracker.
   *
   * @param mangaId the ID of the manga to sync progress for
   */
  public void trackProgress(int mangaId) {
    Optional<ServerVersion> version = suwayomiService.getServerVersion();

    if (version.isEmpty()) {
      log.warn("Failed to get server version");
      return;
    }

    int revision = version.get().getRevisionNumber();

    if (revision > 1510) {
      log.info("Tracking progress manually");
      client.trackProgress(mangaId);
    }
  }

  /**
   * Retrieves the AniList track record for a manga from the Suwayomi server.
   *
   * @param mangaId the ID of the manga to get the track record for
   * @return the track record for the manga or {@code null} if the manga is not tracked on AniList
   */
  public TrackRecord getTrackRecordAniList(long mangaId) {
    return client.getTrackRecord(mangaId, TrackerType.ANILIST.id);
  }

  /**
   * Retrieves the MyAnimeList (MAL) track record for a manga from the Suwayomi server.
   *
   * @param mangaId the ID of the manga to get the track record for
   * @return the track record for the manga or {@code null} if the manga is not tracked on MAL
   */
  public TrackRecord getTrackRecordMAL(long mangaId) {
    return client.getTrackRecord(mangaId, TrackerType.MAL.id);
  }

  /**
   * Updates the tracking data for a manga on the Suwayomi server.
   *
   * @param record the {@link TrackRecord} object representing the updated tracking data for the
   *     manga
   */
  public void updateTrackingData(TrackRecord record) {
    client.updateTrackerData(record);
  }

  /**
   * Retrieves the statistics for a specific tracking record.
   *
   * @param tracker The {@link TrackRecord} object for which the statistics are to be retrieved.
   * @return A {@link SuwayomiMangaStatistics} object representing the statistics for the specified
   * tracking record.
   */
  public SuwayomiMangaStatistics getStatistics(TrackRecord tracker) {
    return new SuwayomiMangaStatistics(tracker);
  }

  /**
   * Retrieves the statuses for a specific tracker.
   *
   * @param record The {@link TrackRecord} object with the tracker type to get the statuses for.
   * @return A list of {@link Status} objects representing the available statuses for the tracker.
   */
  public List<Status> getStatuses(TrackRecord record) {
    return client.getStatuses(record.getTrackerId());
  }

  /**
   * Updates the status of a manga on the tracker.
   *
   * @param tracker The {@link Tracker} object for which the status is to be updated.
   * @param status  The new status value as an integer.
   */
  public void updateMangaStatus(Tracker tracker, int status) {
    var record = getTrackRecord(tracker);

    record.setStatus(status);

    updateTrackingData(record);
  }

  /**
   * Updates the start date of a manga on the tracker.
   *
   * @param tracker   The {@link Tracker} object for which the start date is to be updated.
   * @param startDate The new start date as a {@link MediaDate} object.
   */
  public void updateMangaStartDate(Tracker tracker, MediaDate startDate) {
    var record = getTrackRecord(tracker);

    record.setStartDate(startDate.toInstant());

    updateTrackingData(record);
  }

  /**
   * Retrieves the tracking record for a specific tracker.
   *
   * @param tracker The {@link Tracker} object for which the tracking record is to be retrieved.
   * @return The {@link TrackRecord} object representing the tracking record for the specified
   * tracker.
   * @throws IllegalArgumentException If no tracking record is found for the specified tracker.
   */
  private TrackRecord getTrackRecord(Tracker tracker) {
    TrackRecord record = null;

    if (tracker.hasMalId()) {
      record = getTrackRecordMAL(tracker.getMangaId());
    } else if (tracker.hasAniListId()) {
      record = getTrackRecordAniList(tracker.getMangaId());
    }

    if (record == null) {
      throw new IllegalArgumentException("No record found for tracker");
    }

    return record;
  }

  /**
   * Stops tracking a manga on the tracker.
   *
   * @param tracker      The {@link Tracker} object for which tracking is to be stopped.
   * @param deleteRemote A boolean value indicating whether to delete the remote tracking record.
   */
  public void stopTracking(Tracker tracker, boolean deleteRemote) {
    var trackRecord = getTrackRecord(tracker);

    client.stopTracking(trackRecord.getId(), deleteRemote);
  }

  /**
   * Updates the progress of a manga on the tracker.
   *
   * @param tracker The {@link Tracker} object for which the progress is to be updated.
   * @param value   The new progress value as a double.
   */
  public void updateMangaProgress(Tracker tracker, double value) {
    var record = getTrackRecord(tracker);

    record.setLastChapterRead((float) value);

    updateTrackingData(record);
  }

  /**
   * Updates the score of a manga on the tracker.
   *
   * @param tracker The {@link Tracker} object for which the score is to be updated.
   * @param value   The new score value as a string.
   */
  public void updateMangaScore(Tracker tracker, String value) {
    var record = getTrackRecord(tracker);

    client.updateScore(record.getId(), value);
  }

  /**
   * Retrieves the tracking scores for a specific tracker.
   *
   * @param tracker The {@link Tracker} object for which the tracking scores are to be retrieved.
   * @return A list of strings representing the available tracking scores for the specified tracker.
   */
  public List<String> getTrackingScores(Tracker tracker) {
    var record = getTrackRecord(tracker);

    return client.getTrackingScores(record.getId());
  }
}
