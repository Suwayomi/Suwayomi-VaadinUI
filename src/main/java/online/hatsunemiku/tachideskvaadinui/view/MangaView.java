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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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

  private static final String EMPTY_VALUE = "-";

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
    fullScreenNoHide();
    removeClassName("library-screen");
    addClassName("manga-screen");
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

    if (idParam.isEmpty()) {
      event.rerouteToError(NotFoundException.class, "Manga not found");
      return;
    }

    int mangaId = Integer.parseInt(idParam.get());
    Settings settings = settingsService.getSettings();

    Manga manga;
    try {
      manga = mangaService.getManga(mangaId);
    } catch (Exception e) {
      event.rerouteTo(ServerStartView.class);
      return;
    }

    List<Chapter> chapters = new ArrayList<>(mangaService.getChapterList(mangaId));
    if (chapters.isEmpty()) {
      chapters = new ArrayList<>(mangaService.fetchChapterList(mangaId));
    }
    Collections.reverse(chapters);

    setContent(buildMangaView(settings, manga, chapters));
  }

  @NotNull
  private Div buildMangaView(Settings settings, Manga manga, List<Chapter> chapters) {
    Div viewContainer = new Div();
    viewContainer.addClassName("manga-view-container");

    Div contentGrid = new Div();
    contentGrid.addClassName("manga-content-grid");

    Div infoColumn = buildInfoColumn(manga, chapters);
    Div chaptersColumn = buildChapterColumn(manga, chapters);

    contentGrid.add(infoColumn, chaptersColumn);

    viewContainer.add(buildHeroSection(settings, manga, chapters), contentGrid);
    return viewContainer;
  }

  @NotNull
  private Div buildHeroSection(Settings settings, Manga manga, List<Chapter> chapters) {
    Div heroSection = new Div();
    heroSection.addClassName("manga-hero-section");

    Div heroContent = new Div();
    heroContent.addClassName("manga-hero-content");

    Image coverImage = new Image(settings.getUrl() + manga.getThumbnailUrl(), manga.getTitle());
    coverImage.addClassName("manga-hero-cover-image");

    Div coverContainer = new Div(coverImage);
    coverContainer.addClassName("manga-hero-cover-container");

    Div heroText = new Div();
    heroText.addClassName("manga-hero-text");

    H1 title = new H1(safe(manga.getTitle()));
    title.addClassName("manga-hero-title");

    Div genreContainer = new Div();
    genreContainer.addClassName("manga-hero-genres");
    if (manga.getGenre() != null) {
      for (String genre : manga.getGenre()) {
        Span genreTag = new Span(genre);
        genreTag.addClassName("manga-genre-tag");
        genreContainer.add(genreTag);
      }
    }

    Div metaContainer = new Div();
    metaContainer.addClassName("manga-hero-meta");
    metaContainer.add(createHeroMetaItem(VaadinIcon.USER, "Author: " + safe(manga.getAuthor())));
    metaContainer.add(createHeroMetaItem(VaadinIcon.REFRESH, "Status: " + safe(manga.getStatus())));
    metaContainer.add(createHeroMetaItem(VaadinIcon.HASH, "Source: " + manga.getSource().getDisplayName()));

    Div actions = new Div();
    actions.addClassName("manga-hero-actions");

    Button resumeBtn = getResumeButton(manga, chapters);
    resumeBtn.addClassName("btn-resume");

    Button downloadBtn = getDownloadBtn(chapters);
    downloadBtn.addClassName("btn-glass");

    Button trackBtn = new Button("Tracking", VaadinIcon.BOOKMARK.create());
    trackBtn.addClassName("btn-glass");
    trackBtn.addClickListener(e -> openTrackingDialog(manga));

    Button libraryBtn = getLibraryBtn(manga);
    libraryBtn.addClassName("btn-glass");

    actions.add(resumeBtn, downloadBtn, trackBtn, libraryBtn);
    heroText.add(title, genreContainer, metaContainer, actions);
    heroContent.add(coverContainer, heroText);
    heroSection.add(heroContent);
    return heroSection;
  }

  @NotNull
  private Div buildInfoColumn(Manga manga, List<Chapter> chapters) {
    Div infoColumn = new Div();
    infoColumn.addClassName("manga-info-column");

    Div synopsisCard = new Div();
    synopsisCard.addClassNames("glass-card", "manga-section-card");

    H3 synopsisTitle = new H3("Synopsis");
    synopsisTitle.addClassName("manga-section-title");

    Paragraph synopsisText =
        new Paragraph(
            manga.getDescription() == null || manga.getDescription().isBlank()
                ? "No description available."
                : manga.getDescription());
    synopsisText.addClassName("manga-synopsis-text");
    synopsisCard.add(synopsisTitle, synopsisText);

    if (manga.getDescription() != null && manga.getDescription().length() > 200) {
      synopsisText.addClassName("clamped");
      Button readMoreBtn = new Button("Read More");
      readMoreBtn.addClassName("btn-read-more");
      readMoreBtn.addClickListener(
          e -> {
            if (synopsisText.getClassNames().contains("clamped")) {
              synopsisText.removeClassName("clamped");
              readMoreBtn.setText("Show Less");
            } else {
              synopsisText.addClassName("clamped");
              readMoreBtn.setText("Read More");
            }
          });
      synopsisCard.add(readMoreBtn);
    }

    Div detailsCard = new Div();
    detailsCard.addClassNames("glass-card", "manga-section-card");

    H3 detailsTitle = new H3("Details");
    detailsTitle.addClassName("manga-section-title");

    long readChapters = chapters.stream().filter(Chapter::isRead).count();

    Div detailsList = new Div();
    detailsList.addClassName("manga-details-list");
    detailsList.add(createDetailItem("SOURCE", String.valueOf(manga.getSource().getDisplayName())));
    detailsList.add(createDetailItem("STATUS", safe(manga.getStatus())));
    detailsList.add(createDetailItem("CHAPTERS", String.valueOf(manga.getChapterCount())));
    detailsList.add(createDetailItem("READ", readChapters + " / " + chapters.size()));

    detailsCard.add(detailsTitle, detailsList);
    infoColumn.add(synopsisCard, detailsCard);
    return infoColumn;
  }

  @NotNull
  private Div buildChapterColumn(Manga manga, List<Chapter> chapters) {
    Div chaptersColumn = new Div();
    chaptersColumn.addClassName("manga-chapters-column");

    Div chaptersHeader = new Div();
    chaptersHeader.addClassName("chapters-header");

    Div titleGroup = new Div();
    titleGroup.addClassName("chapters-title-group");

    H3 chaptersTitle = new H3("Chapters");
    chaptersTitle.addClassName("chapters-title");

    Span countBadge = new Span(chapters.size() + " TOTAL");
    countBadge.addClassName("chapters-count-badge");

    Button chapterFilterBtn = new Button(VaadinIcon.FILTER.create());
    chapterFilterBtn.addClassName("chapters-filter-button");
    chapterFilterBtn.setAriaLabel("Filter chapters");

    titleGroup.add(chaptersTitle, countBadge);
    chaptersHeader.add(titleGroup, chapterFilterBtn);

    Div chaptersContainer = new Div();
    chaptersContainer.addClassName("manga-chapters-container");

    ListBox<Chapter> chapterListBox = new ChapterListBox(chapters, mangaService);
    chapterListBox.addClassNames("manga-chapters-listbox", "chapter-list-box");

    chaptersContainer.add(chapterListBox);
    chaptersColumn.add(chaptersHeader, chaptersContainer);
    return chaptersColumn;
  }

  private void openTrackingDialog(Manga manga) {
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
  }

  @NotNull
  private Div createHeroMetaItem(VaadinIcon icon, String text) {
    Div item = new Div();
    item.addClassName("manga-hero-meta-item");
    item.add(icon.create(), new Span(text));
    return item;
  }

  @NotNull
  private Div createDetailItem(String label, String value) {
    Div item = new Div();
    item.addClassName("manga-detail-item");

    Span labelSpan = new Span(label);
    labelSpan.addClassName("manga-detail-label");

    Span valueSpan = new Span(value);
    valueSpan.addClassName("manga-detail-value");

    item.add(labelSpan, valueSpan);
    return item;
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

            List<Chapter> reverseChapters = new ArrayList<>(chapters);
            Collections.reverse(reverseChapters);
            nextChapter = reverseChapters.get(index);
          }

          UI ui = UI.getCurrent();
          RouteUtils.routeToReadingView(ui, manga.getId(), nextChapter.getId());
        });
    return resumeBtn;
  }

  @NotNull
  private Button getLibraryBtn(Manga manga) {
    Button libraryBtn = new Button();
    updateLibraryButtonText(libraryBtn, manga.isInLibrary());

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

            manga.setInLibrary(true);
          }

          updateLibraryButtonText(libraryBtn, manga.isInLibrary());
        });

    return libraryBtn;
  }

  private void updateLibraryButtonText(Button libraryBtn, boolean isInLibrary) {
    if (isInLibrary) {
      libraryBtn.setText("Remove from library");
    } else {
      libraryBtn.setText("Add to library");
    }
  }

  @NotNull
  private String safe(String value) {
    if (value == null || value.isBlank()) {
      return EMPTY_VALUE;
    }
    return value;
  }

  public static class DownloadAllChapterEvent extends ComponentEvent<MangaView> {

    /**
     * Creates a new event using the given source and indicator whether the event originated from
     * the client side or the server side.
     *
     * @param source the source component
     * @param fromClient <code>true</code> if the event originated from the client side, <code>false
     *     </code> otherwise
     */
    public DownloadAllChapterEvent(MangaView source, boolean fromClient) {
      super(source, fromClient);
    }
  }
}
