package online.hatsunemiku.tachideskvaadinui.component.listbox.chapter;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.view.ReadingView;

@CssImport("./css/components/chapter-list-box.css")
public class ChapterListBox extends ListBox<Chapter> {

  public ChapterListBox(List<Chapter> chapters, MangaService mangaService) {
    super();
    setItems(chapters);
    setRenderer(new ChapterRenderer(mangaService));
    addValueChangeListener(
        e -> {

        });
  }
}
