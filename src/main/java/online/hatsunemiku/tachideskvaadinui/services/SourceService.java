/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.SourceMangaList;
import online.hatsunemiku.tachideskvaadinui.services.client.SourceClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SourceService {

  private final RestTemplate client;
  private final SourceClient sourceClient;
  private final SettingsService settingsService;

  public SourceService(
      RestTemplate client, SourceClient sourceClient, SettingsService settingsService) {
    this.client = client;
    this.sourceClient = sourceClient;
    this.settingsService = settingsService;
  }

  public List<Source> getSources() {

    Settings settings = settingsService.getSettings();

    String url = settings.getUrl() + "/api/v1/source/list";

    Source[] sources = client.getForObject(url, Source[].class);

    if (sources == null) {
      return List.of();
    }

    return List.of(sources);
  }

  public SourceMangaList getPopularManga(String sourceId, int page) {
    return sourceClient.getPopularManga(sourceId, page);
  }

  public SourceMangaList getLatestManga(String sourceId, int page) {
    return sourceClient.getLatestManga(sourceId, page);
  }
}
