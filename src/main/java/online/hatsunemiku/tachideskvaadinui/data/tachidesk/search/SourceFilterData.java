/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk.search;

import java.util.List;

public record SourceFilterData(String searchTerm, List<SourceFilterChange> filter) {}
