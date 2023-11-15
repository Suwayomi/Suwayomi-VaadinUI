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
import online.hatsunemiku.tachideskvaadinui.utils.ProfileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SettingsService {
  private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

  @Getter private final Settings settings;

  @Getter(AccessLevel.NONE)
  private final ObjectMapper mapper;
  private final Environment env;
  public SettingsService(ObjectMapper mapper, Environment env) {
    this.mapper = mapper;
    this.env = env;
    settings = deserialize();
  }

  private Settings deserialize() {
    final Settings settings;

    Path projectDirPath;
    if (ProfileUtils.isDev(env)) {
      projectDirPath = PathUtils.getDevDir();
    } else {
      projectDirPath = PathUtils.getProjectDir();
    }

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

  private void serialize() {
    ObjectMapper mapper = new ObjectMapper();

    Path projectDirPath;
    if (ProfileUtils.isDev(env)) {
      projectDirPath = PathUtils.getDevDir();
    } else {
      projectDirPath = PathUtils.getProjectDir();
    }

    Path settingsFile = projectDirPath.resolve("settings.json");

    if (settings == null) {
      logger.error("Settings are null");
      return;
    }

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

  public Settings getDefaults() {
    return new Settings("http://localhost:4567");
  }
}
