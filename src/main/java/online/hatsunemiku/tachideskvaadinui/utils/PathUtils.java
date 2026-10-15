/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.env.Environment;

/**
 * Utility class for getting common paths.
 *
 * @version 1.12.0
 * @since 0.9.0
 */
@UtilityClass
@Slf4j
public class PathUtils {

  /**
   * Retrieves the project directory.
   *
   * @return The project directory specified as a {@link Path} object.
   */
  public static Path getProjectDir() {

    Path appdata;

    if (PlatformUtils.isWindows()) {
      // On Windows, the Local AppData directory is used
      String localAppData = System.getenv("LOCALAPPDATA");
      if (localAppData != null && !localAppData.isBlank()) {
        appdata = Path.of(localAppData);
      } else {
        appdata = Path.of(System.getProperty("user.home"), "AppData", "Local");
      }
    } else {
      String userHome = System.getProperty("user.home");
      if (PlatformUtils.isMac()) {
        // On Mac, the Application Support directory is used
        appdata = Path.of(userHome, "Library", "Application Support");
      } else {
        // On Linux, the user's home directory is used
        appdata = Path.of(userHome);
      }
    }

    Path projectDir;
    // check for linux
    if (PlatformUtils.isLinux()) {
      projectDir = appdata.resolve(".TachideskVaadinUI");
    } else {
      projectDir = appdata.resolve("TachideskVaadinUI");
    }

    return projectDir;
  }

  /**
   * Retrieves the development directory.
   *
   * @return The development directory specified as a {@link Path} object.
   * @throws RuntimeException if the dev directory creation fails.
   */
  public static Path getDevDir() {
    Path devDir = Path.of("./devDir");

    if (Files.notExists(devDir)) {
      try {
        Files.createDirectories(devDir);
      } catch (IOException e) {
        log.error("Failed to create dev directory", e);
        throw new RuntimeException(e);
      }
    }

    return devDir;
  }

  /**
   * Retrieves the resolved project path without an Environment.
   *
   * @return The resolved project path specified as a {@link Path} object.
   */
  public static @NotNull Path getResolvedProjectPath() {
    Path projectDirPath;
    if (ProfileUtils.isDev()) {
      projectDirPath = PathUtils.getDevDir();
    } else {
      projectDirPath = PathUtils.getProjectDir();
    }
    return projectDirPath;
  }

  /**
   * Retrieves the resolved project path. e.g. either the project directory or a development
   * directory depending on the environment.
   *
   * @param env The {@link Environment} object used to determine the appropriate path.
   * @return The resolved project path specified as a {@link Path} object.
   */
  public static @NotNull Path getResolvedProjectPath(Environment env) {
    Path projectDirPath;
    if (ProfileUtils.isDev(env)) {
      projectDirPath = PathUtils.getDevDir();
    } else {
      projectDirPath = PathUtils.getProjectDir();
    }
    return projectDirPath;
  }

  /**
   * Retrieves the logs directory inside the resolved project path and ensures it exists.
   *
   * @return The logs directory specified as a {@link Path} object.
   */
  public static @NotNull Path getLogsDir() {
    Path logsDir = getResolvedProjectPath().resolve("logs");
    if (Files.notExists(logsDir)) {
      try {
        Files.createDirectories(logsDir);
      } catch (IOException e) {
        log.error("Failed to create logs directory", e);
      }
    }
    return logsDir;
  }

  /**
   * Retrieves the logs directory inside the resolved project path and ensures it exists.
   *
   * @param env The {@link Environment} object used to determine the appropriate path.
   * @return The logs directory specified as a {@link Path} object.
   */
  public static @NotNull Path getLogsDir(Environment env) {
    Path logsDir = getResolvedProjectPath(env).resolve("logs");
    if (Files.notExists(logsDir)) {
      try {
        Files.createDirectories(logsDir);
      } catch (IOException e) {
        log.error("Failed to create logs directory", e);
      }
    }
    return logsDir;
  }
}
