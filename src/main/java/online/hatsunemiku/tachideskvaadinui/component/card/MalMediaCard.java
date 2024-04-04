/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.card;

import dev.katsute.mal4j.manga.Manga;

public class MalMediaCard extends Card {

  /**
   * Creates a new instance of the {@link MalMediaCard} class.
   *
   * @param manga the {@link Manga} object to create a card component for.
   */
  public MalMediaCard(Manga manga) {
    super(manga.getTitle(), manga.getMainPicture().getLargeURL());

    setHref("/search/" + manga.getTitle());
  }
}
