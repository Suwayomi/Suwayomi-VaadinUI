/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/** Is used to configure the HTTP related beans. */
@Configuration
public class HttpConfig {

  /**
   * Creates a new and configured instance of the {@link RestTemplate} class.
   *
   * @param builder the {@link RestTemplateBuilder} used to build the {@link RestTemplate} instance.
   * @return a new instance of the {@link RestTemplate} class.
   */
  @Bean
  public RestTemplate buildRestTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  /**
   * Creates a new and configured instance of the {@link WebClient} class.
   *
   * @return a new instance of the {@link WebClient} class.
   */
  @Bean
  public WebClient standardWebClient() {
    return WebClient.create();
  }
}
