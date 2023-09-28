package online.hatsunemiku.tachideskvaadinui.data.settings.reader;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReaderSettings {

  private ReaderDirection direction;

  public ReaderSettings() {
    this.direction = ReaderDirection.RTL;
  }

}
