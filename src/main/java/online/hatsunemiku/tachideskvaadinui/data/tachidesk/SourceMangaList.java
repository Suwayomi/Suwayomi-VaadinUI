package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class SourceMangaList {

  @JsonProperty("mangaList")
  private List<Manga> mangaList;

  @JsonProperty("hasNextPage")
  private boolean hasNextPage;
}
