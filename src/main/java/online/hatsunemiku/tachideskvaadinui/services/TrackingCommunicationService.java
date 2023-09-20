package online.hatsunemiku.tachideskvaadinui.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrackingCommunicationService {

  private final AniListAPIService aniListAPIService;
  private final TrackingDataService dataService;

  public TrackingCommunicationService(AniListAPIService aniListAPIService,
      TrackingDataService dataService) {
    this.aniListAPIService = aniListAPIService;
    this.dataService = dataService;
  }

  /**
   * Sets the progress of a chapter for a manga based on the manga ID, chapter number, and a flag
   * indicating if it should be set to a new value when the chapter is smaller than the current
   * progress. If the flag is {@code true} and the current progress is greater than or equal to the
   * chapter number, the method returns without making any changes. Should the Chapter count pass
   * the condition, any valid tracker will get the updated chapter progress.
   *
   * @param mangaId        the ID of the manga
   * @param chapter        the chapter number to set the progress to
   * @param onlyWhenBigger a flag indicating whether to update the progress only when the chapter is
   *                       bigger than the current progress
   */
  public void setChapterProgress(int mangaId, int chapter, boolean onlyWhenBigger) {
    var tracker = dataService.getTracker(mangaId);

    if (onlyWhenBigger) {
      var statistics = aniListAPIService.getMangaFromList(tracker.getAniListId());

      if (statistics.progress() >= chapter) {
        log.info(
            "Chapter {} is not bigger than current AniList progress {}",
            chapter,
            statistics.progress());
      } else {
        if (tracker.hasAniListId()) {
          aniListAPIService.updateMangaProgress(tracker.getAniListId(), chapter);
        }
      }
    }
  }
}
