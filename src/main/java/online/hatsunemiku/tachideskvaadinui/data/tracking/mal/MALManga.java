/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.mal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MALManga(
    int id,
    String title,
    @JsonProperty("main_picture") MALPicture mainPicture) {}
