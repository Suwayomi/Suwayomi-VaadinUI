/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.context.annotation.Configuration;

/**
 * Vaadin configuration class. <br>
 * Configures the Vaadin UI with the Miku theme and PWA settings.
 *
 * @since 1.10.0
 * @version 1.12.0
 */
@Configuration
@Push
@Theme("miku")
@PWA(name = "Suwayomi VaadinUI", shortName = "VaadinUI")
public class VaadinConfig implements AppShellConfigurator {

  @Override
  public void configurePage(AppShellSettings settings) {
    AppShellConfigurator.super.configurePage(settings);
    settings.addFavIcon("icon", "icons/favicon-32x32.png", "32x32");
    settings.addFavIcon("icon", "icons/favicon-144x144.png", "144x144");
    settings.addFavIcon("shortcut icon", "icons/favicon.ico", "256x256");
  }
}
