/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import lombok.Data;

/**
 * Represents the tracking status of a manga for a tracker on Suwayomi.
 */
@Data
public class Status {

  private String name;
  private int value;
}
