package online.hatsunemiku.tachideskvaadinui.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.Meta;
import online.hatsunemiku.tachideskvaadinui.startup.download.ReadableProgressByteChannel;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import online.hatsunemiku.tachideskvaadinui.utils.TachideskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TachideskMaintainer {

  private static final Logger logger = LoggerFactory.getLogger(TachideskMaintainer.class);
  private final RestTemplate client;
  private final TachideskStarter starter;
  @Getter private boolean updating = false;
  @Getter private double progress = 0;

  public TachideskMaintainer(RestTemplate client, TachideskStarter starter) {
    this.client = client;
    this.starter = starter;
  }

  @EventListener(ApplicationReadyEvent.class)
  @Async
  public void start() {
    logger.info("Starting Tachidesk...");
    new Thread(this::startup).start();

    logger.info("Checking for updates...");

    Meta oldServer = SerializationUtils.deseralizeMetadata();

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

    File serverDir = new File("server");

    if (!checkServerDir(serverDir)) {
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

    deleteOldServerFile(oldServer, serverDir);

    oldServer.setJarName(newServerMeta.getJarName());
    oldServer.setJarVersion(newServerMeta.getJarVersion());
    oldServer.setJarRevision(newServerMeta.getJarRevision());
    oldServer.setJarLocation(serverFile.getPath());

    SerializationUtils.serializeMetadata(oldServer);

    logger.info("Restarting server...");
    starter.startJar();

    logger.info("Update complete");
  }

  private static void deleteOldServerFile(Meta oldServer, File serverDir) {

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
   * @param serverDir The {@link File} object representing the server directory
   * @return True if the server directory exists or was created successfully, false otherwise
   */
  private boolean checkServerDir(File serverDir) {
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
    File serverDir = new File("server");
    File serverFile = new File(serverDir, existing.getJarName());

    if (!serverFile.exists()) {
      logger.info("Server file not found");
      return false;
    }

    return true;
  }

  private void startup() {
    starter.startJar();
  }
}
