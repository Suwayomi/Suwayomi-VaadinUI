/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import io.netty.handler.logging.LogLevel;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.time.Duration;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.UrlChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.client.WebSocketGraphQlClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.StandardWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Getter
@Service
public class WebClientService {

  private static final Logger log = LoggerFactory.getLogger(WebClientService.class);
  private WebClient webClient;
  private HttpGraphQlClient graphQlClient;
  private WebSocketGraphQlClient webSocketGraphQlClient;

  public WebClientService(SettingsService settingsService) {
    Settings settings = settingsService.getSettings();

    this.webClient = WebClient.create(settings.getUrl());
    initGraphQlClient(settings.getUrl());
    initWebSocketGraphQlClient(settings.getUrl());
  }

  @EventListener(UrlChangeEvent.class)
  protected void onUrlChange(UrlChangeEvent event) {
    this.webClient = WebClient.create(event.getUrl());

    initGraphQlClient(event.getUrl());
    initWebSocketGraphQlClient(event.getUrl());
  }

  @PreDestroy
  protected void destroy() {
    if (webSocketGraphQlClient != null) {
      webSocketGraphQlClient.stop().block(Duration.ofSeconds(5));
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

  private void initWebSocketGraphQlClient(String url) {
    url = url + "/api/graphql";
    url = url.replace("//api", "/api");
    url = url.replace("http", "ws");
    url = url.replace("https", "wss");

    WebSocketClient webSocketClient = new StandardWebSocketClient();

    URI uri = URI.create(url);

    this.webSocketGraphQlClient = WebSocketGraphQlClient.create(uri, webSocketClient);
  }
}
