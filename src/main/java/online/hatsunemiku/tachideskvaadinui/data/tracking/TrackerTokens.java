/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking;

import java.time.Instant;
import lombok.Getter;

@Getter
public class TrackerTokens {
  private OAuthData aniListToken;

  public TrackerTokens() {
    aniListToken = null;
  }

  public boolean hasAniListToken() {
    if (aniListToken == null) {
      return false;
    }

    if (aniListToken.getAccessToken().isEmpty()) {
      return false;
    }

    return !aniListToken.getExpiresAsInstant().isBefore(Instant.now());
  }

  public void setAniListAuth(OAuthResponse response) {
    aniListToken = new OAuthData(response);
  }
}
