/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.client.CategoryClient;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;

/**
 * This class provides methods for interacting with the Tachidesk Category API.
 */
@Slf4j
@Service
public class CategoryService {
  private final CategoryClient categoryClient;

  /**
   * Constructs a new instance of {@link CategoryService} with the specified {@link CategoryClient}.
   *
   * @param categoryClient the client used to perform operations related to categories
   */
  @Contract(pure = true)
  public CategoryService(CategoryClient categoryClient) {
    this.categoryClient = categoryClient;
  }

  /**
   * Creates a new category with the given name.
   *
   * @param name the name of the category
   * @return true if the category was created successfully, false otherwise
   */
  public boolean createCategory(String name) {
    return categoryClient.createCategory(name);
  }

  /**
   * Deletes the category with the specified category ID.
   *
   * @param categoryId the ID of the category to delete
   * @return true if the category was deleted successfully, false otherwise
   */
  public boolean deleteCategory(int categoryId) {
    return categoryClient.deleteCategory(categoryId);
  }

  /**
   * Retrieves a list of categories from the server.
   *
   * @return a {@link List} of {@link Category} objects representing the categories retrieved from
   * the server
   * @throws RuntimeException if an error occurs while retrieving the categories
   */
  public List<Category> getCategories() {
    return categoryClient.getCategories();
  }

  /**
   * Retrieves a list of manga belonging to a specific category.
   *
   * @param categoryId the ID of the category to retrieve manga from
   * @return a {@link List} of {@link Manga} objects representing the manga retrieved from the
   * specified category
   */
  public List<Manga> getMangaFromCategory(int categoryId) {
    return categoryClient.getCategoryManga(categoryId);
  }
}
