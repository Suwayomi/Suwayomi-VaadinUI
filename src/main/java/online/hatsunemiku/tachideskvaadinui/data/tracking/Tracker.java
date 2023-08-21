package online.hatsunemiku.tachideskvaadinui.data.tracking;

import lombok.Data;
import lombok.Setter;

@Data
public class Tracker {

  @Setter
  private long mangaId;
  private int aniListId;

  public boolean hasAniListId() {
    return aniListId != 0;
  }


}
