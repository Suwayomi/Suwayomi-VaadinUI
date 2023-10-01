/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
