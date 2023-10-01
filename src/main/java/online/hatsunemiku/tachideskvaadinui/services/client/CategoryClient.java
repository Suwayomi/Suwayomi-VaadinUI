/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import feign.Headers;
import java.net.URI;
import java.util.List;
import java.util.Map;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Represents a client for interacting with the Category API Endpoint. */
@FeignClient(name = "category-service", url = "http://localhost:8080")
public interface CategoryClient {

  /**
   * Creates a new category.
   *
   * @param baseUrl the base URL of the API
   * @param formParams the form parameters for creating the category
   */
  @PostMapping(value = "/api/v1/category", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @Headers("Content-Type: application/x-www-form-urlencoded")
  void createCategory(URI baseUrl, @RequestBody Map<String, ?> formParams);

  /**
   * Deletes a category.
   *
   * @param baseUrl the base URL of the API
   * @param categoryId the ID of the category to delete
   */
  @DeleteMapping("/api/v1/category/{categoryId}")
  void deleteCategory(URI baseUrl, @PathVariable int categoryId);

  /**
   * Retrieves all categories from the API.
   *
   * @param baseUrl the base URL of the API
   * @return a list of {@link Category} objects representing the categories retrieved from the API
   */
  @GetMapping("/api/v1/category")
  List<Category> getCategories(URI baseUrl);

  /**
   * Retrieves manga from a specific category from the API.
   *
   * @param baseUrl the base URL of the API
   * @param categoryId the ID of the category to retrieve manga from
   * @return a list of {@link Manga} objects representing the manga retrieved from the API
   */
  @GetMapping("/api/v1/category/{categoryId}")
  List<Manga> getMangaFromCategory(URI baseUrl, @PathVariable int categoryId);
}
