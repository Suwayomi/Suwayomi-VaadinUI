package online.hatsunemiku.tachideskvaadinui.startup;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
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
    openInBrowserItem.addActionListener(e -> {
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
