/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.Instant;
import lombok.Data;

/**
 * Represents a track record of a user for a manga.
 */
@Data
public class TrackRecord {

  private int id;
  private long libraryId;
  private int mangaId;
  private long remoteId;
  private int trackerId;

  private String remoteUrl;

  private String title;
  private float lastChapterRead;
  private int totalChapters;
  private String displayScore;

  private Instant finishDate;
  private Instant startDate;
  private float score;
  private int status;

  /**
   * Constructs a new TrackRecord object with the given parameters.
   *
   * @param id              the ID of the track record
   * @param libraryId       the ID of the manga in the library as a string
   * @param mangaId         the ID of the manga
   * @param remoteId        the ID of the manga on the tracker as a string
   * @param trackerId       the ID of the tracker
   * @param remoteUrl       the URL of the manga on the tracker
   * @param title           the title of the manga
   * @param lastChapterRead the last chapter read by the user
   * @param totalChapters   the total number of chapters in the manga
   * @param displayScore    the visual representation of the score for the manga on the tracker
   * @param finishDate      the date the user finished reading the manga as a unix timestamp string
   * @param startDate       the date the user started reading the manga as a unix timestamp string
   * @param score           the score of the manga
   * @param status          the status of the manga
   */
  @JsonCreator
  private TrackRecord(
      int id,
      String libraryId,
      int mangaId,
      String remoteId,
      int trackerId,
      String remoteUrl,
      String title,
      float lastChapterRead,
      int totalChapters,
      String displayScore,
      String finishDate,
      String startDate,
      float score,
      int status) {
    this.id = id;

    this.libraryId = libraryId == null ? 0 : Long.parseLong(libraryId);
    this.remoteId = remoteId == null ? 0 : Long.parseLong(remoteId);

    this.mangaId = mangaId;
    this.trackerId = trackerId;
    this.remoteUrl = remoteUrl;
    this.title = title;
    this.lastChapterRead = lastChapterRead;
    this.totalChapters = totalChapters;
    this.displayScore = displayScore;

    // dates = String with unix timestamp
    if (finishDate == null || finishDate.equals("0")) {
      this.finishDate = null;
    } else {
      this.finishDate = Instant.ofEpochMilli(Long.parseLong(finishDate));
    }

    if (startDate == null || startDate.equals("0")) {
      this.startDate = null;
    } else {
      this.startDate = Instant.ofEpochMilli(Long.parseLong(startDate));
    }

    this.score = score;
    this.status = status;
  }
}
