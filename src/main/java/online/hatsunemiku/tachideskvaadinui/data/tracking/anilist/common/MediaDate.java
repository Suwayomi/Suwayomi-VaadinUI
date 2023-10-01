/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common;

import java.time.LocalDate;

public record MediaDate(Integer year, Integer month, Integer day) {

  public MediaDate(LocalDate date) {
    this(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
  }
}
