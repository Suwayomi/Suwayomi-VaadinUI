package online.hatsunemiku.tachideskvaadinui.services.client.suwayomi

import com.apollographql.apollo.api.DefaultUpload
import com.apollographql.apollo.api.Optional
import kotlinx.coroutines.runBlocking
import online.hatsunemiku.tachideskvaadinui.data.settings.FlareSolverrSettings
import online.hatsunemiku.tachideskvaadinui.graphql.suwayomi.*
import online.hatsunemiku.tachideskvaadinui.services.WebClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path

/**
 * The SuwayomiSettingsClient class is responsible for making API requests to the Suwayomi Server
 * for updating and retrieving server settings.
 */
@Component
class SuwayomiSettingsClient(private val clientService: WebClientService) {

    companion object {
        private val log = LoggerFactory.getLogger(SuwayomiSettingsClient::class.java)
    }

    /**
     * Updates the user's extension repositories on the Suwayomi Server.
     *
     * @param extensionRepoUrls a list of extension repository URLs as strings.
     * @return `true` if the extension repositories were updated successfully, `false`
     */
    fun updateExtensionRepos(extensionRepoUrls: List<String>): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(UpdateExtensionReposMutation(Optional.present(extensionRepoUrls))).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while updating extensionRepos: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while updating extensionRepos: No data")
                data.setSettings.settings.extensionRepos == extensionRepoUrls
            } catch (e: Exception) {
                throw RuntimeException("Error while updating extensionRepos", e)
            }
        }
    }

    /**
     * Retrieves the user's extension repositories from the Suwayomi Server.
     *
     * @return a list of extension repository URLs as strings.
     */
    fun getExtensionRepos(): List<String> {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetExtensionReposQuery()).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while getting extensionRepos: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while getting extensionRepos: No data")
                data.settings.extensionRepos
            } catch (e: Exception) {
                throw RuntimeException("Error while getting extensionRepos", e)
            }
        }
    }

    /**
     * Retrieves the FlareSolverr settings from the Suwayomi Server.
     *
     * @return the FlareSolverr settings from the server as a [FlareSolverrSettings] object.
     */
    fun getFlareSolverrSettings(): FlareSolverrSettings {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.query(GetFlareSolverrSettingsQuery()).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while getting FlareSolverrSettings: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while getting FlareSolverrSettings - data is null")
                
                FlareSolverrSettings().apply {
                    isEnabled = data.settings.flareSolverrEnabled == true
                    sessionName = data.settings.flareSolverrSessionName
                    sessionTTL = data.settings.flareSolverrSessionTtl?.toInt() ?: 0
                    timeout = data.settings.flareSolverrTimeout?.toInt() ?: 0
                    url = data.settings.flareSolverrUrl
                }
            } catch (e: Exception) {
                throw RuntimeException("Error while getting FlareSolverrSettings", e)
            }
        }
    }

    /**
     * Updates the FlareSolverr URL on the Suwayomi Server.
     *
     * @param url the new FlareSolverr URL
     * @return `true` if the FlareSolverr URL was updated successfully, `false` otherwise
     */
    fun updateFlareSolverrUrl(url: String): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(UpdateFlareSolverrUrlMutation(url)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while updating FlareSolverr URL: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while updating FlareSolverr URL - data is null")
                data.setSettings.settings.flareSolverrUrl == url
            } catch (e: Exception) {
                throw RuntimeException("Error while updating FlareSolverr URL", e)
            }
        }
    }

    /**
     * Updates the FlareSolverr enabled status on the Suwayomi Server.
     *
     * @param enabled the new enabled status
     * @return `true` if the FlareSolverr enabled status was updated successfully, `false`
     *     otherwise
     */
    fun updateFlareSolverrEnabledStatus(enabled: Boolean): Boolean {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(UpdateFlareSolverrEnabledStatusMutation(enabled)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while updating FlareSolverr enabled status: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while updating FlareSolverr enabled status - data is null")
                data.setSettings.settings.flareSolverrEnabled == enabled
            } catch (e: Exception) {
                throw RuntimeException("Error while updating FlareSolverr enabled status", e)
            }
        }
    }

    /**
     * Creates a backup on the Suwayomi Server, including categories and chapters.
     *
     * @return a String containing the relative API URL for the server API to download the created
     *     backup.
     */
    fun createBackup(): String {
        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        return runBlocking {
            try {
                val response = apolloClient.mutation(CreateBackupMutation()).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while creating backup: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while creating backup: No data")
                data.createBackup.url
            } catch (e: Exception) {
                throw RuntimeException("Error while creating backup", e)
            }
        }
    }

    /**
     * Restores a backup to the Suwayomi server from the specified backup file.
     *
     * @param backupFile the [Path] to the backup file to be restored
     * @throws RuntimeException if the backup file does not exist, or if there is an error during the
     *     restoration process
     */
    fun restoreBackup(backupFile: Path) {
        if (!Files.exists(backupFile)) {
            throw RuntimeException("Backup file does not exist")
        }

        val apolloClient = clientService.apolloClient ?: throw RuntimeException("ApolloClient not initialized")

        try {
            val bytes = Files.readAllBytes(backupFile)
            val upload = DefaultUpload.Builder()
                .content(bytes)
                .contentType("application/octet-stream")
                .build()

            runBlocking {
                val response = apolloClient.mutation(RestoreBackupMutation(upload)).execute()
                if (response.hasErrors()) {
                    throw RuntimeException("Error while restoring backup: " + response.errors)
                }

                val data = response.data ?: throw RuntimeException("Error while restoring backup: No data")
                log.debug("Restored backup: {}", data.restoreBackup.status)
            }
        } catch (e: Exception) {
            throw RuntimeException("Error while restoring backup", e)
        }
    }
}
