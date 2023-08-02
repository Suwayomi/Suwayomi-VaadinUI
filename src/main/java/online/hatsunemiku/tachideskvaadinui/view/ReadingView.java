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
import online.hatsunemiku.tachideskvaadinui.component.reader.MangaReader;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

@Route("reading/:mangaId(\\d+)/:chapterIndex(\\d+(?:\\.\\d+)?)")
@CssImport("./css/reading.css")
public class ReadingView extends StandardLayout
    implements BeforeEnterObserver, BeforeLeaveObserver {

  private final MangaService mangaService;
  private final SettingsService settingsService;
  private String mangaId;
  private int currentChapterIndex;
  private Div mangaImages;

  public ReadingView(MangaService mangaService, SettingsService settingsService) {
    super("Reading");

    this.mangaService = mangaService;
    this.settingsService = settingsService;

    fullScreen();
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    var idparam = event.getRouteParameters().get("mangaId");
    var chapterparam = event.getRouteParameters().get("chapterIndex");

    if (idparam.isEmpty()) {
      event.rerouteToError(NotFoundException.class, "Manga not found");
      return;
    }

    if (chapterparam.isEmpty()) {
      event.rerouteToError(NotFoundException.class, "Chapter not found");
      return;
    }

    String mangaIdStr = idparam.get();
    String chapter = chapterparam.get();

    long mangaId = Long.parseLong(mangaIdStr);

    int chapterIndex = Integer.parseInt(chapter);

    Chapter chapterObj = mangaService.getChapter(mangaId, chapterIndex);

    boolean hasNext;

    try {
      hasNext = mangaService.getChapter(mangaId, chapterIndex + 1) != null;
    } catch (Exception e) {
      hasNext = false;
    }

    if (chapterObj == null) {
      event.rerouteToError(NotFoundException.class, "Chapter not found");
      return;
    }

    this.mangaId = mangaIdStr;

    MangaReader reader = new MangaReader(chapterObj, settingsService, mangaService, hasNext);

    setContent(reader);
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
