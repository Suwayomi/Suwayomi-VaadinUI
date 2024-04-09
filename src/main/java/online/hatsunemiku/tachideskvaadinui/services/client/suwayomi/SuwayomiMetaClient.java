/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi;

import online.hatsunemiku.tachideskvaadinui.data.tachidesk.ServerVersion;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Component;

/** Retrieves metadata about the Suwayomi Server through its API. */
@Component
public class SuwayomiMetaClient {

  private final WebClientService webClientService;

  /**
   * Creates a new instance of the {@link SuwayomiMetaClient} class.
   *
   * @param webClientService the {@link WebClientService} used for making API requests to the
   *                         Suwayomi Server.
   */
  public SuwayomiMetaClient(WebClientService webClientService) {
    this.webClientService = webClientService;
  }

  /**
   * Retrieves the version of the Suwayomi Server though its API. This method will block until the
   * response is received.
   *
   * @return the version of the Suwayomi Server.
   */
  public ServerVersion getServerVersion() {
    var client = webClientService.getDgsGraphQlClient();

    @Language("graphql")
    String query =
        """
            query {
              aboutServer {
                version
                revision
              }
            }
            """;

    var response = client.reactiveExecuteQuery(query).block();

    if (response == null) {
      throw new RuntimeException("Failed to retrieve server version");
    }

    return response.extractValueAsObject("aboutServer", ServerVersion.class);
  }
}
