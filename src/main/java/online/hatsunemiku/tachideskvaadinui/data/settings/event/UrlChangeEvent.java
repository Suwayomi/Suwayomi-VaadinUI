package online.hatsunemiku.tachideskvaadinui.data.settings.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UrlChangeEvent extends ApplicationEvent {

  private final String url;

  public UrlChangeEvent(Object source, String url) {
    super(source);
    this.url = url;
  }

}
