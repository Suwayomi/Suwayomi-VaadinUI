package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi

import kotlinx.coroutines.runBlocking
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.ServerVersion
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetServerVersionQuery
import online.hatsunemiku.tachideskvaadinui.services.WebClientService
import org.springframework.stereotype.Component

/** Retrieves metadata about the Suwayomi Server through its API. */
@Component
class SuwayomiMetaClient(private val webClientService: WebClientService) {

    /**
     * Retrieves the version of the Suwayomi Server though its API. This method will block until the
     * response is received.
     *
     * @return the version of the Suwayomi Server.
     */
    fun getServerVersion(): ServerVersion {
        val apolloClient = webClientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetServerVersionQuery()).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Failed to retrieve server version: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Failed to retrieve server version: No data")
                ServerVersion(data.aboutServer.version, data.aboutServer.revision)
            } catch (e: Exception) {
                throw RuntimeException("Failed to retrieve server version", e)
            }
        }
    }
}
