/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.reader;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderDirection;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingCommunicationService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;

/**
 * A base class for different manga reader implementations that provides common functionality and
 * events.
 */
public abstract class Reader extends Div {

  protected final Chapter chapter;
  protected final MangaService mangaService;
  protected final SettingsService settingsService;

  protected Reader(
      Chapter chapter,
      TrackingDataService dataService,
      TrackingCommunicationService trackingCommunicationService,
      MangaService mangaService,
      SettingsService settingsService) {
    this.chapter = chapter;
    this.mangaService = mangaService;
    this.settingsService = settingsService;
  }

  /** Loads the chapter pages and displays them in the reader. */
  protected abstract void loadChapter();

  /**
   * Retrieves the reading direction of the reader.
   *
   * @return The {@link ReaderDirection} corresponding to the reading direction of the reader.
   */
  protected abstract ReaderDirection getReaderDirection();

  /**
   * Retrieves the index of the current page in the reader. This is a 0-based index. e.g. the first
   * page has index 0.
   *
   * @return The index of the current page in the reader.
   */
  protected abstract int getPageIndex();

  /**
   * Makes the reader display the page at the given index. If the index is out of bounds, the reader
   * will not move and no action will be taken.
   *
   * @param index The index of the page to move to. This is a 0-based index, where the first page
   *     has an index of 0.
   */
  protected abstract void moveToPage(int index);

  /**
   * Makes the reader display the previous page. This is equivalent to calling {@link
   * #moveToPage(int)} with the index of the previous page.
   */
  protected abstract void moveToPreviousPage();

  /**
   * Makes the reader display the next page. This is equivalent to calling {@link #moveToPage(int)}
   * with the index of the next page.
   */
  protected abstract void moveToNextPage();

  /**
   * Sends a page change event indicating that the reader has displayed to a new page. e.g. Goes
   * from page 10 to page 11 or from page 11 to page 10. This method creates a new instance of
   * {@link ReaderPageIndexChangeEvent} and fires the event.
   *
   * @param index The index of the page that was moved to. This is a 0-based index, where the first
   *     page has an index of 0.
   * @see ReaderPageIndexChangeEvent
   */
  protected void sendPageChangeEvent(int index) {
    var event = new ReaderPageIndexChangeEvent(this, false, index);
    fireEvent(event);
  }

  public Registration addReaderPageIndexChangeListener(
      ComponentEventListener<ReaderPageIndexChangeEvent> listener) {
    return addListener(ReaderPageIndexChangeEvent.class, listener);
  }
}
