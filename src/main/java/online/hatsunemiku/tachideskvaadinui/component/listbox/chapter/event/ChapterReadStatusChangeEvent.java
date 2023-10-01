/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.listbox.chapter.event;

import com.vaadin.flow.component.Component;
import lombok.Getter;

@Getter
public class ChapterReadStatusChangeEvent
    extends com.vaadin.flow.component.ComponentEvent<Component> {

  private final boolean read;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param fromClient <code>true</code> if the event originated from the client side, <code>false
   *     </code> otherwise
   */
  public ChapterReadStatusChangeEvent(Component source, boolean fromClient, boolean read) {
    super(source, fromClient);
    this.read = read;
  }
}
