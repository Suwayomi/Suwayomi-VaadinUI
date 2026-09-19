/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view.source;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.scroller.source.ExploreType;
import online.hatsunemiku.tachideskvaadinui.component.scroller.source.SourceExploreScroller;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

@Route("source/explore/:id(\\d+)")
public class SourceExploreView extends StandardLayout implements BeforeEnterObserver {

  private final SourceService sourceService;
  private SourceExploreScroller scroller;
  private Div scrollerHost;
  private H2 heading;
  private Span subtitle;
  private final SettingsService settingsService;

  public SourceExploreView(SourceService sourceService, SettingsService settingsService) {
    super("Source Explore");
    this.sourceService = sourceService;
    this.settingsService = settingsService;
    addClassNames("source-explore-screen", "library-screen");
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
    setContent(content(sourceId));
  }

  private Div content(String sourceId) {
    Div content = new Div();
    content.addClassName("source-explore-container");
    content.setSizeFull();

    Div hero = new Div();
    hero.addClassName("source-explore-hero");

    Div heroLeft = new Div();
    heroLeft.addClassName("source-explore-hero-left");
    heading = new H2("Popular Updates");
    subtitle = new Span("The latest releases and most read titles in your network.");
    subtitle.addClassName("source-explore-subtitle");
    heroLeft.add(heading, subtitle);

    Div modeSwitcher = new Div();
    modeSwitcher.addClassName("source-explore-buttons");

    Button popular = new Button("Popular");
    Button latest = new Button("Latest");
    popular.addClassName("source-explore-mode");
    latest.addClassName("source-explore-mode");

    latest.addClickListener(
        e -> {
          if (scroller.getType() == ExploreType.LATEST) {
            return;
          }

          switchOutScroller(ExploreType.LATEST, sourceId);
          setActiveMode(popular, latest);
          setHeader(ExploreType.LATEST);
        });

    popular.addClickListener(
        e -> {
          if (scroller.getType() == ExploreType.POPULAR) {
            return;
          }

          switchOutScroller(ExploreType.POPULAR, sourceId);
          setActiveMode(latest, popular);
          setHeader(ExploreType.POPULAR);
        });

    modeSwitcher.add(popular, latest);
    hero.add(heroLeft, modeSwitcher);

    scrollerHost = new Div();
    scrollerHost.addClassName("source-explore-scroller-host");
    scrollerHost.setSizeFull();

    scroller =
        new SourceExploreScroller(sourceService, ExploreType.POPULAR, sourceId, settingsService);
    scroller.setSizeFull();

    scrollerHost.add(scroller);
    content.add(hero, scrollerHost);

    setHeader(ExploreType.POPULAR);
    setActiveMode(latest, popular);

    return content;
  }

  private void setActiveMode(Button inactiveButton, Button activeButton) {
    inactiveButton.removeClassName("active");
    activeButton.addClassName("active");
  }

  private void setHeader(ExploreType type) {
    if (type == ExploreType.LATEST) {
      heading.setText("Latest Updates");
      subtitle.setText("Fresh chapter drops and recently updated titles.");
      return;
    }

    heading.setText("Popular Updates");
    subtitle.setText("The latest releases and most read titles in your network.");
  }

  private void switchOutScroller(ExploreType type, String sourceId) {
    scrollerHost.removeAll();
    scroller = new SourceExploreScroller(sourceService, type, sourceId, settingsService);
    scroller.setSizeFull();
    scrollerHost.add(scroller);
  }
}
