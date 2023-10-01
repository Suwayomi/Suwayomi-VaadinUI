/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Manga {

  @JsonProperty("sourceId")
  private String sourceId;

  @JsonProperty("artist")
  private String artist;

  @JsonProperty("chaptersLastFetchedAt")
  private int chaptersLastFetchedAt;

  @JsonProperty("description")
  private String description;

  @JsonProperty("unreadCount")
  private int unreadCount;

  @JsonProperty("source")
  private Object source;

  @JsonProperty("title")
  private String title;

  @JsonProperty("freshData")
  private boolean freshData;

  @JsonProperty("thumbnailUrlLastFetched")
  private int thumbnailUrlLastFetched;

  @JsonProperty("inLibraryAt")
  private int inLibraryAt;

  @JsonProperty("genre")
  private List<String> genre;

  @JsonProperty("realUrl")
  private String realUrl;

  @JsonProperty("initialized")
  private boolean initialized;

  @JsonProperty("id")
  private int id;

  @JsonProperty("thumbnailUrl")
  private String thumbnailUrl;

  @JsonProperty("lastFetchedAt")
  private int lastFetchedAt;

  @Setter
  @JsonProperty("inLibrary")
  private boolean inLibrary;

  @JsonProperty("author")
  private String author;

  @JsonProperty("chapterCount")
  private int chapterCount;

  @JsonProperty("url")
  private String url;

  @JsonProperty("updateStrategy")
  private String updateStrategy;

  @JsonProperty("chaptersAge")
  private int chaptersAge;

  @JsonProperty("lastChapterRead")
  private Chapter lastChapterRead;

  @JsonProperty("downloadCount")
  private int downloadCount;

  @JsonProperty("age")
  private int age;

  @JsonProperty("status")
  private String status;
}
