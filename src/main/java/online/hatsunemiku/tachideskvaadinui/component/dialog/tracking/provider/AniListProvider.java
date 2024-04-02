/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider;

import java.util.List;
import lombok.AllArgsConstructor;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;
import online.hatsunemiku.tachideskvaadinui.services.tracker.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService;

@AllArgsConstructor
public class AniListProvider implements TrackerProvider {
  private AniListAPIService aniListAPI;
  private SuwayomiTrackingService suwayomiAPI;

  @Override
  public boolean canSetPrivate() {
    return true;
  }

  @Override
  public List<TrackerSearchResult> search(String query) {
    return suwayomiAPI.searchAniList(query);
  }

  @Override
  public void submitToTracker(boolean isPrivate, int mangaId, int externalId) {
    if (isPrivate && !aniListAPI.isMangaInList(externalId)) {
      aniListAPI.addMangaToList(mangaId, true);
    }

    suwayomiAPI.trackOnAniList(mangaId, externalId);
  }
}
