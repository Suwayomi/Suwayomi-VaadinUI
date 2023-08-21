package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist;

import lombok.Getter;

public enum AniListScoreFormat {
  POINT_100("POINT_100", 100, 1),
  POINT_10_DECIMAL("POINT_10_DECIMAL", 10, 1),
  POINT_10("POINT_10", 10, 1),
  POINT_5("POINT_5", 5, 1),
  POINT_3("POINT_3", 3, 1);

  private final String format;
  @Getter
  private final int maxScore;
  @Getter
  private final int minScore;

  AniListScoreFormat(String format, int maxScore, int minScore) {
    this.format = format;
    this.maxScore = maxScore;
    this.minScore = minScore;
  }
}
