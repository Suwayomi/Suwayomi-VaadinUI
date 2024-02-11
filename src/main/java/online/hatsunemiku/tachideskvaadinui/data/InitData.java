/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.Data;

@Data
public class InitData {
  @JsonProperty("suwayomiSettings")
  private boolean suwayomiSettings;

  public InitData(@JsonProperty("suwayomiSettings") boolean suwayomiSettings) {
    this.suwayomiSettings = suwayomiSettings;
  }

  public InitData() {
    this(false);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (InitData) obj;
    return this.suwayomiSettings == that.suwayomiSettings;
  }

  @Override
  public int hashCode() {
    return Objects.hash(suwayomiSettings);
  }

  @Override
  public String toString() {
    return "InitData[" + "suwayomiSettings=" + suwayomiSettings + ']';
  }
}
