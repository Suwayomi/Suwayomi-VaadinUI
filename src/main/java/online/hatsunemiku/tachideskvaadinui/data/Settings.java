package online.hatsunemiku.tachideskvaadinui.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.data.tracking.TrackerTokens;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

public class Settings {

  @NonNull
  private String url;
  @JsonProperty("trackers")
  private final Map<Long, Tracker> trackers;
  @Getter
  @JsonProperty("trackerTokens")
  private final TrackerTokens trackerTokens;

  public Settings(@NotNull String url) {
    this.url = url;
    trackers = new HashMap<>();
    trackerTokens = new TrackerTokens();
  }

  @JsonCreator
  public Settings(@JsonProperty("url") @NotNull String url,
      @JsonProperty("trackers") Map<Long, Tracker> trackers,
      @JsonProperty("trackerTokens") TrackerTokens trackerTokens) {
    this.url = url;
    this.trackers = Objects.requireNonNullElseGet(trackers, HashMap::new);
    this.trackerTokens = Objects.requireNonNullElseGet(trackerTokens, TrackerTokens::new);
  }

  public Tracker getTracker(long mangaId) {
    trackers.putIfAbsent(mangaId, new Tracker());
    return trackers.get(mangaId);
  }

  public void setUrl(@NonNull String url) {
    this.url = url;
  }

  @NonNull
  public String getUrl() {
    return url;
  }
}
