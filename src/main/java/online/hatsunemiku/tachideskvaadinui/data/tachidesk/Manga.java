package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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
  private Object lastChapterRead;

  @JsonProperty("downloadCount")
  private int downloadCount;

  @JsonProperty("age")
  private int age;

  @JsonProperty("status")
  private String status;

  public String getSourceId() {
    return sourceId;
  }

  public String getArtist() {
    return artist;
  }

  public int getChaptersLastFetchedAt() {
    return chaptersLastFetchedAt;
  }

  public String getDescription() {
    return description;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public Object getSource() {
    return source;
  }

  public String getTitle() {
    return title;
  }

  public boolean isFreshData() {
    return freshData;
  }

  public int getThumbnailUrlLastFetched() {
    return thumbnailUrlLastFetched;
  }

  public int getInLibraryAt() {
    return inLibraryAt;
  }

  public List<String> getGenre() {
    return genre;
  }

  public String getRealUrl() {
    return realUrl;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public int getId() {
    return id;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public int getLastFetchedAt() {
    return lastFetchedAt;
  }

  public boolean isInLibrary() {
    return inLibrary;
  }

  public String getAuthor() {
    return author;
  }

  public int getChapterCount() {
    return chapterCount;
  }

  public String getUrl() {
    return url;
  }

  public String getUpdateStrategy() {
    return updateStrategy;
  }

  public int getChaptersAge() {
    return chaptersAge;
  }

  public Object getLastChapterRead() {
    return lastChapterRead;
  }

  public int getDownloadCount() {
    return downloadCount;
  }

  public int getAge() {
    return age;
  }

  public String getStatus() {
    return status;
  }
}
