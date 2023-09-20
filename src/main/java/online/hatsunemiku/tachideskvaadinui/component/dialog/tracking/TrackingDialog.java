package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoIcon;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListScoreFormat;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.responses.AniListMangaStatistics;
import online.hatsunemiku.tachideskvaadinui.services.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import org.jetbrains.annotations.NotNull;
import org.vaadin.miki.superfields.dates.SuperDatePicker;
import org.vaadin.miki.superfields.numbers.SuperIntegerField;

@CssImport("./css/components/dialog/tracking/tracking-dialog.css")
@Slf4j
public class TrackingDialog extends Dialog {

  private final AniListAPIService aniListAPI;
  private final TrackingDataService dataService;

  public TrackingDialog(
      TrackingDataService dataService, Manga manga, AniListAPIService aniListAPIService) {
    super();
    this.dataService = dataService;
    this.aniListAPI = aniListAPIService;

    VerticalLayout buttons = new VerticalLayout();

    Tracker tracker = dataService.getTracker(manga.getId());

    if (!tracker.hasAniListId()) {
      Button aniListBtn = new Button("Anilist");
      aniListBtn.addClickListener(
          e -> {
            if (!aniListAPIService.hasAniListToken()) {
              String url = aniListAPIService.getAniListAuthUrl();
              getUI().ifPresent(ui -> ui.getPage().open(url));
              return;
            }

            displaySearch(manga.getTitle(), manga.getId());
            updateButtons(aniListBtn, tracker);
          });

      updateButtons(aniListBtn, tracker);

      buttons.add(aniListBtn);

      add(buttons);
    } else {

      Div statistics = getTrackingStatistics(tracker);

      add(statistics);
    }
  }

  @NotNull
  private Div getTrackingStatistics(Tracker tracker) {
    var mangaStats = aniListAPI.getMangaFromList(tracker.getAniListId());

    Div statistics = new Div();
    statistics.addClassName("tracking-dialog-statistics");

    ComboBox<AniListStatus> status = getTrackingStatusField(tracker, mangaStats);

    var maxChapters = aniListAPI.getChapterCount(tracker.getAniListId());

    SuperIntegerField chapter = getTrackingChapterField(tracker, mangaStats, maxChapters);

    SuperIntegerField score = getTrackingScoreField(tracker, mangaStats);

    SuperDatePicker endDate = new SuperDatePicker();
    SuperDatePicker startDate = getTrackingStartDateField(tracker, mangaStats, endDate);

    configureTrackingEndDateField(tracker, endDate, mangaStats, startDate);

    statistics.add(status, chapter, score, startDate, endDate);
    return statistics;
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
      Tracker tracker, AniListMangaStatistics mangaStats, Optional<Integer> maxChapters) {
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

          if (maxChapters.isPresent() && e.getValue() > maxChapters.get()) {
            chapter.setValue(e.getOldValue());
            return;
          }

          aniListAPI.updateMangaProgress(tracker.getAniListId(), e.getValue());
        });
    return chapter;
  }

  private void displaySearch(String mangaName, long mangaId) {
    var dialog = new TrackingMangaChoiceDialog(mangaName, mangaId, aniListAPI, dataService);
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

  private void updateButtons(Button aniListBtn, Tracker tracker) {
    if (tracker.hasAniListId()) {
      aniListBtn.setIcon(LumoIcon.CHECKMARK.create());
      log.info("AniList ID: {}", tracker.getAniListId());
    } else {
      aniListBtn.setIcon(LumoIcon.CROSS.create());
    }
  }
}
