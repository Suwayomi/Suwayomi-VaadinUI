/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "download", url = "localhost:8080")
public interface DownloadClient {

  /**
   * Downloads a single chapter of a manga from a specified base URL.
   *
   * @param baseUrl the base URL of the manga server
   * @param mangaId the ID of the manga
   * @param chapterIndex the index of the chapter to download
   */
  @GetMapping("/api/v1/download/{mangaId}/chapter/{chapterIndex}")
  void downloadSingleChapter(
      URI baseUrl, @PathVariable int mangaId, @PathVariable int chapterIndex);

  /**
   * Downloads multiple chapters of a manga from a specified base URL.
   *
   * @param baseUrl the base URL of the manga server
   * @param downloadRequest the request containing the IDs of the chapters to download
   */
  @PostMapping("/api/v1/download/batch")
  void downloadMultipleChapters(URI baseUrl, @RequestBody DownloadChapterRequest downloadRequest);

  // http://localhost:4567/api/v1/manga/4687/chapter/1
  @DeleteMapping("/api/v1/manga/{mangaId}/chapter/{chapterIndex}")
  void deleteSingleChapter(URI baseUrl, @PathVariable int mangaId, @PathVariable int chapterIndex);

  /**
   * Represents a request to download chapters.
   *
   * <p>The {@code DownloadChapterRequest} class is a data class that encapsulates the chapter IDs
   * to be downloaded. It is used to communicate between the TachideskUI backend and the Tachidesk
   * Server.
   */
  @Data
  @AllArgsConstructor
  class DownloadChapterRequest {

    private List<Integer> chapterIds;
  }
}
