package online.hatsunemiku.tachideskvaadinui.component.listbox.chapter;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.listbox.ListBox;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;

@CssImport("./css/components/chapter-list-box.css")
public class ChapterListBox extends ListBox<Chapter> {

  public ChapterListBox(List<Chapter> chapters, MangaService mangaService) {
    super();
    setItems(chapters);
    setRenderer(new ChapterRenderer(mangaService));
  }
}
