/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import java.net.URI;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.SourceMangaList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "sourceClient", url = "http://localhost:4567")
public interface SourceClient {

  @GetMapping("/source/{sourceId}/popular/{page}")
  SourceMangaList getPopularManga(URI baseUrl, @PathVariable long sourceId, @PathVariable int page);

  @GetMapping("/source/{sourceId}/latest/{page}")
  SourceMangaList getLatestManga(URI baseUrl, @PathVariable long sourceId, @PathVariable int page);
}
