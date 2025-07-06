/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.startup;

import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.Meta;
import online.hatsunemiku.tachideskvaadinui.data.server.event.ServerEventPublisher;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.UrlChangeEvent;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SuwayomiService;
import online.hatsunemiku.tachideskvaadinui.utils.BrowserUtils;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Is responsible for starting the Suwayomi server. Additionally, it checks if the server is running
 * and publishes an event if it is.
 */
@Service
@Slf4j
public class SuwayomiStarter {

  private static final Logger logger = LoggerFactory.getLogger(SuwayomiStarter.class);
  private final SettingsService settingsService;
  private final ServerEventPublisher serverEventPublisher;
  private final SuwayomiService suwayomiApi;
  private Process serverProcess;
  private ScheduledExecutorService serverChecker;
  private ScheduledExecutorService startChecker;

  /**
   * Creates a new instance of the {@link SuwayomiStarter} class.
   *
   * @param settingsService The {@link SettingsService} used for retrieving settings.
   * @param serverEventPublisher The {@link ServerEventPublisher} used for publishing server events
   *     to the application.
   * @param suwayomiApi The {@link SuwayomiService} used for checking if the server is running.
   */
  public SuwayomiStarter(
      SettingsService settingsService,
      ServerEventPublisher serverEventPublisher,
      SuwayomiService suwayomiApi) {
    this.settingsService = settingsService;
    this.serverEventPublisher = serverEventPublisher;
    this.suwayomiApi = suwayomiApi;
  }

  /**
   * Starts the Tachidesk server jar file within the specified project directory. Configures the
   * required data directory and Java version, and initiates the execution of the server jar file.
   *
   * @param projectDir The base directory of the project containing the jar file and configuration.
   * @see online.hatsunemiku.tachideskvaadinui.utils.PathUtils#getResolvedProjectPath(Environment)
   *     getResolvedProjectPath
   */
  public void startJar(File projectDir) {
    log.info("Starting Tachidesk Server Jar...");

    Meta meta = SerializationUtils.deserializeMetadata(projectDir.toPath());

    String jarLocation = meta.getJarLocation();

    if (jarLocation.isEmpty()) {
      logger.info("No jar location found");
      return;
    }

    File dataDirFile = new File(projectDir, "data");

    String dataDirFormat = "-Dsuwayomi.tachidesk.config.server.rootDir=%s";
    String dataDirArg = String.format(dataDirFormat, dataDirFile.getAbsolutePath());

    log.info("Checking for java installation...");
    boolean isJavaInstalled;
    try {
      Process process = Runtime.getRuntime().exec(new String[] {"java", "-version"});
      isJavaInstalled = process.waitFor() == 0;
    } catch (IOException | InterruptedException e) {
      log.error("Failed to check if java is installed");
      isJavaInstalled = false;
    }

    if (!isJavaInstalled) {
      String url =
          "https://github.com/aless2003/Tachidesk-VaadinUI/blob/master/Install%20Process.md#when-starting-its-stuck-on-waiting-for-server-to-start";
      try {
        BrowserUtils.openBrowser(url);
      } catch (IOException e) {
        log.error("Failed to open browser", e);
      }
      System.exit(-1); // skipcq JAVA-W0060 - Controlled exit, application can't run if Java isn't
      // installed.
      return;
    }

    log.info("Starting jar with data dir: {}", dataDirFile.getAbsolutePath());
    ProcessBuilder processBuilder = new ProcessBuilder("java", dataDirArg, "-jar", jarLocation);

    File logFile = new File(projectDir, "server.log");

    processBuilder.redirectOutput(logFile);

    try {
      serverProcess = processBuilder.start();
      log.info("Started Jar");
      startServerCheck();
      Runtime.getRuntime().addShutdownHook(new Thread(this::stopJar));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Initializes the server checking mechanism and starts periodic tasks to monitor the server's
   * status.
   *
   * <p>The `startChecker` service executes the {@code checkIfServerIsRunning} method every 5
   * seconds starting immediately. This approach ensures that the server's running status is checked
   * frequently and events are published promptly when the server is detected to be operational.
   */
  private void startServerCheck() {
    serverChecker = Executors.newSingleThreadScheduledExecutor();
    startChecker = Executors.newSingleThreadScheduledExecutor();

    // skipcq: JAVA-W1087
    startChecker.scheduleAtFixedRate(this::checkIfServerIsRunning, 0, 5, TimeUnit.SECONDS);
  }

  /**
   * Stops the currently running server process if it exists and shuts down any associated scheduled
   * tasks. This method is executed automatically when the application is shutting down due to the
   * {@link PreDestroy} annotation.
   *
   * <p>If a server process was started by the application, it will attempt to terminate it
   * gracefully. If the server process does not support normal termination, it will be forcibly
   * terminated.
   *
   * <p>Also ensures that any active server health-checking thread pools are properly shut down.
   */
  @PreDestroy
  public void stopJar() {
    log.info("Stopping Jar");

    if (serverChecker != null) {
      serverChecker.shutdownNow();
    }

    if (serverProcess == null) {
      return;
    }

    if (serverProcess.supportsNormalTermination()) {
      serverProcess.destroy();
    } else {
      serverProcess.destroyForcibly();
    }
  }

  /**
   * Handles the URL change event triggered when the application's URL is modified. If the new URL
   * differs from the default URL in the settings, it stops the server process, as that means the
   * User intends to use his own Server instance.
   *
   * @param event An instance of {@link UrlChangeEvent}.
   */
  @EventListener(UrlChangeEvent.class)
  public void onUrlChange(UrlChangeEvent event) {
    String newUrl = event.getUrl();

    log.info("Url changed to {}", newUrl);

    Settings defaults = settingsService.getDefaults();

    if (!newUrl.equals(defaults.getUrl())) {
      stopJar();
    }
  }

  /**
   * Checks if the Server is running, if so publishes a {@link
   * online.hatsunemiku.tachideskvaadinui.data.server.event.ServerStartedEvent ServerStartedEvent},
   * shuts down the current {@link #startChecker} instance and sets it to {@code null}.
   */
  private void checkIfServerIsRunning() {
    if (Thread.interrupted()) {
      return;
    }

    if (!checkServerConnection()) {
      return;
    }

    logger.info("Server is running");
    serverEventPublisher.publishServerStartedEvent();
    startChecker.shutdownNow();
    startChecker = null;
  }

  /**
   * Checks if the server is running. It both checks if the server process exists and if the server
   * is reachable and gives a valid response.
   *
   * @return {@code true} if the server is running, {@code false} otherwise.
   */
  private boolean checkServerConnection() {
    if (serverProcess == null) {
      throw new RuntimeException("Server process is null");
    }

    try {
      var optional = suwayomiApi.getServerVersion();

      if (optional.isEmpty()) {
        return false;
      }

      var version = optional.get();

      logger.info(
          "Server version: Major={},Minor={},Patch={} with Revision={}",
          version.getMajorVersion(),
          version.getMinorVersion(),
          version.getPatchVersion(),
          version.getRevisionNumber());
      return true;
    } catch (Exception e) {
      logger.info("Server not running", e);
      return false;
    }
  }
}
