/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.card;

import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMedia;

public class AniListMediaCard extends Card {

  public AniListMediaCard(AniListMedia media) {
    super(getTitle(media), media.coverImage().large());

    setHref("/search/" + getTitle(media));
  }

  private static String getTitle(AniListMedia media) {
    var title = media.title();

    String titleString = title.userPreferred();

    if (titleString == null) {
      titleString = title.enlgish();
    }

    if (titleString == null) {
      titleString = title.romaji();
    }

    if (titleString == null) {
      titleString = title.native_();
    }

    return titleString;
  }
}
