/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.apollo;

import com.apollographql.java.client.ApolloClient;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApolloConfig {

  @Bean(name = "aniListClient")
  public ApolloClient aniListClient() {
    return new ApolloClient.Builder()
      .serverUrl("https://graphql.anilist.co")
      .build();
  }

  @Bean(name = "suwayomiClient")
  public ApolloClient suwayomiClient(SettingsService settingsService) {

    var url = settingsService.getSettings().getUrl();

    return new ApolloClient.Builder()
      .serverUrl(url + "/graphql")
      .build();
  }
}
