/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoIcon;
import dev.katsute.mal4j.manga.property.MangaStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider.Suwayomi.SuwayomiProvider;
import online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider.TrackerProvider;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Status;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackerType;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.AniListMangaStatistics;
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.MALMangaStatistics;
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.MangaStatistics;
import online.hatsunemiku.tachideskvaadinui.data.tracking.statistics.SuwayomiMangaStatistics;
import online.hatsunemiku.tachideskvaadinui.services.SuwayomiService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.MyAnimeListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.vaadin.miki.superfields.dates.SuperDatePicker;
import org.vaadin.miki.superfields.numbers.SuperDoubleField;

/**
 * TrackingDialog is a custom dialog component used for tracking manga progress with Tracking
 * services. It also allows the user to authenticate with a tracking service.
 */
@CssImport("./css/components/dialog/tracking/tracking-dialog.css")
@Slf4j
public class TrackingDialog extends Dialog {

  private final AniListAPIService aniListAPI;
  private final SuwayomiTrackingService suwayomiTrackingService;
  private final TrackingDataService dataService;
  private final MyAnimeListAPIService malAPI;
  private final SuwayomiService suwayomiService;

  /**
   * Constructs a {@link TrackingDialog} with the given parameters.
   *
   * @param dataService             The {@link TrackingDataService} used for storing tracking data.
   * @param manga                   the {@link Manga} to track with the dialog.
   * @param aniListAPIService       the {@link AniListAPIService} used for making requests to the
   *                                AniList API.
   * @param suwayomiTrackingService the {@link SuwayomiTrackingService} used for making requests to
   *                                the Suwayomi API.
   * @param malAPI                  the {@link MyAnimeListAPIService} used for making requests to
   *                                the MyAnimeList API.
   */
  public TrackingDialog(
      TrackingDataService dataService,
      Manga manga,
      AniListAPIService aniListAPIService,
      SuwayomiTrackingService suwayomiTrackingService,
      MyAnimeListAPIService malAPI,
      SuwayomiService suwayomiService) {
    super();
    this.dataService = dataService;
    this.aniListAPI = aniListAPIService;
    this.suwayomiTrackingService = suwayomiTrackingService;
    this.malAPI = malAPI;
    this.suwayomiService = suwayomiService;

    Tracker tracker = dataService.getTracker(manga.getId());

    // for bugged trackers that have both MAL and AniList IDs
    if (tracker.hasAniListId() && tracker.hasMalId()) {
      tracker.removeMalId();
      tracker.removeAniListId();
    }

    if (tracker.hasAniListId()) {
      if (!suwayomiTrackingService.isMangaTrackedOnAniList(manga.getId())) {
        tracker.removeAniListId();
      }
    }

    if (tracker.hasMalId()) {
      if (!suwayomiTrackingService.isMangaTrackedOnMAL(manga.getId())) {
        tracker.removeMalId();
      }
    }

    if (!tracker.hasAniListId() && !tracker.hasMalId()) {
      addTrackingButtons(manga, aniListAPIService, tracker);
    } else {
      Div statistics;

      try {
        TrackerProvider provider = new SuwayomiProvider(suwayomiTrackingService, aniListAPIService);
        statistics = getTrackingStatistics(tracker, provider);
        add(statistics);
      } catch (RuntimeException e) {

        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setText("Error getting manga stats: " + e.getMessage());
        notification.setDuration(5000);
        notification.open();

        addTrackingButtons(manga, aniListAPIService, tracker);
      }
    }
  }

