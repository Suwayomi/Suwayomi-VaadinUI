package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import java.util.Collections;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.utils.MangaDataUtils;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.springframework.web.client.RestTemplate;
import org.vaadin.firitin.components.orderedlayout.VScroller;

@Route("reading/:mangaId(\\d+)/:chapterId(\\d+(?:\\.\\d+)?)")
@CssImport("./css/reading.css")
public class ReadingView extends StandardLayout
    implements BeforeEnterObserver, BeforeLeaveObserver {

  private final RestTemplate client;
  private final SettingsService settingsService;
  private String mangaId;
  private int currentChapterIndex;
  private Div mangaImages;
  private List<Chapter> chapterList;

  public ReadingView(RestTemplate client, SettingsService settingsService) {
    super("Reading");

    this.client = client;
    this.settingsService = settingsService;

    fullScreen();
  }

  private Chapter getChapter(Settings settings, String id, String chapter) {
    String chapterEndpoint = settings.getUrl() + "/api/v1/manga/" + id + "/chapter/" + chapter;

    return client.getForObject(chapterEndpoint, Chapter.class);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    Settings settings = settingsService.getSettings();

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

    String mangaId = idparam.get();
    String chapter = chapterparam.get();

    chapterList = MangaDataUtils.getChapterList(settings, mangaId, client);
    Collections.reverse(chapterList);

    Chapter chapterObj = getChapter(settings, mangaId, chapter);

    if (chapterObj == null) {
      event.rerouteToError(NotFoundException.class, "Chapter not found");
      return;
    }

    this.mangaId = mangaId;

    for (int i = 0; i < chapterList.size(); i++) {
      Chapter c = chapterList.get(i);

      if (c.getId() == chapterObj.getId()) {
        currentChapterIndex = i;
        break;
      }
    }

    mangaImages = new Div();
    mangaImages.addClassName("chapter-images");

    addChapterImages(settings, mangaId, chapter, chapterObj);

    UI.getCurrent()
        .access(
            () -> UI.getCurrent().getPage().executeJs("document.body.style.overflow = 'hidden';"));

    VScroller scroller = new VScroller();
    scroller.addClassName("chapter-scroller");

    scroller.setContent(mangaImages);
    setContent(scroller);

    scroller.addScrollToEndListener(e -> loadNextChapter());
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
    currentChapterIndex++;

    if (currentChapterIndex >= chapterList.size()) {
      return;
    }

    Settings settings = settingsService.getSettings();

    Chapter chapterObj = chapterList.get(currentChapterIndex);
    String chapter = String.valueOf(chapterObj.getIndex());

    addNewChapter(settings, mangaId, chapter, chapterObj);
  }

  private void addNewChapter(Settings settings, String id, String chapter, Chapter chapterObj) {
    Div nextChapterAnnouncement = new Div();
    nextChapterAnnouncement.addClassName("next-chapter-announcement");

    String format = "Chapter %s";
    String nextChapterText = String.format(format, chapterObj.getIndex());

    Div announcement = new Div();
    announcement.addClassName("announcement-title");
    announcement.setText("Next Chapter!");

    Div announcementText = new Div();
    announcementText.addClassName("announcement-chapter-number");
    announcementText.setText(nextChapterText);

    nextChapterAnnouncement.add(announcement, announcementText);

    mangaImages.add(nextChapterAnnouncement);
    addChapterImages(settings, id, chapter, chapterObj);
  }

  @Override
  public void beforeLeave(BeforeLeaveEvent event) {
    UI.getCurrent()
        .access(
            () -> UI.getCurrent().getPage().executeJs("document.body.style.overflow = 'auto';"));
  }
}
