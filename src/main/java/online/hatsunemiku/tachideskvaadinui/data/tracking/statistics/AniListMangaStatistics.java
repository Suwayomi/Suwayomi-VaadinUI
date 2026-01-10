/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.statistics;

import java.util.Objects;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.GetMangaFromUserQuery.MediaList;

/** Represents the statistics for a manga on AniList. E.g. the score or the number of chapters. */
public class AniListMangaStatistics implements MangaStatistics {

  private final AniListStatus status;
  private final int progress;
  private final double score;
  private final MediaDate startedAt;
  private final MediaDate completedAt;

  /**
   * Creates a new {@link AniListMangaStatistics} object with the given parameters.
   *
   * @param status The status of the manga on AniList.
   * @param progress The number of chapters read by the user.
   * @param score The score the user gave to the manga.
   * @param startedAt The date the user started reading the manga.
   * @param completedAt The date the user completed the manga.
   */
  public AniListMangaStatistics(
      AniListStatus status, int progress, double score, MediaDate startedAt, MediaDate completedAt) {
    this.status = status;
    this.progress = progress;
    this.score = score;
    this.startedAt = startedAt;
    this.completedAt = completedAt;
  }

  /**
   * Constructs a new {@code AniListMangaStatistics} instance using the provided {@code MediaList}.
   *
   * @param mediaList The {@code MediaList} object containing the status, progress, score, start date,
   *                  and completion date of the manga. It provides the necessary data to initialize
   *                  the statistics for the manga entry on AniList.
   */
  public AniListMangaStatistics(MediaList mediaList) {
    this.status = AniListStatus.valueOf(mediaList.status.rawValue);
    this.progress = mediaList.progress;
    this.score = mediaList.score;
    this.startedAt = new MediaDate(mediaList.startedAt.year, mediaList.startedAt.month, mediaList.startedAt.day);
    this.completedAt = new MediaDate(mediaList.completedAt.year, mediaList.completedAt.month, mediaList.completedAt.day);
  }

  /**
   * The status of the manga on AniList.
   *
   * @return The {@link AniListStatus status} of the manga.
   */
  public AniListStatus status() {
    return status;
  }

  @Override
  public double progress() {
    return progress;
  }

  @Override
  public double score() {
    return score;
  }

  @Override
  public MediaDate startedAt() {
    return startedAt;
  }

  @Override
  public MediaDate completedAt() {
    return completedAt;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (AniListMangaStatistics) obj;
    return Objects.equals(this.status, that.status)
        && this.progress == that.progress
        && this.score == that.score
        && Objects.equals(this.startedAt, that.startedAt)
        && Objects.equals(this.completedAt, that.completedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, progress, score, startedAt, completedAt);
  }

  @Override
  public String toString() {
    return "AniListMangaStatistics["
        + "status="
        + status
        + ", "
        + "progress="
        + progress
        + ", "
        + "score="
        + score
        + ", "
        + "startedAt="
        + startedAt
        + ", "
        + "completedAt="
        + completedAt
        + ']';
  }
}
