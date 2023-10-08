/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import online.hatsunemiku.tachideskvaadinui.data.tachidesk.SourceMangaList;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.springframework.stereotype.Component;

@Component
public class SourceClient {

  private final WebClientService webClientService;

  public SourceClient(WebClientService webClientService) {
    this.webClientService = webClientService;
  }

  public SourceMangaList getPopularManga(String sourceId, int page) {
    return getMangaFromSource(sourceId, page, SourceType.POPULAR);
  }

  public SourceMangaList getLatestManga(String sourceId, int page) {
    return getMangaFromSource(sourceId, page, SourceType.LATEST);
  }

  private SourceMangaList getMangaFromSource(String sourceId, int page, SourceType type) {
    //language=GraphQL
    String query = """
        mutation getPopularSourceManga($sourceId: LongString!, $page: Int!, $type: FetchSourceMangaType!) {
          fetchSourceManga(input: {page: $page, source: $sourceId, type: $type}) {
            hasNextPage
            mangas {
              id
              thumbnailUrl
              title
            }
          }
        }
        """;

    var graphClient = webClientService.getGraphQlClient();

    var result = graphClient.document(query)
        .variable("sourceId", sourceId)
        .variable("page", page)
        .variable("type", type)
        .retrieve("fetchSourceManga")
        .toEntity(SourceMangaList.class)
        .block();

    if (result == null) {
      throw new RuntimeException("Error while fetching popular manga");
    }

    return result;
  }



  private enum SourceType {
    POPULAR,
    LATEST
  }

}
