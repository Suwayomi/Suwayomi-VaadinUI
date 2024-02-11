/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class Source implements Comparable<Source> {

  @JsonProperty("supportsLatest")
  private boolean supportsLatest;

  @JsonProperty("isConfigurable")
  private boolean isConfigurable;

  @JsonProperty("isNsfw")
  private boolean isNsfw;

  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("name")
  private String name;

  @JsonProperty("id")
  private String id;

  @JsonProperty("iconUrl")
  private String iconUrl;

  @JsonProperty("lang")
  private String lang;

  @Override
  public int compareTo(@NotNull Source o) {
    return displayName.compareTo(o.getDisplayName());
  }
}
