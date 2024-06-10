/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * This class represents the settings used for FlareSolverr}.
 *
 * @see <a href="https://github.com/FlareSolverr/FlareSolverr">FlareSolverr</a>
 */
@Data
public class FlareSolverrSettings {

  /** A boolean indicating whether FlareSolverr is enabled on the server. */
  @JsonProperty("flareSolverrEnabled")
  private boolean enabled;

  /** The name of the FlareSolverr session. */
  @JsonProperty("flareSolverrSessionName")
  private String sessionName;

  /** The time-to-live (TTL) of the FlareSolverr session. */
  @JsonProperty("flareSolverrSessionTtl")
  private int sessionTTL;

  /** The timeout for the FlareSolverr session, in seconds. */
  @JsonProperty("flareSolverrTimeout")
  private int timeout;

  /** The URL of the FlareSolverr server. e.g. http://localhost:8191 */
  @JsonProperty("flareSolverrUrl")
  private String url;
}
