package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Chapter {

  @JsonProperty("pageCount")
  private int pageCount;

  @JsonProperty("read")
  private boolean read;

  @JsonProperty("mangaId")
  private int mangaId;

  @JsonProperty("scanlator")
  private String scanlator;

  @JsonProperty("bookmarked")
  private boolean bookmarked;

  @JsonProperty("chapterCount")
  private int chapterCount;

  @JsonProperty("index")
  private int index;

  @JsonProperty("fetchedAt")
  private int fetchedAt;

  @JsonProperty("chapterNumber")
  private int chapterNumber;

  @JsonProperty("downloaded")
  private boolean downloaded;

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

  public int getPageCount() {
    return pageCount;
  }

  public boolean isRead() {
    return read;
  }

  public int getMangaId() {
    return mangaId;
  }

  public Object getScanlator() {
    return scanlator;
  }

  public boolean isBookmarked() {
    return bookmarked;
  }

  public int getChapterCount() {
    return chapterCount;
  }

  public int getIndex() {
    return index;
  }

  public int getFetchedAt() {
    return fetchedAt;
  }

  public int getChapterNumber() {
    return chapterNumber;
  }

  public boolean isDownloaded() {
    return downloaded;
  }

  public String getUrl() {
    return url;
  }

  public int getLastReadAt() {
    return lastReadAt;
  }

  public long getUploadDate() {
    return uploadDate;
  }

  public int getLastPageRead() {
    return lastPageRead;
  }

  public String getName() {
    return name;
  }

  public String getRealUrl() {
    return realUrl;
  }

  public int getId() {
    return id;
  }

  @Override
  public String toString() {
    return name;
  }
}
