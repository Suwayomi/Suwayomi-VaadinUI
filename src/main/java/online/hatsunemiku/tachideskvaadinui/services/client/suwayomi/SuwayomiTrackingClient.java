/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi;

import com.jayway.jsonpath.TypeRef;
import java.time.Duration;
import java.util.logging.Level;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SuwayomiTrackingClient {

  private static final Logger log = LoggerFactory.getLogger(SuwayomiTrackingClient.class);
  private final WebClientService clientService;

  public SuwayomiTrackingClient(WebClientService clientService) {
    this.clientService = clientService;
  }

  public boolean isTrackerLoggedIn(int id) {
    @Language("graphql")
    String query =
        """
            query IsTrackerLoggedIn($id: Int!) {
              tracker(id: $id) {
                isLoggedIn
                isTokenExpired
              }
            }
            """;

    Map<String, Integer> variables = Map.of("id", id);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while checking if tracker is logged in");
    }

    return response.extractValueAsObject("tracker.isLoggedIn", Boolean.class);
  }

  public String getTrackerAuthUrl(int id) {
    @Language("graphql")
    String query =
        """
            query GetTrackerAuthUrl($id: Int!) {
              tracker(id: $id) {
                authUrl
              }
            }
            """;

    Map<String, Integer> variables = Map.of("id", id);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while getting tracker auth url");
    }

    return response.extractValueAsObject("tracker.authUrl", String.class);
  }

  public void loginTracker(String url, int id) {
    @Language("graphql")
    String query = """
        mutation LoginTracker($url: String!, $trackerId: Int!) {
          loginTrackerOAuth(input: {callbackUrl: $url, trackerId: $trackerId}) {
            isLoggedIn
          }
        }
        """;

    Map<String, Object> variables = Map.of("url", url, "trackerId", id);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while logging in tracker");
    }

    if (!response.extractValueAsObject("loginTrackerOAuth.isLoggedIn", Boolean.class)) {
      log.error("Server returned false after logging in the tracker with id {}", id);
    }
  }

  public List<TrackerSearchResult> searchTracker(String query, int id) {
    @Language("graphql")
    String graphQuery = """
        query searchTracker($query: String!, $id: Int!) {
          searchTracker(input: {query: $query, trackerId: $id}) {
            trackSearches {
              coverUrl
              id
              publishingStatus
              publishingType
              remoteId
              startDate
              summary
              title
              totalChapters
              trackingUrl
            }
          }
        }
        """;

    Map<String, Object> variables = Map.of("query", query, "id", id);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(graphQuery, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while searching tracker");
    }

    if (response.hasErrors()) {
      String errorText = "Error while searching tracker: " + response.getErrors();
      log.error(errorText);
      throw new RuntimeException(errorText);
    }

    TypeRef<List<TrackerSearchResult>> typeRef = new TypeRef<>() {
    };

    return response.extractValueAsObject("searchTracker.trackSearches", typeRef);
  }

  public void trackMangaOnTracker(int mangaId, int externalId, int trackerId) {
    @Language("graphql")
    var query = """
        mutation TrackManga($mangaId: Int!, $remoteId: Int!, $trackerId: Int!) {
          bindTrack(input: {mangaId: $mangaId, remoteId: $remoteId, trackerId: $trackerId}) {
            trackRecord {
              id
            }
          }
        }
        """;

    var variables = Map.of("mangaId", mangaId, "externalId", externalId, "trackerId", trackerId);

    var graphClient = clientService.getDgsGraphQlClient();

    Duration timeout = Duration.ofSeconds(60);
    var optional = graphClient.reactiveExecuteQuery(query, variables).blockOptional(timeout);

    if (optional.isEmpty()) {
      throw new RuntimeException(
          "Didn't receive a response from the server after trying to track the manga");
    }
  }
}
