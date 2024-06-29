/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.TrackingDialog;
import online.hatsunemiku.tachideskvaadinui.component.listbox.chapter.ChapterListBox;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SuwayomiService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.MyAnimeListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService;
import online.hatsunemiku.tachideskvaadinui.utils.RouteUtils;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.jetbrains.annotations.NotNull;

/**
 * MangaView is a view for displaying manga information such as chapters and cover image. It also
 * allows the user to download chapters, add the manga to their library and more.
 */
@Route("manga/:id(\\d+)")
@CssImport("./css/manga.css")
public class MangaView extends StandardLayout implements BeforeEnterObserver {

  private final MangaService mangaService;
  private final SettingsService settingsService;
  private final AniListAPIService aniListAPIService;
  private final TrackingDataService dataService;
  private final SuwayomiTrackingService suwayomiTrackingService;
  private final MyAnimeListAPIService malAPI;
  private final SuwayomiService suwayomiService;

  /**
   * Creates a MangaView object.
   *
   * @param mangaService The {@link MangaService} for accessing the manga data from the server.
   * @param settingsService The {@link SettingsService} for accessing and managing application
   *     settings.
   * @param aniListAPIService The {@link AniListAPIService} for connecting to the AniList API.
   * @param dataService The {@link TrackingDataService} for tracking manga reading data.
   * @param suwayomiTrackingService The {@link SuwayomiTrackingService} for Suwayomi tracking.
   */
  public MangaView(
      MangaService mangaService,
      SettingsService settingsService,
      AniListAPIService aniListAPIService,
      TrackingDataService dataService,
      SuwayomiTrackingService suwayomiTrackingService,
      MyAnimeListAPIService malAPI,
      SuwayomiService suwayomiService) {
    super("Manga");
    this.mangaService = mangaService;
    this.settingsService = settingsService;
    this.aniListAPIService = aniListAPIService;
    this.dataService = dataService;
    this.suwayomiTrackingService = suwayomiTrackingService;
    this.malAPI = malAPI;
    this.suwayomiService = suwayomiService;
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

    int mangaId = Integer.parseInt(id);

    Manga manga;
    try {
      manga = mangaService.getManga(mangaId);
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

    if (chapters.isEmpty()) {
      chapters = mangaService.fetchChapterList(mangaId);
    }

    Collections.reverse(chapters);

    ListBox<Chapter> chapterListBox = new ChapterListBox(chapters, mangaService);

    Div buttons = getButtons(manga, chapters);

    H1 mangaTitle = new H1(manga.getTitle());
    mangaTitle.addClassName("manga-title");

    container.add(mangaTitle, imageContainer, buttons, chapterListBox);
    setContent(container);
  }

  /**
   * Retrieves and constructs the buttons needed for functionality for a manga.
   *
   * @param manga The {@link Manga} object for which to retrieve the buttons.
   * @param chapters The list of {@link Chapter} objects available for the manga.
   * @return The constructed {@link Div} element containing the buttons.
   */
  @NotNull
  private Div getButtons(Manga manga, List<Chapter> chapters) {
    Div buttons = new Div();
    buttons.addClassName("manga-buttons");

    Button libraryBtn = getLibraryBtn(manga);
    libraryBtn.addClassName("manga-btn");

    Button downloadBtn = getDownloadBtn(chapters);

    Button trackBtn = new Button("Tracking", LumoIcon.RELOAD.create());
    trackBtn.addClassName("manga-btn");

    trackBtn.addClickListener(
        e -> {
          var dialog =
              new TrackingDialog(
                  dataService,
                  manga,
                  aniListAPIService,
                  suwayomiTrackingService,
                  malAPI,
                  suwayomiService,
                  mangaService);
          dialog.open();
        });

    Button resumeBtn = getResumeButton(manga, chapters);

    buttons.add(libraryBtn, resumeBtn, downloadBtn, trackBtn);
    return buttons;
  }

  @NotNull
  private Button getDownloadBtn(List<Chapter> chapters) {
    Button downloadBtn = new Button("Download", LumoIcon.DOWNLOAD.create());
    downloadBtn.addClassName("manga-btn");
    downloadBtn.addClickListener(
        e -> {
          var ids = chapters.stream().map(Chapter::getId).toList();

          if (!mangaService.downloadMultipleChapter(ids)) {
            Notification notification = new Notification("Failed to download chapters", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
            return;
          }

          UI ui = UI.getCurrent();
          ComponentUtil.fireEvent(ui, new DownloadAllChapterEvent(this, false));

          Notification notification = new Notification("Downloading chapters", 3000);
          notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
          notification.setPosition(Notification.Position.MIDDLE);
          notification.open();
        });
    return downloadBtn;
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

          Chapter nextChapter = null;

          var lastChapter = manga.getLastChapterRead();
          if (lastChapter == null) {

            var reversed = new ArrayList<>(chapters);
            Collections.reverse(reversed);

            for (Chapter chapter : reversed) {
              if (chapter.isRead()) {
                continue;
              }

              nextChapter = chapter;
              break;
            }

            if (nextChapter == null) {
              nextChapter = chapters.getFirst();
            }

          } else {
            int id = lastChapter.getId();

            int index = 0;

            for (Chapter chapter : chapters) {
              if (chapter.getId() == id) {
                break;
              }

              index++;
            }

            if (index == chapters.size() - 1) {
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

          RouteUtils.routeToReadingView(ui, manga.getId(), nextChapter.getId());
        });
    return resumeBtn;
  }

  @NotNull
  private Button getLibraryBtn(Manga manga) {
    Button libraryBtn = new Button();
    libraryBtn.addClickListener(
        e -> {
          if (manga.isInLibrary()) {
            boolean success = mangaService.removeMangaFromLibrary(manga.getId());

            if (!success) {
              Notification notification =
                  new Notification("Failed to remove manga from library", 3000);
              notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
              notification.setPosition(Notification.Position.MIDDLE);
              notification.open();
              return;
            }

            libraryBtn.setText("Add to library");
            manga.setInLibrary(false);
          } else {
            boolean success = mangaService.addMangaToLibrary(manga.getId());

            if (!success) {
              Notification notification = new Notification("Failed to add manga to library", 3000);
              notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
              notification.setPosition(Notification.Position.MIDDLE);
              notification.open();
              return;
            }

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

  public static class DownloadAllChapterEvent extends ComponentEvent<MangaView> {

    /**
     * Creates a new event using the given source and indicator whether the event originated from
     * the client side or the server side.
     *
     * @param source the source component
     * @param fromClient <code>true</code> if the event originated from the client side, <code>false
     *                   </code> otherwise
     */
    public DownloadAllChapterEvent(MangaView source, boolean fromClient) {
      super(source, fromClient);
    }
  }
}
