package online.hatsunemiku.tachideskvaadinui.services.client;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.runtime.java.ApolloCallback;
import com.apollographql.apollo.runtime.java.ApolloClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SourceSearchResult;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.SearchSourceMutation;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class SearchClient {

  private final WebClientService webClientService;

  public SearchClient(WebClientService clientService) {
    this.webClientService = clientService;
  }

  public SourceSearchResult search(String searchQuery, int page, String sourceId) {
    var apolloClient = webClientService.getApolloClient();

    CompletableFuture<ApolloResponse<SearchSourceMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new SearchSourceMutation(sourceId, page, searchQuery)).enqueue(new ApolloCallback<SearchSourceMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<SearchSourceMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while searching: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.fetchSourceManga == null) {
        throw new RuntimeException("Error while searching");
      }

      var result = data.fetchSourceManga;

      var mangaList = result.mangas.stream()
          .map(node -> {
            Manga manga = new Manga();
            manga.setId(node.id);
            manga.setThumbnailUrl(node.thumbnailUrl);
            manga.setTitle(node.title);
            manga.setInLibrary(false);
            return manga;
          })
          .collect(Collectors.toList());

      return new SourceSearchResult(mangaList, Boolean.TRUE.equals(result.hasNextPage), page);
    } catch (Exception e) {
      throw new RuntimeException("Error while searching", e);
    }
  }
}
