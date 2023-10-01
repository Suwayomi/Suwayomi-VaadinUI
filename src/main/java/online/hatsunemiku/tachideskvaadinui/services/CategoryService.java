/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.client.CategoryClient;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;

/** This class provides methods for interacting with the Tachidesk Category API. */
@Slf4j
@Service
public class CategoryService {

  private final CategoryClient categoryClient;
  private final SettingsService settingsService;

  /**
   * Constructs a new CategoryService object.
   *
   * @param categoryClient the client used for interacting with the category API
   * @param settingsService the service used for retrieving settings
   */
  @Contract(pure = true)
  public CategoryService(CategoryClient categoryClient, SettingsService settingsService) {
    this.categoryClient = categoryClient;
    this.settingsService = settingsService;
  }

  /**
   * Creates a new category with the given name.
   *
   * @param name the name of the category
   * @return true if the category was created successfully, false otherwise
   */
  public boolean createCategory(String name) {
    URI baseUrl = URI.create(settingsService.getSettings().getUrl());

    try {
      categoryClient.createCategory(baseUrl, Map.of("name", name));
      return true;
    } catch (Exception e) {
      log.error("Failed to create category", e);
      return false;
    }
  }

  /**
   * Deletes the category with the specified category ID.
   *
   * @param categoryId the ID of the category to delete
   * @return true if the category was deleted successfully, false otherwise
   */
  public boolean deleteCategory(int categoryId) {
    URI baseUrl = URI.create(settingsService.getSettings().getUrl());

    try {
      categoryClient.deleteCategory(baseUrl, categoryId);
      return true;
    } catch (Exception e) {
      log.error("Failed to delete category", e);
      return false;
    }
  }

  /**
   * Retrieves a list of categories from the server.
   *
   * @return a {@link List} of {@link Category} objects representing the categories retrieved from
   *     the server
   * @throws RuntimeException if an error occurs while retrieving the categories
   */
  public List<Category> getCategories() {
    URI baseUrl = URI.create(settingsService.getSettings().getUrl());

    try {
      return categoryClient.getCategories(baseUrl);
    } catch (Exception e) {
      log.debug("Failed to get categories", e);
      throw new RuntimeException(e);
    }
  }

  public List<Manga> getMangaFromCategory(int categoryId) {
    URI baseUrl = URI.create(settingsService.getSettings().getUrl());

    try {
      return categoryClient.getMangaFromCategory(baseUrl, categoryId);
    } catch (Exception e) {
      log.error("Failed to get manga from category", e);
      throw new RuntimeException(e);
    }
  }
}
