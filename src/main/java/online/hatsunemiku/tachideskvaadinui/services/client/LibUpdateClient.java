/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.springframework.stereotype.Component;

@Component
public class LibUpdateClient {

  private final WebClientService webClientService;

  public LibUpdateClient(WebClientService webClientService) {
    this.webClientService = webClientService;
  }

  public boolean fetchUpdate() {
    // TODO: Check for skipped jobs and throw custom exception if any
    // language=GraphQL
    String runningQuery =
        """
        mutation updateLibraryManga {
          updateLibraryManga(input: {}) {
            updateStatus {
              isRunning
            }
          }
        }
        """;

    var graphClient = webClientService.getGraphQlClient();

    Boolean isRunning =
        graphClient
            .document(runningQuery)
            .retrieve("updateLibraryManga.updateStatus.isRunning")
            .toEntity(Boolean.class)
            .block();

    if (isRunning == null) {
      throw new RuntimeException("Error while updating library");
    }

    if (!isRunning) {
      // language=GraphQL
      String hasSkippedQuery =
          """
              query hasSkipped {
                updateStatus {
                  skippedJobs {
                    mangas {
                      nodes {
                        id
                      }
                    }
                  }
                }
              }
              """;

      var skippedManga =
          graphClient
              .document(hasSkippedQuery)
              .retrieve("updateStatus.skippedJobs.mangas.nodes")
              .toEntityList(SkippedManga.class)
              .block();

      if (skippedManga == null) {
        throw new RuntimeException("Error while updating library");
      }

      isRunning = !skippedManga.isEmpty();
    }

    return isRunning;
  }

  private static class SkippedManga {
    @JsonProperty("id")
    private Long id;
  }
}
