package online.hatsunemiku.tachideskvaadinui.component.listbox.chapter;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.view.ReadingView;

@CssImport("./css/components/chapter-list-box.css")
public class ChapterListBox extends ListBox<Chapter> {

  public ChapterListBox(List<Chapter> chapters) {
    super();
    setItems(chapters);
    setRenderer(new ChapterRenderer());
    addValueChangeListener(
        e -> {
          int mangaId = e.getValue().getMangaId();

          RouteParam mangaIdParam = new RouteParam("mangaId", String.valueOf(mangaId));

          double chapterNumber = e.getValue().getChapterNumber();
          RouteParam chapterIndexParam;
          if (chapterNumber % 1 == 0) {
            chapterIndexParam = new RouteParam("chapterIndex", String.valueOf((int) chapterNumber));
          } else {
            chapterIndexParam = new RouteParam("chapterIndex", String.valueOf(chapterNumber));
          }

          RouteParameters params = new RouteParameters(mangaIdParam, chapterIndexParam);

          UI.getCurrent().navigate(ReadingView.class, params);
        });
  }
}
