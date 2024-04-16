/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider.Suwayomi;

import dev.katsute.mal4j.manga.Manga;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackerType;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListScoreFormat;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.MALMangaStatistics;
import online.hatsunemiku.tachideskvaadinui.services.tracker.MyAnimeListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService;

/**
 * A {@link SuwayomiProvider} implementation for MyAnimeList. Uses the MAL API to get data to
 * consumers of this provider.
 */
public class MALTrackerProvider extends SuwayomiProvider {

  private final MyAnimeListAPIService malAPI;

  public MALTrackerProvider(SuwayomiTrackingService suwayomiAPI, MyAnimeListAPIService malAPI) {
    super(suwayomiAPI);
    this.malAPI = malAPI;
  }

  @Override
  public TrackerType getTrackerType() {
    return TrackerType.MAL;
  }

  public MALMangaStatistics getStatistics(Tracker tracker) {
    int id = tracker.getMalId();

    Manga manga = malAPI.getManga(id);
    var trackedManga = manga.getListStatus();

    var status = trackedManga.getStatus();
    var score = trackedManga.getScore();
    var progress = trackedManga.getChaptersRead();
    var started = trackedManga.getStartDate().toInstant();

    Date finishDate = trackedManga.getFinishDate();

    Instant finished;
    if (finishDate == null) {
      finished = null;
    } else {
      finished = finishDate.toInstant();
    }

    MediaDate startedAt = null;
    if (started != null) {
      LocalDateTime dt = LocalDateTime.ofInstant(started, ZoneId.systemDefault());

      startedAt = new MediaDate(dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth());
    }

    MediaDate finishedAt = null;
    if (finished != null) {
      LocalDateTime dt = LocalDateTime.ofInstant(finished, ZoneId.systemDefault());

      finishedAt = new MediaDate(dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth());
    }

    return new MALMangaStatistics(status, progress, score, startedAt, finishedAt);
  }

  @Override
  public Integer getMaxChapter(Tracker tracker) {
    int id = tracker.getMalId();
    int max = malAPI.getManga(id).getChapters();

    if (max == 0) {
      return null;
    }

    return max;
  }

  @Override
  public AniListScoreFormat getScoreFormat() {
    return AniListScoreFormat.POINT_10;
  }
}
