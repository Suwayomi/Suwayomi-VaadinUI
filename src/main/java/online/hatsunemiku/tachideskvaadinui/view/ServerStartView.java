/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.startup.SuwayomiMaintainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.vaadin.firitin.components.progressbar.VProgressBar;

/**
 * The {@code ServerStartView} class represents the UI displayed while waiting for the server to
 * start. This view includes progress indicators, update notifications, and a settings button for
 * navigation to the {@link SettingsView}.
 */
@Route
@CssImport("./css/serverstart.css")
public class ServerStartView extends VerticalLayout {

  private static final Logger logger = LoggerFactory.getLogger(ServerStartView.class);
  private final RestTemplate client;
  private final SuwayomiMaintainer maintainer;
  private final SettingsService settingsService;
  private final Div updateNotice;
  private final ProgressBar progress;
  private final Div downloadText;
  private final ScheduledExecutorService executor;
  private Instant countdown;
  private boolean hasSentNotification = false;

  public ServerStartView(
      RestTemplate client, SuwayomiMaintainer maintainer, SettingsService settingsService) {

    this.client = client;
    this.settingsService = settingsService;
    this.executor = Executors.newSingleThreadScheduledExecutor();
    this.maintainer = maintainer;

    executor.scheduleAtFixedRate(this::update, 500, 500, MILLISECONDS);
    setId("server-start-view");

    Div progressContainer = new Div();
    progressContainer.setClassName("waiting-progress-container");

    progress = new VProgressBar();
    progress.setClassName("waiting-bar");

    downloadText = new Div();
    downloadText.setClassName("waiting-label");

    progressContainer.add(progress, downloadText);

    progress.setIndeterminate(true);
    progress.setWidth(90, Unit.PERCENTAGE);

    Div head = new Div();
    head.setClassName("waiting-head");

    H1 title = new H1("Waiting for Server to start up");
    title.setClassName("waiting-title");

    updateNotice = new Div();
    updateNotice.setText("Updating to new Version");
    updateNotice.setClassName("waiting-update");

    head.add(title, updateNotice);

    Button settingsBtn = new Button(VaadinIcon.COG.create());
    settingsBtn.addClickListener(
        e -> {
          UI ui = UI.getCurrent();
          ui.navigate(SettingsView.class);
        });

    settingsBtn.setClassName("waiting-settings-btn");
    add(head, progressContainer, settingsBtn);
  }

  private void update() {
    checkConnection();
    updateUi();
  }

  /**
   * Updates the UI based on the status of the {@link SuwayomiMaintainer}.
   */
  private void updateUi() {
    boolean updating = maintainer.isUpdating();

    var possibleUI = getUI();

    if (possibleUI.isEmpty()) {
      return;
    }

    UI ui = possibleUI.get();

    if (updating) {
      double progressPercent = maintainer.getProgress() * 100;

      String updateText = "%.2f%%".formatted(progressPercent);
      countdown = null;
      ui.access(
          () -> {
            updateNotice.setVisible(true);
            progress.setValue(maintainer.getProgress());
            downloadText.setText(updateText);
            progress.setIndeterminate(false);
          });
    } else {
      if (countdown == null && !hasSentNotification) {
        countdown = Instant.now().plusSeconds(60);
      }
      ui.access(
          () -> {
            updateNotice.setVisible(false);
            downloadText.setVisible(false);
            progress.setIndeterminate(true);
            if (countdown != null && countdown.isBefore(Instant.now())) {
              Notification notification = new Notification();
              Div text = new Div(
                  "Server didn't start up in time or hasn't started downloading at all, please submit an issue on GitHub if you see this.");
              Anchor anchor = new Anchor("https://github.com/Suwayomi/Suwayomi-VaadinUI/issues",
                  "Submit Issue");
              anchor.getStyle().set("color", "#7FFFD4");
              anchor.getStyle().set("textDecoration", "underline");
              anchor.getStyle().set("background-color", "#80808080");
              anchor.getStyle().set("border-radius", "5px");
              text.getStyle().set("textAlign", "center");
              anchor.getStyle().set("textAlign", "center");

              notification.add(text, anchor);
              notification.addThemeVariants(LUMO_ERROR);
              notification.open();
              hasSentNotification = true;
              countdown = null;
            }
          });
    }
  }

  private void checkConnection() {
    String url = settingsService.getSettings().getUrl() + "/api/v1/settings/about";

    try {
      var response = client.getForEntity(url, Void.class);

      if (response.getStatusCode().is2xxSuccessful()) {

        Optional<UI> optUi = getUI();

        if (optUi.isEmpty()) {
          logger.error("Couldn't access UI");
          executor.shutdownNow();
          return;
        }

        UI ui = optUi.get();
        ui.access(() -> ui.navigate(RootView.class));

        executor.shutdownNow();
      }
    } catch (ResourceAccessException e) {
      logger.debug("No Connection to Server yet", e);
    }
  }
}
