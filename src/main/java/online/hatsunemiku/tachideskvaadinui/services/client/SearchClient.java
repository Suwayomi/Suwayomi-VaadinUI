/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SourceSearchResult;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SourceSearchResult.SearchResponse;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.springframework.stereotype.Component;

@Component
public class SearchClient {

  private final WebClientService webClientService;

  public SearchClient(WebClientService webClientService) {
    this.webClientService = webClientService;
  }

  public SourceSearchResult search(String searchQuery, int page, String sourceId) {
    // language=GraphQL
    String query =
        """
        mutation searchSource($sourceId: LongString!, $page: Int!, $query: String!) {
          fetchSourceManga(
            input: {page: $page, source: $sourceId, type: SEARCH, query: $query}
          ) {
            mangas {
              id
              thumbnailUrl
              title
            }
            hasNextPage
          }
        }
        """;

    var graphClient = webClientService.getGraphQlClient();

    var result =
        graphClient
            .document(query)
            .variable("sourceId", sourceId)
            .variable("page", page)
            .variable("query", searchQuery)
            .retrieve("fetchSourceManga")
            .toEntity(SearchResponse.class)
            .block();

    if (result == null) {
      throw new RuntimeException("Error while searching");
    }

    return new SourceSearchResult(result.mangas(), result.hasNextPage(), page);
  }
}
