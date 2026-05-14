package online.hatsunemiku.tachideskvaadinui.services.client;

import com.apollographql.apollo.api.ApolloResponse;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SourceSearchResult;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.SearchSourceMutation;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class SearchClient {

  private final WebClientService webClientService;

  public SearchClient(WebClientService clientService) {
    this.webClientService = clientService;
  }

  public SourceSearchResult search(String searchQuery, int page, String sourceId) {
    var apolloClient = webClientService.getApolloClient();

    CompletableFuture<ApolloResponse<SearchSourceMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new SearchSourceMutation(sourceId, page, searchQuery)).enqueue(future::complete);

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
