package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking;

import com.vaadin.flow.component.ComponentEvent;

public class TrackerMangaChosenEvent extends ComponentEvent<TrackingDialog> {

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source     the source component
   * @param fromClient <code>true</code> if the event originated from the client
   *                   side, <code>false</code> otherwise
   */
  public TrackerMangaChosenEvent(TrackingDialog source, boolean fromClient) {
    super(source, fromClient);
  }
}
