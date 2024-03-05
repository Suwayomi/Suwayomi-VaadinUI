/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.reader.paged;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Image;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.reader.Reader;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.ReaderSettingsChangeEvent;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderDirection;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderSettings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingCommunicationService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.Swiper;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.SwiperConfig;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.constants.LanguageDirection;

/**
 * The PagedReader class is responsible for displaying and controlling the reading of manga chapters
 * in a paged format. It extends the {@link Reader} class and provides additional functionality
 * specific to paged reading.
 */
@Slf4j
public class PagedReader extends Reader {
  private final Swiper swiper;
  private final ExecutorService trackerExecutor;

  /**
   * Constructs a {@link PagedReader} object.
   *
   * @param chapter The Chapter object representing the chapter being read.
   * @param dataService The TrackingDataService object used for tracking chapter progress.
   * @param trackingCommunicationService The TrackingCommunicationService object used for
   *     communication with tracking service.
   * @param mangaService The MangaService object used for manga-related operations.
   * @param settingsService The SettingsService object used for managing reader settings.
   */
  public PagedReader(
      Chapter chapter,
      TrackingDataService dataService,
      TrackingCommunicationService trackingCommunicationService,
      MangaService mangaService,
      SettingsService settingsService) {
    super(chapter, dataService, trackingCommunicationService, mangaService, settingsService);
    addClassName("paged-reader");
    this.trackerExecutor = Executors.newSingleThreadExecutor();

    var config = SwiperConfig.builder().zoom(true).centeredSlides(true).build();

    swiper = new Swiper(config);

    UI ui = UI.getCurrent();
    ComponentUtil.addListener(
        ui,
        ReaderSettingsChangeEvent.class,
        e -> {
          var direction = e.getNewSettings().getDirection();

          switch (direction) {
            case RTL -> swiper.changeLanguageDirection(LanguageDirection.RIGHT_TO_LEFT);
            case LTR -> swiper.changeLanguageDirection(LanguageDirection.LEFT_TO_RIGHT);
            case VERTICAL ->
                log.info("Can't change to vertical direction inside PagedReader - Ignored");
            default -> throw new IllegalStateException("Unexpected value: " + direction);
          }
        });

    ReaderSettings settings = settingsService.getSettings().getReaderSettings(chapter.getMangaId());

    switch (settings.getDirection()) {
      case RTL -> swiper.changeLanguageDirection(LanguageDirection.RIGHT_TO_LEFT);
      case LTR -> swiper.changeLanguageDirection(LanguageDirection.LEFT_TO_RIGHT);
      default -> throw new IllegalStateException("Unexpected value: " + settings.getDirection());
    }

    /*This is a JavaScript function as it feels more sluggish when it has
     * to send data back to the server. Therefore, the server is responsible
     * for the mouse wheel's zoom function.
     */
    swiper
        .getElement()
        .executeJs(
            """
                                var zoomListener = function (e) {
                                 if ($0.swiper.zoom === undefined) {
                                  console.info("Removing zoom listener.");
                                  removeEventListener('wheel', zoomListener);
                                  return;
                                 }

                                  var zoom = $0.swiper.zoom.scale;
                                  if (e.deltaY < 0) {
                                    zoom += 0.5;
                                  } else {
                                    zoom -= 0.5;
                                  }

                                  if (zoom < 1) {
                                    zoom = 1;
                                  }

                                  if (zoom > 3) {
                                    zoom = 3;
                                  }

                                  $0.swiper.zoom.in(zoom);
                                  };

                                addEventListener('wheel', zoomListener);
                                """,
            swiper.getElement());

    loadChapter();

    Tracker tracker = dataService.getTracker(chapter.getMangaId());

    if (tracker.hasAniListId()) {
      swiper.addActiveIndexChangeEventListener(
          e -> {
            if (e.getActiveIndex() == chapter.getPageCount() - 1) {
              log.info("Last page of chapter {}", chapter.getChapterNumber());
              trackerExecutor.submit(
                  () ->
                      trackingCommunicationService.setChapterProgress(
                          chapter.getMangaId(), chapter.getChapterNumber(), true));
              e.unregisterListener();
            }
          });
    }

    swiper.addReachEndEventListener(
        e -> {
          if (mangaService.setChapterRead(chapter.getId())) {
            log.info("Set chapter {} to read", chapter.getName());
          } else {
            log.warn("Couldn't set chapter {} to read", chapter.getName());
          }
        });

    swiper.addActiveIndexChangeEventListener(e -> sendPageChangeEvent(e.getActiveIndex()));

    add(swiper);
  }

  @Override
  protected void loadChapter() {

    var urls = mangaService.getChapterPages(chapter.getId());

    Settings settings = settingsService.getSettings();
    String baseUrl = settings.getUrl();

    for (int i = 0; i < urls.size(); i++) {
      String url = baseUrl + urls.get(i);

      Image image = new Image(url, "Page %d".formatted(i + 1));

      if (i > 1) {
        image.getElement().setAttribute("loading", "lazy");
      }

      image.addClassName("manga-page");

      swiper.addZoomable(true, image);
    }
  }

  @Override
  protected ReaderDirection getReaderDirection() {
    var langDir = swiper.getLanguageDirection();

    if (langDir == LanguageDirection.LEFT_TO_RIGHT) {
      return ReaderDirection.LTR;
    } else {
      return ReaderDirection.RTL;
    }
  }

  @Override
  protected int getPageIndex() {
    return swiper.getActiveIndex();
  }

  @Override
  protected void moveToPage(int index) {
    swiper.slideTo(index);
  }

  @Override
  protected void moveToPreviousPage() {
    swiper.slidePrev();
  }

  @Override
  protected void moveToNextPage() {
    swiper.slideNext();
  }
}
