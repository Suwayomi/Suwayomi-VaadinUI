package online.hatsunemiku.tachideskvaadinui.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

public class Settings {

  @NonNull private String url;

  public Settings(@NotNull @JsonProperty("url") String url) {
    this.url = url;
  }

  public void setUrl(@NonNull String url) {
    this.url = url;
  }

  @NonNull
  public String getUrl() {
    return url;
  }
}
