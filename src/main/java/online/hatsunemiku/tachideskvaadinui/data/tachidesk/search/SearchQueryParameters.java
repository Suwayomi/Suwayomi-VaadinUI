package online.hatsunemiku.tachideskvaadinui.data.tachidesk.search;

import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchQueryParameters {

  private int pageNum;
  @Nullable private String searchTerm;
}
