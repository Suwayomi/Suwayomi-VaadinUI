package online.hatsunemiku.tachideskvaadinui.services.client;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.api.Optional;
import com.apollographql.java.client.ApolloCallback;
import com.apollographql.java.client.ApolloDisposable;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;
import online.hatsunemiku.tachideskvaadinui.exceptions.AndroidOnlyException;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.*;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import online.hatsunemiku.tachideskvaadinui.services.client.exception.InvalidResponseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MangaClient {

    private final WebClientService clientService;

    public MangaClient(WebClientService clientService) {
        this.clientService = clientService;
    }

    public boolean addMangaToCategories(List<Integer> categoryIds, int mangaId) {
        var apolloClient = clientService.getApolloClient();

        CompletableFuture<ApolloResponse<AddMangaToCategoriesMutation.Data>> future = new CompletableFuture<>();
        apolloClient.mutation(new AddMangaToCategoriesMutation(new Optional.Present<List<Integer>>(categoryIds), mangaId)).enqueue(new ApolloCallback<AddMangaToCategoriesMutation.Data>() {
            @Override
            public void onResponse(@NotNull ApolloResponse<AddMangaToCategoriesMutation.Data> response) {
                future.complete(response);
            }
        });

        try {
            var response = future.join();
            if (response.hasErrors()) {
                throw new RuntimeException("Error while adding manga to categories: " + response.errors);
            }

            var data = response.data;
            if (data == null || data.updateMangaCategories == null || data.updateMangaCategories.manga == null) {
                throw new RuntimeException("Error while adding manga to categories");
            }

            var newCategoryIds = data.updateMangaCategories.manga.categories.nodes.stream()
                    .map(node -> node.id)
                    .collect(Collectors.toList());

            for (int categoryId : categoryIds) {
                if (!newCategoryIds.contains(categoryId)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error while adding manga to categories", e);
        }
    }

    public boolean removeMangaFromCategories(List<Integer> categoryIds, int mangaId) {
        var apolloClient = clientService.getApolloClient();

        CompletableFuture<ApolloResponse<RemoveMangaFromCategoriesMutation.Data>> future = new CompletableFuture<>();
        apolloClient.mutation(new RemoveMangaFromCategoriesMutation(new Optional.Present<List<Integer>>(categoryIds), mangaId)).enqueue(new ApolloCallback<RemoveMangaFromCategoriesMutation.Data>() {
            @Override
            public void onResponse(@NotNull ApolloResponse<RemoveMangaFromCategoriesMutation.Data> response) {
                future.complete(response);
            }
        });

        try {
            var response = future.join();
            if (response.hasErrors()) {
                throw new RuntimeException("Error while removing manga from categories: " + response.errors);
            }

            var data = response.data;
            if (data == null || data.updateMangaCategories == null || data.updateMangaCategories.manga == null) {
                throw new RuntimeException("Error while removing manga from categories");
            }

            var newCategoryIds = data.updateMangaCategories.manga.categories.nodes.stream()
                    .map(node -> node.id)
                    .collect(Collectors.toList());

            for (int id : categoryIds) {
                if (newCategoryIds.contains(id)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error while removing manga from categories", e);
        }
    }

    /**
     * Retrieves the chapter information based on the given chapter ID.
     *
     * @param chapterId The ID of the chapter to retrieve.
     * @return The Chapter object representing the retrieved chapter information.
     * @throws RuntimeException if an error occurs while parsing the JSON response.
     */
    public Chapter getChapter(long chapterId) {
        var apolloClient = clientService.getApolloClient();

        CompletableFuture<ApolloResponse<GetChapterQuery.Data>> future = new CompletableFuture<>();
        apolloClient.query(new GetChapterQuery((int) chapterId)).enqueue(new ApolloCallback<GetChapterQuery.Data>() {
            @Override
            public void onResponse(@NotNull ApolloResponse<GetChapterQuery.Data> response) {
                future.complete(response);
            }
        });

        try {
            var response = future.join();
            if (response.hasErrors()) {
                throw new RuntimeException("Error while getting chapter: " + response.errors);
            }

            var data = response.data;
            if (data == null || data.chapter == null) {
                return null;
            }

            return mapToChapter(data.chapter);
        } catch (Exception e) {
            throw new RuntimeException("Error while getting chapter", e);
        }
    }

    public List<Chapter> getChapters(int mangaId) {
        var apolloClient = clientService.getApolloClient();

        CompletableFuture<ApolloResponse<GetMangaChaptersQuery.Data>> future = new CompletableFuture<>();
        apolloClient.query(new GetMangaChaptersQuery(mangaId)).enqueue(new ApolloCallback<GetMangaChaptersQuery.Data>() {
            @Override
            public void onResponse(@NotNull ApolloResponse<GetMangaChaptersQuery.Data> response) {
                future.complete(response);
            }
        });

        try {
            var response = future.join();
            if (response.hasErrors()) {
                throw new RuntimeException("Error while getting manga chapters: " + response.errors);
            }

            var data = response.data;
            if (data == null || data.manga == null || data.manga.chapters == null) {
                return List.of();
            }

            return data.manga.chapters.nodes.stream()
                    .map(this::mapToChapter)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error while getting manga chapters", e);
        }
    }

    /**
     * Fetches the list of chapters for the given manga ID.
     *
     * @param mangaId The ID of the manga for which to fetch the chapters.
     * @return The list of {@link Chapter} objects representing the fetched chapters.
     * @throws InvalidResponseException if the response from the server is invalid
     * @throws RuntimeException         if there's an error fetching the corresponding manga
     */
    public List<Chapter> fetchChapterList(int mangaId) {
        // Fetch Manga to be able to fetch all chapters for it
        var manga = getManga(mangaId);

        if (manga == null) {
            throw new RuntimeException("Error while fetching manga " + mangaId);
        }

        var apolloClient = clientService.getApolloClient();

        CompletableFuture<ApolloResponse<FetchChapterListMutation.Data>> future = new CompletableFuture<>();
        apolloClient.mutation(new FetchChapterListMutation(mangaId)).enqueue(new ApolloCallback<FetchChapterListMutation.Data>() {
            @Override
            public void onResponse(@NotNull ApolloResponse<FetchChapterListMutation.Data> response) {
                future.complete(response);
            }
        });

        try {
            var response = future.join();
            if (response.hasErrors()) {
                throw new InvalidResponseException("Invalid response from server for manga " + mangaId, null);
            }

            var data = response.data;
            if (data == null || data.fetchChapters == null || data.fetchChapters.chapters == null) {
                return List.of();
            }

            return data.fetchChapters.chapters.stream()
                    .map(this::mapToChapter)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching chapter list", e);
        }
    }

    /**
     * Add a manga to the library.
     *
     * @param mangaId the ID of the manga to be added
     * @return true if the manga was successfully added to the library, false otherwise
     * @throws RuntimeException if there is an error while parsing the JSON response
     */
    public boolean addMangaToLibrary(int mangaId) {
        return updateMangaLibraryStatus(mangaId, true);
    }

    /**
     * Remove a manga from the library and all categories it's currently in.
     *
     * @param mangaId the ID of the manga to be removed
     * @return true if the manga was successfully removed from the library and its categories, false
     * otherwise
     */
    public boolean removeMangaFromLibrary(int mangaId) {

        var manga = getManga(mangaId);

        if (manga == null) {
            return false;
        }

        var categories = manga.getMangaCategories();

        if (categories != null) {
            var categoryIds = categories.stream().map(Category::getId).toList();

            if (!removeMangaFromCategories(categoryIds, mangaId)) {
                return false;
            }
        }

        return !updateMangaLibraryStatus(mangaId, false);
    }

    /**
     * Set a chapter as read.
     *
     * @param chapterId the ID of the chapter to be marked as read
     * @return {@code true} if the chapter was successfully marked as read, {@code false} otherwise
     * @throws RuntimeException if there is an error while updating the chapter status
     */
    public boolean setChapterRead(int chapterId) {
        return updateChapterReadStatus(chapterId, true);
    }

    /**
     * Set a chapter as unread.
     *
     * @param chapterId the ID of the chapter to be marked as unread
     * @return {@code true} if the chapter was successfully marked as unread, {@code false} otherwise
     * @throws RuntimeException if there is an error while updating the chapter status
     */
    public boolean setChapterUnread(int chapterId) {
        return !updateChapterReadStatus(chapterId, false);
    }

    public Manga getManga(long mangaId) {
        var apolloClient = clientService.getApolloClient();

        CompletableFuture<ApolloResponse<FetchMangaMutation.Data>> future = new CompletableFuture<>();
        apolloClient.mutation(new FetchMangaMutation((int) mangaId)).enqueue(future::complete);

        try {
            var response = future.join();
            if (response.hasErrors()) {
                throw new RuntimeException("Error while fetching manga: " + response.errors);
            }

            var data = response.data;
            if (data == null || data.fetchManga == null || data.fetchManga.manga == null) {
                return null;
            }

            return mapToManga(data.fetchManga.manga);
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching manga", e);
        }
    }

    /**
     * Update the read status of a specific chapter.
     *
     * @param chapterId the ID of the chapter to update
     * @param read      the new read status of the chapter
     * @return the new read status of the chapter after the update, either {@code true} or {@code
     * false}
     * @throws RuntimeException if there is an error while parsing the JSON response
     */
    private boolean updateChapterReadStatus(int chapterId, boolean read) {
        var apolloClient = clientService.getApolloClient();

        CompletableFuture<ApolloResponse<SetChapterReadStatusMutation.Data>> future = new CompletableFuture<>();
        apolloClient.mutation(new SetChapterReadStatusMutation(chapterId, read)).enqueue(new ApolloCallback<SetChapterReadStatusMutation.Data>() {
            @Override
            public void onResponse(@NotNull ApolloResponse<SetChapterReadStatusMutation.Data> response) {
                future.complete(response);
            }
        });

        try {
            var response = future.join();
            if (response.hasErrors()) {
                throw new RuntimeException("Error while updating chapter read status: " + response.errors);
            }

            var data = response.data;
            if (data == null || data.updateChapter == null || data.updateChapter.chapter == null) {
                return false;
            }

            return Boolean.TRUE.equals(data.updateChapter.chapter.isRead);
        } catch (Exception e) {
            throw new RuntimeException("Error while updating chapter read status", e);
        }
    }

    /**
     * Updates the library status of a manga.
     *
     * @param mangaId the ID of the manga to update
     * @param add     true to add the manga to the library, false to remove it from the library
     * @return true if the manga is in the library after the update, false otherwise
     * @throws RuntimeException if there is an error while parsing the JSON response
     */
    private boolean updateMangaLibraryStatus(int mangaId, boolean add) {
        var apolloClient = clientService.getApolloClient();

        CompletableFuture<ApolloResponse<UpdateMangaLibraryStatusMutation.Data>> future = new CompletableFuture<>();
        apolloClient.mutation(new UpdateMangaLibraryStatusMutation(mangaId, add)).enqueue(future::complete);

        try {
            var response = future.join();
            if (response.hasErrors()) {
                throw new RuntimeException("Error while updating manga library status: " + response.errors);
            }

            var data = response.data;
            if (data == null || data.updateManga == null || data.updateManga.manga == null) {
                return false;
            }

            return Boolean.TRUE.equals(data.updateManga.manga.inLibrary);
        } catch (Exception e) {
            throw new RuntimeException("Error while updating manga library status", e);
        }
    }

    public List<String> getChapterPages(int chapterId) {
        var apolloClient = clientService.getApolloClient();

        CompletableFuture<ApolloResponse<GetChapterPagesMutation.Data>> future = new CompletableFuture<>();
        apolloClient.mutation(new GetChapterPagesMutation(chapterId)).enqueue(future::complete);

        var response = future.join();
        if (response.hasErrors()) {
            assert response.errors != null;
            if (response.errors.getFirst().getMessage().contains("kotlinx-coroutines-android")) {
                throw new AndroidOnlyException();
            }

            throw new RuntimeException("Error while getting chapter pages: " + response.errors);
        }

        var data = response.data;
        if (data == null || data.fetchChapterPages == null) {
            return List.of();
        }

        return data.fetchChapterPages.pages;
    }

    /**
     * Retrieves a list of {@link Manga} that are currently in the library.
     *
     * @return the list of manga, which are in the library
     */
    public List<Manga> getLibraryManga() {
        var apolloClient = clientService.getApolloClient();

        CompletableFuture<ApolloResponse<GetLibraryMangaQuery.Data>> future = new CompletableFuture<>();
        apolloClient.query(new GetLibraryMangaQuery()).enqueue(future::complete);

        try {
            var response = future.join();
            if (response.hasErrors()) {
                throw new RuntimeException("Error while retrieving library manga: " + response.errors);
            }

            var data = response.data;
            if (data == null || data.categories == null) {
                throw new RuntimeException("Error while retrieving library manga");
            }

            return data.categories.nodes.parallelStream()
                    .flatMap(node -> node.mangas.nodes.stream())
                    .map(this::mapToManga)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error while retrieving library manga", e);
        }
    }

    private Chapter mapToChapter(GetChapterQuery.Chapter node) {
        Chapter chapter = new Chapter();
        chapter.setId(node.id);
        chapter.setName(node.name);
        chapter.setChapterNumber(node.chapterNumber.floatValue());
        chapter.setDownloaded(Boolean.TRUE.equals(node.isDownloaded));
        chapter.setRead(false);
        chapter.setMangaId(node.mangaId);
        chapter.setUrl("");
        chapter.setPageCount(node.pageCount);
        return chapter;
    }

    private Chapter mapToChapter(GetMangaChaptersQuery.Node node) {
        Chapter chapter = new Chapter();
        chapter.setId(node.id);
        chapter.setName(node.name);
        chapter.setChapterNumber(node.chapterNumber.floatValue());
        chapter.setDownloaded(Boolean.TRUE.equals(node.isDownloaded));
        chapter.setRead(Boolean.TRUE.equals(node.isRead));
        chapter.setMangaId(node.mangaId);
        chapter.setUrl(node.url);
        chapter.setPageCount(node.pageCount);
        return chapter;
    }

    private Chapter mapToChapter(FetchChapterListMutation.Chapter node) {
        Chapter chapter = new Chapter();
        chapter.setId(node.id);
        chapter.setName(node.name);
        chapter.setChapterNumber(node.chapterNumber.floatValue());
        chapter.setDownloaded(Boolean.TRUE.equals(node.isDownloaded));
        chapter.setRead(Boolean.TRUE.equals(node.isRead));
        chapter.setMangaId(node.mangaId);
        chapter.setUrl(node.url);
        chapter.setPageCount(node.pageCount);
        return chapter;
    }

    private Manga mapToManga(FetchMangaMutation.Manga node) {
        Manga manga = new Manga();
        manga.setId(node.id);
        manga.setAuthor(node.author);
        manga.setTitle(node.title);
        manga.setDescription(node.description);
        manga.setThumbnailUrl(node.thumbnailUrl);
        manga.setInLibrary(Boolean.TRUE.equals(node.inLibrary));
        manga.setStatus(node.status != null ? node.status.rawValue : null);

        if (node.source != null) {
            Source source = new Source();
            source.setId(node.source.id != null ? node.source.id.toString() : null);
            source.setName(node.source.name);
            source.setDisplayName(node.source.displayName);
            source.setLang(node.source.lang);
            source.setIconUrl(node.source.iconUrl);
            source.setSupportsLatest(Boolean.TRUE.equals(node.source.supportsLatest));
            source.setConfigurable(Boolean.TRUE.equals(node.source.isConfigurable));
            source.setNsfw(Boolean.TRUE.equals(node.source.isNsfw));
            manga.setSource(source);
        }

        if (node.lastReadChapter != null) {
            Chapter lastChapter = new Chapter();
            lastChapter.setId(node.lastReadChapter.id);
            manga.setLastChapterRead(lastChapter);
        }

        if (node.categories != null && node.categories.nodes != null) {
            manga.setCategories(new Manga.MangaCategories(node.categories.nodes.stream()
                    .map(catNode -> {
                        Category c = new Category();
                        c.setId(catNode.id);
                        return c;
                    }).collect(Collectors.toList())));
        }

        if (node.chapters != null && node.chapters.nodes != null) {
            manga.setChapterCount(node.chapters.nodes.size());
        }

        return manga;
    }

    private Manga mapToManga(GetLibraryMangaQuery.Node1 node) {
        Manga manga = new Manga();
        manga.setId(node.id);
        manga.setTitle(node.title);
        manga.setThumbnailUrl(node.thumbnailUrl);
        manga.setInLibrary(Boolean.TRUE.equals(node.inLibrary));

        if (node.lastReadChapter != null) {
            Chapter lastChapter = new Chapter();
            lastChapter.setId(node.lastReadChapter.id);
            manga.setLastChapterRead(lastChapter);
        }

        return manga;
    }
}
