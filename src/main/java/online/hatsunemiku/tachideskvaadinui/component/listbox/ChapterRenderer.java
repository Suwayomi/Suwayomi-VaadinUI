package online.hatsunemiku.tachideskvaadinui.component.listbox;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.text.SimpleDateFormat;
import java.util.Date;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;

public class ChapterRenderer extends ComponentRenderer<HorizontalLayout, Chapter> {

  public ChapterRenderer() {
    super(ChapterRenderer::createPresentation);
  }

  private static HorizontalLayout createPresentation(Chapter chapter) {
    HorizontalLayout container = new HorizontalLayout();
    container.addClassName("chapter-list-box-item");

    Div background = new Div();
    background.setClassName("chapter-list-box-item-background");
    container.add(background);

    Div title = new Div();
    title.setText("Chapter " + chapter.getChapterNumber());
    title.setClassName("chapter-list-box-item-title");

    Date uploadDate = new Date(chapter.getUploadDate());
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    String formattedDate = formatter.format(uploadDate);

    Div date = new Div();
    date.setText(formattedDate);
    date.setClassName("chapter-list-box-item-date");

    container.add(title, date);

    return container;
  }


}
