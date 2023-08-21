package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist;

import com.fasterxml.jackson.annotation.JsonProperty;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;

public record AniListMedia(
    int id,
    MediaTitle title,
    MediaCoverImage coverImage,
    String format,
    String status,
    int chapters,
    String description,
    @JsonProperty("startDate") MediaDate date) {


  public record MediaTitle(
      String userPreferred,
      String romaji,
      String enlgish,
      @JsonProperty("native") String native_
  ) {

  }

  public record MediaCoverImage(String large) {

  }


}
