/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.utils.PathUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Service for managing the application settings. <br>
 * Also, responsible for saving the settings to disk and distributing them to the application.
 *
 * @version 1.12.0
 * @since 0.9.0
 */
@Service
@Slf4j
public class SettingsService {

  private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

  @Getter @NotNull private final Settings settings;

  @Getter(AccessLevel.NONE)
  private final ObjectMapper mapper;

  private final Environment env;

  public SettingsService(ObjectMapper mapper, Environment env) {
    this.mapper = mapper;
    this.env = env;
    settings = deserialize();
  }

  /**
   * Deserializes the settings from disk.
   *
   * @return The deserialized {@link Settings} instance.
   */
  private Settings deserialize() {
    final Settings settings;

    Path projectDirPath = PathUtils.getResolvedProjectPath(env);

    Path settingsFile = projectDirPath.resolve("settings.json");

    if (!Files.exists(settingsFile)) {
      settings = getDefaults();
      serialize();
      return settings;
    }

    Settings tempSettings;
    try (var in = Files.newInputStream(settingsFile)) {
      tempSettings = mapper.readValue(in, Settings.class);
      if (tempSettings == null) {
        tempSettings = getDefaults();
      }
    } catch (EOFException e) {
      settings = getDefaults();
      serialize();
      return settings;
    } catch (IOException e) {
      logger.error("Could not read settings file", e);
      throw new RuntimeException(e);
    }

    settings = tempSettings;
    return settings;
  }

  /** Serializes the settings to disk. */
  private void serialize() {
    ObjectMapper mapper = new ObjectMapper();

    Path projectDirPath = PathUtils.getResolvedProjectPath(env);

    Path settingsFile = projectDirPath.resolve("settings.json");

    try (var out = Files.newOutputStream(settingsFile, CREATE, WRITE)) {
      mapper.writeValue(out, settings);
    } catch (IOException e) {
      logger.error("Could not write settings file", e);
      throw new RuntimeException(e);
    }
  }

  @EventListener(ContextClosedEvent.class)
  public void onShutdownEvent() {
    logger.info("Saving settings");
    serialize();
  }

  public @NotNull Settings getDefaults() {
    return new Settings("http://localhost:4567");
  }
}
