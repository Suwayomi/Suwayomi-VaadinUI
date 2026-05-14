/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.mal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MALMangaStatus {
  @JsonProperty("reading")
  Reading("Reading", "reading"),
  @JsonProperty("completed")
  Completed("Completed", "completed"),
  @JsonProperty("on_hold")
  OnHold("On Hold", "on_hold"),
  @JsonProperty("dropped")
  Dropped("Dropped", "dropped"),
  @JsonProperty("plan_to_read")
  PlanToRead("Plan to Read", "plan_to_read");

  private final String status;
  private final String apiValue;

  MALMangaStatus(String status, String apiValue) {
    this.status = status;
    this.apiValue = apiValue;
  }

  @JsonValue
  public String getApiValue() {
    return apiValue;
  }

  @Override
  public String toString() {
    return status;
  }
}
