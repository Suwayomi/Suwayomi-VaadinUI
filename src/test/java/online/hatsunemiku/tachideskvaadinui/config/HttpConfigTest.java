/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import online.hatsunemiku.tachideskvaadinui.utils.TachideskUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@SpringBootTest
class HttpConfigTest {

  @Autowired private RestClient restClient;

  @Autowired private WebClient standardWebClient;

  @Autowired private HttpClient reactorHttpClient;

  @Test
  void testRestClientResolvesGitHub() {
    assertNotNull(restClient);
    String jarUrl = TachideskUtils.getNewestJarUrl(restClient);
    assertNotNull(jarUrl, "Jar URL should be successfully fetched via RestClient");
    assertTrue(jarUrl.contains("Suwayomi-Server"), "Jar URL should contain Suwayomi-Server");
  }

  @Test
  void testWebClientAndReactorHttpClientConfigured() {
    assertNotNull(standardWebClient);
    assertNotNull(reactorHttpClient);
  }
}
