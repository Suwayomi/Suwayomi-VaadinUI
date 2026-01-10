/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OSUtils {

  public static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("windows");
  }


}
