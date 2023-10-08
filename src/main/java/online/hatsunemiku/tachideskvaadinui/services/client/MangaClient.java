/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MangaClient {

  private final WebClientService clientService;

  public MangaClient(WebClientService clientService) {
    this.clientService = clientService;
  }

  public boolean addMangaToCategories(List<Integer> categoryIds, int mangaId) {
    String query = """
        mutation addMangaToCategories($categoryIds: [Int!], $mangaId: Int!) {
          updateMangaCategories(
            input: {id: $mangaId, patch: {addToCategories: $categoryIds}}
          ) {
            manga {
              categories {
                nodes {
                  id
                }
              }
            }
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    var tempCategoryIds = graphClient.document(query)
        .variable("categoryIds", categoryIds)
        .variable("mangaId", mangaId)
        .retrieve("updateMangaCategories.manga.categories.nodes")
        .toEntityList(UpdateMangaCategoryId.class)
        .block();

    if (tempCategoryIds == null) {
      throw new RuntimeException("Error while adding manga to categories");
    }

    var newCategoryIds = tempCategoryIds.stream()
        .filter(Objects::nonNull)
        .map(UpdateMangaCategoryId::id)
        .toList();

    for (int categoryId : newCategoryIds) {
      if (!categoryIds.contains(categoryId)) {
        return false;
      }
    }

    return true;
  }

  public boolean removeMangaFromCategories(List<Integer> categoryIds, int mangaId) {
    String query = """
        mutation removeMangaFromCategories($categoryIds: [Int!], $mangaId: Int!) {
          updateMangaCategories(
            input: {id: $mangaId, patch: {removeFromCategories: $categoryIds}}
          ) {
            manga {
              categories {
                nodes {
                  id
                }
              }
            }
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    var tempCategoryIds = graphClient.document(query)
        .variable("categoryIds", categoryIds)
        .variable("mangaId", mangaId)
        .retrieve("updateMangaCategories.manga.categories.nodes")
        .toEntityList(UpdateMangaCategoryId.class)
        .block();

    if (tempCategoryIds == null) {
      throw new RuntimeException("Error while removing manga from categories");
    }

    var newCategoryIds = tempCategoryIds.stream()
        .filter(Objects::nonNull)
        .map(UpdateMangaCategoryId::id)
        .toList();

    for (int id : newCategoryIds) {
      if (categoryIds.contains(id)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Retrieves the chapter information based on the given chapter ID.
   *
   * @param chapterId The ID of the chapter to retrieve.
   * @return The Chapter object representing the retrieved chapter information.
   * @throws RuntimeException if an error occurs while parsing the JSON response.
   */
  public Chapter getChapter(long chapterId) {
    String query = """
        query MyQuery($id: Int!) {
          chapter(id: $id) {
            mangaId
            isDownloaded
            chapterNumber
            name
            id
            pageCount
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    return graphClient.document(query)
        .variable("id", chapterId)
        .retrieve("chapter")
        .toEntity(Chapter.class)
        .block();
  }

  public List<Chapter> getChapterList(int mangaId) {
    String query = """
        mutation MyMutation($mangaId: Int!) {
             fetchChapters(input: { mangaId: $mangaId }) {
               chapters {
                 url
                 chapterNumber
                 mangaId
                 name
                 uploadDate
                 isRead
                 isDownloaded
                 id
                 pageCount
                 manga {
                   chapters {
                     edges {
                       cursor
                     }
                   }
                 }
               }
             }
           }
           
        """;

    var graphClient = clientService.getGraphQlClient();

    return graphClient.document(query)
        .variable("mangaId", mangaId)
        .retrieve("fetchChapters.chapters")
        .toEntityList(Chapter.class)
        .block();
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
      var categoryIds = categories.stream()
          .map(Category::getId)
          .toList();

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
    String query = """
        mutation FetchManga($id: Int!) {
          fetchManga(input: {id: $id}) {
            manga {
              thumbnailUrl
              title
              inLibrary
              id
              lastReadChapter {
                id
              }
              categories {
                nodes {
                  id
                }
              }
            }
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    return graphClient.document(query)
        .variable("id", mangaId)
        .retrieve("fetchManga.manga")
        .toEntity(Manga.class)
        .block();
  }

  /**
   * Update the read status of a specific chapter.
   *
   * @param chapterId the ID of the chapter to update
   * @param read      the new read status of the chapter
   * @return the new read status of the chapter after the update, either {@code true} or
   * {@code false}
   * @throws RuntimeException if there is an error while parsing the JSON response
   */
  private boolean updateChapterReadStatus(int chapterId, boolean read) {
    String query = """
        mutation SetChapterReadStatus($id: Int!, $isRead: Boolean!) {
          updateChapter(input: {patch: {isRead: $isRead}, id: $id}) {
            chapter {
              isRead
            }
          }
        }""";

    var graphClient = clientService.getGraphQlClient();
    Boolean readStatus = graphClient.document(query)
        .variable("id", chapterId)
        .variable("isRead", read)
        .retrieve("updateChapter.chapter.isRead")
        .toEntity(Boolean.class)
        .block();

    return Objects.requireNonNullElse(readStatus, false);
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
    String query = """
        mutation UpdateMangaLibraryStatus($id: Int!, $add: Boolean!) {
          updateManga(input: {id: $id, patch: {inLibrary: $add}}) {
            manga {
              inLibrary
            }
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();
    Boolean success = graphClient.document(query)
        .variable("id", mangaId)
        .variable("add", add)
        .retrieve("updateManga.manga.inLibrary")
        .toEntity(Boolean.class)
        .block();

    return Objects.requireNonNullElse(success, false);
  }

  public List<String> getChapterPages(int chapterId) {
    String query = """
        mutation getChapterPages($chapterId: Int!) {
          fetchChapterPages(input: {chapterId: $chapterId}) {
            pages
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    return graphClient.document(query)
        .variable("chapterId", chapterId)
        .retrieve("fetchChapterPages.pages")
        .toEntityList(String.class)
        .block();
  }

  private record UpdateMangaCategoryId(int id) {

  }
}
