package online.hatsunemiku.tachideskvaadinui.services.client

import com.apollographql.apollo.api.Optional
import kotlinx.coroutines.runBlocking
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.CreateCategoryMutation
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.DeleteCategoryMutation
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetCategoriesQuery
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetCategoryMangaQuery
import online.hatsunemiku.tachideskvaadinui.services.WebClientService
import org.springframework.stereotype.Component

@Component
class CategoryClient(private val clientService: WebClientService) {

    /**
     * Creates a new category with the specified name.
     *
     * @param name the name of the category
     * @return true if the category was successfully created, false otherwise
     * @throws RuntimeException if there was an error while creating the category
     */
    fun createCategory(name: String): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(CreateCategoryMutation(name)).execute()
                if (response.hasErrors()) {
                    return@runBlocking false
                }

                val data = response.data
                if (data?.createCategory?.category == null) {
                    throw RuntimeException("Error while creating category")
                }

                true
            } catch (e: Exception) {
                throw RuntimeException("Error while creating category", e)
            }
        }
    }

    /**
     * Deletes the category with the specified category ID.
     *
     * @param categoryId the ID of the category to be deleted
     * @return true if the category was successfully deleted, false otherwise
     * @throws RuntimeException if there was an error while deleting the category
     */
    fun deleteCategory(categoryId: Int): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(DeleteCategoryMutation(categoryId)).execute()
                if (response.hasErrors()) {
                    return@runBlocking false
                }

                val data = response.data
                // deleteCategory returns null if the category doesn't exist, meaning there was nothing to delete
                data?.deleteCategory?.category != null
            } catch (e: Exception) {
                throw RuntimeException("Error while deleting category", e)
            }
        }
    }

    /**
     * Retrieves a list of categories.
     *
     * @return a list of Category objects representing the categories
     * @throws RuntimeException if there was an error while retrieving the categories
     */
    fun getCategories(): List<Category> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetCategoriesQuery()).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while getting categories: " + response.errors)
                }

                val data = response.data
                if (data?.categories == null) {
                    throw RuntimeException("Error while getting categories")
                }

                val categories = data.categories.nodes.map { node ->
                    Category(
                        id = node.id,
                        name = node.name,
                        order = node.order,
                        isDef = node.default
                    )
                }.toMutableList()

                categories.sortBy { it.order }

                categories
            } catch (e: Exception) {
                throw RuntimeException("Error while getting categories", e)
            }
        }
    }

    /**
     * Retrieves a list of manga belonging to a specific category.
     *
     * @param categoryId the ID of the category
     * @return a [List] of [Manga] objects representing the manga in the category
     * @throws RuntimeException if there was an error while retrieving the category manga
     */
    fun getCategoryManga(categoryId: Int): List<Manga> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetCategoryMangaQuery(Optional.present(categoryId))).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while getting category manga: " + response.errors)
                }

                val data = response.data
                if (data?.category?.mangas == null) {
                    throw RuntimeException("Error while getting category manga")
                }

                data.category.mangas.nodes.map { node ->
                    Manga().apply {
                        thumbnailUrl = node.thumbnailUrl
                        title = node.title
                        isInLibrary = node.inLibrary
                        id = node.id
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException("Error while getting category manga", e)
            }
        }
    }
}
