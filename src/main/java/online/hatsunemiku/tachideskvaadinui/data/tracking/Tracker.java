/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking;

import lombok.Data;
import lombok.Setter;

@Data
public class Tracker {

  @Setter private long mangaId;
  private int aniListId;
  private int malId;
  private boolean isPrivate;

  public boolean hasAniListId() {
    return aniListId != 0;
  }

  public boolean hasMalId() {
    return malId != 0;
  }

  public void removeAniListId() {
    aniListId = 0;
  }
}
