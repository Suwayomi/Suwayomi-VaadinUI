/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Getter;

public class OAuthData {

  @Getter
  @JsonProperty("access_token")
  private final String accessToken;

  @Getter
  @JsonProperty("token_type")
  private final String tokenType;

  @JsonProperty("expires")
  private final String expires;

  OAuthData(OAuthResponse response) {
    String access_token = response.getAccessToken();
    String token_type = response.getTokenType();

    Instant now = Instant.now();
    Instant expires_in = now.plusSeconds(response.getExpiresIn());

    String expires = expires_in.toString();

    this.accessToken = access_token;
    this.tokenType = token_type;
    this.expires = expires;
  }

  @JsonCreator
  private OAuthData(String accessToken, String tokenType, String expires) {
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.expires = expires;
  }

  @JsonIgnore
  public Instant getExpiresAsInstant() {
    return Instant.parse(expires);
  }
}
