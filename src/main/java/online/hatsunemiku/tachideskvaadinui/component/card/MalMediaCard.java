/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.card;

import online.hatsunemiku.tachideskvaadinui.data.tracking.mal.MALManga;

/** Represents a {@link Card} component for displaying Manga information from MyAnimeList. */
public class MalMediaCard extends Card {

  /**
   * Creates a new instance of the {@link MalMediaCard} class.
   *
   * @param manga the {@link MALManga} object to create a card component for.
   */
  public MalMediaCard(MALManga manga) {
    super(manga.title(), manga.mainPicture().largeURL());

    setHref("/search/" + manga.title());
  }
}
