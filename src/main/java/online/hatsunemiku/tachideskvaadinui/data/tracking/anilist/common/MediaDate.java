/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MediaDate(Integer year, Integer month, Integer day) {

  public MediaDate(@NotNull LocalDate date) {
    this(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
  }

  /**
   * Converts the {@link MediaDate} to an {@link Instant}.
   *
   * @return an {@link Instant} representing the start of the day in the UTC time zone, or null if
   *     any of the year, month, or day values are null
   */
  public @Nullable Instant toInstant() {
    if (year() == null || month() == null || day() == null) {
      return null;
    }

    LocalDate date = LocalDate.of(year(), month(), day());

    return date.atStartOfDay().toInstant(ZoneOffset.UTC);
  }
}
