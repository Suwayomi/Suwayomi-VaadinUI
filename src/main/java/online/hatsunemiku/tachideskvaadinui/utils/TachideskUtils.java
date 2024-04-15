/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import online.hatsunemiku.tachideskvaadinui.data.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

@UtilityClass
public class TachideskUtils {

  private static final Logger logger = LoggerFactory.getLogger(TachideskUtils.class);
  private static final Pattern JAR_PATTERN =
      Pattern.compile(
          "https://github\\.com/Suwayomi/Suwayomi-Server-preview/releases/download/(v\\d+\\.\\d+\\.\\d+(-r\\d+)?)/(Suwayomi-Server-v\\d+\\.\\d+\\.\\d+-r(\\d+)\\.jar)");

  public static String getNewestJarUrl(RestTemplate client) {
    String githubApi = "https://api.github.com/repos/Suwayomi/Suwayomi-Server-preview/releases/latest";
    String json = client.getForObject(githubApi, String.class);

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
}
