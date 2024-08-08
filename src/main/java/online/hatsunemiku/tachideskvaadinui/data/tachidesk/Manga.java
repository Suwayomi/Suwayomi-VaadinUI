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

/**
 * Represents a manga object from the Suwayomi API.
 *
 * @since 0.9.0
 * @version 1.12.0
 */
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

  @JsonProperty("chapters")
  @Getter(value = lombok.AccessLevel.NONE)
  private Chapters chapters;

  @JsonProperty("categories")
  @Getter(value = lombok.AccessLevel.NONE)
  private MangaCategories categories;

  public int getLastChapterId() {
    List<Edge> edges = chapters.getEdge();

    if (edges.isEmpty()) {
      return 0;
    }

    return edges.get(edges.size() - 1).getNode().getId();
  }

  public int getFirstChapterId() {
    List<Edge> edges = chapters.getEdge();

    if (edges.isEmpty()) {
      return 0;
    }

    return edges.get(0).getNode().getId();
  }

  public List<Category> getMangaCategories() {
    return categories.getNodes();
  }

  /** Represents chapters of a manga with the total count of chapters. */
  @Getter
  private static class Chapters {

    @JsonProperty("edges")
    private List<Edge> edge;

    @JsonProperty("totalCount")
    private long totalCount;
  }

  @Getter
  private static class Edge {

    @JsonProperty("node")
    private Node node;
  }

  @Getter
  private static class Node {

    @JsonProperty("id")
    private int id;
  }

  @Getter
  private static class MangaCategories {

    @JsonProperty("nodes")
    private List<Category> nodes;
  }

  /**
   * Get the total number of chapters for this manga. If the chapter count is not set, the count
   * will be retrieved from the {@link Chapters chapters} object if available. The default value is
   * 0.
   *
   * @return the total number of chapters for this manga
   */
  public int getChapterCount() {
    if (this.chapterCount != 0) {
      return chapterCount;
    }

    if (chapters == null) {
      return 0;
    }

    return (int) chapters.getTotalCount();
  }
}
