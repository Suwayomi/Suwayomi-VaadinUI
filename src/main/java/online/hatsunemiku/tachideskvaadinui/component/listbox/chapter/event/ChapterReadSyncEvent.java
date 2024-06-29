/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.listbox.chapter.event;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import java.util.List;
import lombok.Getter;

@Getter
public class ChapterReadSyncEvent extends ComponentEvent<Button> {

  private final List<Float> chapterNumbers;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param chapterNumbers the chapter numbers that were set to read
   */
  public ChapterReadSyncEvent(Button source, List<Float> chapterNumbers) {
    super(source, false);
    this.chapterNumbers = List.copyOf(chapterNumbers);
  }
}
