package online.hatsunemiku.tachideskvaadinui.component.tab.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import lombok.Getter;

@Getter
public class CategoryTabHighlightEvent extends ComponentEvent<Component> {
  private final boolean highlight;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source     the source component
   * @param fromClient <code>true</code> if the event originated from the client
   *                   side, <code>false</code> otherwise
   */
  public CategoryTabHighlightEvent(Component source, boolean fromClient, boolean highlight) {
    super(source, fromClient);
    this.highlight = highlight;
  }
}
