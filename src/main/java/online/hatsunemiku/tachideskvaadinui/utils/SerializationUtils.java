package online.hatsunemiku.tachideskvaadinui.utils;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import online.hatsunemiku.tachideskvaadinui.data.Meta;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationUtils {

  private static final Logger logger = LoggerFactory.getLogger(SerializationUtils.class);

  /**
   * Serializes the {@link Meta} object to a JSON file.
   *
   * @param meta the {@link Meta} object to be serialized
   * @param projectDir the directory where the JSON file will be created
   * @throws RuntimeException if there was an error during serialization
   */
  public static void serializeMetadata(Meta meta, @NotNull Path projectDir) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      Path metaPath = projectDir.resolve("meta.json");
      mapper.writeValue(metaPath.toFile(), meta);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Deserializes the {@link Meta} object from a JSON file.
   *
   * @param projectDir the directory where the JSON file is located
   * @return the deserialized {@link Meta} object
   * @throws RuntimeException if there was an error during deserialization
   */
  public static Meta deserializeMetadata(@NotNull Path projectDir) {
    ObjectMapper mapper = new ObjectMapper();

    try {
      Path metaPath = projectDir.resolve("meta.json");

      if (!Files.exists(metaPath)) {
        return new Meta();
      }

      return mapper.readValue(metaPath.toFile(), Meta.class);
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
