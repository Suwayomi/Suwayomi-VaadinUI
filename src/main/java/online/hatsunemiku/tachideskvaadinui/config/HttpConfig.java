/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.config;

import io.netty.resolver.DefaultAddressResolverGroup;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/** Is used to configure the HTTP related beans. */
@Configuration
public class HttpConfig {

  @Bean
  public HttpClient reactorHttpClient() {
    return HttpClient.create()
        .resolver(DefaultAddressResolverGroup.INSTANCE)
        .followRedirect(true)
        .responseTimeout(Duration.ofSeconds(30));
  }

  @Bean
  public RestClient restClient() {
    java.net.http.HttpClient jdkHttpClient =
        java.net.http.HttpClient.newBuilder()
            .followRedirects(Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    return RestClient.builder()
        .requestFactory(new JdkClientHttpRequestFactory(jdkHttpClient))
        .build();
  }

  /**
   * Creates a new and configured instance of the {@link WebClient} class.
   *
   * @param httpClient the Netty {@link HttpClient} configured with system address resolver.
   * @return a new instance of the {@link WebClient} class.
   */
  @Bean
  public WebClient standardWebClient(HttpClient httpClient) {
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }
}
