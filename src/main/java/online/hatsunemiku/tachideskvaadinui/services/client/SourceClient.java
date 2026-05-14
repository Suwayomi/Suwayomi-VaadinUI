package online.hatsunemiku.tachideskvaadinui.services.client;

import com.apollographql.apollo.api.ApolloResponse;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.SourceMangaList;
import online.hatsunemiku.tachideskvaadinui.exceptions.CloudflareException;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetPopularSourceMangaMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.type.FetchSourceMangaType;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        apolloClient.mutation(new GetPopularSourceMangaMutation(sourceId, page, type)).enqueue(future::complete);

        try {
            var response = future.join();
            if (response.hasErrors()) {
                assert response.errors != null;
                if (response.errors.getFirst().getMessage().contains("Cloudflare bypass currently disabled")) {
                    throw new CloudflareException("Cloudflare bypass currently disabled");
                }
                throw new RuntimeException("Error while fetching source manga: " + response.errors.getFirst().getMessage());
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
        } catch (CloudflareException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching source manga", e);
        }
    }
}
