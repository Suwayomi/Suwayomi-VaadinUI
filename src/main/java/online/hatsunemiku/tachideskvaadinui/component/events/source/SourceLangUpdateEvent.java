package online.hatsunemiku.tachideskvaadinui.component.events.source;

import com.vaadin.flow.component.ComponentEvent;
import java.util.List;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.component.scroller.source.SourceScroller;

public class SourceLangUpdateEvent extends ComponentEvent<SourceScroller> {

  @Getter private final List<String> languages;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param languages the new List of languages
   */
  public SourceLangUpdateEvent(SourceScroller source, List<String> languages) {
    super(source, false);
    this.languages = List.copyOf(languages);
  }
}
