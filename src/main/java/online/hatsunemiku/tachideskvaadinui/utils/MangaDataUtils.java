/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import static org.springframework.http.HttpMethod.GET;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

@UtilityClass
public class MangaDataUtils {

  public static List<Chapter> getChapterList(
      Settings settings, String mangaId, RestTemplate client) {
    String chapterEndpoint = settings.getUrl() + "/api/v1/manga/" + mangaId + "/chapters";

    var typeRef = new ParameterizedTypeReference<List<Chapter>>() {};
    var chapter = client.exchange(chapterEndpoint, GET, null, typeRef).getBody();

    if (chapter == null) {
      return new ArrayList<>();
    }

    return chapter;
  }
}
