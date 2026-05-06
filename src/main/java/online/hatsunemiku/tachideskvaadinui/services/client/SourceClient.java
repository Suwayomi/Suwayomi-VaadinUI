package online.hatsunemiku.tachideskvaadinui.services.client;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.runtime.java.ApolloCallback;
import com.apollographql.apollo.runtime.java.ApolloClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.SourceMangaList;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetPopularSourceMangaMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.type.FetchSourceMangaType;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class SourceClient {

  private final WebClientService webClientService;

  public SourceClient(WebClientService clientService) {
    this.webClientService = clientService;
  }

  public SourceMangaList getPopularManga(String sourceId, int page) {
    return getMangaFromSource(sourceId, page, FetchSourceMangaType.POPULAR);
  }

  public SourceMangaList getLatestManga(String sourceId, int page) {
    return getMangaFromSource(sourceId, page, FetchSourceMangaType.LATEST);
  }

  private SourceMangaList getMangaFromSource(String sourceId, int page, FetchSourceMangaType type) {
    var apolloClient = webClientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetPopularSourceMangaMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new GetPopularSourceMangaMutation(sourceId, page, type)).enqueue(new ApolloCallback<GetPopularSourceMangaMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetPopularSourceMangaMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while fetching source manga: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.fetchSourceManga == null) {
        throw new RuntimeException("Error while fetching source manga");
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

      SourceMangaList sourceMangaList = new SourceMangaList();
      sourceMangaList.setMangaList(mangaList);
      sourceMangaList.setHasNextPage(Boolean.TRUE.equals(result.hasNextPage));
      return sourceMangaList;
    } catch (Exception e) {
      throw new RuntimeException("Error while fetching source manga", e);
    }
  }
}
