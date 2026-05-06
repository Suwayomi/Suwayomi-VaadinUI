package online.hatsunemiku.tachideskvaadinui.services.client;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.runtime.java.ApolloCallback;
import com.apollographql.apollo.runtime.java.ApolloClient;
import com.apollographql.apollo.runtime.java.ApolloDisposable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.DeleteChapterMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.DownloadChaptersMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.TrackDownloadsSubscription;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class DownloadClient {

  private final WebClientService clientService;

  public DownloadClient(WebClientService clientService) {
    this.clientService = clientService;
  }

  /**
   * Downloads the chapters specified by the given list of chapterIds.
   *
   * @param chapterIds The list of {@link online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter#getId() chapter IDs} to download.
   * @return True if all chapters were successfully downloaded, false otherwise.
   */
  public boolean downloadChapters(List<Integer> chapterIds) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<DownloadChaptersMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new DownloadChaptersMutation(chapterIds)).enqueue(new ApolloCallback<DownloadChaptersMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<DownloadChaptersMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while downloading chapters: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.enqueueChapterDownloads == null || data.enqueueChapterDownloads.downloadStatus == null) {
        throw new RuntimeException("Error while downloading chapters");
      }

      var newChapterIds = data.enqueueChapterDownloads.downloadStatus.queue.stream()
          .map(node -> node.chapter.id)
          .collect(Collectors.toList());

      // check if newChapterIds contains all chapterIds
      for (int chapterId : chapterIds) {
        if (!newChapterIds.contains(chapterId)) {
          return false;
        }
      }

      return true;
    } catch (Exception e) {
      throw new RuntimeException("Error while downloading chapters", e);
    }
  }

  /**
   * Deletes the chapter specified by the given chapterId.
   *
   * @param chapterId The {@link online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter#getId() chapter ID} to delete.
   * @return True if the chapter was successfully deleted, false otherwise.
   */
  public boolean deleteChapter(int chapterId) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<DeleteChapterMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new DeleteChapterMutation(chapterId)).enqueue(new ApolloCallback<DeleteChapterMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<DeleteChapterMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while deleting chapter: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.deleteDownloadedChapter == null || data.deleteDownloadedChapter.chapters == null) {
        throw new RuntimeException("Error while deleting chapter");
      }

      var deletionFail = data.deleteDownloadedChapter.chapters.isDownloaded;

      return !Boolean.TRUE.equals(deletionFail);
    } catch (Exception e) {
      throw new RuntimeException("Error while deleting chapter", e);
    }
  }

  public Flux<List<DownloadChangeEvent>> trackDownloads() {
    var apolloClient = clientService.getApolloClient();

    return Flux.create(sink -> {
        ApolloDisposable disposable = apolloClient.subscription(new TrackDownloadsSubscription()).enqueue(new ApolloCallback<TrackDownloadsSubscription.Data>() {
            @Override
            public void onResponse(@NotNull ApolloResponse<TrackDownloadsSubscription.Data> response) {
                if (response.hasErrors()) {
                    sink.error(new RuntimeException("Error in download subscription: " + response.errors));
                    return;
                }
                var data = response.data;
                if (data != null && data.downloadChanged != null) {
                    sink.next(data.downloadChanged.queue.stream()
                        .map(node -> new DownloadChangeEvent(
                            node.progress.floatValue(),
                            node.state.toString(),
                            new EnqueuedChapter(node.chapter.id)
                        ))
                        .collect(Collectors.toList()));
                }
            }
        });
        sink.onDispose(disposable::dispose);
    });
  }

  public record EnqueuedChapter(int id) {}

  public record DownloadChangeEvent(float progress, String state, EnqueuedChapter chapter) {}
}
