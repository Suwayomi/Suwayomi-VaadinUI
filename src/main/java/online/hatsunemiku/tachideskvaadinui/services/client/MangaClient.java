/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import online.hatsunemiku.tachideskvaadinui.utils.GraphQLUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MangaClient {

  private final WebClientService clientService;
  private final ObjectMapper mapper;

  public MangaClient(ObjectMapper mapper, WebClientService clientService) {
    this.mapper = mapper;
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

    String variables = """
        {
          "categoryIds": %s,
          "mangaId": %d
        }
        """.formatted(categoryIds, mangaId);

    var webClient = clientService.getWebClient();

    String json = GraphQLUtils.sendGraphQLRequest(query, variables, webClient);

    try {
      JsonArray nodes = getMangaCategoriesFromResponse(json);

      for (int i = 0; i < nodes.length(); i++) {
        JsonObject node = nodes.getObject(i);
        int id = (int) node.getNumber("id");

        //If the manga is not in the category, adding it failed
        if (!categoryIds.contains(id)) {
          return false;
        }
      }

      //If the manga is in all the categories in the list, adding it succeeded
      return true;
    } catch (Exception e) {
      log.error("Error while parsing JSON response", e);
      throw new RuntimeException(e);
    }
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

    String variables = """
        {
          "categoryIds": %s,
          "mangaId": %d
        }
        """.formatted(categoryIds, mangaId);

    var webClient = clientService.getWebClient();

    String json = GraphQLUtils.sendGraphQLRequest(query, variables, webClient);

    try {
      JsonArray nodes = getMangaCategoriesFromResponse(json);

      for (int i = 0; i < nodes.length(); i++) {
        JsonObject node = nodes.getObject(i);
        int id = (int) node.getNumber("id");

        //If the manga is in any of the categories, removing it failed
        if (categoryIds.contains(id)) {
          return false;
        }
      }

      //If the manga is not in any of the categories, removing it succeeded
      return true;
    } catch (Exception e) {
      log.error("Error while parsing JSON response", e);
      throw new RuntimeException(e);
    }

  }

  private JsonArray getMangaCategoriesFromResponse(String json) {
    JsonObject jsonObject = Json.parse(json);
    JsonObject data = jsonObject.getObject("data");
    JsonObject updateMangaCategories = data.getObject("updateMangaCategories");
    JsonObject manga = updateMangaCategories.getObject("manga");
    JsonObject categories = manga.getObject("categories");
    return categories.getArray("nodes");
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

    String variables = """
        {
          "id": %d
        }
        """.formatted(chapterId);

    var webClient = clientService.getWebClient();

    String json = GraphQLUtils.sendGraphQLRequest(query, variables, webClient);

    try {
      JsonObject jsonObject = Json.parse(json);
      JsonObject data = jsonObject.getObject("data");
      JsonObject chapter = data.getObject("chapter");
      return mapper.readValue(chapter.toJson(), Chapter.class);
    } catch (JsonProcessingException e) {
      log.error("Error while parsing JSON", e);
      throw new RuntimeException(e);
    }

  }

  public List<Chapter> getChapterList(int mangaId) {
    String query = """
        query GetChapterList($id: Int!) {
          manga(id: $id) {
            chapters {
              nodes {
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
        }
        """;

    String variables = """
          {
            "id": %d
          }
        """.formatted(mangaId);

    var webClient = clientService.getWebClient();
    String json = GraphQLUtils.sendGraphQLRequest(query, variables, webClient);

    TypeReference<List<Chapter>> typeReference = new TypeReference<>() {
    };

    try {
      JsonObject jsonObject = Json.parse(json);
      JsonObject data = jsonObject.getObject("data");
      JsonObject manga = data.getObject("manga");
      JsonObject chapters = manga.getObject("chapters");
      JsonArray nodes = chapters.getArray("nodes");

      return mapper.readValue(nodes.toJson(), typeReference);
    } catch (JsonProcessingException e) {
      log.error("Error while parsing JSON", e);
      throw new RuntimeException(e);
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
   * Remove a manga from the library.
   *
   * @param mangaId the ID of the manga to be removed
   * @return true if the manga was successfully removed from the library, false otherwise
   * @throws RuntimeException if there is an error while parsing the JSON response
   */
  public boolean removeMangaFromLibrary(int mangaId) {
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
            }
          }
        }
        """;

    String variables = """
        {
          "id": %d
        }""".formatted(mangaId);

    var webClient = clientService.getWebClient();

    String json = GraphQLUtils.sendGraphQLRequest(query, variables, webClient);

    try {
      JsonObject jsonObject = Json.parse(json);
      JsonObject data = jsonObject.getObject("data");
      JsonObject fetchManga = data.getObject("fetchManga");
      JsonObject manga = fetchManga.getObject("manga");
      return mapper.readValue(manga.toJson(), Manga.class);
    } catch (JsonProcessingException e) {
      log.error("Error while parsing JSON", e);
      throw new RuntimeException(e);
    }
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

    String variables = """
        {
        "id": %d,
        "isRead": %s
        }""".formatted(chapterId, read);

    var webClient = clientService.getWebClient();

    String json = GraphQLUtils.sendGraphQLRequest(query, variables, webClient);

    try {
      JsonObject jsonObject = Json.parse(json);
      JsonObject data = jsonObject.getObject("data");
      JsonObject updateChapter = data.getObject("updateChapter");
      JsonObject chapter = updateChapter.getObject("chapter");
      return chapter.getBoolean("isRead");
    } catch (Exception e) {
      log.error("Error while parsing JSON response", e);
      throw new RuntimeException(e);
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
    String query = """
        mutation UpdateMangaLibraryStatus($id: Int!, $add: Boolean!) {
          updateManga(input: {id: $id, patch: {inLibrary: $add}}) {
            manga {
              inLibrary
            }
          }
        }
        """;

    String variables = """
        {
          "id": %d,
          "add": %s
        }
        """.formatted(mangaId, add);

    var webClient = clientService.getWebClient();

    String json = GraphQLUtils.sendGraphQLRequest(query, variables, webClient);

    try {
      JsonObject jsonObject = Json.parse(json);
      JsonObject data = jsonObject.getObject("data");
      JsonObject updateManga = data.getObject("updateManga");
      JsonObject manga = updateManga.getObject("manga");
      return manga.getBoolean("inLibrary");
    } catch (Exception e) {
      log.error("Error while parsing JSON response", e);
      throw new RuntimeException(e);
    }
  }

  public List<String> getChapterPages(int chapterId) {
    String query = """
        mutation getChapterPages($chapterId: Int!) {
          fetchChapterPages(input: {chapterId: $chapterId}) {
            pages
          }
        }
        """;

    String variables = """
        {
          "chapterId": %d
        }
        """.formatted(chapterId);

    var webClient = clientService.getWebClient();

    String json = GraphQLUtils.sendGraphQLRequest(query, variables, webClient);
    TypeReference<List<String>> typeReference = new TypeReference<>() {
    };
    try {
      JsonObject jsonObject = Json.parse(json);
      JsonObject data = jsonObject.getObject("data");
      JsonObject fetchChapterPages = data.getObject("fetchChapterPages");
      JsonArray pages = fetchChapterPages.getArray("pages");
      return mapper.readValue(pages.toJson(), typeReference);
    } catch (Exception e) {
      log.error("Error while parsing JSON response", e);
      throw new RuntimeException(e);
    }
  }
}
