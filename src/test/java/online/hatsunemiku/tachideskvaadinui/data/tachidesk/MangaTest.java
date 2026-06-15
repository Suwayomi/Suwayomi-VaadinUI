/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MangaTest {

  @Test
  void testGetFormattedStatus() {
    Manga manga = new Manga();

    // Default null/blank
    manga.setStatus(null);
    assertEquals("-", manga.getFormattedStatus());

    manga.setStatus("");
    assertEquals("-", manga.getFormattedStatus());

    manga.setStatus("   ");
    assertEquals("-", manga.getFormattedStatus());

    // Basic mappings
    manga.setStatus("ONGOING");
    assertEquals("Ongoing", manga.getFormattedStatus());

    manga.setStatus("ongoing");
    assertEquals("Ongoing", manga.getFormattedStatus());

    manga.setStatus("COMPLETED");
    assertEquals("Completed", manga.getFormattedStatus());

    manga.setStatus("LICENSED");
    assertEquals("Licensed", manga.getFormattedStatus());

    manga.setStatus("PUBLISHING_FINISHED");
    assertEquals("Publishing Finished", manga.getFormattedStatus());

    manga.setStatus("CANCELLED");
    assertEquals("Cancelled", manga.getFormattedStatus());

    manga.setStatus("ON_HIATUS");
    assertEquals("On Hiatus", manga.getFormattedStatus());

    manga.setStatus("UNKNOWN");
    assertEquals("Unknown", manga.getFormattedStatus());

    // Non-standard value fallback
    manga.setStatus("SOME_OTHER_STATUS");
    assertEquals("SOME_OTHER_STATUS", manga.getFormattedStatus());
  }
}
