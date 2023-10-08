/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import online.hatsunemiku.tachideskvaadinui.services.client.DownloadClient.EnqueueChapterDownloadId.EnqueuedChapter;
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

    var graphClient = clientService.getGraphQlClient();

    var tempChapterIds = graphClient.document(query)
        .variable("chapterIds", chapterIds)
        .retrieve("enqueueChapterDownloads.downloadStatus.queue")
        .toEntityList(EnqueueChapterDownloadId.class)
        .block();

    if (tempChapterIds == null) {
      throw new RuntimeException("Error while downloading chapters");
    }

    var newChapterIds = tempChapterIds.stream()
        .filter(Objects::nonNull)
        .map(EnqueueChapterDownloadId::chapter)
        .map(EnqueuedChapter::id)
        .toList();

    //check if newChapterIds contains all chapterIds
    for (int chapterId : chapterIds) {
      if (!newChapterIds.contains(chapterId)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Deletes the chapter specified by the given chapterId.
   *
   * @param chapterId The {@link Chapter#getId() chapter ID} to delete.
   * @return True if the chapter was successfully deleted, false otherwise.
   */
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

    var graphClient = clientService.getGraphQlClient();

    var deletionFail = graphClient.document(query)
        .variable("id", chapterId)
        .retrieve("deleteDownloadedChapter.chapters.isDownloaded")
        .toEntity(Boolean.class)
        .block();

    if (deletionFail == null) {
      throw new RuntimeException("Error while deleting chapter");
    }

    return !deletionFail;
  }

  protected record EnqueueChapterDownloadId(EnqueuedChapter chapter) {

    protected record EnqueuedChapter(int id) {}
  }
}
