/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@With
@EqualsAndHashCode
public class Chapter implements Comparable<Chapter> {

  @JsonProperty("pageCount")
  private int pageCount;

  @JsonProperty("isRead")
  private boolean isRead;

  @JsonProperty("mangaId")
  private int mangaId;

  @JsonProperty("scanlator")
  private String scanlator;

  @JsonProperty("bookmarked")
  private boolean bookmarked;

  @JsonProperty("chapterCount")
  private int chapterCount;

  @JsonProperty("fetchedAt")
  private int fetchedAt;

  @JsonProperty("chapterNumber")
  private float chapterNumber;

  @JsonProperty("isDownloaded")
  private boolean isDownloaded;

  @JsonProperty("url")
  private String url;

  @JsonProperty("lastReadAt")
  private int lastReadAt;

  @JsonProperty("uploadDate")
  private long uploadDate;

  @JsonProperty("lastPageRead")
  private int lastPageRead;

  @JsonProperty("name")
  private String name;

  @JsonProperty("realUrl")
  private String realUrl;

  @JsonProperty("id")
  private int id;

  @JsonProperty("manga")
  private Manga manga;

  @Override
  public String toString() {
    return name;
  }

  /**
   * Compares this chapter to another chapter based on the chapter number.
   *
   * @param o the object to be compared.
   * @return a negative integer, zero, or a positive integer as this chapter is less than, equal to,
   *     or greater than the specified chapter.
   */
  @Override
  public int compareTo(@NotNull Chapter o) {
    if (Objects.equals(this, o)) {
      return 0;
    }

    return Float.compare(chapterNumber, o.chapterNumber);
  }
}
