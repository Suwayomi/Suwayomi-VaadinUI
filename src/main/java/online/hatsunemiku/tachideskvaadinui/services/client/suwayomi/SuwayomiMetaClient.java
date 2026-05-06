package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.runtime.java.ApolloCallback;
import com.apollographql.apollo.runtime.java.ApolloClient;
import java.util.concurrent.CompletableFuture;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.ServerVersion;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetServerVersionQuery;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/** Retrieves metadata about the Suwayomi Server through its API. */
@Component
public class SuwayomiMetaClient {

  private final WebClientService webClientService;

  /**
   * Creates a new instance of the {@link SuwayomiMetaClient} class.
   *
   * @param webClientService the {@link WebClientService} used for making API requests to the
   *     Suwayomi Server.
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
    var apolloClient = webClientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetServerVersionQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetServerVersionQuery()).enqueue(new ApolloCallback<GetServerVersionQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetServerVersionQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Failed to retrieve server version: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.aboutServer == null) {
        throw new RuntimeException("Failed to retrieve server version");
      }

      return new ServerVersion(data.aboutServer.version, data.aboutServer.revision);
    } catch (Exception e) {
      throw new RuntimeException("Failed to retrieve server version", e);
    }
  }
}
