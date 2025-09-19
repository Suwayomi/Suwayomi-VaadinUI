/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.settings.FlareSolverrSettings;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.intellij.lang.annotations.Language;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * The SuwayomiSettingsClient class is responsible for making API requests to the Suwayomi Server
 * for updating and retrieving server settings.
 */
@Slf4j
@Component
public class SuwayomiSettingsClient {

  private final WebClientService clientService;

  /**
   * Creates a new instance of the {@link SuwayomiSettingsClient} class.
   *
   * @param clientService the {@link WebClientService} used for making API requests to the Suwayomi
   *     Server.
   */
  public SuwayomiSettingsClient(WebClientService clientService) {
    this.clientService = clientService;
  }

  /**
   * Updates the user's extension repositories on the Suwayomi Server.
   *
   * @param extensionRepoUrls a list of extension repository URLs as strings.
   * @return {@code true} if the extension repositories were updated successfully, {@code false}
   */
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

  /**
   * Retrieves the user's extension repositories from the Suwayomi Server.
   *
   * @return a list of extension repository URLs as strings.
   */
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

  /**
   * Retrieves the FlareSolverr settings from the Suwayomi Server.
   *
   * @return the FlareSolverr settings from the server as a {@link FlareSolverrSettings} object.
   */
  public FlareSolverrSettings getFlareSolverrSettings() {
    @Language("graphql")
    String query =
        """
            query GetFlareSolverrSettings {
              settings {
                flareSolverrEnabled
                flareSolverrSessionName
                flareSolverrSessionTtl
                flareSolverrTimeout
                flareSolverrUrl
              }
            }
            """;

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query).block();

    if (response == null) {
      throw new RuntimeException("Error while getting FlareSolverrSettings - response is null");
    }

    return response.extractValueAsObject("settings", FlareSolverrSettings.class);
  }

  /**
   * Updates the FlareSolverr URL on the Suwayomi Server.
   *
   * @param url the new FlareSolverr URL
   * @return {@code true} if the FlareSolverr URL was updated successfully, {@code false} otherwise
   */
  public boolean updateFlareSolverrUrl(String url) {
    @Language("graphql")
    String query =
        """
            mutation UpdateFlareSolverrUrl($url: String!) {
              setSettings(input: {settings: {flareSolverrUrl: $url}}) {
                settings {
                  flareSolverrUrl
                }
              }
            }
            """;

    var variables = Map.of("url", url);

    var graphClient = clientService.getDgsGraphQlClient();

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException("Error while updating FlareSolverr URL - response is null");
    }

    var flareSolverrUrl =
        response.extractValueAsObject("setSettings.settings.flareSolverrUrl", String.class);

    return flareSolverrUrl.equals(url);
  }

  /**
   * Updates the FlareSolverr enabled status on the Suwayomi Server.
   *
   * @param enabled the new enabled status
   * @return {@code true} if the FlareSolverr enabled status was updated successfully, {@code false}
   *     otherwise
   */
  public boolean updateFlareSolverrEnabledStatus(boolean enabled) {
    @Language("graphql")
    String query =
        """
            mutation UpdateFlareSolverrEnabledStatus($enabled: Boolean!) {
              setSettings(input: {settings: {flareSolverrEnabled: $enabled}}) {
                settings {
                  flareSolverrEnabled
                }
              }
            }
            """;

    var graphClient = clientService.getDgsGraphQlClient();

    var variables = Map.of("enabled", enabled);

    var response = graphClient.reactiveExecuteQuery(query, variables).block();

    if (response == null) {
      throw new RuntimeException(
          "Error while updating FlareSolverr enabled status - response is null");
    }

    var flareSolverrEnabled =
        response.extractValueAsObject("setSettings.settings.flareSolverrEnabled", Boolean.class);

    return flareSolverrEnabled.equals(enabled);
  }

  /**
   * Creates a backup on the Suwayomi Server, including categories and chapters.
   *
   * @return a String containing the relative API URL for the server API to download the created
   *     backup.
   */
  public String createBackup() {
    @Language("graphql")
    String query =
        """
        mutation createBackup {
          createBackup(input: {includeCategories: true, includeChapters: true}) {
            url
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();
    return graphClient.document(query).retrieve("createBackup.url").toEntity(String.class).block();
  }

  /**
   * Restores a backup to the Suwayomi server from the specified backup file.
   *
   * @param backupFile the {@link Path} to the backup file to be restored
   * @throws RuntimeException if the backup file does not exist, or if there is an error during the
   *     restoration process
   */
  public void restoreBackup(Path backupFile) {

    if (!Files.exists(backupFile)) {
      throw new RuntimeException("Backup file does not exist");
    }

    @Language("graphql")
    String query =
        """
        mutation RestoreBackup($backup: Upload!) {
          restoreBackup(input: {backup: $backup}) {
            status {
              totalManga
              state
              mangaProgress
            }
          }
        }
        """;

    query = query.replace("\n", "").strip();

    @Language("json")
    String operations =
        """
        {
          "query": "%s",
          "variables": {"backup":  null}
        }
        """;

    String operationBody = String.format(operations, query);

    @Language("json")
    String map = """
        {
          "0": ["variables.backup"]
        }
        """;

    File file = backupFile.toFile();
    FileSystemResource uploadFile = new FileSystemResource(file);

    MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("operations", operationBody);
    requestBody.add("map", map);
    requestBody.add("0", uploadFile);

    var webClient = clientService.getWebClient();

    String response =
        webClient
            .post()
            .uri("api/graphql")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(requestBody))
            .retrieve()
            .bodyToMono(String.class)
            .block();

    if (response == null) {
      throw new RuntimeException("Error while restoring backup");
    }

    log.debug("Restored backup: {}", response);
  }
}
