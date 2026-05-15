/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk.event;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;

/**
 * Event that is fired when a manga update is completed. <br>
 * Contains a list of completed jobs and a boolean indicating if the update is still running. <br>
 * Any listeners should either fail while the update is still running or should be independent of
 * the update status.
 *
 * @since 1.12.0
 * @version 1.12.0
 */
@Getter
@AllArgsConstructor
public class MangaUpdateEvent {

  private boolean isRunning;
  private List<Manga> completedJobs;
}
