package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
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


  @Override
  public String toString() {
    return name;
  }
}
