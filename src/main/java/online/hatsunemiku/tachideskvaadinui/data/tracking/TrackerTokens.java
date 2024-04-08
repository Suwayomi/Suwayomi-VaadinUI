/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * This class represents the tokens used for authentication with different tracker APIs. It stores
 * {@link OAuthData} for each API.
 */
@Getter
public class TrackerTokens {
  private OAuthData aniListToken;
  @Setter private OAuthData malToken;

  /** Creates a new instance of the {@link TrackerTokens} class. */
  public TrackerTokens() {
    aniListToken = null;
  }

  /**
   * Checks if the AniList token is present and not expired.
   *
   * @return {@code true} if the AniList token is present and not expired, {@code false} otherwise.
   */
  public boolean hasAniListToken() {
    if (aniListToken == null) {
      return false;
    }

    if (aniListToken.getAccessToken().isEmpty()) {
      return false;
    }

    return !aniListToken.getExpiresAsInstant().isBefore(Instant.now());
  }

  /**
   * Checks if the MAL token is present and not expired.
   *
   * @return {@code true} if the MAL token is present and not expired, {@code false} otherwise.
   */
  public boolean hasMalToken() {
    if (malToken == null) {
      return false;
    }

    if (malToken.getAccessToken().isEmpty()) {
      return false;
    }

    boolean expired = malToken.getExpiresAsInstant().isBefore(Instant.now());

    return malToken.getRefreshToken() != null || !expired;
  }

  /**
   * Sets the AniList authentication data.
   *
   * @param response the {@link OAuthResponse} containing the AniList authentication data.
   */
  public void setAniListAuth(OAuthResponse response) {
    aniListToken = new OAuthData(response);
  }
}
