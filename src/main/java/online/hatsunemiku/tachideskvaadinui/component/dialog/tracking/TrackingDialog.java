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
import java.time.LocalDate;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider.AniListProvider;
import online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider.SuwayomiProvider;
import online.hatsunemiku.tachideskvaadinui.component.dialog.tracking.provider.TrackerProvider;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListScoreFormat;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.responses.AniListMangaStatistics;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.vaadin.miki.superfields.dates.SuperDatePicker;
import org.vaadin.miki.superfields.numbers.SuperIntegerField;

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

  /**
   * Constructs a {@link TrackingDialog} with the given parameters.
   *
   * @param dataService The {@link TrackingDataService} used for storing tracking data.
   * @param manga the {@link Manga} to track with the dialog.
   * @param aniListAPIService the {@link AniListAPIService} used for making requests to the AniList
   *     API.
   * @param suwayomiTrackingService the {@link SuwayomiTrackingService} used for making requests to
   *     the Suwayomi API.
   */
  public TrackingDialog(
      TrackingDataService dataService,
      Manga manga,
      AniListAPIService aniListAPIService,
      SuwayomiTrackingService suwayomiTrackingService) {
    super();
    this.dataService = dataService;
    this.aniListAPI = aniListAPIService;
    this.suwayomiTrackingService = suwayomiTrackingService;

    Tracker tracker = dataService.getTracker(manga.getId());

    if (!tracker.hasAniListId()) {
      addTrackingButtons(manga, aniListAPIService, tracker);
    } else {

      Div statistics;
      try {
        statistics = getTrackingStatistics(tracker);
        add(statistics);
      } catch (RuntimeException e) {
        addTrackingButtons(manga, aniListAPIService, tracker);
      } catch (Exception e) {
        log.error("Error retrieving tracking statistics from Tracker", e);
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setText("Error retrieving tracking statistics");
        notification.open();
      }
    }
  }

  private void addTrackingButtons(
      Manga manga, AniListAPIService aniListAPIService, Tracker tracker) {
    VerticalLayout buttons = new VerticalLayout();
    buttons.setId("tracking-dialog-tracker-buttons");

    Button aniListBtn = new Button("Anilist");
    Button malBtn = new Button("MyAnimeList");

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

          TrackerProvider provider =
              new AniListProvider(aniListAPIService, suwayomiTrackingService);

          try {
            displaySearch(manga.getTitle(), manga.getId(), provider);
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

          TrackerProvider provider = new SuwayomiProvider(suwayomiTrackingService);

          try {
            displaySearch(manga.getTitle(), manga.getId(), provider);
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
  private Div getTrackingStatistics(Tracker tracker) throws RuntimeException {
    AniListMangaStatistics mangaStats;
    try {
      mangaStats = aniListAPI.getMangaFromList(tracker.getAniListId());
    } catch (WebClientResponseException.NotFound e) {
      log.debug("Manga not found on AniList", e);
      Notification notification = new Notification();
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.setText(
          """
              The manga wasn't found on AniList.
              Removing AniList tracking for this Manga.
              You can add it again if you want to track it with AniList.
              """);
      notification.setDuration(5000);
      notification.open();
      tracker.removeAniListId();
      throw new RuntimeException("Manga not found on AniList");
    }

    Div content = new Div();

    ComboBox<AniListStatus> status = getTrackingStatusField(tracker, mangaStats);

    var maxChapters = aniListAPI.getChapterCount(tracker.getAniListId()).orElse(null);

    SuperIntegerField chapter = getTrackingChapterField(tracker, mangaStats, maxChapters);

    SuperIntegerField score = getTrackingScoreField(tracker, mangaStats);

    SuperDatePicker endDate = new SuperDatePicker();
    SuperDatePicker startDate = getTrackingStartDateField(tracker, mangaStats, endDate);

    Checkbox privateCheckbox = getPrivateCheckboxField(tracker);

    configureTrackingEndDateField(tracker, endDate, mangaStats, startDate);

    Div statistics = new Div();
    statistics.addClassName("tracking-dialog-statistics");

    statistics.add(status, chapter, score, startDate, endDate, privateCheckbox);

    Button trackingDeleteBtn = new Button("Remove AniList Tracking", VaadinIcon.TRASH.create());
    trackingDeleteBtn.addClickListener(
        e -> {
          tracker.removeAniListId();
          close();
        });

    var nukeBtn = new Button("Remove from AniList and stop Tracking", VaadinIcon.BOMB.create());
    nukeBtn.addClickListener(
        e -> {
          aniListAPI.removeMangaFromList(tracker.getAniListId());
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

  private void configureTrackingEndDateField(
      Tracker tracker,
      SuperDatePicker endDate,
      AniListMangaStatistics mangaStats,
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
            aniListAPI.updateMangaEndDate(tracker.getAniListId(), date);
            return;
          }

          if (startDate.getValue() == null) {
            startDate.setValue(e.getValue());
          } else {
            if (!startDate.getValue().isAfter(e.getValue())) {
              endDate.setValue(e.getOldValue());
              return;
            }
          }

          MediaDate date = new MediaDate(e.getValue());
          aniListAPI.updateMangaEndDate(tracker.getAniListId(), date);
        });
  }

  @NotNull
  private SuperDatePicker getTrackingStartDateField(
      Tracker tracker, AniListMangaStatistics mangaStats, SuperDatePicker endDate) {
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
            aniListAPI.updateMangaStartDate(tracker.getAniListId(), date);
            return;
          }

          if (e.getValue().isAfter(endDate.getValue())) {
            startDate.setValue(e.getOldValue());

            Notification notification = new Notification();
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Start date cannot be after end date");
            notification.open();
            return;
          }

          MediaDate date = new MediaDate(e.getValue());
          aniListAPI.updateMangaStartDate(tracker.getAniListId(), date);
        });
    return startDate;
  }

  @NotNull
  private SuperIntegerField getTrackingScoreField(
      Tracker tracker, AniListMangaStatistics mangaStats) {
    AniListScoreFormat format = aniListAPI.getScoreFormat();

    SuperIntegerField score = new SuperIntegerField();
    score.setNullValueAllowed(true);
    score.setPreventingInvalidInput(true);
    score.setPlaceholder("Score");
    score.addClassName("two-span");
    if (mangaStats.score() != 0) {
      score.setValue(mangaStats.score());
    } else {
      score.setValue(null);
    }
    score.addValueChangeListener(
        e -> {
          if (e.getValue() == null || e.getValue() == 0) {
            score.setValue(null);
            aniListAPI.updateMangaScore(tracker.getAniListId(), 0);
            return;
          }

          if (e.getValue() > format.getMaxScore()) {
            score.setValue(e.getOldValue());
            Notification notification = new Notification();
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Score cannot be higher than " + format.getMaxScore());
            notification.open();
            return;
          }

          if (e.getValue() < format.getMinScore()) {
            score.setValue(e.getOldValue());
            Notification notification = new Notification();
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Score cannot be lower than " + format.getMinScore());
            notification.open();
            return;
          }

          aniListAPI.updateMangaScore(tracker.getAniListId(), e.getValue());
        });
    return score;
  }

  @NotNull
  private ComboBox<AniListStatus> getTrackingStatusField(
      Tracker tracker, AniListMangaStatistics mangaStats) {
    ComboBox<AniListStatus> status = new ComboBox<>();
    status.setItems(AniListStatus.values());
    status.setPlaceholder("Status");
    status.setValue(mangaStats.status());
    status.addClassName("two-span");
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

  @NotNull
  private SuperIntegerField getTrackingChapterField(
      Tracker tracker, AniListMangaStatistics mangaStats, Integer maxChapters) {
    SuperIntegerField chapter = new SuperIntegerField();
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

          if (maxChapters != null && e.getValue() > maxChapters) {
            chapter.setValue(e.getOldValue());
            return;
          }

          aniListAPI.updateMangaProgress(tracker.getAniListId(), e.getValue());
        });
    return chapter;
  }

  private void displaySearch(String mangaName, long mangaId, TrackerProvider trackerProvider) {
    var dialog = new TrackingMangaChoiceDialog(mangaName, mangaId, trackerProvider, dataService);
    dialog.open();

    dialog.addOpenedChangeListener(
        e -> {
          if (!e.isOpened()) {
            Tracker tracker = dataService.getTracker(mangaId);
            if (tracker.hasAniListId()) {
              removeAll();
              add(getTrackingStatistics(tracker));
            }
          }
        });
  }

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
