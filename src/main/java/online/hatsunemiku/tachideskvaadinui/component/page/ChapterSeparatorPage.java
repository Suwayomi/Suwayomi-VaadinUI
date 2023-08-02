package online.hatsunemiku.tachideskvaadinui.component.page;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;

@CssImport("./css/components/page/chapter-separator-page.css")
public class ChapterSeparatorPage extends Div {
  public ChapterSeparatorPage(Chapter chapter) {
    addClassName("chapter-separator-page");

    String chapterTitle = "Next Chapter!";

    String chapterNumber = "End of Chapter " + chapter.getChapterNumber();

    H1 title = new H1(chapterTitle);
    title.addClassName("chapter-separator-title");

    H2 number = new H2(chapterNumber);
    number.addClassName("chapter-separator-number");

    add(title, number);
  }
}
