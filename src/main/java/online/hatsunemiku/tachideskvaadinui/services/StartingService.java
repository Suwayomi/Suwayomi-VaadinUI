package online.hatsunemiku.tachideskvaadinui.services;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class StartingService {

  @EventListener(ApplicationReadyEvent.class)
  public void startBrowser() {
    Desktop desktop = Desktop.getDesktop();
    try {
      desktop.browse(URI.create("http://localhost:8080"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
