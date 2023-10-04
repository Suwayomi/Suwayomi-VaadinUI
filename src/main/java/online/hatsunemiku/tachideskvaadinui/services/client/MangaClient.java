/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import java.net.URI;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mangaClient", url = "http://localhost:8080")
public interface MangaClient {

  @GetMapping("/api/v1/manga/{mangaId}/chapter/{chapterIndex}")
  Chapter getChapter(URI baseUrl, @PathVariable long mangaId, @PathVariable int chapterIndex);

  @GetMapping("/api/v1/manga/{mangaId}/category/{categoryId}")
  void addMangaToCategory(URI baseUrl, @PathVariable long mangaId, @PathVariable long categoryId);

  @DeleteMapping("/api/v1/manga/{mangaId}/category/{categoryId}")
  void removeMangaFromCategory(
      URI baseUrl, @PathVariable long mangaId, @PathVariable long categoryId);
}
