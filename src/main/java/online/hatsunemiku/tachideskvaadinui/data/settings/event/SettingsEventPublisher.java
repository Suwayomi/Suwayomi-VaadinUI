package online.hatsunemiku.tachideskvaadinui.data.settings.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SettingsEventPublisher {

  private final ApplicationEventPublisher publisher;

  public SettingsEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  public void publishUrlChangeEvent(Object source, String url) {
    var event = new UrlChangeEvent(source, url);
    publisher.publishEvent(event);
  }
}
