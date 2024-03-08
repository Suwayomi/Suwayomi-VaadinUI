/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.reader.strip;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import lombok.Getter;

/** Represents an event that is fired when a new manga page is viewed. */
@DomEvent("manga-page-view")
@Getter
public class MangaPageViewEvent extends ComponentEvent<Component> {
  private final int pageIndex;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param fromClient <code>true</code> if the event originated from the client side, <code>false
   *     </code> otherwise
   */
  public MangaPageViewEvent(
      Component source, boolean fromClient, @EventData("event.detail.pageIndex") int pageIndex) {
    super(source, fromClient);
    this.pageIndex = pageIndex;
  }
}
