/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.card.AniListMediaCard;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMedia;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.MangaList;
import online.hatsunemiku.tachideskvaadinui.services.tracker.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

@Route("anilist")
@CssImport("./css/views/anilist.css")
public class AniListView extends StandardLayout {
  public AniListView(AniListAPIService apiService) {
    super("AniList");

    addClassName("anilist-view");

    if (!apiService.hasAniListToken()) {

      Div content = new Div();
      content.addClassName("button-container");

      Button authBtn = new Button("Authenticate");
      authBtn.addClickListener(
          e -> {
            String url = apiService.getAniListAuthUrl();
            getUI().ifPresent(ui -> ui.getPage().open(url));
          });

      content.add(authBtn);
      setContent(content);
      return;
    }

    MangaList list = apiService.getMangaList();

    VerticalLayout content = new VerticalLayout();

    var reading = getContentSection("Reading", list.reading());
    var planToRead = getContentSection("Plan to read", list.planToRead());
    var completed = getContentSection("Completed", list.completed());
    var onHold = getContentSection("On hold", list.onHold());
    var dropped = getContentSection("Dropped", list.dropped());

    content.add(reading, planToRead, completed, onHold, dropped);

    setContent(content);
  }

  private Div getContentSection(String title, List<AniListMedia> media) {
    Div section = new Div();
    section.addClassName("anilist-content");

    Div titleSection = new Div();
    titleSection.addClassName("anilist-title-section");
    titleSection.setText(title);

    Div contentGrid = new Div();
    contentGrid.addClassName("anilist-content-grid");

    for (AniListMedia manga : media) {
      contentGrid.add(new AniListMediaCard(manga));
    }

    section.add(titleSection, contentGrid);
    return section;
  }
}
