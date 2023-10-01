/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AniListMangaListResponse(@JsonProperty("data") Data data) {

  public record Data(@JsonProperty("Page") Page page) {}

  public record Page(List<AniListMedia> media) {}
}
