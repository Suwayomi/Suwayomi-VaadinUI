/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@With
@EqualsAndHashCode
public class Chapter {

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
}
