/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.reader.strip;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ScrollOptions;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.reader.Reader;
import online.hatsunemiku.tachideskvaadinui.component.reader.ReaderPageIndexChangeEvent;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderDirection;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingCommunicationService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StripReader is a class that extends the {@link Reader} class and provides functionality for
 * displaying manga pages in a strip/vertical format.
 */
public class StripReader extends Reader {

  private static final Logger log = LoggerFactory.getLogger(StripReader.class);
  private int currentPageIndex = -1;
  private List<Image> pages = new ArrayList<>();

  /**
   * Constructs a {@link StripReader} object.
   *
   * @param chapter The Chapter object representing the chapter being read.
   * @param dataService The TrackingDataService object used for tracking chapter progress.
   * @param trackingCommunicationService The TrackingCommunicationService object used for
   *     communication with tracking service.
   * @param mangaService The MangaService object used for manga-related operations.
   * @param settingsService The SettingsService object used for managing reader settings.
   */
  public StripReader(
      Chapter chapter,
      TrackingDataService dataService,
      TrackingCommunicationService trackingCommunicationService,
      MangaService mangaService,
      SettingsService settingsService) {
    super(chapter, dataService, trackingCommunicationService, mangaService, settingsService);
    addClassName("strip-reader");

    // language=JavaScript
    String jsObserver =
        """
            const observer = new IntersectionObserver((entries) => {
                   entries.forEach(entry => {
                       console.log(entry)

                       if (entry.isIntersecting) {
                       console.log('Intersecting');
                           //send manga page view event
                           const page = entry.target;
                           const pageIndex = page.getAttribute('data-page-index');

                           const event = new CustomEvent('manga-page-view', {
                               detail: {
                                   pageIndex: pageIndex
                               },
                               bubbles: true
                           });

                           page.dispatchEvent(event);
                           console.log('Event dispatched', event);
                       }
                   });
               }, {
                   root: null,
                   rootMargin: '0px',
                   threshold: 0.7
               });

               var pages = document.querySelectorAll('.manga-page');

               pages.forEach(page => {
                   observer.observe(page);
               });
            """;

    UI ui = getUI().orElseGet(UI::getCurrent);

    if (ui == null) {
      throw new IllegalStateException("No UI available");
    }

    var pending = ui.getPage().executeJs(jsObserver);

    loadChapter();

    pending.then(result -> log.info("Strip observer loaded"));

    addMangaPageViewListener(
        e -> {
          currentPageIndex = e.getPageIndex();

          boolean fromClient = e.isFromClient();

          var event = new ReaderPageIndexChangeEvent(this, fromClient, currentPageIndex);
          fireEvent(event);

          if (currentPageIndex == pages.size() - 1) {
            sendReachEndEvent();
          }
        });
  }

  @Override
  protected void loadChapter() {
    Div container = new Div();
    container.addClassName("page-container");
    int chapterId = chapter.getId();

    var images = mangaService.getChapterPages(chapterId);

    var baseUrl = settingsService.getSettings().getUrl();

    List<Image> pages = new ArrayList<>();

    for (int i = 0; i < images.size(); i++) {
      var url = images.get(i);
      var completeUrl = baseUrl + url;

      String altText = "Page %d".formatted(i);

      Image image = new Image(completeUrl, altText);

      image.getElement().setAttribute("data-page-index", String.valueOf(i));

      image.addClassName("manga-page");

      pages.add(image);

      container.add(image);
    }

    this.pages = pages;

    add(container);
  }

  @Override
  protected ReaderDirection getReaderDirection() {
    var settings = settingsService.getSettings();
    var mangaSettings = settings.getReaderSettings(chapter.getMangaId());

    return mangaSettings.getDirection();
  }

  @Override
  protected int getPageIndex() {
    return currentPageIndex;
  }

  @Override
  protected void moveToPage(int index) {

    if (index < 0 || index >= pages.size()) {
      return;
    }

    ScrollOptions options = new ScrollOptions();
    options.setBehavior(ScrollOptions.Behavior.SMOOTH);
    options.setBlock(ScrollOptions.Alignment.START);

    pages.get(index).getElement().scrollIntoView(options);
  }

  @Override
  protected void moveToPreviousPage() {
    int previousPage = currentPageIndex - 1;
    moveToPage(previousPage);
  }

  @Override
  protected void moveToNextPage() {
    int nextPage = currentPageIndex + 1;
    moveToPage(nextPage);
  }

  /**
   * Adds a listener for the {@link MangaPageViewEvent}
   *
   * @param listener the listener to be added
   * @return a registration object that can be used to remove the listener
   */
  public Registration addMangaPageViewListener(
      ComponentEventListener<MangaPageViewEvent> listener) {
    return addListener(MangaPageViewEvent.class, listener);
  }
}
