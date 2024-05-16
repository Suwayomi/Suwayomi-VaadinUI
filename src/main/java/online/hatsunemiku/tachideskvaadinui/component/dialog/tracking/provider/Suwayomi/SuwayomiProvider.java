/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider.Suwayomi;

import java.util.List;
import lombok.AllArgsConstructor;
import online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider.TrackerProvider;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Status;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackRecord;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackerType;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.MangaStatistics;
import online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService;

/**
 * Represents a provider for the Suwayomi tracking service. This class provides methods for
 * interacting with the Suwayomi API and handling tracking requests to Suwayomi tracking services.
 * It implements the {@link TrackerProvider} interface.
 */
@AllArgsConstructor
public class SuwayomiProvider implements TrackerProvider {

  protected SuwayomiTrackingService suwayomiAPI;

  @Override
  public boolean canSetPrivate() {
    return false;
  }

  public List<TrackerSearchResult> search(String query, TrackerType type) {
    if (type == TrackerType.MAL) {
      return suwayomiAPI.searchMAL(query);
    } else if (type == TrackerType.ANILIST) {
      return suwayomiAPI.searchAniList(query);
    } else {
      return null;
    }
  }

  @Override
  public void submitToTracker(boolean isPrivate, int mangaId, int externalId, TrackerType trackerType) {
    if (isPrivate) {
      throw new IllegalArgumentException("Suwayomi does not support private entries");
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

  private TrackRecord getTrackRecord(Tracker tracker) {
    if (tracker.hasMalId()) {
      return suwayomiAPI.getTrackRecordMAL(tracker.getMangaId());
    } else if (tracker.hasAniListId()) {
      return suwayomiAPI.getTrackRecordAniList(tracker.getMangaId());
    } else {
      return null;
    }
  }

  public List<Status> getTrackerStatuses(Tracker tracker) {
    TrackRecord record = getTrackRecord(tracker);

    if (record == null) {
      throw new IllegalArgumentException("No record found for tracker");
    }

    return suwayomiAPI.getStatuses(record);
  }
}
