/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import java.io.File;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import online.hatsunemiku.tachideskvaadinui.data.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@UtilityClass
public class TachideskUtils {

  private static final Logger logger = LoggerFactory.getLogger(TachideskUtils.class);
  private static final Pattern JAR_PATTERN =
      Pattern.compile(
          "https://github\\.com/Suwayomi/Suwayomi-Server/releases/download/(v\\d+\\.\\d+\\.\\d+(-r\\d+)?)/(Suwayomi-Server-v\\d+\\.\\d+\\.(\\d+)\\.jar)");

  public static String getNewestJarUrl(RestClient client) {
    String githubApi = "https://api.github.com/repos/Suwayomi/Suwayomi-Server/releases/latest";
    String json = client.get()
            .uri(githubApi)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(String.class);

    if (json == null) {
      return null;
    }

    Matcher matcher = JAR_PATTERN.matcher(json);

    if (!matcher.find()) {
      return null;
    }

    String jarUrl = matcher.group();

    logger.info("Downloading newest server from {}", jarUrl);

    return jarUrl;
  }

  public static Optional<Meta> getMetaFromUrl(String url) {
    Matcher matcher = JAR_PATTERN.matcher(url);

    if (!matcher.matches()) {
      return Optional.empty();
    }

    Meta meta = new Meta();

    meta.setJarVersion(matcher.group(1));
    meta.setJarRevision(matcher.group(4));
    meta.setJarName(matcher.group(3));

    return Optional.of(meta);
  }

  /**
   * Validates if the given file is a valid, uncorrupted JAR file that has a main class.
   *
   * @param file the file to validate
   * @return {@code true} if the file is a valid JAR, {@code false} otherwise
   */
  public static boolean isValidJar(File file) {
    if (file == null || !file.exists() || !file.isFile()) {
      return false;
    }
    try (JarFile jarFile = new JarFile(file)) {
      Manifest manifest = jarFile.getManifest();
      if (manifest == null) {
        return false;
      }
      Attributes mainAttributes = manifest.getMainAttributes();
      String mainClass = mainAttributes.getValue(Attributes.Name.MAIN_CLASS);
      return mainClass != null && !mainClass.trim().isEmpty();
    } catch (Exception e) {
      logger.warn("Jar validation failed for file {}: {}", file.getPath(), e.getMessage());
      return false;
    }
  }
}
