/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import lombok.experimental.UtilityClass;
import online.hatsunemiku.tachideskvaadinui.view.ReadingView;

@UtilityClass
public class NavigationUtils {

  public void navigateToReader(int mangaId, int chapterId, UI ui) {
    String mangaIdStr = Integer.toString(mangaId);
    String chapterIdString = Integer.toString(chapterId);

    RouteParam mangaIdParam = new RouteParam("mangaId", mangaIdStr);
    RouteParam chapterIdParam = new RouteParam("chapterId", chapterIdString);

    RouteParameters params = new RouteParameters(mangaIdParam, chapterIdParam);

    ui.navigate(ReadingView.class, params);
  }
}
