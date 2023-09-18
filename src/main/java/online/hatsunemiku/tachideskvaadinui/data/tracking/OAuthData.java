package online.hatsunemiku.tachideskvaadinui.data.tracking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import lombok.Getter;

public class OAuthData {

  @Getter
  private final String accessToken;
  @Getter
  private final String tokenType;
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

  @JsonIgnore
  public Instant getExpiresAsInstant() {
    return Instant.parse(expires);
  }
}
