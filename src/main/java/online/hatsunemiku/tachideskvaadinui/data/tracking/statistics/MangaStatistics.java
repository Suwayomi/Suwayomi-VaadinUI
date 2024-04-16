/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.statistics;

import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;

/**
 * Represents the statistics of a manga. For example, the score or the number of chapters read.
 */
public interface MangaStatistics {

  /**
   * The number of chapters read (progress) by the user.
   * @return The progress of the user for the manga.
   */
  int progress();

  /**
   * The score the user gave to the manga.
   * @return The score.
   */
  int score();

  /**
   * The date the user started reading the manga.
   * @return The {@link MediaDate} object with the start date.
   */
  MediaDate startedAt();

  /**
   * The date the user completed the manga.
   * @return The {@link MediaDate} object with the completion date.
   */
  MediaDate completedAt();
}
