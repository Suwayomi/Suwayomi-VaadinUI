/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view.source;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.combo.LangComboBox;
import online.hatsunemiku.tachideskvaadinui.component.events.source.SourceFilterUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.component.scroller.source.SourceScroller;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

/**
 * This class is used to create the view for the sources in the application. It includes the
 * functionality for filtering and displaying sources.
 *
 * @author aless2003
 * @version 1.1.0
 * @since 1.0.0
 */
@Route("sources")
@CssImport("./css/views/sources.css")
public class SourcesView extends StandardLayout {

  /**
   * Creates a new SourcesView object with the given sources and settings service.
   *
   * @param sources The {@link SourceService} to use to get the sources
   * @param settingsService The {@link SettingsService} to use to get the settings
   */
  public SourcesView(SourceService sources, SettingsService settingsService) {
    super("Discover");

    fullScreenNoHide();
    addClassName("library-screen");

    Div hero = new Div();
    hero.setClassName("manager-hero");

    Div left = new Div();
    left.setClassName("manager-hero-left");
    H2 title = new H2("Discovery");
    Span subtitle = new Span("Explore and discover new manga from various sources.");
    subtitle.setClassName("manager-subtitle");
    left.add(title, subtitle);

    Div right = new Div();
    right.setClassName("manager-hero-right");

    TextField nameFilter = new TextField();
    nameFilter.setPlaceholder("Search sources...");
    nameFilter.addClassName("manager-search");
    nameFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.addValueChangeListener(
        e -> {
          String filterText = e.getValue();
          if (filterText == null) {
            return;
          }

          SourceFilterUpdateEvent event = new SourceFilterUpdateEvent(nameFilter, filterText);
          ComponentUtil.fireEvent(UI.getCurrent(), event);
        });

    LangComboBox langFilter = new LangComboBox();
    langFilter.addClassName("source-lang-filter-discovery");
    langFilter.setAllowCustomValue(false);
    langFilter.addLangUpdateEventListener(
        e -> {
          if (langFilter.getValue() != null) {
            return null;
          }

          Settings settings = settingsService.getSettings();
          var sourceLang = settings.getDefaultSourceLang();
          if (sourceLang != null) {
            langFilter.setValue(sourceLang);
          }

          return null;
        });
    
    right.add(nameFilter, langFilter);
    hero.add(left, right);

    SourceScroller scroller = new SourceScroller(sources, settingsService);
    scroller.addLangUpdateEventListener(langFilter);

    Div container = new Div(hero, scroller);
    container.setClassName("library-view-container");
    container.setSizeFull();
    
    setContent(container);
  }
}
