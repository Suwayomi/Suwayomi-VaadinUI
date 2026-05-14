package online.hatsunemiku.tachideskvaadinui.services.client;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCallback;
import com.apollographql.java.client.ApolloDisposable;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.event.MangaUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.HasSkippedQuery;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.TrackMangaUpdateSubscription;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.UpdateLibraryMangaMutation;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Client responsible for any server communication related to manga library updates.
 *
 * @version 1.12.0
 * @since 0.9.0
 */
@Component
public class LibUpdateClient {

  private final WebClientService webClientService;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Creates a new {@link LibUpdateClient} instance.
   *
   * @param webClientService The {@link WebClientService} used to communicate with the server
   * @param eventPublisher The {@link ApplicationEventPublisher} used to publish events
   */
  public LibUpdateClient(
      WebClientService webClientService, ApplicationEventPublisher eventPublisher) {
    this.webClientService = webClientService;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Starts the library update process on the server.
   *
   * @return {@code true} if the update process has started running, {@code false} otherwise
   */
  public boolean fetchUpdate() {
    var apolloClient = webClientService.getApolloClient();

    CompletableFuture<ApolloResponse<UpdateLibraryMangaMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new UpdateLibraryMangaMutation()).enqueue(new ApolloCallback<UpdateLibraryMangaMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<UpdateLibraryMangaMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while updating library: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.updateLibraryManga == null || data.updateLibraryManga.updateStatus == null) {
        throw new RuntimeException("Error while updating library");
      }

      Boolean isRunning = data.updateLibraryManga.updateStatus.isRunning;

      if (!Boolean.TRUE.equals(isRunning)) {
        CompletableFuture<ApolloResponse<HasSkippedQuery.Data>> skippedFuture = new CompletableFuture<>();
        apolloClient.query(new HasSkippedQuery()).enqueue(new ApolloCallback<HasSkippedQuery.Data>() {
          @Override
          public void onResponse(@NotNull ApolloResponse<HasSkippedQuery.Data> response) {
            skippedFuture.complete(response);
          }
        });

        var skippedResponse = skippedFuture.join();
        if (skippedResponse.hasErrors()) {
            throw new RuntimeException("Error while checking skipped jobs: " + skippedResponse.errors);
        }
        var skippedData = skippedResponse.data;
        if (skippedData == null || skippedData.updateStatus == null || skippedData.updateStatus.skippedJobs == null) {
            throw new RuntimeException("Error while updating library");
        }
        isRunning = !skippedData.updateStatus.skippedJobs.mangas.nodes.isEmpty();
      }

      return isRunning;
    } catch (Exception e) {
      throw new RuntimeException("Error while updating library", e);
    }
  }

  /** Opens a WebSocket connection to the server to track the update status of the manga library. */
  public void startUpdateTracking() {
    var apolloClient = webClientService.getApolloClient();

    Flux.<MangaUpdateEvent>create(sink -> {
        ApolloDisposable disposable = apolloClient.subscription(new TrackMangaUpdateSubscription()).enqueue(new ApolloCallback<TrackMangaUpdateSubscription.Data>() {
            @Override
            public void onResponse(@NotNull ApolloResponse<TrackMangaUpdateSubscription.Data> response) {
                if (response.hasErrors()) {
                    sink.error(new RuntimeException("Error in update tracking subscription: " + response.errors));
                    return;
                }
                var data = response.data;
                if (data == null || data.updateStatusChanged == null) {
                    sink.error(new RuntimeException("Couldn't retrieve update run status"));
                    return;
                }

                var completedManga = data.updateStatusChanged.completeJobs.mangas.nodes.stream()
                    .map(node -> {
                        Manga manga = new Manga();
                        manga.setId(node.id);
                        manga.setTitle(node.title);
                        return manga;
                    }).collect(Collectors.toList());

                sink.next(new MangaUpdateEvent(Boolean.TRUE.equals(data.updateStatusChanged.isRunning), completedManga));
            }
        });
        sink.onDispose(disposable::dispose);
    })
        .doOnNext(
            event -> {
              if (event.isRunning()) {
                return;
              }

              // send event to event bus
              eventPublisher.publishEvent(event);
            })
        .onErrorComplete(
            e -> {
              Thread.ofVirtual().start(this::restartUpdateTracking);

              return true;
            })
        .subscribe();
  }

  /**
   * Restarts the update tracking after a delay of 5 seconds. This is used to prevent the client
   * from spamming the server with requests in case of an error, such as the server not running yet.
   */
  private void restartUpdateTracking() {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    startUpdateTracking();
  }
}
