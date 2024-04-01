/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.katsute.mal4j.AccessToken;
import java.time.Instant;
import javax.annotation.Nullable;
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

  @Getter
  @JsonProperty("refresh_token")
  private final String refreshToken;

  OAuthData(OAuthResponse response) {
    String access_token = response.getAccessToken();
    String token_type = response.getTokenType();
    String refresh_token = response.getRefreshToken();

    Instant now = Instant.now();
    Instant expires_in = now.plusSeconds(response.getExpiresIn());

    String expires = expires_in.toString();

    this.accessToken = access_token;
    this.tokenType = token_type;
    this.expires = expires;
    this.refreshToken = refresh_token;
  }

  public OAuthData(AccessToken token) {
    String access_token = token.getToken();
    String token_type = "Bearer";
    String refresh_token = token.getRefreshToken();

    Instant expiry = Instant.ofEpochSecond(token.getExpiryEpochSeconds());
    String expires = expiry.toString();

    this.accessToken = access_token;
    this.tokenType = token_type;
    this.expires = expires;
    this.refreshToken = refresh_token;
  }

  @JsonCreator
  private OAuthData(String accessToken,
      String tokenType,
      @JsonAlias("expires_in") @JsonProperty("expires") String expires,
      @Nullable String refreshToken) {
    this.accessToken = accessToken;
    this.tokenType = tokenType;

    Instant expiry;

    try {
      int expiresIn = Integer.parseInt(expires);
      expiry = Instant.now().plusSeconds(expiresIn);
    } catch (NumberFormatException e) {
      expiry = Instant.parse(expires);
    }

    //formats to ISO 8601 -> YYYY-MM-DDTHH:MM:SSZ Example: 2024-02-27T19:17:51Z
    expires = expiry.toString();

    this.expires = expires;
    this.refreshToken = refreshToken;
  }

  @JsonIgnore
  public Instant getExpiresAsInstant() {
    return Instant.parse(expires);
  }
}
