/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import feign.FeignException;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SourceSearchResult;
import online.hatsunemiku.tachideskvaadinui.services.client.SearchClient;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

  private final SearchClient searchClient;

  public SearchService(SearchClient searchClient) {
    this.searchClient = searchClient;
  }

  /**
   * Performs a search using the provided query, sourceId, and pageNum.
   *
   * @param query The search query.
   * @param sourceId The sourceId to search within.
   * @param pageNum The page number for pagination.
   * @return The search response containing the results.
   * @throws FeignException if an error occurs during the search.
   */
  public SourceSearchResult search(String query, String sourceId, int pageNum) {
    return searchClient.search(query, pageNum, sourceId);
  }
}
