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

@Component
public class LibUpdateClient {

  private final WebClientService webClientService;
  private final ApplicationEventPublisher eventPublisher;

  public LibUpdateClient(WebClientService webClientService,
      ApplicationEventPublisher eventPublisher) {
    this.webClientService = webClientService;
    this.eventPublisher = eventPublisher;
  }

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

  public void startUpdateTracking() {
    @Language("GraphQL")
    String query = """
     subscription TrackMangaUpdate {
       updateStatusChanged {
         completeJobs {
           mangas {
             nodes {
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

    graphClient.document(query)
        .executeSubscription()
        .<MangaUpdateEvent>handle((data, sink) -> {
          var completedManga = data.field("updateStatusChanged.completeJobs.mangas.nodes")
              .toEntityList(Manga.class);

          Boolean isRunning = data.field("updateStatusChanged.isRunning").toEntity(Boolean.class);

          if (isRunning == null) {
            sink.error(new RuntimeException("Couldn't retrieve update run status"));
            return;
          }

          sink.next(new MangaUpdateEvent(isRunning, completedManga));
        })
        .doOnNext(event -> {
          if (event.isRunning()) {
            return;
          }

          // send event to event bus
          eventPublisher.publishEvent(event);
        })
        .subscribe();


  }

  private static class SkippedManga {
    @JsonProperty("id")
    private Long id;
  }
}
