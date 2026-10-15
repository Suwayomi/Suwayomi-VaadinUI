/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.logging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import online.hatsunemiku.tachideskvaadinui.utils.PathUtils;
import online.hatsunemiku.tachideskvaadinui.utils.ProfileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LogDirTest {

  private static final Logger log = LoggerFactory.getLogger(LogDirTest.class);

  @Test
  void testGetLogsDir() {
    Path logsDir = PathUtils.getLogsDir();
    assertNotNull(logsDir);
    assertTrue(Files.exists(logsDir), "Logs directory should exist on disk");
    assertTrue(logsDir.endsWith("logs"), "Logs directory should end with 'logs'");
  }

  @Test
  void testLogDirPropertyDefiner() {
    LogDirPropertyDefiner definer = new LogDirPropertyDefiner();
    String propertyValue = definer.getPropertyValue();
    assertNotNull(propertyValue);
    assertFalse(propertyValue.contains("\\"), "Property value should use forward slashes");
    assertTrue(propertyValue.endsWith("/logs"), "Property value should end with '/logs'");
  }

  @Test
  void testProfileUtilsIsDevSystemProperty() {
    String original = System.getProperty("spring.profiles.active");
    try {
      System.setProperty("spring.profiles.active", "dev");
      assertTrue(ProfileUtils.isDev());

      System.setProperty("spring.profiles.active", "prod");
      assertFalse(ProfileUtils.isDev());
    } finally {
      if (original != null) {
        System.setProperty("spring.profiles.active", original);
      } else {
        System.clearProperty("spring.profiles.active");
      }
    }
  }

  @Test
  void testLogbackAppendsToConfiguredLogDir() {
    String testMessage = "Test log entry for LogDirTest: " + System.currentTimeMillis();
    log.info(testMessage);

    Path infoLog = PathUtils.getLogsDir().resolve("info.log");
    assertTrue(Files.exists(infoLog), "info.log should exist in the logs directory");
  }
}
