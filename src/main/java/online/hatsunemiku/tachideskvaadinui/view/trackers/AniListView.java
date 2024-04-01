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

@Route("anilist")
@CssImport("./css/views/anilist.css")
public class AniListView extends TrackingLayout {

  private final AniListAPIService aniListAPI;
  private final MangaList list;

  public AniListView(AniListAPIService apiService) {
    super("AniList");

    addClassName("anilist-view");

    this.aniListAPI = apiService;
    this.list = apiService.getMangaList();

    init();
  }

  private Div getContentSection(String title, List<AniListMedia> media) {
    Div section = new Div();
    section.addClassName("import-content");

    Div titleSection = new Div();
    titleSection.addClassName("import-title-section");
    titleSection.setText(title);

    Div contentGrid = new Div();
    contentGrid.addClassName("import-content-grid");

    for (AniListMedia manga : media) {
      contentGrid.add(new AniListMediaCard(manga));
    }

    section.add(titleSection, contentGrid);
    return section;
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
    return getContentSection("Reading", list.reading());
  }

  @Override
  public Div getPlanToReadSection() {
    return getContentSection("Plan to read", list.planToRead());
  }

  @Override
  public Div getCompletedSection() {
    return getContentSection("Completed", list.completed());
  }

  @Override
  public Div getOnHoldSection() {
    return getContentSection("On hold", list.onHold());
  }

  @Override
  public Div getDroppedSection() {
    return getContentSection("Dropped", list.dropped());
  }


}
