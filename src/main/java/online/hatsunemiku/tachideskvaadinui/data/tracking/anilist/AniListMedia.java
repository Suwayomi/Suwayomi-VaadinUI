/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist;

import com.fasterxml.jackson.annotation.JsonProperty;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.GetMangaListOfUserQuery.Entry;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.GetMangaListOfUserQuery.Title;
import online.hatsunemiku.tachideskvaadinui.graphql.anilist.type.MediaTitle;

public record AniListMedia(
    MediaTitle title,
    String image,
    AniListStatus status) {

  public record MediaTitle(
      String userPreferred,
      String romaji,
      String enlgish,
      @JsonProperty("native") String native_) {

    public MediaTitle(Title dto) {
      this(dto.userPreferred, dto.romaji, dto.english, dto.native_);
    }
  }

  public AniListMedia(Entry entry) {
    this(new MediaTitle(entry.media.title), entry.media.coverImage.large,
        AniListStatus.valueOf(entry.status.rawValue));
  }

  public record MediaCoverImage(String large) {}
}
