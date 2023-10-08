/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.springframework.stereotype.Component;

@Component
public class LibUpdateClient {

  private final WebClientService webClientService;

  public LibUpdateClient(WebClientService webClientService) {
    this.webClientService = webClientService;
  }

  public boolean fetchUpdate() {
    //language=GraphQL
    String query = """
        mutation updateLibraryManga {
          updateLibraryManga(input: {}) {
            updateStatus {
              isRunning
            }
          }
        }
        """;

    var graphClient = webClientService.getGraphQlClient();

    Boolean isRunning = graphClient.document(query)
        .retrieve("updateLibraryManga.updateStatus.isRunning")
        .toEntity(Boolean.class)
        .block();

    if (isRunning == null) {
      throw new RuntimeException("Error while updating library");
    }

    return isRunning;
  }
}
