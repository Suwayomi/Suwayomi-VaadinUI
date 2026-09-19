/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import lombok.experimental.UtilityClass;

/**
 * Utility class for platform-specific checks.
 *
 * @version 1.15.0
 * @since 1.15.0
 */
@UtilityClass
public class PlatformUtils {

  /**
   * Checks if the current operating system is Windows.
   *
   * @return {@code true} if the current OS is Windows, {@code false} otherwise.
   */
  public static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("win");
  }

  /**
   * Checks if the current operating system is macOS.
   *
   * @return {@code true} if the current OS is macOS, {@code false} otherwise.
   */
  public static boolean isMac() {
    return System.getProperty("os.name").toLowerCase().contains("mac");
  }

  /**
   * Checks if the current operating system is Linux.
   *
   * @return {@code true} if the current OS is Linux, {@code false} otherwise.
   */
  public static boolean isLinux() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("nix") || os.contains("nux") || os.contains("aix");
  }
}
