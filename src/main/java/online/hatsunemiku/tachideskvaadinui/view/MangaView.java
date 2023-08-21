package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
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
import online.hatsunemiku.tachideskvaadinui.utils.MangaDataUtils;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.client.RestTemplate;

@Route("manga/:id(\\d+)")
@CssImport("./css/manga.css")
public class MangaView extends StandardLayout implements BeforeEnterObserver {

  private final RestTemplate client;
  private final MangaService mangaService;
  private final SettingsService settingsService;
  private final AniListAPIService aniListAPIService;

  public MangaView(
      RestTemplate client,
      MangaService mangaService,
      SettingsService settingsService,
      AniListAPIService aniListAPIService) {
    super("Manga");
    this.client = client;
    this.mangaService = mangaService;
    this.settingsService = settingsService;
    this.aniListAPIService = aniListAPIService;
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

    Manga manga;
    try {
      manga = getManga(settings, id);
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

    ListBox<Chapter> chapters = getChapters(settings, id);

    Div buttons = getButtons(manga);

    H1 mangaTitle = new H1(manga.getTitle());
    mangaTitle.addClassName("manga-title");

    container.add(mangaTitle, imageContainer, buttons, chapters);
    setContent(container);
  }

  @NotNull
  private Div getButtons(Manga manga) {
    Div buttons = new Div();
    buttons.addClassName("manga-buttons");

    Button libraryBtn = getLibraryBtn(manga);
    libraryBtn.addClassName("manga-btn");

    Button trackBtn = new Button("Tracking", LumoIcon.RELOAD.create());
    trackBtn.addClassName("manga-btn");

    trackBtn.addClickListener(
        e -> {
          TrackingDialog dialog = new TrackingDialog(settingsService, manga, aniListAPIService);
          dialog.open();
        });

    buttons.add(libraryBtn, trackBtn);
    return buttons;
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

  private Manga getManga(Settings settings, String id) {
    String mangaEndpoint = settings.getUrl() + "/api/v1/manga/" + id;

    Manga manga = client.getForObject(mangaEndpoint, Manga.class);

    if (manga == null) {
      throw new NotFoundException("Manga not found");
    }

    return manga;
  }

  private ListBox<Chapter> getChapters(Settings settings, String mangaId) {

    List<Chapter> chapter = MangaDataUtils.getChapterList(settings, mangaId, client);

    return new ChapterListBox(chapter);
  }
}
