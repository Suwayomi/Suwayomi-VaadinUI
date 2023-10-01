/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist;

import lombok.Getter;

@Getter
public enum AniListStatus {
  CURRENT("Reading"),
  PLANNING("Planning"),
  COMPLETED("Completed"),
  DROPPED("Dropped"),
  PAUSED("Paused"),
  REPEATING("Re-reading");

  private final String status;

  AniListStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return status;
  }
}
