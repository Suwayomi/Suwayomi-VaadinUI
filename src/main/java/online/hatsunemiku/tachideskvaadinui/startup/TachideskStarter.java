/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.startup;

import jakarta.annotation.PreDestroy;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.Meta;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.UrlChangeEvent;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.utils.BrowserUtils;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.swing.*;

@Service
@Slf4j
public class TachideskStarter {

  private static final Logger logger = LoggerFactory.getLogger(TachideskStarter.class);
  private Process serverProcess;
  private ScheduledExecutorService serverChecker;
  private final SettingsService settingsService;

  public TachideskStarter(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

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
      System.exit(-1);
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

  private void startServerCheck() {
    serverChecker = Executors.newSingleThreadScheduledExecutor();
  }

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

  @EventListener(UrlChangeEvent.class)
  public void onUrlChange(UrlChangeEvent event) {
    String newUrl = event.getUrl();

    log.info("Url changed to {}", newUrl);

    Settings defaults = settingsService.getDefaults();

    if (!newUrl.equals(defaults.getUrl())) {
      stopJar();
    }
  }
}
