/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi;

import java.util.List;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.springframework.stereotype.Component;

/**
 * The SuwayomiSettingsClient class is responsible for making API requests to the Suwayomi Server
 * for updating and retrieving server settings.
 */
@Component
public class SuwayomiSettingsClient {
  private final WebClientService clientService;

  public SuwayomiSettingsClient(WebClientService clientService) {
    this.clientService = clientService;
  }

  public boolean updateExtensionRepos(List<String> extensionRepoUrls) {
    // language=GraphQL
    String query =
        """
            mutation UpdateExtensionRepos($extensionRepoUrls: [String!]) {
              setSettings(input: {settings: {extensionRepos: $extensionRepoUrls}}) {
                settings {
                  extensionRepos
                }
              }
            }
            """;

    var graphClient = clientService.getGraphQlClient();

    var extensionRepos =
        graphClient
            .document(query)
            .variable("extensionRepoUrls", extensionRepoUrls)
            .retrieve("setSettings.settings.extensionRepos")
            .toEntityList(String.class)
            .block();

    // check if the extensionRepos are the same
    if (extensionRepos == null) {
      throw new RuntimeException("Error while updating extensionRepos");
    }

    return extensionRepos.equals(extensionRepoUrls);
  }

  public List<String> getExtensionRepos() {
    // language=GraphQL
    String query =
        """
            query GetExtensionRepos {
              settings {
                extensionRepos
              }
            }
            """;

    var graphClient = clientService.getGraphQlClient();

    var extensionRepos =
        graphClient
            .document(query)
            .retrieve("settings.extensionRepos")
            .toEntityList(String.class)
            .block();

    if (extensionRepos == null) {
      throw new RuntimeException("Error while getting extensionRepos");
    }

    return extensionRepos;
  }
}
