package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.SettingsEventPublisher;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.apache.commons.validator.routines.UrlValidator;
import org.vaadin.miki.superfields.text.SuperTextField;

@Route("settings")
@CssImport("./css/views/settings-view.css")
public class SettingsView extends StandardLayout {

  private static final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
  private final SettingsEventPublisher settingsEventPublisher;

  public SettingsView(SettingsService settingsService, SettingsEventPublisher eventPublisher) {
    super("Settings");
    setClassName("settings-view");

    this.settingsEventPublisher = eventPublisher;

    FormLayout content = new FormLayout();
    content.setClassName("settings-content");

    SuperTextField urlField = new SuperTextField("URL");
    Binder<Settings> binder = new Binder<>(Settings.class);
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
        .bind(Settings::getUrl, (settings, url) -> {
          settings.setUrl(url);
          settingsEventPublisher.publishUrlChangeEvent(this, url);
        });

    binder.setBean(settingsService.getSettings());

    content.add(urlField);

    setContent(content);
  }
}
