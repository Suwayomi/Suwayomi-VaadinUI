/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services

import com.apollographql.apollo.ApolloClient
import jakarta.annotation.PreDestroy
import online.hatsunemiku.tachideskvaadinui.data.settings.event.UrlChangeEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

/**
 * The WebClientService class is responsible for creating and managing clients used by other
 * services to communicate with APIs.
 */
@Service
class WebClientService(settingsService: SettingsService) {

    companion object {
        private val log = LoggerFactory.getLogger(WebClientService::class.java)
    }

    final var webClient: WebClient
        private set

    final var apolloClient: ApolloClient? = null


    init {
        val settings = (settingsService as Any).javaClass.getMethod("getSettings").invoke(settingsService)
        val url = settings.javaClass.getMethod("getUrl").invoke(settings) as String
        webClient = WebClient.create(url)
        initApolloClient(url)
    }

    /**
     * Handles an [UrlChangeEvent] by updating the clients with the new URL of the server
     * instance. Should only be called by Spring when an [UrlChangeEvent] is published.
     *
     * @param event the [UrlChangeEvent] to handle.
     */
    @EventListener(UrlChangeEvent::class)
    protected fun onUrlChange(event: UrlChangeEvent) {
        val url = event.javaClass.getMethod("getUrl").invoke(event) as String
        webClient = WebClient.create(url)
        initApolloClient(url)
    }

    @PreDestroy
    protected fun destroy() {
        apolloClient?.close()
    }

    /**
     * Initializes the Apollo GraphQL client.
     *
     * @param url the URL of the GraphQL server without the `/api/graphql` path.
     */
    private fun initApolloClient(url: String) {
        var httpUrl = "$url/api/graphql"
        httpUrl = httpUrl.replace("//api", "/api")

        val wsUrl = httpUrl.replace("http", "ws").replace("https", "wss")

        apolloClient?.close()

        apolloClient = ApolloClient.Builder()
            .serverUrl(httpUrl)
            .webSocketServerUrl(wsUrl)
            .build()
    }
}
