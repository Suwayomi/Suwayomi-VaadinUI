/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.statistics;

import dev.katsute.mal4j.manga.property.MangaStatus;
import java.util.Objects;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;

/**
 * Represents the statistics for a manga on MyAnimeList. E.g. the score or the number of chapters.
 */
public class MALMangaStatistics implements MangaStatistics {

  private final MangaStatus status;
  private final int progress;
  private final int score;
  private final MediaDate startedAt;
  private final MediaDate completedAt;

  public MALMangaStatistics(
      MangaStatus status, int progress, int score, MediaDate startedAt, MediaDate completedAt) {
    this.status = status;
    this.progress = progress;
    this.score = score;
    this.startedAt = startedAt;
    this.completedAt = completedAt;
  }

  public MangaStatus status() {
    return status;
  }

  public int progress() {
    return progress;
  }

  public int score() {
    return score;
  }

  public MediaDate startedAt() {
    return startedAt;
  }

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
    var that = (MALMangaStatistics) obj;
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
    return "MALMangaStatistics["
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
