/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.UrlChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.client.WebSocketGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.StandardWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

/**
 * The WebClientService class is responsible for creating and managing clients used by other
 * services to communicate with APIs.
 */
@Getter
@Service
public class WebClientService {

  private static final Logger log = LoggerFactory.getLogger(WebClientService.class);
  private WebClient webClient;
  private HttpGraphQlClient graphQlClient;
  private WebSocketGraphQlClient webSocketGraphQlClient;
  private WebClientGraphQLClient dgsGraphQlClient;

  /**
   * Creates a new instance of the {@link WebClientService} class.
   *
   * @param settingsService the {@link SettingsService} used for getting the current settings.
   */
  public WebClientService(SettingsService settingsService) {
    Settings settings = settingsService.getSettings();

    this.webClient = WebClient.create(settings.getUrl());
    initGraphQlClient(settings.getUrl());
    initWebSocketGraphQlClient(settings.getUrl());
    initDgsGraphQlClient(settings.getUrl());
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

    initGraphQlClient(event.getUrl());
    initWebSocketGraphQlClient(event.getUrl());
    initDgsGraphQlClient(event.getUrl());
  }

  @PreDestroy
  protected void destroy() {
    if (webSocketGraphQlClient != null) {
      webSocketGraphQlClient.stop().block(Duration.ofSeconds(10));
    }
  }

  private void initGraphQlClient(String url) {
    url = url + "/api/graphql";
    url = url.replace("//api", "/api");

    // Uncomment this and the clientConnector in the WebClient.builder() to enable logs for graphQL
    // in debug.log
    /*  HttpClient nettyClient =
            HttpClient.create().wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
    */
    // 4MB memory limit
    WebClient graphClient =
        WebClient.builder()
            .baseUrl(url)
            //          .clientConnector(new ReactorClientHttpConnector(nettyClient))
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
            .build();

    this.graphQlClient = HttpGraphQlClient.create(graphClient);
  }

  /**
   * Initializes the WebSocket GraphQL client with the given URL.
   *
   * @param url the URL of the GraphQL server without the {@code /api/graphql} path.
   */
  private void initWebSocketGraphQlClient(String url) {
    url = url + "/api/graphql";
    url = url.replace("//api", "/api");
    url = url.replace("http", "ws");
    url = url.replace("https", "wss");

    WebSocketClient webSocketClient = new StandardWebSocketClient();

    URI uri = URI.create(url);

    this.webSocketGraphQlClient =
        WebSocketGraphQlClient.builder(uri, webSocketClient)
            .keepAlive(Duration.of(10, ChronoUnit.SECONDS))
            .build();
  }

  /**
   * Initializes the DGS GraphQL client.
   *
   * @param url the URL of the GraphQL server without the {@code /api/graphql} path.
   */
  private void initDgsGraphQlClient(String url) {
    url = url + "/api/graphql";
    url = url.replace("//api", "/api");

    WebClient internal = WebClient.create(url);

    this.dgsGraphQlClient = MonoGraphQLClient.createWithWebClient(internal);
  }
}
