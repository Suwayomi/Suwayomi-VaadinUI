/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.listbox.chapter.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import java.util.List;
import lombok.Getter;

/**
 * Event fired when manga chapters are queued/started downloading.
 */
@Getter
public class ChapterDownloadSyncEvent extends ComponentEvent<Component> {

  private final List<Integer> chapterIds;

  /**
   * Creates a new ChapterDownloadSyncEvent.
   *
   * @param source     the source component
   * @param chapterIds the list of chapter IDs that are downloading
   */
  public ChapterDownloadSyncEvent(Component source, List<Integer> chapterIds) {
    super(source, false);
    this.chapterIds = List.copyOf(chapterIds);
  }
}
