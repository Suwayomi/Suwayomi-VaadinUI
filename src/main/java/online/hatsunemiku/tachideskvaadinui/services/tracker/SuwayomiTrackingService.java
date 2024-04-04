/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;
import online.hatsunemiku.tachideskvaadinui.services.client.suwayomi.SuwayomiTrackingClient;
import org.springframework.stereotype.Service;

/**
 * Represents a Service for handling tracking requests to the Suwayomi Server. This service is a
 * wrapper around the {@link SuwayomiTrackingClient} class and provides methods to abstract the
 * inner workings of the client. Also provides convenience methods for handling tracking requests.
 */
@Service
public class SuwayomiTrackingService {

  private final SuwayomiTrackingClient client;

  /**
   * Represents a Suwayomi Tracking Service.
   *
   * @param client the {@link SuwayomiTrackingClient} used for handling tracking requests to the
   *               Suwayomi Server.
   */
  public SuwayomiTrackingService(SuwayomiTrackingClient client) {
    this.client = client;
  }

  public boolean isAniListAuthenticated() {
    int id = TrackerType.ANILIST.id;
    return client.isTrackerLoggedIn(id);
  }

  public boolean isMALAuthenticated() {
    int id = TrackerType.MAL.id;
    return client.isTrackerLoggedIn(id);
  }

  public String getAniListAuthUrl() {
    int id = TrackerType.ANILIST.id;
    return client.getTrackerAuthUrl(id) + getStateAuthParam(id);
  }

  public String getMALAuthUrl() {
    int id = TrackerType.MAL.id;
    return client.getTrackerAuthUrl(id) + getStateAuthParam(id);
  }

  public List<TrackerSearchResult> searchAniList(String query) {
    int id = TrackerType.ANILIST.id;
    return client.searchTracker(query, id);
  }

  public List<TrackerSearchResult> searchMAL(String query) {
    int id = TrackerType.MAL.id;
    return client.searchTracker(query, id);
  }

  public void trackOnAniList(int mangaId, int externalId) {
    int id = TrackerType.ANILIST.id;
    client.trackMangaOnTracker(mangaId, externalId, id);
  }

  public void trackOnMAL(int mangaId, int externalId) {
    int id = TrackerType.MAL.id;
    client.trackMangaOnTracker(mangaId, externalId, id);
  }

  public void loginSuwayomi(String url, int trackerId) {
    client.loginTracker(url, trackerId);
  }

  private String getStateAuthParam(int id) {

    String jsonTemplate =
        """
            {\
            "redirectUrl":"http://localhost:8080/validate/suwayomi",\
            "trackerId":%d,\
            "anyOtherInfo":"%s"\
            }\
            """
            .strip();

    String template = "&state=%s";

    String trackerName = TrackerType.fromId(id).name();

    String json = jsonTemplate.formatted(id, trackerName);

    return template.formatted(json);
  }

  /** An enumeration representing different types of trackers. */
  @Getter
  private enum TrackerType {
    MAL(1),
    ANILIST(2);

    private final int id;

    /**
     * Instantiates a TrackerType object with the specified id.
     *
     * @param id the id of the Tracker on the Suwayomi Server
     */
    TrackerType(int id) {
      this.id = id;
    }

    public static TrackerType fromId(int id) {
      var match =
          Arrays.stream(TrackerType.values())
              .filter(trackerType -> trackerType.id == id)
              .findFirst();

      return match.orElse(null);
    }
  }
}
