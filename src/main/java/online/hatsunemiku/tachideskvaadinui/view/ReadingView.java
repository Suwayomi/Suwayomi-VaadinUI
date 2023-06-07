package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.data.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.springframework.web.client.RestTemplate;
import org.vaadin.firitin.components.orderedlayout.VScroller;

@Route("reading/:mangaId(\\d+)/:chapterId(\\d+)")
@CssImport("./css/reading.css")
public class ReadingView extends StandardLayout implements BeforeEnterObserver {

  private RestTemplate client;
  private String mangaId;
  private int currentChapter;
  private Div mangaImages;

  public ReadingView(RestTemplate client) {
    super("Reading");

    this.client = client;
    fullScreen();
  }

  private Chapter getChapter(Settings settings, String id, String chapter) {
    String chapterEndpoint = settings.getUrl() + "/api/v1/manga/" + id + "/chapter/" + chapter;

    return client.getForObject(chapterEndpoint, Chapter.class);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    Settings settings = SerializationUtils.deseralizeSettings();

    var idparam = event.getRouteParameters().get("mangaId");
    var chapterparam = event.getRouteParameters().get("chapterId");

    if (idparam.isEmpty()) {
      event.rerouteToError(NotFoundException.class, "Manga not found");
      return;
    }

    if (chapterparam.isEmpty()) {
      event.rerouteToError(NotFoundException.class, "Chapter not found");
      return;
    }

    String id = idparam.get();
    String chapter = chapterparam.get();

    Chapter chapterObj = getChapter(settings, id, chapter);

    if (chapterObj == null) {
      event.rerouteToError(NotFoundException.class, "Chapter not found");
      return;
    }

    currentChapter = Integer.parseInt(chapter);
    mangaId = id;


    mangaImages = new Div();
    mangaImages.addClassName("chapter-images");

    addChapterImages(settings, id, chapter, chapterObj);

    UI.getCurrent().access(() -> {
      UI.getCurrent().getPage().executeJs("document.body.style.overflow = 'hidden';");
    });

    VScroller scroller = new VScroller();
    scroller.addClassName("chapter-scroller");

    scroller.setContent(mangaImages);
    setContent(scroller);

    scroller.addScrollToEndListener(e -> {
      loadNextChapter();
    });
  }

  private void addChapterImages(Settings settings, String id, String chapter, Chapter chapterObj) {
    int pages = chapterObj.getPageCount();

    for (int i = 0; i < pages; i++) {
      String format = "%s/api/v1/manga/%s/chapter/%s/page/%s";

      String url = String.format(format, settings.getUrl(), id, chapter, i);

      Image image = new Image();
      image.setSrc(url);
      image.addClassName("chapter-image");

      Div imageContainer = new Div();
      imageContainer.addClassName("chapter-image-container");
      imageContainer.add(image);
      mangaImages.add(imageContainer);
    }
  }

  private void loadNextChapter() {
    Settings settings = SerializationUtils.deseralizeSettings();
    currentChapter++;

    String chapter = String.valueOf(currentChapter);

    Chapter chapterObj = getChapter(settings, mangaId, chapter);

    addChapterImages(settings, mangaId, chapter, chapterObj);
  }
}
