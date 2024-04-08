/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Represents the response of an OAuth request from AniList. */
@Getter
@NoArgsConstructor
public class OAuthResponse {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("token_type")
  private String tokenType;

  @JsonProperty("expires_in")
  private long expiresIn;

  /**
   * Creates a new instance of the {@link OAuthResponse} class.
   *
   * @param accessToken the access token returned by the OAuth server.
   * @param expiresIn the {@link Instant} instance that represents the point in time at which the
   *     access token expires.
   * @param tokenType the type of token returned by the OAuth Server. e.g. "Bearer".
   */
  @SuppressWarnings("unused") // Used by Spring when deserializing a request in AuthAPI
  public OAuthResponse(String accessToken, Instant expiresIn, String tokenType) {
    this.accessToken = accessToken;
    this.expiresIn = expiresIn.getEpochSecond();
    this.tokenType = tokenType;
  }
}
