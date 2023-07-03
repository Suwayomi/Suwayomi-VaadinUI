package online.hatsunemiku.tachideskvaadinui.services;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

  private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);
  private Settings settings;
  public SettingsService() {
    deserialize();
  }


  private void serialize() {
    ObjectMapper mapper = new ObjectMapper();

    Path settingsFile = Path.of("settings.json");

    try (var out = Files.newOutputStream(settingsFile, CREATE, WRITE)) {
      mapper.writeValue(out, settings);
    } catch (IOException e) {
      logger.error("Could not write settings file", e);
      throw new RuntimeException(e);
    }
  }

  private void deserialize() {
    ObjectMapper mapper = new ObjectMapper();

    Path settingsFile = Path.of("settings.json");

    if (!Files.exists(settingsFile)) {
      settings = getDefaults();
      serialize();
      return;
    }

    try (var in = Files.newInputStream(settingsFile)) {
      settings = mapper.readValue(in, Settings.class);
    } catch (EOFException e) {
      settings = getDefaults();
      serialize();
      return;
    } catch (IOException e) {
      logger.error("Could not read settings file", e);
      throw new RuntimeException(e);
    }

    if (settings == null) {
      settings = getDefaults();
      serialize();
    }
  }

  @EventListener(ContextClosedEvent.class)
  public void onShutdownEvent() {
    logger.info("Saving settings");
    serialize();
  }

  private Settings getDefaults() {
    return new Settings("http://localhost:4567");
  }

  public Settings getSettings() {
    return settings;
  }
}
