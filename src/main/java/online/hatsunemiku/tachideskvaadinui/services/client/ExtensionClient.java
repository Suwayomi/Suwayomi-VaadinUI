package online.hatsunemiku.tachideskvaadinui.services.client;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.runtime.java.ApolloCallback;
import com.apollographql.apollo.runtime.java.ApolloClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetExtensionsMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.InstallExtensionMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.UpdateExtensionMutation;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExtensionClient {

  private final WebClientService clientService;

  public ExtensionClient(WebClientService clientService) {
    this.clientService = clientService;
  }

  public boolean updateExtension(String extensionId) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<UpdateExtensionMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new UpdateExtensionMutation(extensionId)).enqueue(new ApolloCallback<UpdateExtensionMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<UpdateExtensionMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while updating extension: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.updateExtension == null || data.updateExtension.extension == null) {
        throw new RuntimeException("Error while updating extension");
      }

      return !Boolean.TRUE.equals(data.updateExtension.extension.hasUpdate);
    } catch (Exception e) {
      throw new RuntimeException("Error while updating extension", e);
    }
  }

  /**
   * Retrieves a list of extensions from the GraphQL server.
   *
   * @return a {@link List list} of {@link Extension} objects
   * @throws RuntimeException if there is an error while retrieving the extensions
   */
  public List<Extension> getExtensions() {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetExtensionsMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new GetExtensionsMutation()).enqueue(new ApolloCallback<GetExtensionsMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetExtensionsMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while retrieving extensions: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.fetchExtensions == null || data.fetchExtensions.extensions == null) {
        throw new RuntimeException("Error while retrieving extensions");
      }

      return data.fetchExtensions.extensions.stream()
          .map(node -> {
            Extension extension = new Extension();
            extension.setPkgName(node.pkgName);
            extension.setApkName(node.apkName);
            extension.setInstalled(Boolean.TRUE.equals(node.isInstalled));
            extension.setNsfw(Boolean.TRUE.equals(node.isNsfw));
            extension.setObsolete(Boolean.TRUE.equals(node.isObsolete));
            extension.setLang(node.lang);
            extension.setName(node.name);
            extension.setHasUpdate(Boolean.TRUE.equals(node.hasUpdate));
            extension.setIconUrl(node.iconUrl);
            return extension;
          })
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error while retrieving extensions", e);
    }
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
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<InstallExtensionMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new InstallExtensionMutation(extensionId, install, !install)).enqueue(new ApolloCallback<InstallExtensionMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<InstallExtensionMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while updating extension install status: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.updateExtension == null || data.updateExtension.extension == null) {
        throw new RuntimeException("Error while updating extension install status");
      }

      return Boolean.TRUE.equals(data.updateExtension.extension.isInstalled);
    } catch (Exception e) {
      throw new RuntimeException("Error while updating extension install status", e);
    }
  }
}
