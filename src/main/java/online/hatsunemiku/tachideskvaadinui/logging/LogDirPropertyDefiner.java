/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.logging;

import ch.qos.logback.core.PropertyDefinerBase;
import online.hatsunemiku.tachideskvaadinui.utils.PathUtils;

/**
 * Property definer for Logback to resolve the log directory dynamically
 * based on the application's resolved project directory.
 */
public class LogDirPropertyDefiner extends PropertyDefinerBase {

  @Override
  public String getPropertyValue() {
    return PathUtils.getLogsDir().toAbsolutePath().normalize().toString().replace('\\', '/');
  }
}
