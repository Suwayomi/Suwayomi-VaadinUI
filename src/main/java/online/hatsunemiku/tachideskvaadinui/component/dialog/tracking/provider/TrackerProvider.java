/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider;

import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;

/**
 * Represents a provider for a tracking service. It contains methods for searching for manga on the
 * tracker and submitting manga to the tracker. Implementations of this interface are responsible
 * for handling the specifics of the tracker's API.
 */
public interface TrackerProvider {

  /**
   * Checks if the tracker supports setting entries to private.
   *
   * @return {@code true} if the tracker supports setting entries to private, {@code false}
   *     otherwise
   */
  boolean canSetPrivate();

  /**
   * Searches for manga on the tracker.
   *
   * @param query the search query to use when searching for manga
   * @return a list of {@link TrackerSearchResult} objects representing the search results
   */
  List<TrackerSearchResult> search(String query);

  /**
   * @param isPrivate whether the entry should be set to private
   * @param mangaId the id of the manga according to Suwayomi
   * @param externalId the id of the manga on the tracker
   * @throws IllegalArgumentException if `isPrivate` is set to true and the tracker does not support
   *     private entries
   */
  void submitToTracker(boolean isPrivate, int mangaId, int externalId);

  /** Equivalent to calling `submitToTracker(false, mangaId, externalId)` */
  default void submitToTracker(int mangaId, int externalId) {
    submitToTracker(false, mangaId, externalId);
  }
}
