package online.hatsunemiku.tachideskvaadinui.component.reader;

import com.vaadin.flow.component.ComponentEvent;
import lombok.Getter;

@Getter
public class ReaderChapterChangeEvent extends ComponentEvent<MangaReader> {
  private final long mangaId;
  private final int chapterIndex;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param fromClient <code>true</code> if the event originated from the client side, <code>false
   *     </code> otherwise
   * @param mangaId The ID of the {@link online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga
   *     Manga} to which the next chapter belongs
   * @param chapterIndex The index of the next chapter
   */
  public ReaderChapterChangeEvent(
      MangaReader source, boolean fromClient, long mangaId, int chapterIndex) {
    super(source, fromClient);
    this.mangaId = mangaId;
    this.chapterIndex = chapterIndex;
  }
}
