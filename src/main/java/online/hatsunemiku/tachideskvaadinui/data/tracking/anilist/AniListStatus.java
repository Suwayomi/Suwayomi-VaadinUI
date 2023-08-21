package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist;

import lombok.Getter;

@Getter
public enum AniListStatus {
  CURRENT("Reading"),
  PLANNING("Planning"),
  COMPLETED("Completed"),
  DROPPED("Dropped"),
  PAUSED("Paused"),
  REPEATING("Re-reading");

  private final String status;

  AniListStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return status;
  }
}
