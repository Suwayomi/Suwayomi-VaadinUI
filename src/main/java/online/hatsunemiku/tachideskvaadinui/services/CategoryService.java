package online.hatsunemiku.tachideskvaadinui.services;

import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.services.client.CategoryClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CategoryService {

  private final CategoryClient categoryClient;
  private final SettingsService settingsService;

  public CategoryService(CategoryClient categoryClient, SettingsService settingsService) {
    this.categoryClient = categoryClient;
    this.settingsService = settingsService;
  }

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

  public List<Category> getCategories() {
    URI baseUrl = URI.create(settingsService.getSettings().getUrl());

    try {
      return categoryClient.getCategories(baseUrl);
    } catch (Exception e) {
      log.error("Failed to get categories", e);
      throw new RuntimeException(e);
    }
  }

}
