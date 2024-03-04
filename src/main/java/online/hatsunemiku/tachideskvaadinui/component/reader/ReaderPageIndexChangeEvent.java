/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.reader;

import com.vaadin.flow.component.ComponentEvent;
import lombok.Getter;

@Getter
public class ReaderPageIndexChangeEvent extends ComponentEvent<Reader> {
  private final int pageIndex;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param fromClient <code>true</code> if the event originated from the client side, <code>false
   *     </code> otherwise
   */
  public ReaderPageIndexChangeEvent(Reader source, boolean fromClient, int pageIndex) {
    super(source, fromClient);
    this.pageIndex = pageIndex;
  }
}
