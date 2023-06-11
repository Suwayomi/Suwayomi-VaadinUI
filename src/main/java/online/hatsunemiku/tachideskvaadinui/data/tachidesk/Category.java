package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class Category {
  private int id;
  private int order;
  private String name;
  @JsonAlias(value = "default")
  private boolean def;

}
