package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist;

import java.util.List;

public record MangaList(
    List<AniListMedia> reading,
    List<AniListMedia> planToRead,
    List<AniListMedia> completed,
    List<AniListMedia> onHold,
    List<AniListMedia> dropped) {

  public MangaList(
      List<AniListMedia> reading,
      List<AniListMedia> planToRead,
      List<AniListMedia> completed,
      List<AniListMedia> onHold,
      List<AniListMedia> dropped) {
    this.reading = List.copyOf(reading);
    this.planToRead = List.copyOf(planToRead);
    this.completed = List.copyOf(completed);
    this.onHold = List.copyOf(onHold);
    this.dropped = List.copyOf(dropped);
  }
}
