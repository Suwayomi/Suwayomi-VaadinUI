/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.startup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.Meta;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.UrlChangeEvent;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.startup.download.ReadableProgressByteChannel;
import online.hatsunemiku.tachideskvaadinui.utils.PathUtils;
import online.hatsunemiku.tachideskvaadinui.utils.ProfileUtils;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import online.hatsunemiku.tachideskvaadinui.utils.TachideskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class TachideskMaintainer {

  private static final Logger logger = LoggerFactory.getLogger(TachideskMaintainer.class);
  private final RestTemplate client;
  private final TachideskStarter starter;
  private final SettingsService settingsService;
  private static File serverDir;
  private final File projectDir;
  @Getter private boolean updating = false;
  @Getter private double progress = 0;

  public TachideskMaintainer(
      RestTemplate client,
      TachideskStarter starter,
      SettingsService settingsService,
      Environment env) {
    this.client = client;
    this.starter = starter;
    this.settingsService = settingsService;

    if (ProfileUtils.isDev(env)) {
      projectDir = PathUtils.getDevDir().toFile();
    } else {
      projectDir = PathUtils.getProjectDir().toFile();
    }

    serverDir = new File(projectDir, "server");
  }

  @EventListener({ApplicationReadyEvent.class, UrlChangeEvent.class})
  @Async
  public void start() {
    logger.info("Starting Tachidesk...");

    Settings settings = settingsService.getSettings();
    Settings defaultSettings = settingsService.getDefaults();

    if (!settings.getUrl().equals(defaultSettings.getUrl())) {
      return;
    }

    if (!checkProjectDir()) {
      return;
    }

    if (!checkServerDir()) {
      return;
    }

    checkServerConfig();

    logger.info("Checking for updates...");

    Meta oldServer = SerializationUtils.deserializeMetadata(projectDir.toPath());

    if (!oldServer.getJarLocation().isEmpty()) {
      new Thread(this::startup).start();
    }

    logger.info("Current jar file: {}", oldServer.getJarName());

    String jarUrl;
    try {
      jarUrl = TachideskUtils.getNewestJarUrl(client);
    } catch (Exception e) {
      logger.error("Failed to check for updates", e);
      return;
    }

    if (jarUrl == null) {
      logger.info("No new version found");
      return;
    }

    Optional<Meta> newMeta = TachideskUtils.getMetaFromUrl(jarUrl);

    if (newMeta.isEmpty()) {
      logger.info("No new version found");
      return;
    }

    Meta newServerMeta = newMeta.get();

    if (checkMeta(oldServer, newServerMeta) && checkServer(oldServer)) {
      logger.info("No new version found");
      return;
    }

    File serverFile = new File(serverDir, newServerMeta.getJarName());

    try {
      logger.info("Downloading new version...");
      downloadServerFile(jarUrl, serverFile);
    } catch (Exception e) {
      logger.error("Failed to download new version", e);
      return;
    }

    logger.info("New version downloaded");

    starter.stopJar();

    deleteOldServerFile(oldServer);

    oldServer.setJarName(newServerMeta.getJarName());
    oldServer.setJarVersion(newServerMeta.getJarVersion());
    oldServer.setJarRevision(newServerMeta.getJarRevision());
    oldServer.setJarLocation(serverFile.getPath());

    SerializationUtils.serializeMetadata(oldServer, projectDir.toPath());

    logger.info("Restarting server...");
    starter.startJar(projectDir);

    logger.info("Update complete");
  }

  /**
   * Checks if the project directory exists and creates it if it does not.
   *
   * @return {@code true} if the project directory exists or was successfully created, {@code false}
   *     otherwise.
   */
  private boolean checkProjectDir() {
    if (!projectDir.exists()) {
      if (!projectDir.mkdir()) {
        log.error("Failed to create project directory");
        return false;
      }
    }

    return true;
  }

  private void checkServerConfig() {
    log.info("Checking for config File...");

    File dataDir = new File(projectDir, "data");

    if (!dataDir.exists()) {
      if (!dataDir.mkdir()) {
        log.error("Failed to create data directory");
        throw new RuntimeException("Failed to create data directory");
      }
    }

    File config = new File(dataDir, "server.conf");

    if (!config.exists()) {
      try {
        Resource defaultConfig = new ClassPathResource("server/server.conf");

        Files.copy(defaultConfig.getInputStream(), config.toPath());
      } catch (FileNotFoundException e) {
        log.error("Default config not found", e);
        throw new RuntimeException(e);
      } catch (IOException e) {
        log.error("Failed to copy default config", e);
        throw new RuntimeException(e);
      }
    }
  }

  private static void deleteOldServerFile(Meta oldServer) {

    if (!oldServer.getJarLocation().isEmpty()) {
      return;
    }

    File oldServerFile = new File(serverDir, oldServer.getJarName());

    if (!oldServerFile.exists()) {
      return;
    }

    if (oldServerFile.delete()) {
      return;
    }

    logger.info("Failed to delete old version");
    oldServerFile.deleteOnExit();
  }

  private void downloadServerFile(String jarUrl, File serverFile) throws IOException {
    updating = true;
    URL url = new URL(jarUrl);

    URLConnection connection = url.openConnection();
    int size = connection.getContentLength();

    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
    var progressChannel =
        new ReadableProgressByteChannel(rbc, read -> this.progress = (double) read / size);
    try (FileOutputStream fos = new FileOutputStream(serverFile)) {
      fos.getChannel().transferFrom(progressChannel, 0, Long.MAX_VALUE);
    }
    updating = false;
  }

  /**
   * Checks if the server directory exists and creates it if it doesn't
   *
   * @return True if the server directory exists or was created successfully, false otherwise
   */
  private boolean checkServerDir() {
    if (!serverDir.exists()) {
      if (!serverDir.mkdir()) {
        logger.error("Failed to create server directory");
        return false;
      }
    }
    return true;
  }

  private boolean checkMeta(Meta oldServer, Meta newServer) {

    boolean matchingVersion = oldServer.getJarVersion().equals(newServer.getJarVersion());

    if (!matchingVersion) {
      logger.info("New version {} found", newServer.getJarVersion());
    }

    boolean matchingRevision = oldServer.getJarRevision().equals(newServer.getJarRevision());

    if (!matchingRevision) {
      logger.info("New revision {}", newServer.getJarRevision());
    }

    return matchingVersion && matchingRevision;
  }

  private boolean checkServer(Meta existing) {

    var serverDir = new File(projectDir, "server");
    File serverFile = new File(serverDir, existing.getJarName());

    if (!serverFile.exists()) {
      logger.info("Server file not found");
      return false;
    }

    return true;
  }

  private void startup() {
    starter.startJar(projectDir);
  }
}
