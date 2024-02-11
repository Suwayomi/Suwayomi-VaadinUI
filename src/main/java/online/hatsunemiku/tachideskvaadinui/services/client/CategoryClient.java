/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import java.util.Comparator;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.springframework.graphql.client.FieldAccessException;
import org.springframework.stereotype.Component;

@Component
public class CategoryClient {

  private final WebClientService clientService;

  public CategoryClient(WebClientService clientService) {
    this.clientService = clientService;
  }

  /**
   * Creates a new category with the specified name.
   *
   * @param name the name of the category
   * @return true if the category was successfully created, false otherwise
   * @throws RuntimeException if there was an error while creating the category
   */
  public boolean createCategory(String name) {
    String query =
        """
        mutation CreateCategory($name: String!) {
          createCategory(input: {name: $name}) {
            category {
              id
            }
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    Integer id;
    try {
      id =
          graphClient
              .document(query)
              .variable("name", name)
              .retrieve("createCategory.category.id")
              .toEntity(Integer.class)
              .block();
    } catch (FieldAccessException e) {
      return false;
    }

    if (id == null) {
      throw new RuntimeException("Error while creating category");
    }

    return true;
  }

  /**
   * Deletes the category with the specified category ID.
   *
   * @param categoryId the ID of the category to be deleted
   * @return true if the category was successfully deleted, false otherwise
   * @throws RuntimeException if there was an error while deleting the category
   */
  public boolean deleteCategory(int categoryId) {
    String query =
        """
        mutation DeleteCategory($categoryId: Int!) {
          deleteCategory(input: {categoryId: $categoryId}) {
            category {
              id
            }
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    Integer id =
        graphClient
            .document(query)
            .variable("categoryId", categoryId)
            .retrieve("deleteCategory.category.id")
            .toEntity(Integer.class)
            .block();

    // deleteCategory returns null if the category doesn't exist, meaning there was nothing to
    // delete
    return id != null;
  }

  /**
   * Retrieves a list of categories.
   *
   * @return a list of Category objects representing the categories
   * @throws RuntimeException if there was an error while retrieving the categories
   */
  public List<Category> getCategories() {
    String query =
        """
        query GetCategories {
          categories {
            nodes {
              default
              id
              name
              order
            }
          }
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    var categories =
        graphClient
            .document(query)
            .retrieve("categories.nodes")
            .toEntityList(Category.class)
            .block();

    if (categories == null) {
      throw new RuntimeException("Error while getting categories");
    }

    categories.sort(Comparator.comparingInt(Category::getOrder));

    return categories;
  }

  /**
   * Retrieves a list of manga belonging to a specific category.
   *
   * @param categoryId the ID of the category
   * @return a {@link List list} of {@link Manga} objects representing the manga in the category
   * @throws RuntimeException if there was an error while retrieving the category manga
   */
  public List<Manga> getCategoryManga(int categoryId) {
    String query =
        """
        query GetCategoryManga($categoryId: Int = 10) {
          category(id: $categoryId) {
            mangas {
              nodes {
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
        }
        """;

    var graphClient = clientService.getGraphQlClient();

    var categoryManga =
        graphClient
            .document(query)
            .variable("categoryId", categoryId)
            .retrieve("category.mangas.nodes")
            .toEntityList(Manga.class)
            .block();

    if (categoryManga == null) {
      throw new RuntimeException("Error while getting category manga");
    }

    return categoryManga;
  }
}
