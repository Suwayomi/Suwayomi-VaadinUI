/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * Represents a map of manga IDs to their respective chapter count. <br> Example: {3445: 43, 1234:
 * 39, 139: 1}
 *
 * @version 1.12.0
 * @since 1.12.0
 */
@Data
public class MangaChapterCount {

  private Map<Integer, Integer> mangaMap;

  @JsonCreator
  public MangaChapterCount(Map<Integer, Integer> mangaMap) {
    this.mangaMap = new HashMap<>(mangaMap);
  }

  public MangaChapterCount() {
    mangaMap = new HashMap<>();
  }

  /**
   * Updates the chapter count of the manga with the given ID.
   *
   * @param mangaId      The ID of the manga to update the chapter count of.
   * @param chapterCount The new chapter count of the manga.
   */
  public void updateChapterCount(int mangaId, int chapterCount) {
    mangaMap.put(mangaId, chapterCount);
  }

  /**
   * Returns the last known chapter count of the manga with the given ID.
   *
   * @param mangaId The ID of the manga to get the chapter count of.
   * @return The chapter count of the manga with the given ID or -1 if the manga is not in the map.
   */
  public int getChapterCount(int mangaId) {
    if (!mangaMap.containsKey(mangaId)) {
      return -1;
    }

    return mangaMap.get(mangaId);
  }
}
