/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import com.apollographql.apollo.runtime.java.ApolloClient;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import okhttp3.OkHttpClient;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.UrlChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The WebClientService class is responsible for creating and managing clients used by other
 * services to communicate with APIs.
 */
@Getter
@Service
public class WebClientService {

  private static final Logger log = LoggerFactory.getLogger(WebClientService.class);
  private final OkHttpClient okHttpClient;
  private WebClient webClient;
  private ApolloClient apolloClient;

  /**
   * Creates a new instance of the {@link WebClientService} class.
   *
   * @param settingsService the {@link SettingsService} used for getting the current settings.
   * @param okHttpClient the shared {@link OkHttpClient} instance.
   */
  public WebClientService(SettingsService settingsService, OkHttpClient okHttpClient) {
    this.okHttpClient = okHttpClient;
    Settings settings = settingsService.getSettings();

    this.webClient = WebClient.create(settings.getUrl());
    initApolloClient(settings.getUrl());
  }

  /**
   * Handles an {@link UrlChangeEvent} by updating the clients with the new URL of the server
   * instance. Should only be called by Spring when an {@link UrlChangeEvent} is published.
   *
   * @param event the {@link UrlChangeEvent} to handle.
   */
  @EventListener(UrlChangeEvent.class)
  protected void onUrlChange(UrlChangeEvent event) {
    this.webClient = WebClient.create(event.getUrl());

    initApolloClient(event.getUrl());
  }

  @PreDestroy
  protected void destroy() {
    if (apolloClient != null) {
      apolloClient.close();
    }
  }

  /**
   * Initializes the Apollo GraphQL client.
   *
   * @param url the URL of the GraphQL server without the {@code /api/graphql} path.
   */
  private void initApolloClient(String url) {
    String httpUrl = url + "/api/graphql";
    httpUrl = httpUrl.replace("//api", "/api");

    String wsUrl = httpUrl.replace("http", "ws").replace("https", "wss");

    if (this.apolloClient != null) {
      try {
        this.apolloClient.close();
      } catch (Exception e) {
        log.error("Error while closing ApolloClient", e);
      }
    }

    this.apolloClient =
        new ApolloClient.Builder()
            .okHttpClient(okHttpClient)
            .serverUrl(httpUrl)
            .webSocketServerUrl(wsUrl)
            .build();
  }
}
