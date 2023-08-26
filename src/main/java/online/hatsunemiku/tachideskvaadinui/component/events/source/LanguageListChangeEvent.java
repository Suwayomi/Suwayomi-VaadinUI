package online.hatsunemiku.tachideskvaadinui.component.events.source;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import java.util.List;
import lombok.Getter;

@Getter
public class LanguageListChangeEvent extends ComponentEvent<Component> {

  private final List<String> languages;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param languages the new List of languages
   */
  public LanguageListChangeEvent(Component source, List<String> languages) {
    super(source, false);
    this.languages = List.copyOf(languages);
  }
}
