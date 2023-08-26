package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class Source implements Comparable<Source> {

  @JsonProperty("supportsLatest")
  private boolean supportsLatest;

  @JsonProperty("isConfigurable")
  private boolean isConfigurable;

  @JsonProperty("isNsfw")
  private boolean isNsfw;

  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("name")
  private String name;

  @JsonProperty("id")
  private long id;

  @JsonProperty("iconUrl")
  private String iconUrl;

  @JsonProperty("lang")
  private String lang;

  @Override
  public int compareTo(@NotNull Source o) {
    return displayName.compareTo(o.getDisplayName());
  }
}