  /**
   * Adds the tracking buttons to the dialog.
   *
   * @param manga             the {@link Manga} to track
   * @param aniListAPIService the {@link AniListAPIService} to communicate with AniList with
   * @param tracker           the {@link Tracker} instance to update the button states via
   *                          {@link #updateButtons(Button, Button, Tracker)}
   */
  private void addTrackingButtons(
      Manga manga, AniListAPIService aniListAPIService, Tracker tracker) {
    VerticalLayout buttons = new VerticalLayout();
    buttons.setId("tracking-dialog-tracker-buttons");

    Button aniListBtn = new Button("AniList");
    Button malBtn = new Button("MyAnimeList");

    var version = suwayomiService.getServerVersion();

    if (version.orElseThrow().getRevisionNumber() < 1510) {
      malBtn.setEnabled(false);
    }

    aniListBtn.addClickListener(
        e -> {
          boolean isAniListAuthenticated = true;

          if (!aniListAPIService.hasAniListToken()) {
            String url = aniListAPIService.getAniListAuthUrl();
            getUI().ifPresent(ui -> ui.getPage().open(url));
            isAniListAuthenticated = false;
          }

          if (!suwayomiTrackingService.isAniListAuthenticated()) {
            String url = suwayomiTrackingService.getAniListAuthUrl();
            getUI().ifPresent(ui -> ui.getPage().open(url));
            isAniListAuthenticated = false;
          }

          if (!isAniListAuthenticated) {
            return;
          }

          SuwayomiProvider provider = new SuwayomiProvider(suwayomiTrackingService, aniListAPIService);

          try {
            displaySearch(manga.getTitle(), manga.getId(), provider, TrackerType.ANILIST);
          } catch (WebClientResponseException.InternalServerError
                   | WebClientRequestException error) {
            log.error("Invalid response from AniList", error);
            Notification notification = new Notification();
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Couldn't correctly connect to AniList\nPlease try again");
            notification.open();
          }
          updateButtons(aniListBtn, malBtn, tracker);
        });

    malBtn.addClickListener(
        e -> {
          if (!suwayomiTrackingService.isMALAuthenticated()) {
            String url = suwayomiTrackingService.getMALAuthUrl();
            getUI().ifPresent(ui -> ui.getPage().open(url));
            return;
          }

          if (!malAPI.hasMalToken()) {
            String url = malAPI.getAuthUrl();
            getUI().ifPresent(ui -> ui.getPage().open(url));
            return;
          }

          SuwayomiProvider provider = new SuwayomiProvider(suwayomiTrackingService, aniListAPI);

          try {
            displaySearch(manga.getTitle(), manga.getId(), provider, TrackerType.MAL);
          } catch (WebClientResponseException.InternalServerError
                   | WebClientRequestException error) {
            log.error("Invalid response from MyAnimeList", error);
            Notification notification = new Notification();
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Couldn't correctly connect to MyAnimeList\nPlease try again");
            notification.open();
          } catch (RuntimeException ex) {
            log.error("Error displaying search", ex);
            Notification notification = new Notification();
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Error displaying search: " + ex.getMessage());
            notification.open();
          }

          updateButtons(aniListBtn, malBtn, tracker);
        });

    updateButtons(aniListBtn, malBtn, tracker);

    buttons.add(aniListBtn, malBtn);

    add(buttons);
  }

