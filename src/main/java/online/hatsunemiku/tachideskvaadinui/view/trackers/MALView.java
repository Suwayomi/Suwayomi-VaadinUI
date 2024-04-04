/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view.trackers;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import dev.katsute.mal4j.manga.Manga;
import dev.katsute.mal4j.manga.property.MangaStatus;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.card.MalMediaCard;
import online.hatsunemiku.tachideskvaadinui.services.tracker.MyAnimeListAPIService;
import online.hatsunemiku.tachideskvaadinui.view.layout.TrackingLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("mal")
public class MALView extends TrackingLayout {

  private static final Logger log = LoggerFactory.getLogger(MALView.class);
  private final MyAnimeListAPIService malAPI;

  /**
   * MALView is a view for displaying MyAnimeList entries to import.
   *
   * @param malAPI An instance of the {@link MyAnimeListAPIService} class
   */
  public MALView(MyAnimeListAPIService malAPI) {
    super("MyAnimeList");

    this.malAPI = malAPI;

    init();
  }

  @Override
  public boolean hasToken() {
    return malAPI.hasMalToken();
  }

  @Override
  public void authenticate() {
    var auth = malAPI.getAuthUrl();
    var ui = getUI().orElse(UI.getCurrent());

    if (ui == null) {
      log.debug("Couldn't authenticate MAL: UI is null");
      return;
    }

    ui.getPage().open(auth);
  }

  @Override
  public Div getReadingSection() {
    var list = malAPI.getMangaWithStatus(MangaStatus.Reading);

    var cards = getCards(list);

    return getContentSection("Reading", cards);
  }

  @Override
  public Div getPlanToReadSection() {
    var list = malAPI.getMangaWithStatus(MangaStatus.PlanToRead);

    var cards = getCards(list);

    return getContentSection("Plan to Read", cards);
  }

  @Override
  public Div getCompletedSection() {
    var list = malAPI.getMangaWithStatus(MangaStatus.Completed);

    var cards = getCards(list);

    return getContentSection("Completed", cards);
  }

  @Override
  public Div getOnHoldSection() {
    var list = malAPI.getMangaWithStatus(MangaStatus.OnHold);

    var cards = getCards(list);

    return getContentSection("On Hold", cards);
  }

  @Override
  public Div getDroppedSection() {
    var list = malAPI.getMangaWithStatus(MangaStatus.Dropped);

    var cards = getCards(list);

    return getContentSection("Dropped", cards);
  }

  private List<MalMediaCard> getCards(List<Manga> media) {
    return media.stream().map(MalMediaCard::new).toList();
  }
}
