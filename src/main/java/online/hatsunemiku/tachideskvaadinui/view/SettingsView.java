/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.SettingsEventPublisher;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.apache.commons.validator.routines.UrlValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.client.ResourceAccessException;
import org.vaadin.miki.superfields.checkbox.SuperCheckbox;
import org.vaadin.miki.superfields.text.SuperTextField;

@Route("settings")
@CssImport("./css/views/settings-view.css")
public class SettingsView extends StandardLayout {

  private static final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
  private final SettingsEventPublisher settingsEventPublisher;

  public SettingsView(
      SettingsService settingsService,
      SettingsEventPublisher eventPublisher,
      SourceService sourceService) {
    super("Settings");
    setClassName("settings-view");

    this.settingsEventPublisher = eventPublisher;

    FormLayout content = new FormLayout();
    content.setClassName("settings-content");

    Binder<Settings> binder = new Binder<>(Settings.class);
    SuperTextField urlField = createUrlFieldWithValidation(binder);
    var defaultSearchLangField = createSearchLangField(sourceService, binder);

    Div checkboxContainer = new Div();
    checkboxContainer.addClassName("checkbox-container");

    SuperCheckbox checkbox = new SuperCheckbox().withLabel("Startup Popup").withId("start-popup");
    checkbox.setValue(settingsService.getSettings().isStartPopup());
    binder.forField(checkbox).bind(Settings::isStartPopup, Settings::setStartPopup);

    checkboxContainer.add(checkbox);

    binder.setBean(settingsService.getSettings());

    content.add(urlField, defaultSearchLangField);
    content.add(checkboxContainer, 2);

    setContent(content);
  }

  private ComboBox<String> createSearchLangField(
      SourceService sourceService, Binder<Settings> binder) {
    ComboBox<String> defaultSearchLang = new ComboBox<>("Default Search Language");
    defaultSearchLang.setAllowCustomValue(false);

    List<Source> sources;
    try {
      sources = sourceService.getSources();
    } catch (ResourceAccessException e) {
      defaultSearchLang.setReadOnly(true);
      defaultSearchLang.setItems("Not available, because server is not running");
      defaultSearchLang.setValue("Not available, because server is not running");
      return defaultSearchLang;
    }
    var langs = new ArrayList<>(sources.stream().map(Source::getLang).distinct().toList());
    defaultSearchLang.setItems(langs);

    binder
        .forField(defaultSearchLang)
        .withValidator(
            (lang, context) -> {
              if (lang == null || lang.isEmpty()) {
                return ValidationResult.error("Default Search Language cannot be empty");
              }

              if (lang.equals("Loading...")) {
                return ValidationResult.error("Default Search Language cannot be Loading...");
              }

              return ValidationResult.ok();
            })
        .bind(Settings::getDefaultSearchLang, Settings::setDefaultSearchLang);

    return defaultSearchLang;
  }

  @NotNull
  private SuperTextField createUrlFieldWithValidation(Binder<Settings> binder) {
    SuperTextField urlField = new SuperTextField("URL");
    binder
        .forField(urlField)
        .withValidator(
            (url, context) -> {
              if (url == null || url.isEmpty()) {
                return ValidationResult.error("URL cannot be empty");
              }

              if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return ValidationResult.error("URL must start with http:// or https://");
              }

              String urlWithoutPort = url.split(":\\d+")[0];

              if (!urlValidator.isValid(urlWithoutPort)) {
                return ValidationResult.error("URL is not valid");
              }

              return ValidationResult.ok();
            })
        .bind(
            Settings::getUrl,
            (settings, url) -> {
              settings.setUrl(url);
              settingsEventPublisher.publishUrlChangeEvent(this, url);
            });
    return urlField;
  }
}