  /**
   * Retrieves the tracking statistics for a given Tracker.
   *
   * @param tracker the Tracker used to retrieve the tracking statistics.
   * @return a Div element containing the tracking statistics.
   * @throws RuntimeException if the manga is not found on AniList.
   */
  @NotNull
  private Div getTrackingStatistics(Tracker tracker, TrackerProvider provider)
      throws RuntimeException {

    // get manga stats via provider
    MangaStatistics mangaStats;
    try {
      mangaStats = provider.getStatistics(tracker);
    } catch (Exception e) {
      log.error("Error getting manga stats", e);
      Notification notification = new Notification();
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.setText("Error getting manga stats: " + e.getMessage());
      notification.open();
      throw new RuntimeException("Error getting manga stats");
    }

    Div content = new Div();

    ComboBox<?> status = getTrackingStatusField(tracker, mangaStats, provider);

    var maxChapters = provider.getMaxChapter(tracker);

    SuperDoubleField chapter = getTrackingChapterField(tracker, mangaStats, maxChapters);

    ComboBox<String> score = getTrackingScoreField(tracker, mangaStats);

    SuperDatePicker endDate = new SuperDatePicker();
    SuperDatePicker startDate = getTrackingStartDateField(tracker, mangaStats, endDate);

    Checkbox privateCheckbox = getPrivateCheckboxField(tracker);

    if (!tracker.hasAniListId()) {
      privateCheckbox.setVisible(false);
      privateCheckbox.setEnabled(false);
    }

    configureTrackingEndDateField(tracker, endDate, mangaStats, startDate);

    Div statistics = new Div();
    statistics.addClassName("tracking-dialog-statistics");

    statistics.add(status, chapter, score, startDate, endDate, privateCheckbox);

    String trackerName;

    if (tracker.hasAniListId()) {
      trackerName = "AniList";
    } else if (tracker.hasMalId()) {
      trackerName = "MyAnimeList";
    } else {
      trackerName = "Suwayomi";
    }

    String trashBtnText = "Remove %s tracking".formatted(trackerName);
    Button trackingDeleteBtn = new Button(trashBtnText, VaadinIcon.TRASH.create());
    trackingDeleteBtn.addClickListener(
        e -> {
          if (tracker.hasAniListId()) {
            suwayomiTrackingService.stopTracking(tracker, false);
            tracker.removeAniListId();
          } else if (tracker.hasMalId()) {
            suwayomiTrackingService.stopTracking(tracker, false);
            tracker.removeMalId();
          } else {
            throw new IllegalArgumentException("Tracker has no ID");
          }

          close();
        });

    String nukeBtnText = "Remove from %s and stop Tracking".formatted(trackerName);
    var nukeBtn = new Button(nukeBtnText, VaadinIcon.BOMB.create());
    nukeBtn.addClickListener(
        e -> {

          var version = suwayomiService.getServerVersion();

          if (version.isEmpty()) {
            throw new RuntimeException("Failed to get server version");
          }

          if (tracker.hasAniListId()) {
            suwayomiTrackingService.stopTracking(tracker, true);

            if (version.get().getRevisionNumber() < 1510) {
              aniListAPI.removeMangaFromList(tracker.getAniListId());
            }

            tracker.removeAniListId();
          } else if (tracker.hasMalId()) {
            suwayomiTrackingService.stopTracking(tracker, true);

            if (version.get().getRevisionNumber() < 1510) {
              malAPI.removeMangaFromList(tracker.getMalId());
            }

            tracker.removeMalId();
          } else {
            throw new IllegalArgumentException("Tracker has no ID");
          }

          tracker.removeAniListId();
          close();
        });

    var buttons = new Div();
    buttons.add(trackingDeleteBtn, nukeBtn);
    buttons.addClassName("tracking-dialog-remove-buttons");

    content.add(statistics, buttons);

    return content;
  }

  @NotNull
  private Checkbox getPrivateCheckboxField(Tracker tracker) {
    Checkbox privateCheckbox = new Checkbox("Private");
    privateCheckbox.setValue(tracker.isPrivate());
    privateCheckbox.addValueChangeListener(
        e -> {
          tracker.setPrivate(e.getValue());
          aniListAPI.updateMangaPrivacyStatus(tracker.getAniListId(), e.getValue());
        });
    return privateCheckbox;
  }

