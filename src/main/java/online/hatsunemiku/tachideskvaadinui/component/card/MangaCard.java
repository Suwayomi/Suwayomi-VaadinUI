/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.card;

import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;

@Slf4j
public class MangaCard extends Card {

  public MangaCard(Settings settings, Manga manga) {
    super(manga.getTitle(), settings.getUrl() + manga.getThumbnailUrl());

    String link = "/manga/" + manga.getId();
    setHref(link);
  }
}
