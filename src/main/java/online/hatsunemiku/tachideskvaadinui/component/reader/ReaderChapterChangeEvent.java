/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.reader;

import com.vaadin.flow.component.ComponentEvent;
import java.util.List;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;

@Getter
public class ReaderChapterChangeEvent extends ComponentEvent<MangaReader> {
  private final int mangaId;
  private final int chapterId;
  private final List<Chapter> chapters;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param fromClient <code>true</code> if the event originated from the client side, <code>false
   *     </code> otherwise
   * @param mangaId The ID of the {@link online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga
   *     Manga} to which the next chapter belongs
   * @param chapterId The ID of the next {@link
   *     online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter Chapter}
   */
  public ReaderChapterChangeEvent(
      MangaReader source, boolean fromClient, int mangaId, int chapterId, List<Chapter> chapters) {
    super(source, fromClient);
    this.mangaId = mangaId;
    this.chapterId = chapterId;
    this.chapters = chapters;
  }
}
