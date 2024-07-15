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


  public void addManga(int mangaId, int chapterCount) {
    mangaMap.put(mangaId, chapterCount);
  }

  public void updateChapterCount(int mangaId, int chapterCount) {
    mangaMap.put(mangaId, chapterCount);
  }

  public int getChapterCount(int mangaId) {
    if (!mangaMap.containsKey(mangaId)) {
      return -1;
    }

    return mangaMap.get(mangaId);
  }

}
