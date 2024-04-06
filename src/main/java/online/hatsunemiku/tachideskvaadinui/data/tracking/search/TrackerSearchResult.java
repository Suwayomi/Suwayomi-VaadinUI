/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.Getter;
import org.jetbrains.annotations.Contract;

/**
 * Represents the search result for a manga on a tracker on the Suwayomi Server.
 *
 * <p>This class is used for deserializing the JSON response from the Suwayomi Server and contains
 * useful information about the manga, as well as metadata that can be used to retrieve more
 * information.
 */
@Getter
public final class TrackerSearchResult {

  /** The url of the cover image for the manga. */
  @JsonProperty("coverUrl")
  private final String coverUrl;

  /** The id of the manga on Suwayomi. */
  @JsonProperty("id")
  private final int id;

  /** The id of the manga on the tracker site. */
  @JsonProperty("remoteId")
  private final int remoteId;

  /** The publishing status of the manga. */
  @JsonProperty("publishingStatus")
  private final String status;

  /** The type of media - e.g. manga, light novel, web novel, etc. */
  @JsonProperty("publishingType")
  private final String type;

  /** The date on which the manga started publishing. */
  @JsonProperty("startDate")
  private final String startDate;

  /** A summary of the manga. */
  @JsonProperty("summary")
  private final String summary;

  /** The title of the manga. */
  @JsonProperty("title")
  private final String title;

  /** The total number of chapters in the manga. */
  @JsonProperty("totalChapters")
  private final int totalChapters;

  /**
   * The url of the manga on the tracker site. For AniList for example: <a
   * href="https://anilist.co/manga/112981">https://anilist.co/manga/112981</a>
   */
  @JsonProperty("trackingUrl")
  private final String trackingUrl;

  /**
   * Constructs a new instance of the {@link TrackerSearchResult} class. This is used for
   * deserializing the JSON response from the Suwayomi Server.
   *
   * @param coverUrl The url of the cover image for the manga.
   * @param id The id of the manga on Suwayomi.
   * @param remoteId The id of the manga on the tracker site. e.g. AniList, MAL, etc.
   * @param status The publishing status of the manga. e.g. Finished, Publishing, etc.
   * @param type The type of media - e.g. manga, light novel, web novel, etc.
   * @param startDate The date on which the manga started publishing.
   * @param summary A summary/description of the manga.
   * @param title The title of the manga.
   * @param totalChapters The total number of chapters in the manga.
   * @param trackingUrl The url of the manga on the tracker site.
   */
  @Contract(pure = true)
  public TrackerSearchResult(
      @JsonProperty("coverUrl") String coverUrl,
      @JsonProperty("id") int id,
      @JsonProperty("remoteId") int remoteId,
      @JsonProperty("publishingStatus") String status,
      @JsonProperty("publishingType") String type,
      @JsonProperty("startDate") String startDate,
      @JsonProperty("summary") String summary,
      @JsonProperty("title") String title,
      @JsonProperty("totalChapters") int totalChapters,
      @JsonProperty("trackingUrl") String trackingUrl) {
    this.coverUrl = coverUrl;
    this.id = id;
    this.remoteId = remoteId;
    this.status = status;
    this.type = type;
    this.startDate = startDate;
    this.summary = summary;
    this.title = title;
    this.totalChapters = totalChapters;
    this.trackingUrl = trackingUrl;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (TrackerSearchResult) obj;
    return Objects.equals(this.coverUrl, that.coverUrl)
        && this.id == that.id
        && Objects.equals(this.status, that.status)
        && Objects.equals(this.type, that.type)
        && Objects.equals(this.startDate, that.startDate)
        && Objects.equals(this.summary, that.summary)
        && Objects.equals(this.title, that.title)
        && this.totalChapters == that.totalChapters
        && Objects.equals(this.trackingUrl, that.trackingUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        coverUrl, id, status, type, startDate, summary, title, totalChapters, trackingUrl);
  }

  /**
   * Returns the type of the manga formatted with the first letter capitalized.
   * @return The formatted type of the manga.
   */
  public String getTypeFormatted() {
    return type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
  }

  /**
   * Returns the status of the manga formatted with the first letter capitalized.
   * @return The formatted status of the manga.
   */
  public String getStatusFormatted() {
    return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
  }
}
