/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import elemental.json.Json;
import elemental.json.JsonObject;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@UtilityClass
public class GraphQLUtils {

  public static String sendGraphQLRequest(String query, String variables, WebClient client) {
    JsonObject variablesJson = Json.parse(variables);

    JsonObject json = Json.createObject();
    json.put("query", query);
    json.put("variables", variablesJson);

    return client
        .post()
        .uri("/api/graphql")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(json.toJson())
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }
}
