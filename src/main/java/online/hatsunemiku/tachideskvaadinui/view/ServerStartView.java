package online.hatsunemiku.tachideskvaadinui.view;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.startup.TachideskMaintainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.vaadin.firitin.components.progressbar.VProgressBar;

@Route
@CssImport("./css/serverstart.css")
public class ServerStartView extends VerticalLayout {

  private static final Logger logger = LoggerFactory.getLogger(ServerStartView.class);
  ScheduledExecutorService executor;
  private final RestTemplate client;
  private final TachideskMaintainer maintainer;
  private final SettingsService settingsService;
  private final Div updateNotice;
  private final ProgressBar progress;
  private final Div downloadText;

  public ServerStartView(
      RestTemplate client, TachideskMaintainer maintainer, SettingsService settingsService) {

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

    add(head, progressContainer);
  }

  private void update() {
    checkConnection();
    updateUi();
  }

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
      ui.access(() -> {
        updateNotice.setVisible(true);
        progress.setValue(maintainer.getProgress());
        downloadText.setText(updateText);
        progress.setIndeterminate(false);
      });
    } else {
      ui.access(() -> {
        updateNotice.setVisible(false);
        downloadText.setVisible(false);
        progress.setIndeterminate(true);
      });
    }
  }

  private void checkConnection() {
    String url = settingsService.getSettings().getUrl() + "/api/v1/meta";

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
    } catch (Exception e) {
      logger.debug("No Connection to Server yet", e);
    }
  }
}
