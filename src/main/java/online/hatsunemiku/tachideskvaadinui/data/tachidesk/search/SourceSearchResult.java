/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk.search;

import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;

public record SourceSearchResult(List<Manga> manga, boolean hasNextPage, int page) {

  public record SearchResponse(List<Manga> mangas, boolean hasNextPage) {}
}
