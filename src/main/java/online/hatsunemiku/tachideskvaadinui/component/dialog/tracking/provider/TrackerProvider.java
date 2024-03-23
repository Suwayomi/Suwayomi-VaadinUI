/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider;

import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;

import java.util.List;

public interface TrackerProvider {


    public boolean canSetPrivate();

    public List<TrackerSearchResult> search(String query);

    /**
     * @param isPrivate whether the entry should be set to private
     * @param mangaId the id of the manga according to Suwayomi
     * @param externalId the id of the manga on the tracker
     * @throws IllegalArgumentException if `isPrivate` is set to true and the tracker does not support private entries
     */
    public void submitToTracker(boolean isPrivate, int mangaId, int externalId);

    /**
     * Equivalent to calling `submitToTracker(false, mangaId, externalId)`
     */
    public default void submitToTracker(int mangaId, int externalId) {
        submitToTracker(false, mangaId, externalId);
    }
}
