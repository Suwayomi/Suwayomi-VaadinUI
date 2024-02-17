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
import online.hatsunemiku.tachideskvaadinui.view.MangaView;
import online.hatsunemiku.tachideskvaadinui.view.ReadingView;

@UtilityClass
public class NavigationUtils {

  public static void navigateToManga(int mangaId, UI ui) {
    String mangaIdStr = Integer.toString(mangaId);
    RouteParam mangaIdParam = new RouteParam("id", mangaIdStr);
    RouteParameters params = new RouteParameters(mangaIdParam);
    ui.navigate(MangaView.class, params);
  }
}
