/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.scroller.ExtensionScroller;
import online.hatsunemiku.tachideskvaadinui.services.ExtensionService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("extensions")
@CssImport("./css/views/extensions.css")
public class ExtensionsView extends StandardLayout {

  private static final Logger log = LoggerFactory.getLogger(ExtensionsView.class);

  public ExtensionsView(ExtensionService extensionsService, SettingsService settingsService) {
    super("Extensions");
    setClassName("extensions-view");
    VerticalLayout content = new VerticalLayout();

    ExtensionScroller extensionsList;
    try {
      extensionsList = new ExtensionScroller(extensionsService, settingsService);
    } catch (Exception e) {
      log.debug("ExtensionScroller couldn't be created", e);
      // redirect to settings
      UI ui = getUI().orElseGet(UI::getCurrent);

      if (ui == null) {
        log.error("Couldn't access UI", e);
        throw new RuntimeException("Couldn't access UI", e);
      }

      ui.navigate("settings#extensions");

      return;
    }

    TextField search = new TextField("Search");
    search.setPlaceholder("Search");
    search.setClearButtonVisible(true);

    search.addValueChangeListener(
        e -> {
          if (e.getValue().isEmpty()) {
            extensionsList.reset();
          }

          extensionsList.search(e.getValue());
        });

    content.add(search, extensionsList);

    setContent(content);
  }
}
