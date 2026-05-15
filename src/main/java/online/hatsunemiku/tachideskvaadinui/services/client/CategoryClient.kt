package online.hatsunemiku.tachideskvaadinui.services.client;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.api.Optional;
import com.apollographql.java.client.ApolloCallback;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.CreateCategoryMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.DeleteCategoryMutation;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetCategoriesQuery;
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetCategoryMangaQuery;
import online.hatsunemiku.tachideskvaadinui.services.WebClientService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<CreateCategoryMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new CreateCategoryMutation(name)).enqueue(future::complete);

    try {
      var response = future.join();
      if (response.hasErrors()) {
        return false;
      }

      var data = response.data;
      if (data == null || data.createCategory == null || data.createCategory.category == null) {
        throw new RuntimeException("Error while creating category");
      }

      return true;
    } catch (Exception e) {
      throw new RuntimeException("Error while creating category", e);
    }
  }

  /**
   * Deletes the category with the specified category ID.
   *
   * @param categoryId the ID of the category to be deleted
   * @return true if the category was successfully deleted, false otherwise
   * @throws RuntimeException if there was an error while deleting the category
   */
  public boolean deleteCategory(int categoryId) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<DeleteCategoryMutation.Data>> future = new CompletableFuture<>();
    apolloClient.mutation(new DeleteCategoryMutation(categoryId)).enqueue(new ApolloCallback<DeleteCategoryMutation.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<DeleteCategoryMutation.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        return false;
      }

      var data = response.data;
      // deleteCategory returns null if the category doesn't exist, meaning there was nothing to delete
      return data != null && data.deleteCategory != null && data.deleteCategory.category != null;
    } catch (Exception e) {
      throw new RuntimeException("Error while deleting category", e);
    }
  }

  /**
   * Retrieves a list of categories.
   *
   * @return a list of Category objects representing the categories
   * @throws RuntimeException if there was an error while retrieving the categories
   */
  public List<Category> getCategories() {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetCategoriesQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetCategoriesQuery()).enqueue(new ApolloCallback<GetCategoriesQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetCategoriesQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while getting categories: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.categories == null) {
        throw new RuntimeException("Error while getting categories");
      }

      var categories = data.categories.nodes.stream()
          .map(node -> {
            Category category = new Category();
            category.setDef(Boolean.TRUE.equals(node.default_));
            category.setId(node.id);
            category.setName(node.name);
            category.setOrder(node.order);
            return category;
          })
          .collect(Collectors.toList());

      categories.sort(Comparator.comparingInt(Category::getOrder));

      return categories;
    } catch (Exception e) {
      throw new RuntimeException("Error while getting categories", e);
    }
  }

  /**
   * Retrieves a list of manga belonging to a specific category.
   *
   * @param categoryId the ID of the category
   * @return a {@link List list} of {@link Manga} objects representing the manga in the category
   * @throws RuntimeException if there was an error while retrieving the category manga
   */
  public List<Manga> getCategoryManga(int categoryId) {
    var apolloClient = clientService.getApolloClient();

    CompletableFuture<ApolloResponse<GetCategoryMangaQuery.Data>> future = new CompletableFuture<>();
    apolloClient.query(new GetCategoryMangaQuery(new Optional.Present<>(categoryId))).enqueue(new ApolloCallback<GetCategoryMangaQuery.Data>() {
      @Override
      public void onResponse(@NotNull ApolloResponse<GetCategoryMangaQuery.Data> response) {
        future.complete(response);
      }
    });

    try {
      var response = future.join();
      if (response.hasErrors()) {
        throw new RuntimeException("Error while getting category manga: " + response.errors);
      }

      var data = response.data;
      if (data == null || data.category == null || data.category.mangas == null) {
        throw new RuntimeException("Error while getting category manga");
      }

      return data.category.mangas.nodes.stream()
          .map(node -> {
            Manga manga = new Manga();
            manga.setThumbnailUrl(node.thumbnailUrl);
            manga.setTitle(node.title);
            manga.setInLibrary(Boolean.TRUE.equals(node.inLibrary));
            manga.setId(node.id);
            return manga;
          })
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error while getting category manga", e);
    }
  }
}
