/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FlareSolverrSettings {

  @JsonProperty("flareSolverrEnabled")
  private boolean enabled;

  @JsonProperty("flareSolverrSessionName")
  private String sessionName;

  @JsonProperty("flareSolverrSessionTtl")
  private int sessionTTL;

  @JsonProperty("flareSolverrTimeout")
  private int timeout;

  @JsonProperty("flareSolverrUrl")
  private String url;
}