  /**
   * Configures the end date field for tracking a manga.
   *
   * @param tracker    the tracker to update the end date for
   * @param endDate    the end date field to configure
   * @param mangaStats the statistics for the manga
   * @param startDate  the start date field to check against
   */
  private void configureTrackingEndDateField(
      Tracker tracker,
      SuperDatePicker endDate,
      MangaStatistics mangaStats,
      SuperDatePicker startDate) {
    endDate.setPlaceholder("End date");
    endDate.setClearButtonVisible(true);
    endDate.addClassName("three-span");
    if (mangaStats.completedAt() != null) {
      var end = mangaStats.completedAt();
      if (end.year() != null && end.month() != null && end.day() != null) {
        LocalDate date = LocalDate.of(end.year(), end.month(), end.day());
        endDate.setValue(date);
      }
    }
    endDate.addValueChangeListener(
        e -> {
          if (e.getValue() == null) {
            MediaDate date = new MediaDate(null, null, null);

            if (mangaStats instanceof AniListMangaStatistics) {
              aniListAPI.updateMangaEndDate(tracker.getAniListId(), date);
            } else if (mangaStats instanceof MALMangaStatistics) {
              log.warn("End date is null, but MAL doesn't support null end dates");

              Notification notification = new Notification();
              notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
              notification.setText("MyAnimeList doesn't support removing the end date");
              notification.open();
            } else {
              throw new IllegalArgumentException("Unknown MangaStatistics type");
            }

            return;
          }

          if (startDate.getValue() == null) {
            startDate.setValue(e.getValue());
          } else {
            if (startDate.getValue().isAfter(e.getValue())) {
              endDate.setValue(e.getOldValue());
              return;
            }
          }

          MediaDate date = new MediaDate(e.getValue());

          if (mangaStats instanceof AniListMangaStatistics) {
            aniListAPI.updateMangaEndDate(tracker.getAniListId(), date);
          } else if (mangaStats instanceof MALMangaStatistics) {
            malAPI.updateMangaListEndDate(tracker.getMalId(), date);
          } else {
            throw new IllegalArgumentException("Unknown MangaStatistics type");
          }
        });
  }

  /**
   * Creates a field for tracking the start date of a manga.
   *
   * @param tracker    the tracker to update the start date for
   * @param mangaStats the statistics for the manga
   * @param endDate    the end date field to check against
   * @return a {@link SuperDatePicker} for tracking the start date of the manga
   */
  @NotNull
  private SuperDatePicker getTrackingStartDateField(
      Tracker tracker, MangaStatistics mangaStats, SuperDatePicker endDate) {
    SuperDatePicker startDate = new SuperDatePicker();
    startDate.setPlaceholder("Start date");
    startDate.setClearButtonVisible(true);
    startDate.addClassName("three-span");

    if (mangaStats.startedAt() != null) {
      var start = mangaStats.startedAt();

      if (start.year() != null && start.month() != null && start.day() != null) {
        LocalDate date = LocalDate.of(start.year(), start.month(), start.day());
        startDate.setValue(date);
      }
    }

    startDate.addValueChangeListener(
        e -> {
          if (e.getValue() == null) {
            MediaDate date = new MediaDate(null, null, null);

            suwayomiTrackingService.updateMangaStartDate(tracker, date);

            return;
          }

          if (endDate.getValue() != null && e.getValue().isAfter(endDate.getValue())) {
            startDate.setValue(e.getOldValue());

            Notification notification = new Notification();
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Start date cannot be after end date");
            notification.open();
            return;
          }

          MediaDate date = new MediaDate(e.getValue());

          suwayomiTrackingService.updateMangaStartDate(tracker, date);
        });
    return startDate;
  }

