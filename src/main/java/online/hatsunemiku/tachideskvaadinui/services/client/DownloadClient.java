/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import elemental.json.Json;
import elemental.json.JsonObject;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import online.hatsunemiku.tachideskvaadinui.utils.GraphQLUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DownloadClient {

  private final WebClientService clientService;

  public DownloadClient(WebClientService clientService) {
    this.clientService = clientService;
  }

  /**
   * Downloads the chapters specified by the given list of chapterIds.
   *
   * @param chapterIds The list of {@link Chapter#getId() chapter IDs} to download.
   * @return True if all chapters were successfully downloaded, false otherwise.
   */
  public boolean downloadChapters(List<Integer> chapterIds) {
    String query = """
        mutation downloadChapters($chapterIds: [Int!]!) {
          enqueueChapterDownloads(input: {ids: $chapterIds}) {
            downloadStatus {
              queue {
                chapter {
                  id
                }
              }
            }
          }
        }
        """;

    String variables = """
        {
          "chapterIds": %s
        }
        """.formatted(chapterIds.toString());

    var webClient = clientService.getWebClient();

    String json = GraphQLUtils.sendGraphQLRequest(query, variables, webClient);

    try {
      JsonObject jsonObject = Json.parse(json);
      var data = jsonObject.getObject("data");
      var enqueueChapterDownloads = data.getObject("enqueueChapterDownloads");
      var downloadStatus = enqueueChapterDownloads.getObject("downloadStatus");
      var queue = downloadStatus.getArray("queue");

      HashSet<Integer> ids = new HashSet<>();

      for (int i = 0; i < queue.length(); i++) {
        var chapter = queue.getObject(i).getObject("chapter");
        var id = (int) chapter.getNumber("id");
        ids.add(id);
      }

      return ids.containsAll(chapterIds);
    } catch (Exception e) {
      log.error("Error while parsing JSON", e);
      throw new RuntimeException(e);
    }
  }

  public boolean deleteChapter(int chapterId) {
    String query = """
        mutation deleteChapter($id: Int!) {
          deleteDownloadedChapter(input: {id: $id}) {
            chapters {
              isDownloaded
            }
          }
        }
        """;

    String variables = """
        {
          "id": %d
        }
        """.formatted(chapterId);

    var webClient = clientService.getWebClient();

    String json = GraphQLUtils.sendGraphQLRequest(query, variables, webClient);

    try {
      JsonObject jsonObject = Json.parse(json);
      var data = jsonObject.getObject("data");
      var deleteDownloadedChapter = data.getObject("deleteDownloadedChapter");
      var chapters = deleteDownloadedChapter.getObject("chapters");
      boolean isDownloaded = chapters.getBoolean("isDownloaded");
      return !isDownloaded;
    } catch (Exception e) {
      log.error("Error while parsing JSON", e);
      throw new RuntimeException(e);
    }
  }
}
