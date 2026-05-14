/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.mal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MALMangaListEntry(
    @JsonProperty("node") MALManga manga,
    @JsonProperty("list_status") MALListStatus listStatus) {

  public record MALListStatus(
      MALMangaStatus status,
      int score,
      @JsonProperty("num_chapters_read") int numChaptersRead,
      @JsonProperty("is_rewatching") boolean isRereading,
      @JsonProperty("updated_at") String updatedAt) {}
}
