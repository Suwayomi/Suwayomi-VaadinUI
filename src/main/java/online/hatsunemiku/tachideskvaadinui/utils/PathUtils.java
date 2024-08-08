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
 * @since 0.9.0
 * @version 1.12.0
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
    String os = System.getProperty("os.name").toLowerCase();

    Path appdata;

    if (os.contains("win")) {
      // On Windows, the Local AppData directory is used
      appdata = Path.of(System.getenv("LOCALAPPDATA"));
    } else {
      String userHome = System.getProperty("user.home");
      if (os.contains("mac")) {
        // On Mac, the Application Support directory is used
        appdata = Path.of(userHome, "Library", "Application Support");
      } else {
        // On Linux, the user's home directory is used
        appdata = Path.of(userHome);
      }
    }

    Path projectDir;
    // check for linux
    if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
      projectDir = appdata.resolve(".TachideskVaadinUI");
    } else {
      projectDir = appdata.resolve("TachideskVaadinUI");
    }

    log.debug("Project Dir: {}", projectDir);

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

  public static @NotNull Path getResolvedProjectPath(Environment env) {
    Path projectDirPath;
    if (ProfileUtils.isDev(env)) {
      projectDirPath = PathUtils.getDevDir();
    } else {
      projectDirPath = PathUtils.getProjectDir();
    }
    return projectDirPath;
  }
}
