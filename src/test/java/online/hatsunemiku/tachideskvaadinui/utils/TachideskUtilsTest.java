/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TachideskUtilsTest {

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      textBlock =
          """
          openjdk version "25.0.2" 2026-01-20 LTS                               | 25
          openjdk version "21.0.2" 2024-01-16                                   | 21
          java version "21.0.1" 2023-10-17                                      | 21
          openjdk version "21" 2023-09-19                                       | 21
          openjdk version "21-ea" 2023-09-19                                    | 21
          openjdk version "21.0.1+12"                                           | 21
          openjdk version "17.0.9" 2023-10-17                                   | 17
          openjdk version "11.0.15" 2022-04-19                                  | 11
          java version "1.8.0_312"                                              | 8
          java version "1.7.0_80"                                               | 7
          openjdk version '21.0.5'                                              | 21
          openjdk version 21.0.1                                                | 21
          """)
  void testParseJavaMajorVersion(String versionOutput, int expectedMajor) {
    Optional<Integer> majorVersion = TachideskUtils.parseJavaMajorVersion(versionOutput);
    assertTrue(majorVersion.isPresent());
    assertEquals(expectedMajor, majorVersion.get());
  }

  @Test
  void testParseJavaMajorVersionInvalid() {
    assertTrue(TachideskUtils.parseJavaMajorVersion(null).isEmpty());
    assertTrue(TachideskUtils.parseJavaMajorVersion("").isEmpty());
    assertTrue(TachideskUtils.parseJavaMajorVersion("   ").isEmpty());
    assertTrue(TachideskUtils.parseJavaMajorVersion("bash: java: command not found").isEmpty());
  }
}
