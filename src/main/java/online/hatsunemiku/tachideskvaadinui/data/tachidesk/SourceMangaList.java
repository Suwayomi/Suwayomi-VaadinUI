/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class SourceMangaList {

  @JsonProperty("mangas")
  private List<Manga> mangaList;

  @JsonProperty("hasNextPage")
  private boolean hasNextPage;

  private int page;
}
