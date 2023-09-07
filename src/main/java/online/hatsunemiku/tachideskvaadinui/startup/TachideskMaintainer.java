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
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.Meta;
import online.hatsunemiku.tachideskvaadinui.startup.download.ReadableProgressByteChannel;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import online.hatsunemiku.tachideskvaadinui.utils.TachideskUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
  private static final File serverDir = new File("server");
  @Getter private final File projectDir = getProjectDirFile();
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

    if (!checkProjectDir()) {
      return;
    }

    if (!checkServerDir()) {
      return;
    }

    checkServerConfig();

    new Thread(this::startup).start();

    logger.info("Checking for updates...");

    Meta oldServer = SerializationUtils.deserializeMetadata(projectDir.toPath());

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
   * Retrieves the project directory.
   *
   * @return The project directory specified as a {@link File} object.
   */
  private @NotNull File getProjectDirFile() {
    String os = System.getProperty("os.name").toLowerCase();

    Path appdata;

    if (os.contains("win")) {
      // On Windows, the Local AppData directory is used
      appdata = Path.of(System.getenv("LOCALAPPDATA"));
    } else {
      String userHome = System.getProperty("user.home");
      if (os.contains("mac")) {
        // On Mac, the Application Support directory is used
        appdata = Path.of(userHome, "Library", "Application Support");
      } else {
        // On Linux, the user's home directory is used
        appdata = Path.of(userHome);
      }
    }

    Path projectDir;
    // check for linux
    if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
      projectDir = appdata.resolve(".TachideskVaadinUI");
    } else {
      projectDir = appdata.resolve("TachideskVaadinUI");
    }

    log.debug("Project Dir: {}", projectDir);

    return projectDir.toFile();
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
    File serverDir = new File("server");
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
