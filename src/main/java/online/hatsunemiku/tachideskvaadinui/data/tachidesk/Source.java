package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Source {

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
  private String id;

  @JsonProperty("iconUrl")
  private String iconUrl;

  @JsonProperty("lang")
  private String lang;

  public boolean isSupportsLatest() {
    return supportsLatest;
  }

  public boolean isIsConfigurable() {
    return isConfigurable;
  }

  public boolean isIsNsfw() {
    return isNsfw;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public String getLang() {
    return lang;
  }
}
