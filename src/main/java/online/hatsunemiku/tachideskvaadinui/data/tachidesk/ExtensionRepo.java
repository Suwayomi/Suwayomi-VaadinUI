/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class ExtensionRepo {
    private String url;

    @Override
    public String toString() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExtensionRepo that = (ExtensionRepo) o;

        return Objects.equals(url, that.url);
    }
}
