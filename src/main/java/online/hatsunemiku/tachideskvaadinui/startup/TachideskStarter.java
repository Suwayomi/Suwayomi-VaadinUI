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
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TachideskStarter {

  private static final Logger logger = LoggerFactory.getLogger(TachideskStarter.class);
  private Process serverProcess;
  private ScheduledExecutorService serverChecker;

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
      Desktop desktop = Desktop.getDesktop();
      String url =
          "https://github.com/aless2003/Tachidesk-VaadinUI/blob/master/Install%20Process.md#when-starting-its-stuck-on-waiting-for-server-to-start";
      try {
        desktop.browse(URI.create(url));
        Thread.sleep(6000);
      } catch (IOException e) {
        log.error("Failed to open browser", e);
      } catch (InterruptedException e) {
        log.error("Failed to sleep", e);
      }
      System.exit(-1);
      return;
    }

    log.info("Starting jar with data dir: {}", dataDirFile.getAbsolutePath());
    ProcessBuilder processBuilder = new ProcessBuilder("java", dataDirArg, "-jar", jarLocation);
    processBuilder.redirectOutput(new File("server.log"));

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
}
