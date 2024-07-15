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

@Getter
@AllArgsConstructor
public class MangaUpdateEvent {
  private boolean isRunning;
  private List<Manga> completedJobs;
}
