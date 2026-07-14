/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;


import java.nio.file.Files;
import java.nio.file.Path;
import online.hatsunemiku.tachideskvaadinui.data.Meta;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;

public class SerializationUtils {

  private static final Logger logger = LoggerFactory.getLogger(SerializationUtils.class);
  private static final JsonMapper mapper = new JsonMapper();

  /**
   * Serializes the {@link Meta} object to a JSON file.
   *
   * @param meta the {@link Meta} object to be serialized
   * @param projectDir the directory where the JSON file will be created
   * @throws RuntimeException if there was an error during serialization
   */
  public static void serializeMetadata(Meta meta, @NotNull Path projectDir) {
      Path metaPath = projectDir.resolve("meta.json");
      mapper.writeValue(metaPath.toFile(), meta);
  }

  /**
   * Deserializes the {@link Meta} object from a JSON file.
   *
   * @param projectDir the directory where the JSON file is located
   * @return the deserialized {@link Meta} object
   * @throws RuntimeException if there was an error during deserialization
   */
  public static Meta deserializeMetadata(@NotNull Path projectDir) {
    try {
      Path metaPath = projectDir.resolve("meta.json");

      if (!Files.exists(metaPath) || metaPath.toFile().length() == 0) {
        return new Meta();
      }

      return mapper.readValue(metaPath.toFile(), Meta.class);
    } catch (StreamReadException e) {
      logger.warn("Invalid content in meta.json, returning default Meta", e);
      return new Meta();
    } catch (DatabindException e) {
      logger.warn("Content of meta.json doesn't match expected json layout, returning default Meta",
          e);
      return new Meta();
    } catch (Exception e) {
      logger.warn("Failed to deserialize metadata, returning default Meta", e);
      return new Meta();
    }
  }
}
