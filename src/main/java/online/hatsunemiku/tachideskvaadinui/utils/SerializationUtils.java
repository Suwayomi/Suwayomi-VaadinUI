package online.hatsunemiku.tachideskvaadinui.utils;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import online.hatsunemiku.tachideskvaadinui.data.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationUtils {

  private static final Logger logger = LoggerFactory.getLogger(SerializationUtils.class);

  public static void serializeMetadata(Meta meta) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      File metaFile = new File("meta.json");
      mapper.writeValue(metaFile, meta);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Meta deseralizeMetadata() {
    ObjectMapper mapper = new ObjectMapper();

    try {
      File metaFile = new File("meta.json");

      if (!metaFile.exists()) {
        return new Meta();
      }

      return mapper.readValue(metaFile, Meta.class);
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
