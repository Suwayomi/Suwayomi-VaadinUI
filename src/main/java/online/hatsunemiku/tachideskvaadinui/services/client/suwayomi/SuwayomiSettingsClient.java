package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.api.Optional;
import com.apollographql.apollo.api.DefaultUpload;
import com.apollographql.apollo.api.Upload;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.runtime.java.ApolloCallback;
import com.apollographql.apollo.runtime.java.ApolloClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.settings.FlareSolverrSettings;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.CreateBackupMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetExtensionReposQuery;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetFlareSolverrSettingsQuery;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.RestoreBackupMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.UpdateExtensionReposMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.UpdateFlareSolverrEnabledStatusMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.UpdateFlareSolverrUrlMutation;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

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
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<UpdateExtensionReposMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new UpdateExtensionReposMutation(new Optional.Present<List<String>>(extensionRepoUrls))).enqueue(new ApolloCallback<UpdateExtensionReposMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<UpdateExtensionReposMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while updating extensionRepos: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.setSettings == null || data.setSettings.settings == null) {
        throw new RuntimeException("Error while updating extensionRepos");
      }

      return data.setSettings.settings.extensionRepos.equals(extensionRepoUrls);
    } catch (Exception e) {
      throw new RuntimeException("Error while updating extensionRepos", e);
    }
  }

  /**
   * Retrieves the user's extension repositories from the Suwayomi Server.
   *
   * @return a list of extension repository URLs as strings.
   */
  public List<String> getExtensionRepos() {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetExtensionReposQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetExtensionReposQuery()).enqueue(new ApolloCallback<GetExtensionReposQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetExtensionReposQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while getting extensionRepos: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.settings == null) {
        throw new RuntimeException("Error while getting extensionRepos");
      }

      return data.settings.extensionRepos;
    } catch (Exception e) {
      throw new RuntimeException("Error while getting extensionRepos", e);
    }
  }

  /**
   * Retrieves the FlareSolverr settings from the Suwayomi Server.
   *
   * @return the FlareSolverr settings from the server as a {@link FlareSolverrSettings} object.
   */
  public FlareSolverrSettings getFlareSolverrSettings() {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetFlareSolverrSettingsQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetFlareSolverrSettingsQuery()).enqueue(new ApolloCallback<GetFlareSolverrSettingsQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetFlareSolverrSettingsQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while getting FlareSolverrSettings: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.settings == null) {
        throw new RuntimeException("Error while getting FlareSolverrSettings - data is null");
      }

      FlareSolverrSettings settings = new FlareSolverrSettings();
      settings.setEnabled(Boolean.TRUE.equals(data.settings.flareSolverrEnabled));
      settings.setSessionName(data.settings.flareSolverrSessionName);
      settings.setSessionTTL(data.settings.flareSolverrSessionTtl != null ? data.settings.flareSolverrSessionTtl.intValue() : 0);
      settings.setTimeout(data.settings.flareSolverrTimeout != null ? data.settings.flareSolverrTimeout.intValue() : 0);
      settings.setUrl(data.settings.flareSolverrUrl);
      return settings;
    } catch (Exception e) {
      throw new RuntimeException("Error while getting FlareSolverrSettings", e);
    }
  }

  /**
   * Updates the FlareSolverr URL on the Suwayomi Server.
   *
   * @param url the new FlareSolverr URL
   * @return {@code true} if the FlareSolverr URL was updated successfully, {@code false} otherwise
   */
  public boolean updateFlareSolverrUrl(String url) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<UpdateFlareSolverrUrlMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new UpdateFlareSolverrUrlMutation(url)).enqueue(new ApolloCallback<UpdateFlareSolverrUrlMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<UpdateFlareSolverrUrlMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while updating FlareSolverr URL: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.setSettings == null || data.setSettings.settings == null) {
        throw new RuntimeException("Error while updating FlareSolverr URL - data is null");
      }

      return data.setSettings.settings.flareSolverrUrl.equals(url);
    } catch (Exception e) {
      throw new RuntimeException("Error while updating FlareSolverr URL", e);
    }
  }

  /**
   * Updates the FlareSolverr enabled status on the Suwayomi Server.
   *
   * @param enabled the new enabled status
   * @return {@code true} if the FlareSolverr enabled status was updated successfully, {@code false}
   *     otherwise
   */
  public boolean updateFlareSolverrEnabledStatus(boolean enabled) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<UpdateFlareSolverrEnabledStatusMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new UpdateFlareSolverrEnabledStatusMutation(enabled)).enqueue(new ApolloCallback<UpdateFlareSolverrEnabledStatusMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<UpdateFlareSolverrEnabledStatusMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while updating FlareSolverr enabled status: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.setSettings == null || data.setSettings.settings == null) {
        throw new RuntimeException("Error while updating FlareSolverr enabled status - data is null");
      }

      return Boolean.TRUE.equals(data.setSettings.settings.flareSolverrEnabled) == enabled;
    } catch (Exception e) {
      throw new RuntimeException("Error while updating FlareSolverr enabled status", e);
    }
  }

  /**
   * Creates a backup on the Suwayomi Server, including categories and chapters.
   *
   * @return a String containing the relative API URL for the server API to download the created
   *     backup.
   */
  public String createBackup() {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<CreateBackupMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new CreateBackupMutation()).enqueue(new ApolloCallback<CreateBackupMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<CreateBackupMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while creating backup: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.createBackup == null) {
        throw new RuntimeException("Error while creating backup");
      }

      return data.createBackup.url;
    } catch (Exception e) {
      throw new RuntimeException("Error while creating backup", e);
    }
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

    var apolloClient = clientService.getApolloClient();

    try {
        byte[] bytes = Files.readAllBytes(backupFile);
        Upload upload = new DefaultUpload.Builder()
            .content(bytes)
            .contentType("application/octet-stream")
            .build();

        CompletableFuture<ApolloResponse<RestoreBackupMutation.Data>> future = new CompletableFuture<>();
        apolloClient.mutation(new RestoreBackupMutation(upload)).enqueue(new ApolloCallback<RestoreBackupMutation.Data>() {
          @Override
          public void onResponse(@NotNull ApolloResponse<RestoreBackupMutation.Data> response) {
            future.complete(response);
          }
        });

        var response = future.join();
        if (response.hasErrors()) {
          throw new RuntimeException("Error while restoring backup: " + response.errors);
        }

        var data = response.data;
        if (data == null || data.restoreBackup == null) {
          throw new RuntimeException("Error while restoring backup");
        }

        log.debug("Restored backup: {}", data.restoreBackup.status);
    } catch (Exception e) {
      throw new RuntimeException("Error while restoring backup", e);
    }
  }
}
