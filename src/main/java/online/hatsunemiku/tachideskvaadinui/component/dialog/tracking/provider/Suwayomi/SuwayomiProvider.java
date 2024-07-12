/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider.Suwayomi;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider.TrackerProvider;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Status;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackRecord;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackerType;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.MangaStatistics;
import online.hatsunemiku.tachideskvaadinui.services.tracker.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService;

/**
 * Represents a provider for the Suwayomi tracking service. This class provides methods for
 * interacting with the Suwayomi API and handling tracking requests to Suwayomi tracking services.
 * It implements the {@link TrackerProvider} interface.
 */
@Slf4j
public class SuwayomiProvider implements TrackerProvider {

  protected SuwayomiTrackingService suwayomiAPI;
  private final TrackerType trackerType;
  private AniListAPIService aniListAPIService;

  public SuwayomiProvider(SuwayomiTrackingService suwayomiAPI, TrackerType trackerType) {

    if (trackerType == TrackerType.ANILIST) {
      String msg = "This Constructor should not be used for AniList tracking";
      log.error(msg);
      throw new IllegalArgumentException(msg);
    }

    this.suwayomiAPI = suwayomiAPI;
    this.trackerType = trackerType;
  }

  public SuwayomiProvider(SuwayomiTrackingService suwayomiAPI, TrackerType trackerType,
      AniListAPIService aniListAPIService) {

    if (trackerType != TrackerType.ANILIST) {
      String msg = "This Constructor should only be used for AniList tracking";
      log.error(msg);
      throw new IllegalArgumentException(msg);
    }

    this.suwayomiAPI = suwayomiAPI;
    this.trackerType = trackerType;
    this.aniListAPIService = aniListAPIService;
  }

  @Override
  public boolean canSetPrivate() {
    return trackerType == TrackerType.ANILIST;
  }

  /**
   * Searches for trackers based on the provided query and tracker type.
   *
   * @param query The search query.
   * @param type  The {@link TrackerType} to search through.
   * @return A list of {@link TrackerSearchResult} objects representing the search results. If the
   * tracker type is neither MAL nor AniList, it <b>returns an empty list</b>.
   */
  public List<TrackerSearchResult> search(String query, TrackerType type) {
    if (type == TrackerType.MAL) {
      return suwayomiAPI.searchMAL(query);
    } else if (type == TrackerType.ANILIST) {
      return suwayomiAPI.searchAniList(query);
    } else {
      return List.of();
    }
  }

  @Override
  public void submitToTracker(
      boolean isPrivate, int mangaId, int externalId, TrackerType trackerType) {
    if (isPrivate) {
      if (trackerType != TrackerType.ANILIST) {
        throw new IllegalArgumentException("Tracker does not support private entries");
      }

      aniListAPIService.addMangaToList(externalId, true);
    }

    if (trackerType == TrackerType.MAL) {
      suwayomiAPI.trackOnMAL(mangaId, externalId);
    } else if (trackerType == TrackerType.ANILIST) {
      suwayomiAPI.trackOnAniList(mangaId, externalId);
    }
  }

  @Override
  public MangaStatistics getStatistics(Tracker tracker) {
    TrackRecord record = getTrackRecord(tracker);

    if (record == null) {
      throw new IllegalArgumentException("No record found for tracker");
    }

    return suwayomiAPI.getStatistics(record);
  }

  @Override
  public Integer getMaxChapter(Tracker tracker) {
    TrackRecord record = getTrackRecord(tracker);

    if (record == null) {
      throw new IllegalArgumentException("No record found for tracker");
    }

    return record.getTotalChapters();
  }

  /**
   * Retrieves the tracking record for a given tracker.
   *
   * @param tracker The {@link Tracker} for which to retrieve the tracking record.
   * @return The {@link TrackRecord} for the provided tracker, or {@code null} if the tracker does
   * not have a MAL ID or an AniList ID.
   */
  private TrackRecord getTrackRecord(Tracker tracker) {
    if (tracker.hasMalId()) {
      return suwayomiAPI.getTrackRecordMAL(tracker.getMangaId());
    } else if (tracker.hasAniListId()) {
      return suwayomiAPI.getTrackRecordAniList(tracker.getMangaId());
    } else {
      return null;
    }
  }

  /**
   * Retrieves the list of statuses for a given tracker.
   *
   * @param tracker The {@link Tracker} for which to retrieve the statuses.
   * @return A list of {@link Status} objects representing the statuses for the tracker.
   * @throws IllegalArgumentException if no tracking record is found for the provided tracker.
   */
  public List<Status> getTrackerStatuses(Tracker tracker) {
    TrackRecord record = getTrackRecord(tracker);

    if (record == null) {
      throw new IllegalArgumentException("No record found for tracker");
    }

    return suwayomiAPI.getStatuses(record);
  }
}
