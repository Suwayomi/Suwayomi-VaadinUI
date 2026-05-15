package online.hatsunemiku.tachideskvaadinui.services.client

import kotlinx.coroutines.runBlocking
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.GetExtensionsMutation
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.InstallExtensionMutation
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.UpdateExtensionMutation
import online.hatsunemiku.tachideskvaadinui.services.WebClientService
import org.springframework.stereotype.Component

@Component
class ExtensionClient(private val clientService: WebClientService) {

    fun updateExtension(extensionId: String): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(UpdateExtensionMutation(extensionId)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while updating extension: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while updating extension: No data")
                val extension = data.updateExtension.extension ?: throw RuntimeException("Error while updating extension: Null extension")
                
                extension.hasUpdate == false
            } catch (e: Exception) {
                throw RuntimeException("Error while updating extension", e)
            }
        }
    }

    /**
     * Retrieves a list of extensions from the GraphQL server.
     *
     * @return a [List] of [Extension] objects
     * @throws RuntimeException if there is an error while retrieving the extensions
     */
    fun getExtensions(): List<Extension> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(GetExtensionsMutation()).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while retrieving extensions: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while retrieving extensions: No data")
                val extensions = data.fetchExtensions?.extensions ?: throw RuntimeException("Error while retrieving extensions: Null list")

                extensions.map { node ->
                    Extension().apply {
                        pkgName = node.pkgName
                        apkName = node.apkName
                        isInstalled = node.isInstalled == true
                        isNsfw = node.isNsfw == true
                        isObsolete = node.isObsolete == true
                        lang = node.lang
                        name = node.name
                        isHasUpdate = node.hasUpdate == true
                        iconUrl = node.iconUrl
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException("Error while retrieving extensions", e)
            }
        }
    }

    /**
     * Installs an extension with the given extension ID.
     *
     * @param extensionId the ID of the extension to install
     * @return `true` if the extension is installed successfully, `false` otherwise
     * @throws RuntimeException if there is an error while installing the extension
     */
    fun installExtension(extensionId: String): Boolean {
        return updateExtensionInstallStatus(extensionId, true)
    }

    /**
     * Uninstalls an extension with the given extension ID.
     *
     * @param extensionId the ID of the extension to uninstall
     * @return `true` if the extension is uninstalled successfully, `false` otherwise
     * @throws RuntimeException if there is an error while uninstalling the extension
     */
    fun uninstallExtension(extensionId: String): Boolean {
        return !updateExtensionInstallStatus(extensionId, false)
    }

    private fun updateExtensionInstallStatus(extensionId: String, install: Boolean): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(InstallExtensionMutation(extensionId, install, !install)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while updating extension install status: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while updating extension install status: No data")
                val extension = data.updateExtension.extension ?: throw RuntimeException("Error while updating extension install status: Null extension")

                extension.isInstalled == true
            } catch (e: Exception) {
                throw RuntimeException("Error while updating extension install status", e)
            }
        }
    }
}
