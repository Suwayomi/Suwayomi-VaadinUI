package online.hatsunemiku.tachideskvaadinui.startup;

import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
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

  public void startJar() {

    Meta meta = SerializationUtils.deseralizeMetadata();

    String jarLocation = meta.getJarLocation();

    if (jarLocation.isEmpty()) {
      logger.info("No jar location found");
      return;
    }

    String serverDir = jarLocation.substring(0, jarLocation.lastIndexOf("\\"));

    File serverDirFile = new File(serverDir);
    File dataDirFile = new File(serverDirFile, "data");

    String dataDirArg = "-Dsuwayomi.tachidesk.config.server.rootDir=" + dataDirFile.getAbsolutePath();

    ProcessBuilder processBuilder = new ProcessBuilder("java", dataDirArg, "-jar", jarLocation);
    
    try {
      serverProcess = processBuilder.start();
      Runtime.getRuntime().addShutdownHook(new Thread(this::stopJar));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @PreDestroy
  public void stopJar() {
    log.info("Stopping Jar");
    if (serverProcess == null) {
      return;
    }

    if (serverProcess.supportsNormalTermination()) {
      serverProcess.destroy();
    } else {
      serverProcess.destroyForcibly();
    }
  }

  public void restartJar() {
    stopJar();
    startJar();
  }
}
