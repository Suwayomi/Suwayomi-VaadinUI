/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view.trackers;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.card.AniListMediaCard;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMedia;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.MangaList;
import online.hatsunemiku.tachideskvaadinui.services.tracker.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.view.layout.TrackingLayout;

/** AniListView is a view for displaying AniList entries to import. */
@Route("anilist")
@CssImport("./css/views/anilist.css")
public class AniListView extends TrackingLayout {

  private final AniListAPIService aniListAPI;
  private MangaList list;

  /**
   * AniListView is a view for displaying AniList entries to import.
   *
   * @param apiService the {@link AniListAPIService} instance to use for fetching data from AniList
   */
  public AniListView(AniListAPIService apiService) {
    super("AniList");

    addClassName("anilist-view");

    this.aniListAPI = apiService;

    if (hasToken()) {
      this.list = apiService.getMangaList();
    }

    init();
  }

  @Override
  public boolean hasToken() {
    return aniListAPI.hasAniListToken();
  }

  @Override
  public void authenticate() {
    String url = aniListAPI.getAniListAuthUrl();

    var ui = getUI().orElse(UI.getCurrent());

    if (ui == null) {
      throw new IllegalStateException("No UI found");
    }

    ui.getPage().open(url);
  }

  @Override
  public Div getReadingSection() {
    // Only need to check in this method since all other methods get called after this one
    if (list == null) {
      if (!hasToken()) {
        throw new IllegalStateException("getReadingSection called without token");
      }

      list = aniListAPI.getMangaList();
    }

    var cards = getCards(list.reading());

    return getContentSection("Reading", cards);
  }

  @Override
  public Div getPlanToReadSection() {
    var cards = getCards(list.planToRead());

    return getContentSection("Plan to read", cards);
  }

  @Override
  public Div getCompletedSection() {
    var cards = getCards(list.completed());

    return getContentSection("Completed", cards);
  }

  @Override
  public Div getOnHoldSection() {
    var cards = getCards(list.onHold());

    return getContentSection("On hold", cards);
  }

  @Override
  public Div getDroppedSection() {
    var cards = getCards(list.dropped());

    return getContentSection("Dropped", cards);
  }

  /**
   * Returns a list of {@link AniListMediaCard} objects based on the provided list of {@link
   * AniListMedia} objects.
   *
   * @param media the list of {@link AniListMedia} objects to convert to cards
   * @return a list of {@link AniListMediaCard} objects
   */
  private List<AniListMediaCard> getCards(List<AniListMedia> media) {
    return media.stream().map(AniListMediaCard::new).toList();
  }
}
