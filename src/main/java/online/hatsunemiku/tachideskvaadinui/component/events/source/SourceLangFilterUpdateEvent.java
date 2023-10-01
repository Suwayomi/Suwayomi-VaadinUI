/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.events.source;

import com.vaadin.flow.component.ComponentEvent;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.component.combo.LangComboBox;

public class SourceLangFilterUpdateEvent extends ComponentEvent<LangComboBox> {

  @Getter private final String filterLanguage;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   */
  public SourceLangFilterUpdateEvent(LangComboBox source, String filterLanguage) {
    super(source, false);
    this.filterLanguage = filterLanguage;
  }
}
