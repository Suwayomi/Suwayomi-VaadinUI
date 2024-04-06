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
import java.time.format.DateTimeFormatter;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * Represents a data object that contains the necessary information for OAuth authentication.
 *
 * <p>This class serves as the translated form of various OAuth responses from different OAuth
 * servers and should be used to store the necessary data for authentication.
 */
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

  /**
   * Constructs an {@link OAuthData} object from an OAuthResponse.
   *
   * @param response the {@link OAuthResponse} object to deconstruct into an {@link OAuthData}
   *     object.
   */
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

  /**
   * Constructs an {@link OAuthData} object from an {@link AccessToken} object.
   *
   * @param token the {@link AccessToken} object used to construct the OAuthData. It contains the
   *     necessary data, such as the access token, refresh token, and expiry information.
   */
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

  /**
   * Constructs an {@link OAuthData} object. Only used for deserialization purposes.
   *
   * @param accessToken the access token for OAuth authentication.
   * @param tokenType the type of token returned by the OAuth server. e.g. "Bearer".
   * @param expires the expiry time of the access token as a {@link String}. This can be either a
   *     number of seconds or an ISO 8601 formatted date. see {@link DateTimeFormatter#ISO_INSTANT}
   * @param refreshToken the refresh token for OAuth authentication. May be null.
   */
  @JsonCreator
  private OAuthData(
      String accessToken,
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

    // formats to ISO 8601 -> YYYY-MM-DDTHH:MM:SSZ Example: 2024-02-27T19:17:51Z
    expires = expiry.toString();

    this.expires = expires;
    this.refreshToken = refreshToken;
  }

  /**
   * Retrieves the expiry time of the access token as an {@link Instant}.
   *
   * @return an {@link Instant} object representing the expiry time of the access token.
   */
  @JsonIgnore
  public Instant getExpiresAsInstant() {
    return Instant.parse(expires);
  }
}
