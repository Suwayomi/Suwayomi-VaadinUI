/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExtensionClient {

  private final WebClientService clientService;
  private final ObjectMapper objectMapper;

  public ExtensionClient(WebClientService clientService, ObjectMapper objectMapper) {
    this.clientService = clientService;
    this.objectMapper = objectMapper;
  }

  public boolean updateExtension(String extensionId) {
    //language=GraphQL
    String query = """
        mutation UpdateExtension($extensionId: String!) {
          updateExtension(input: {id: $extensionId, patch: {update: true}}) {
            extension {
              hasUpdate
            }
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    Boolean hasUpdate = graphClient.document(query)
        .variable("extensionId", extensionId)
        .retrieve("updateExtension.extension.hasUpdate")
        .toEntity(Boolean.class)
        .block();

    if (hasUpdate == null) {
      throw new RuntimeException("Error while updating extension");
    }

    return !hasUpdate;
  }

  /**
   * Retrieves a list of extensions from the GraphQL server.
   *
   * @return a {@link List list} of {@link Extension} objects
   * @throws RuntimeException if there is an error while retrieving the extensions
   */
  public List<Extension> getExtensions() {
    //language=GraphQL
    String query = """
        query GetExtensions {
          extensions {
            nodes {
              pkgName
              apkName
              isInstalled
              isNsfw
              isObsolete
              lang
              name
              hasUpdate
              iconUrl
            }
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    //can't use toEntityList because there's too much data, so it exceeds the default buffer size
    var extensions = graphClient.document(query)
        .retrieve("extensions.nodes")
        .toEntityList(Extension.class)
        .block();

    if (extensions == null || extensions.isEmpty()) {
      throw new RuntimeException("Error while retrieving extensions");
    }

    return extensions;
  }

  /**
   * Installs an extension with the given extension ID.
   *
   * @param extensionId the ID of the extension to install
   * @return {@code true} if the extension is installed successfully, {@code false} otherwise
   * @throws RuntimeException if there is an error while installing the extension
   */
  public boolean installExtension(String extensionId) {
    return updateExtensionInstallStatus(extensionId, true);
  }

  /**
   * Uninstalls an extension with the given extension ID.
   *
   * @param extensionId the ID of the extension to uninstall
   * @return {@code true} if the extension is uninstalled successfully, {@code false} otherwise
   * @throws RuntimeException if there is an error while uninstalling the extension
   */
  public boolean uninstallExtension(String extensionId) {
    return !updateExtensionInstallStatus(extensionId, false);
  }

  private boolean updateExtensionInstallStatus(String extensionId, boolean install) {
    //language=GraphQL
    String query = """
        mutation installExtension($extensionId: String!, $install: Boolean!) {
          updateExtension(input: {id: $extensionId, patch: {install: $install}}) {
            extension {
              isInstalled
            }
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    Boolean isInstalled = graphClient.document(query)
        .variable("extensionId", extensionId)
        .variable("install", install)
        .retrieve("updateExtension.extension.isInstalled")
        .toEntity(Boolean.class)
        .block();

    if (isInstalled == null) {
      throw new RuntimeException("Error while updating extension install status");
    }

    return isInstalled;
  }
}
