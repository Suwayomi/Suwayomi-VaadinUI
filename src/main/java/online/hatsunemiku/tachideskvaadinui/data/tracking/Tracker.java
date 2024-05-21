/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import lombok.Setter;

/**
 * Represents a tracked manga. It contains fields for a manga ID, an AniList ID, a MyAnimeList ID,
 * and a flag indicating if the tracker is private.
 */
@Data
public class Tracker {

  /**
   * Represents the ID of a manga on the Suwayomi Server.
   */
  @Setter
  private long mangaId;

  /**
   * Represents the ID of a manga on AniList.
   */
  private int aniListId;

  /**
   * Represents the ID of a manga on MyAnimeList.
   */
  private int malId;

  /**
   * Represents whether the tracker should be treated as private. If true, the tracked manga should
   * if possible be marked as private on external trackers.
   */
  private boolean isPrivate;

  /**
   * Constructs a new Tracker object with the given parameters.
   *
   * @param mangaId   the ID of a manga on the Suwayomi Server
   * @param aniListId the ID of a manga on AniList
   * @param malId     the ID of a manga on MyAnimeList
   * @param isPrivate whether the tracker should be treated as private
   */
  @JsonCreator
  public Tracker(long mangaId, int aniListId, int malId, boolean isPrivate) {
    this.mangaId = mangaId;
    this.aniListId = aniListId;
    this.malId = malId;
    this.isPrivate = isPrivate;
  }

  /**
   * Constructs a new Tracker object with the given mangaId.
   *
   * @param mangaId the ID of a manga on the Suwayomi Server
   */
  public Tracker(long mangaId) {
    this.mangaId = mangaId;
  }

  public boolean hasAniListId() {
    return aniListId != 0;
  }

  /**
   * Checks if the tracker has a MyAnimeList ID.
   *
   * @return {@code true} if the tracker has a MyAnimeList ID, {@code false} otherwise
   */
  public boolean hasMalId() {
    return malId != 0;
  }

  public void removeAniListId() {
    aniListId = 0;
  }

  /**
   * Removes the MyAnimeList ID from the tracker.
   */
  public void removeMalId() {
    malId = 0;
  }
}
