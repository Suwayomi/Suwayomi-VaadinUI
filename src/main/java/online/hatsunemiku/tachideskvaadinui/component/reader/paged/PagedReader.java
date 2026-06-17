/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.reader.paged;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.reader.Reader;
import online.hatsunemiku.tachideskvaadinui.component.reader.strip.MangaPageViewEvent;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.ReaderSettingsChangeEvent;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderDirection;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderSettings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;

/**
 * The PagedReader class is responsible for displaying and controlling the reading of manga chapters
 * in a paged format. It extends the {@link Reader} class and provides additional functionality
 * specific to paged reading using native CSS scroll snapping and pinch-to-zoom.
 */
@Slf4j
@JsModule("./js/paged-reader-splide.js")
@CssImport("@splidejs/splide/dist/css/splide.min.css")
public class PagedReader extends Reader {

  private final Div carousel;
  private final Div list;
  private final List<Div> pageElements = new ArrayList<>();
  private int currentPageIndex = 0;

  /**
   * Constructs a {@link PagedReader} object.
   *
   * @param chapter The Chapter object representing the chapter being read.
   * @param mangaService The MangaService object used for manga-related operations.
   * @param settingsService The SettingsService object used for managing reader settings.
   */
  public PagedReader(Chapter chapter, MangaService mangaService, SettingsService settingsService) {
    super(chapter, mangaService, settingsService);
    addClassName("paged-reader");

    carousel = new Div();
    carousel.addClassName("paged-carousel");
    carousel.addClassName("splide");
    carousel.getElement().getStyle().set("width", "100%");
    carousel.getElement().getStyle().set("height", "100%");

    Div track = new Div();
    track.addClassName("splide__track");
    track.getElement().getStyle().set("width", "100%");
    track.getElement().getStyle().set("height", "100%");

    list = new Div();
    list.addClassName("splide__list");
    list.getElement().getStyle().set("width", "100%");
    list.getElement().getStyle().set("height", "100%");

    track.add(list);
    carousel.add(track);

    UI ui = UI.getCurrent();
    var settingsChangeListener =
        ComponentUtil.addListener(
            ui,
            ReaderSettingsChangeEvent.class,
            e -> {
              var direction = e.getNewSettings().getDirection();

              switch (direction) {
                case RTL -> carousel.getElement().setAttribute("dir", "rtl");
                case LTR -> carousel.getElement().setAttribute("dir", "ltr");
                case VERTICAL -> log.info(
                    "Can't change to vertical direction inside PagedReader - Ignored");
                default -> throw new IllegalStateException("Unexpected value: " + direction);
              }
            });

    addDetachListener(e -> settingsChangeListener.remove());

    ReaderSettings settings = settingsService.getSettings().getReaderSettings(chapter.getMangaId());
    String dirStr;
    switch (settings.getDirection()) {
      case RTL -> {
        carousel.getElement().setAttribute("dir", "rtl");
        dirStr = "rtl";
      }
      case LTR -> {
        carousel.getElement().setAttribute("dir", "ltr");
        dirStr = "ltr";
      }
      default -> throw new IllegalStateException("Unexpected value: " + settings.getDirection());
    }

    loadChapter();

    addListener(
        MangaPageViewEvent.class,
        e -> {
          if (currentPageIndex == e.getPageIndex()) {
            return;
          }
          currentPageIndex = e.getPageIndex();
          sendPageChangeEvent(currentPageIndex);

          if (currentPageIndex == pageElements.size() - 1) {
            sendReachEndEvent();
          }
        });

    carousel.getElement()
        .executeJs("window.initMangaSplide(this, $0, $1, $2);", dirStr, currentPageIndex,
            pageElements.size());

    add(carousel);
  }

  @Override
  protected void loadChapter() {
    var urls = mangaService.getChapterPages(chapter.getId());

    Settings settings = settingsService.getSettings();
    String baseUrl = settings.getUrl();

    for (int i = 0; i < urls.size(); i++) {
      String url = baseUrl + urls.get(i);

      Image image = new Image();
      image.setAlt("Page %d".formatted(i + 1));
      image.addClassName("manga-page");
      image.getElement().setAttribute("draggable", "false");
      image.getStyle().set("opacity", "0");

      if (i == 0) {
        image.setSrc(url);
      } else {
        image.getElement().setAttribute("data-splide-lazy", url);
      }

      Div imgContainer = new Div();
      imgContainer.addClassName("image-container");

      Div spinner = new Div();
      spinner.addClassName("obsidian-spinner");
      spinner.add(new Div(), new Div(), new Div());

      imgContainer.add(spinner, image);

      // Fade in on load and hide spinner, handling cached images immediately
      image.getElement().executeJs(
          "if (this.complete) {" +
          "  this.style.opacity = '1';" +
          "  if (this.parentElement) {" +
          "    const spinner = this.parentElement.querySelector('.obsidian-spinner');" +
          "    if (spinner) spinner.style.display = 'none';" +
          "  }" +
          "} else {" +
          "  this.onload = () => {" +
          "    this.style.opacity = '1';" +
          "    if (this.parentElement) {" +
          "      const spinner = this.parentElement.querySelector('.obsidian-spinner');" +
          "      if (spinner) spinner.style.display = 'none';" +
          "    }" +
          "  };" +
          "}"
      );

      Div slide = new Div();
      slide.addClassName("paged-slide");
      slide.addClassName("splide__slide");
      slide.getElement().setAttribute("data-page-index", String.valueOf(i));
      slide.add(imgContainer);

      list.add(slide);
      pageElements.add(slide);
    }
  }

  @Override
  protected ReaderDirection getReaderDirection() {
    var dirAttr = carousel.getElement().getAttribute("dir");
    if ("rtl".equalsIgnoreCase(dirAttr)) {
      return ReaderDirection.RTL;
    } else {
      return ReaderDirection.LTR;
    }
  }

  @Override
  protected int getPageIndex() {
    return currentPageIndex;
  }

  @Override
  protected void moveToPage(int index) {
    if (index < 0 || index >= pageElements.size()) {
      return;
    }
    currentPageIndex = index;
    sendPageChangeEvent(index);

    carousel.getElement().executeJs(
        "if (this._splide) { this._splide.go($0); }",
        index
    );
  }

  @Override
  protected void moveToPreviousPage() {
    moveToPage(currentPageIndex - 1);
  }

  @Override
  protected void moveToNextPage() {
    moveToPage(currentPageIndex + 1);
  }
}
