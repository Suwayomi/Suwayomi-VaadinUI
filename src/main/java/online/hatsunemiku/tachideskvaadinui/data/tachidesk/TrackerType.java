/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import java.util.Arrays;
import javax.annotation.Nullable;

/**
 * An enumeration representing different types of trackers.
 */
public enum TrackerType {
  MAL(1),
  ANILIST(2);

  public final int id;

  /**
   * Instantiates a TrackerType object with the specified id.
   *
   * @param id the id of the Tracker on the Suwayomi Server
   */
  TrackerType(int id) {
    this.id = id;
  }

  /**
   * Retrieves the {@link TrackerType} object based on the provided id.
   *
   * @param id the id of the TrackerType
   * @return the corresponding {@link TrackerType} object, or null if no match is found
   */
  @Nullable
  public static TrackerType fromId(int id) {
    var match =
        Arrays.stream(TrackerType.values())
            .filter(trackerType -> trackerType.id == id)
            .findFirst();

    return match.orElse(null);
  }
}