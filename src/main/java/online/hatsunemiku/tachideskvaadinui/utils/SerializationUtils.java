package online.hatsunemiku.tachideskvaadinui.utils;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.JsonSerializable;
import java.io.File;
import java.io.IOException;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import org.reflections.serializers.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationUtils {

  private static final Logger logger = LoggerFactory.getLogger(SerializationUtils.class);

  public static void serializeSettings(Settings settings) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      File settingsFile = new File("settings.json");
      mapper.writeValue(settingsFile, settings);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Settings deseralizeSettings() {
    ObjectMapper mapper = new ObjectMapper();

    try {
      File settingsFile = new File("settings.json");

      if (!settingsFile.exists()) {
        return new Settings("http://localhost:4567");
      }

      return mapper.readValue(settingsFile, Settings.class);
    } catch (StreamReadException e) {
      logger.error("Invalid content", e);
      throw new RuntimeException(e);
    } catch (DatabindException e) {
      logger.error("Content of Json file doesn't match expected json layout", e);
      throw new RuntimeException(e);
    } catch (IOException e) {
      logger.error("Settings file couldn't be read", e);
      throw new RuntimeException(e);
    }
  }

}
