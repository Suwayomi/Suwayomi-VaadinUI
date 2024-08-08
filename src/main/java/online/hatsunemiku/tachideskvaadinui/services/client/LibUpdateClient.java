/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.event.MangaUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.intellij.lang.annotations.Language;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

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
    // language=GraphQL
    String runningQuery =
        """
            mutation updateLibraryManga {
              updateLibraryManga(input: {}) {
                updateStatus {
                  isRunning
                }
              }
            }
            """;

    var graphClient = webClientService.getGraphQlClient();

    Boolean isRunning =
        graphClient
            .document(runningQuery)
            .retrieve("updateLibraryManga.updateStatus.isRunning")
            .toEntity(Boolean.class)
            .block();

    if (isRunning == null) {
      throw new RuntimeException("Error while updating library");
    }

    if (!isRunning) {
      // language=GraphQL
      String hasSkippedQuery =
          """
              query hasSkipped {
                updateStatus {
                  skippedJobs {
                    mangas {
                      nodes {
                        id
                      }
                    }
                  }
                }
              }
              """;

      var skippedManga =
          graphClient
              .document(hasSkippedQuery)
              .retrieve("updateStatus.skippedJobs.mangas.nodes")
              .toEntityList(SkippedManga.class)
              .block();

      if (skippedManga == null) {
        throw new RuntimeException("Error while updating library");
      }

      isRunning = !skippedManga.isEmpty();
    }

    return isRunning;
  }

  /** Opens a WebSocket connection to the server to track the update status of the manga library. */
  public void startUpdateTracking() {
    @Language("GraphQL")
    String query =
        """
            subscription TrackMangaUpdate {
              updateStatusChanged {
                completeJobs {
                  mangas {
                    nodes {
                      title
                      chapters {
                        totalCount
                      }
                      id
                    }
                  }
                }
                isRunning
              }
            }
            """;

    var graphClient = webClientService.getWebSocketGraphQlClient();

    graphClient
        .document(query)
        .executeSubscription()
        .<MangaUpdateEvent>handle(
            (data, sink) -> {
              var completedManga =
                  data.field("updateStatusChanged.completeJobs.mangas.nodes")
                      .toEntityList(Manga.class);

              Boolean isRunning =
                  data.field("updateStatusChanged.isRunning").toEntity(Boolean.class);

              if (isRunning == null) {
                sink.error(new RuntimeException("Couldn't retrieve update run status"));
                return;
              }

              sink.next(new MangaUpdateEvent(isRunning, completedManga));
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

  /** Represents a manga that has been skipped during the update process. */
  private static class SkippedManga {

    @JsonProperty("id")
    private Long id;
  }
}
