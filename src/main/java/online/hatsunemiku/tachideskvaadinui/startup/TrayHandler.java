/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.startup;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.FileNotFoundException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
@Slf4j
public class TrayHandler {

  @EventListener(ApplicationStartedEvent.class)
  public void registerTray() {
    if (SystemTray.isSupported()) {
      SystemTray tray = SystemTray.getSystemTray();

      PopupMenu menu = createTrayMenu();

      try {
        var url = ResourceUtils.getURL("classpath:icon/icon.png");
        Image icon = Toolkit.getDefaultToolkit().getImage(url);

        TrayIcon trayIcon = new TrayIcon(icon, "Tachidesk Vaadin UI", menu);

        trayIcon.setImageAutoSize(true);

        tray.add(trayIcon);

        String caption = "Tachidesk Vaadin UI";
        String text =
            """
            Click here or open the browser and enter localhost:8080 - You can also click the icon in the system tray with the right mouse button and select "Open in browser"
            """;
        trayIcon.displayMessage(caption, text, MessageType.INFO);

        trayIcon.addActionListener(
            e -> {
              Desktop desktop = Desktop.getDesktop();
              try {
                desktop.browse(URI.create("http://localhost:8080"));
              } catch (Exception ex) {
                throw new RuntimeException(ex);
              }
            });
      } catch (FileNotFoundException e) {
        log.error("Icon not found", e);
        throw new RuntimeException(e);
      } catch (AWTException e) {
        log.error("Error adding tray icon", e);
        throw new RuntimeException(e);
      }

    } else {
      log.info("System tray not supported");
    }
  }

  private PopupMenu createTrayMenu() {
    PopupMenu menu = new PopupMenu();

    MenuItem exitItem = new MenuItem("Exit");
    exitItem.addActionListener(e -> System.exit(0));

    menu.add(exitItem);

    MenuItem openInBrowserItem = new MenuItem("Open in browser");
    openInBrowserItem.addActionListener(
        e -> {
          Desktop desktop = Desktop.getDesktop();
          try {
            desktop.browse(URI.create("http://localhost:8080"));
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        });

    menu.add(openInBrowserItem);

    return menu;
  }
}
