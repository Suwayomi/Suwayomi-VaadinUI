package online.hatsunemiku.tachideskvaadinui.data.tracking;

import lombok.Getter;
import lombok.Setter;

@Getter
public class TrackerTokens {
  @Setter private String aniListToken;

  private String refreshToken;

  public TrackerTokens() {
    aniListToken = "";
    refreshToken = "";
  }

  public boolean hasAniListToken() {
    return !aniListToken.isEmpty();
  }

  public void setAniListAuth(OAuthResponse response) {
    aniListToken = response.getAccessToken();
    refreshToken = response.getRefreshToken();
  }
}
