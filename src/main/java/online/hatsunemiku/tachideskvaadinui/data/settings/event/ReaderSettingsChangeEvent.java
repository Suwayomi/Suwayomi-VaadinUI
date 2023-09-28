package online.hatsunemiku.tachideskvaadinui.data.settings.event;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.dialog.Dialog;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderSettings;

@Getter
public class ReaderSettingsChangeEvent extends ComponentEvent<Dialog> {
  private final ReaderSettings newSettings;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source     the source component
   * @param fromClient <code>true</code> if the event originated from the client
   *                   side, <code>false</code> otherwise
   */
  public ReaderSettingsChangeEvent(Dialog source, boolean fromClient, ReaderSettings newSettings) {
    super(source, fromClient);
    this.newSettings = newSettings;
  }
}
