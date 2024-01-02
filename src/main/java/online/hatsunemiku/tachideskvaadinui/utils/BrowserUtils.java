/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserUtils {

  private static final Logger log = LoggerFactory.getLogger(BrowserUtils.class);

  public static void openBrowser(String url) throws IOException {
    boolean useDesktop =
        Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);

    if (useDesktop) {
      try {
        Desktop.getDesktop().browse(URI.create(url));
      } catch (IOException e) {
        log.error("Browser couldn't be opened", e);
      }
    } else {
      log.info("Desktop not supported, opening browser via xdg-open");
      Runtime runtime = Runtime.getRuntime();
      try {
        String[] command = {"xdg-open", url};
        var process = runtime.exec(command);

        if (process.waitFor() != 0) {
          throw new IOException("Could not open browser via xdg-open");
        }
      } catch (IOException e) {
        log.error("Browser couldn't be opened", e);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
