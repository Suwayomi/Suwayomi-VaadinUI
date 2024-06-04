/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view.source;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.combo.LangComboBox;
import online.hatsunemiku.tachideskvaadinui.component.events.source.SourceFilterUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.component.scroller.source.SourceScroller;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

/**
 * This class is used to create the view for the sources in the application.
 * It includes the functionality for filtering and displaying sources.
 *
 * @author aless2003
 * @version 1.1.0
 * @since 1.0.0
 */
@Route("sources")
@CssImport("./css/views/sources.css")
public class SourcesView extends StandardLayout {

  public SourcesView(SourceService sources, SettingsService settingsService) {
    super("Sources");

    VerticalLayout content = new VerticalLayout();

    HorizontalLayout filters = new HorizontalLayout();
    filters.addClassName("sources-filters");

    TextField nameFilter = new TextField("Search by name");
    nameFilter.setPlaceholder("LHTranslation");
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
    langFilter.addClassName("source-lang-filter");
    langFilter.setAllowCustomValue(false);
    langFilter.addLangUpdateEventListener(e -> {
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


    filters.add(nameFilter, langFilter);

    SourceScroller scroller = new SourceScroller(sources, settingsService);
    scroller.addLangUpdateEventListener(langFilter);

    content.add(filters, scroller);
    content.addClassName("sources-content");

    setContent(content);
  }
}
