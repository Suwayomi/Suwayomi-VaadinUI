package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.reader.MangaReader;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingCommunicationService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

@Route("reading/:mangaId(\\d+)/:chapterIndex(\\d+(?:\\.\\d+)?)")
@CssImport("./css/reading.css")
public class ReadingView extends StandardLayout
    implements BeforeEnterObserver, BeforeLeaveObserver {

  private final MangaService mangaService;
  private final SettingsService settingsService;
  private final TrackingDataService dataService;
  private final TrackingCommunicationService communicationService;

  public ReadingView(
      MangaService mangaService,
      SettingsService settingsService,
      TrackingDataService dataService,
      TrackingCommunicationService communicationService) {
    super("Reading");

    this.mangaService = mangaService;
    this.settingsService = settingsService;
    this.dataService = dataService;
    this.communicationService = communicationService;

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

    var reader =
        new MangaReader(
            chapterObj, settingsService, dataService, mangaService, communicationService, hasNext);

    setContent(reader);
  }

  @Override
  public void beforeLeave(BeforeLeaveEvent event) {
    UI.getCurrent()
        .access(
            () -> UI.getCurrent().getPage().executeJs("document.body.style.overflow = 'auto';"));
  }
}