  /**
   * Creates a field for tracking the score of a manga.
   *
   * @param tracker    the tracker to update the score for
   * @param mangaStats the statistics for the manga
   * @return a {@link ComboBox} for tracking the score of the manga
   */
  @NotNull
  private ComboBox<String> getTrackingScoreField(Tracker tracker, MangaStatistics mangaStats) {
    ComboBox<String> score = new ComboBox<>();
    List<String> trackingScores = suwayomiTrackingService.getTrackingScores(tracker);
    score.setItems(trackingScores);
    score.setPlaceholder("Score");
    score.addClassName("two-span");

    if (mangaStats.score() != 0) {

      double scoreValue = mangaStats.score();
      if (scoreValue % 1 == 0) {
        score.setValue(String.valueOf((int) scoreValue));
      } else {
        score.setValue(String.valueOf(scoreValue));
      }
    } else {
      score.setValue(null);
    }

    score.addValueChangeListener(
        e -> {
          if (e.getValue() == null || Objects.equals(e.getValue(), "0")) {
            score.setValue(null);
            if (mangaStats instanceof AniListMangaStatistics) {
              aniListAPI.updateMangaScore(tracker.getAniListId(), 0);
            } else if (mangaStats instanceof MALMangaStatistics) {
              malAPI.updateMangaListScore(tracker.getMalId(), 0);
            }
            return;
          }

          double numericValue = Double.parseDouble(e.getValue());
          double biggestValue = Double.parseDouble(trackingScores.get(trackingScores.size() - 1));

          if (numericValue > biggestValue) {
            score.setValue(e.getOldValue());
            Notification notification = new Notification();
            notification.setDuration(5000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Score cannot be higher than " + biggestValue);
            notification.open();
            return;
          }

          if (numericValue < 0) {
            score.setValue(e.getOldValue());
            Notification notification = new Notification();
            notification.setDuration(5000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Score cannot be lower than 0");
            notification.open();
            return;
          }

          if (numericValue == mangaStats.score()) {
            log.debug("Score is the same as before - not updating");
            return;
          }

          suwayomiTrackingService.updateMangaScore(tracker, e.getValue());
        });
    return score;
  }

  /**
   * Creates a ComboBox for selecting the tracking status of a manga.
   *
   * @param tracker    the tracker to update the status for
   * @param mangaStats the statistics for the manga
   * @param provider   the provider with which to get info about the manga
   * @return a {@link ComboBox} for selecting the status of the manga
   */
  @NotNull
  private ComboBox<?> getTrackingStatusField(Tracker tracker, MangaStatistics mangaStats,
      TrackerProvider provider) {
    ComboBox<?> status;

    if (mangaStats instanceof AniListMangaStatistics) {
      status = configureStatusComboBoxAniList(tracker, (AniListMangaStatistics) mangaStats);
    } else if (mangaStats instanceof MALMangaStatistics) {
      status = configureStatusComboBoxMAL(tracker, (MALMangaStatistics) mangaStats);
    } else if (mangaStats instanceof SuwayomiMangaStatistics) {
      status = configureStatusBoxSuwayomi(tracker, (SuwayomiMangaStatistics) mangaStats,
          (SuwayomiProvider) provider);
    } else {
      throw new IllegalArgumentException("Unknown MangaStatistics type");
    }

    status.setPlaceholder("Status");
    status.addClassName("two-span");
    return status;
  }

  /**
   * Creates and configures a ComboBox for selecting the status of a manga on AniList.
   *
   * @param tracker    the tracker to update the status for
   * @param mangaStats the statistics for the manga
   * @return a {@link ComboBox} for selecting the status of the manga
   */
  private ComboBox<AniListStatus> configureStatusComboBoxAniList(
      Tracker tracker, AniListMangaStatistics mangaStats) {
    ComboBox<AniListStatus> status = new ComboBox<>();
    status.setItems(AniListStatus.values());
    status.setValue(mangaStats.status());
    status.addValueChangeListener(
        e -> {
          if (e.getValue() == null) {
            status.setValue(e.getOldValue());
          } else {
            aniListAPI.updateMangaStatus(tracker.getAniListId(), e.getValue());
          }
        });

    return status;
  }

  /**
   * Creates and configures a ComboBox for selecting the status of a manga on MyAnimeList.
   *
   * @param tracker    the tracker to update the status for
   * @param mangaStats the statistics for the manga
   * @return a {@link ComboBox} for selecting the status of the manga
   */
  private ComboBox<MangaStatus> configureStatusComboBoxMAL(
      Tracker tracker, MALMangaStatistics mangaStats) {
    ComboBox<MangaStatus> status = new ComboBox<>();
    status.setItems(MangaStatus.values());
    status.setValue(mangaStats.status());
    status.addValueChangeListener(
        e -> {
          if (e.getValue() == null) {
            status.setValue(e.getOldValue());
          } else {
            malAPI.updateMangaListStatus(tracker.getMalId(), e.getValue());
          }
        });

    return status;
  }

  private ComboBox<Status> configureStatusBoxSuwayomi(Tracker tracker,
      SuwayomiMangaStatistics statistics, SuwayomiProvider provider) {
    ComboBox<Status> status = new ComboBox<>();
    status.setItemLabelGenerator(Status::getName);

    var statuses = provider.getTrackerStatuses(tracker);

    status.setItems(statuses);

    for (Status s : statuses) {
      if (s.getValue() == statistics.status()) {
        status.setValue(s);
        break;
      }
    }

    status.addValueChangeListener(
        e -> {
          if (e.getValue() == null) {
            status.setValue(e.getOldValue());
          } else if (e.getValue().getValue() == statistics.status()) {
            log.debug("Status is the same as before - not updating");
          } else {
            suwayomiTrackingService.updateMangaStatus(tracker, e.getValue().getValue());
          }
        });

    return status;
  }

  /**
   * Creates a Field for tracking the chapter progress of a manga.
   *
   * @param tracker     the tracker to update the progress for
   * @param mangaStats  the statistics for the manga
   * @param maxChapters the maximum number of chapters for the manga
   * @return a {@link SuperDoubleField} for tracking the chapter progress
   */
  @NotNull
  private SuperDoubleField getTrackingChapterField(Tracker tracker, MangaStatistics mangaStats,
      Integer maxChapters) {
    SuperDoubleField chapter = new SuperDoubleField();
    chapter.setPreventingInvalidInput(true);
    chapter.setValue(mangaStats.progress());
    chapter.setPlaceholder("Chapter");
    chapter.addClassName("two-span");
    chapter.addValueChangeListener(
        e -> {
          if (e.getValue() == null) {
            chapter.setValue(e.getOldValue());
            return;
          }

          if (Objects.equals(e.getValue(), e.getOldValue())) {
            return;
          }

          if (e.getValue() < 0) {
            chapter.setValue(e.getOldValue());
            return;
          }

          if (maxChapters != null && maxChapters != 0 && e.getValue() > maxChapters) {
            chapter.setValue(e.getOldValue());
            return;
          }

          if (e.getValue() == mangaStats.progress()) {
            log.debug("Progress same as before, ignoring update");
            return;
          }

          suwayomiTrackingService.updateMangaProgress(tracker, e.getValue());
        });
    return chapter;
  }

  private void displaySearch(String mangaName, int mangaId, TrackerProvider trackerProvider,
      TrackerType trackerType) {
    var dialog = new TrackingMangaChoiceDialog(mangaName, mangaId, trackerProvider, dataService, trackerType);
    dialog.open();

    dialog.addOpenedChangeListener(
        e -> {
          if (!e.isOpened()) {
            Tracker tracker = dataService.getTracker(mangaId);
            if (tracker.hasAniListId() || tracker.hasMalId()) {
              removeAll();
              add(getTrackingStatistics(tracker, trackerProvider));
            }
          }
        });
  }

  /**
   * Updates the tracking buttons with the current tracking status.
   *
   * @param aniListBtn the AniList tracking button
   * @param malBtn     the MyAnimeList tracking button
   * @param tracker    the tracker to check the status of
   */
  private void updateButtons(Button aniListBtn, Button malBtn, Tracker tracker) {
    if (tracker.hasAniListId()) {
      aniListBtn.setIcon(LumoIcon.CHECKMARK.create());
      log.info("AniList ID: {}", tracker.getAniListId());
    } else {
      aniListBtn.setIcon(LumoIcon.CROSS.create());
    }

    if (tracker.hasMalId()) {
      malBtn.setIcon(LumoIcon.CHECKMARK.create());
      log.info("MAL ID: {}", tracker.getMalId());
    } else {
      malBtn.setIcon(LumoIcon.CROSS.create());
    }
  }
}
