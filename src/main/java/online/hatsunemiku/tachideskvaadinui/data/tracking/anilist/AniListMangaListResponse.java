package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AniListMangaListResponse(@JsonProperty("data") Data data) {

  public record Data(@JsonProperty("Page") Page page) {}

  public record Page(List<AniListMedia> media) {}
}
