/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
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
public class VaadinConfig implements AppShellConfigurator {}
