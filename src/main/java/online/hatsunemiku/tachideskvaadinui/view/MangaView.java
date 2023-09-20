package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.TrackingDialog;
import online.hatsunemiku.tachideskvaadinui.component.listbox.chapter.ChapterListBox;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import online.hatsunemiku.tachideskvaadinui.utils.RouteUtils;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.jetbrains.annotations.NotNull;

@Route("manga/:id(\\d+)")
@CssImport("./css/manga.css")
public class MangaView extends StandardLayout implements BeforeEnterObserver {

  private final MangaService mangaService;
  private final SettingsService settingsService;
  private final AniListAPIService aniListAPIService;
  private final TrackingDataService dataService;

  public MangaView(
      MangaService mangaService,
      SettingsService settingsService,
      AniListAPIService aniListAPIService,
      TrackingDataService dataService) {
    super("Manga");
    this.mangaService = mangaService;
    this.settingsService = settingsService;
    this.aniListAPIService = aniListAPIService;
    this.dataService = dataService;
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    Optional<String> idParam = event.getRouteParameters().get("id");
    Settings settings = settingsService.getSettings();

    if (idParam.isEmpty()) {
      event.rerouteToError(NotFoundException.class, "Manga not found");
      return;
    }

    String id = idParam.get();

    long mangaId = Long.parseLong(id);

    Manga manga;
    try {
      manga = mangaService.getMangaFull(mangaId);
    } catch (Exception e) {
      event.rerouteTo(ServerStartView.class);
      return;
    }

    VerticalLayout container = new VerticalLayout();
    container.addClassName("manga-container");

    Image image = new Image();

    String url = settings.getUrl() + manga.getThumbnailUrl();

    Div imageContainer = new Div();

    image.setSrc(url);
    image.addClassName("manga-image");

    imageContainer.addClassName("manga-image-container");
    imageContainer.add(image);

    List<Chapter> chapters = mangaService.getChapterList(mangaId);

    ListBox<Chapter> chapterListBox = new ChapterListBox(chapters, mangaService);

    Div buttons = getButtons(manga, chapters);

    H1 mangaTitle = new H1(manga.getTitle());
    mangaTitle.addClassName("manga-title");

    container.add(mangaTitle, imageContainer, buttons, chapterListBox);
    setContent(container);
  }

  @NotNull
  private Div getButtons(Manga manga, List<Chapter> chapters) {
    Div buttons = new Div();
    buttons.addClassName("manga-buttons");

    Button libraryBtn = getLibraryBtn(manga);
    libraryBtn.addClassName("manga-btn");

    Button trackBtn = new Button("Tracking", LumoIcon.RELOAD.create());
    trackBtn.addClassName("manga-btn");

    trackBtn.addClickListener(
        e -> {
          TrackingDialog dialog = new TrackingDialog(dataService, manga, aniListAPIService);
          dialog.open();
        });

    Button resumeBtn = getResumeButton(manga, chapters);

    buttons.add(libraryBtn, resumeBtn, trackBtn);
    return buttons;
  }

  /**
   * Creates and retrieves the resume button for a manga, which allows the user to resume reading
   * from the last chapter they left off.
   *
   * @param manga The manga object for which to retrieve the resume button.
   * @param chapters The list of chapters available for the manga.
   * @return The resume button with the appropriate click listener.
   */
  @NotNull
  private Button getResumeButton(Manga manga, List<Chapter> chapters) {
    Button resumeBtn = new Button("Resume", LumoIcon.PLAY.create());
    resumeBtn.addClassName("manga-btn");
    resumeBtn.addClickListener(
        e -> {
          if (chapters.isEmpty()) {
            return;
          }

          Chapter nextChapter;

          var lastChapter = manga.getLastChapterRead();
          if (lastChapter == null) {
            nextChapter = chapters.get(chapters.size() - 1);
          } else {
            // 1 based index
            int index = lastChapter.getIndex();

            if (index == chapters.size()) {
              Notification notification = new Notification("No more chapters available", 3000);
              notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
              notification.setPosition(Notification.Position.MIDDLE);
              notification.open();
              return;
            }

            Collections.reverse(chapters);

            nextChapter = chapters.get(index);
          }

          UI ui = UI.getCurrent();

          RouteUtils.routeToReadingView(ui, manga.getId(), nextChapter.getIndex());
        });
    return resumeBtn;
  }

  @NotNull
  private Button getLibraryBtn(Manga manga) {
    Button libraryBtn = new Button();
    libraryBtn.addClickListener(
        e -> {
          if (manga.isInLibrary()) {
            mangaService.removeMangaFromLibrary(manga.getId());
            libraryBtn.setText("Add to library");
            manga.setInLibrary(false);
          } else {
            mangaService.addMangaToLibrary(manga.getId());
            libraryBtn.setText("Remove from library");
            manga.setInLibrary(true);
          }
        });

    if (manga.isInLibrary()) {
      libraryBtn.setText("Remove from library");
    } else {
      libraryBtn.setText("Add to library");
    }
    return libraryBtn;
  }
}
