/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view.source;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.scroller.source.ExploreType;
import online.hatsunemiku.tachideskvaadinui.component.scroller.source.SourceExploreScroller;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

@Route("source/explore/:id(\\d+)")
@CssImport("./css/views/source-explore.css")
public class SourceExploreView extends StandardLayout
    implements BeforeEnterObserver, BeforeLeaveObserver {

  private final SourceService sourceService;
  private SourceExploreScroller scroller;
  private final SettingsService settingsService;

  public SourceExploreView(SourceService sourceService, SettingsService settingsService) {
    super("Source Explore");
    this.sourceService = sourceService;
    this.settingsService = settingsService;
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    var params = event.getRouteParameters();

    var id = params.get("id");

    if (id.isEmpty()) {
      event.rerouteToError(NotFoundException.class, "Source not found");
      return;
    }

    String sourceId;
    try {
      Long.parseLong(id.get());
      sourceId = id.get();
    } catch (Exception e) {
      event.rerouteToError(NotFoundException.class, "Source not found");
      return;
    }

    fullScreenNoHide();
    UI.getCurrent()
        .access(
            () -> UI.getCurrent().getPage().executeJs("document.body.style.overflow = 'hidden';"));

    setContent(content(sourceId));
  }

  private Div content(String sourceId) {
    Div content = new Div();
    content.addClassName("source-explore-container");

    Div buttons = new Div();
    buttons.addClassName("source-explore-buttons");

    Button popular = new Button("Popular");
    Button latest = new Button("Latest");

    latest.addClickListener(
        e -> {
          if (scroller.getType() == ExploreType.LATEST) {
            return;
          }

          switchOutScroller(content, ExploreType.LATEST, sourceId);

          disableButton(latest);
          enableButton(popular);
        });

    popular.addClickListener(
        e -> {
          if (scroller.getType() == ExploreType.POPULAR) {
            return;
          }

          switchOutScroller(content, ExploreType.POPULAR, sourceId);

          disableButton(popular);
          enableButton(latest);
        });

    buttons.add(popular, latest);

    scroller =
        new SourceExploreScroller(sourceService, ExploreType.POPULAR, sourceId, settingsService);

    content.add(buttons, scroller);

    disableButton(popular);

    return content;
  }

  private void disableButton(Button button) {
    button.setEnabled(false);
  }

  private void enableButton(Button button) {
    button.setEnabled(true);
  }

  private void switchOutScroller(Div content, ExploreType type, String sourceId) {
    content.remove(content.getComponentAt(1));
    scroller = new SourceExploreScroller(sourceService, type, sourceId, settingsService);
    content.add(scroller);
  }

  @Override
  public void beforeLeave(BeforeLeaveEvent event) {
    UI.getCurrent()
        .access(
            () -> UI.getCurrent().getPage().executeJs("document.body.style.overflow = 'auto';"));
  }
}
