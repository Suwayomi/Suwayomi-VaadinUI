/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.client.DownloadClient;
import online.hatsunemiku.tachideskvaadinui.services.client.DownloadClient.DownloadChangeEvent;
import online.hatsunemiku.tachideskvaadinui.services.client.MangaClient;
import online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;

/**
 * This class is responsible for handling all operations related to manga. This includes adding and
 * removing manga from the library, fetching chapters, downloading chapters and more.
 */
@Service
@Slf4j
public class MangaService {

  private final MangaClient mangaClient;
  private final DownloadClient downloadClient;
  private final Flux<List<DownloadChangeEvent>> downloadChangeEventTracker;
  private final SuwayomiTrackingService suwayomiTrackingService;

  /**
   * Creates a new MangaService.
   *
   * @param mangaClient the {@link MangaClient} to use for fetching manga data
   * @param downloadCLient the {@link DownloadClient} to use for downloading chapters
   * @param suwayomiTrackingService the {@link SuwayomiTrackingService} to use for tracking progress
   */
  @Autowired
  public MangaService(
      MangaClient mangaClient,
      DownloadClient downloadCLient,
      SuwayomiTrackingService suwayomiTrackingService) {
    this.mangaClient = mangaClient;
    this.downloadClient = downloadCLient;
    this.downloadChangeEventTracker = downloadCLient.trackDownloads();
    this.suwayomiTrackingService = suwayomiTrackingService;
  }

  /**
   * Adds a manga to the library.
   *
   * @param mangaId the ID of the manga to be added
   * @return {@code true} if the manga was successfully added to the library, {@code false}
   *     otherwise
   */
  public boolean addMangaToLibrary(int mangaId) {
    return mangaClient.addMangaToLibrary(mangaId);
  }

  /**
   * Removes a manga from the library.
   *
   * @param mangaId the ID of the manga to be removed
   * @return {@code true} if the manga was successfully removed from the library, {@code false}
   *     otherwise
   */
  public boolean removeMangaFromLibrary(int mangaId) {
    return mangaClient.removeMangaFromLibrary(mangaId);
  }

  /**
   * Retrieves the cached list of chapters for a manga. This method does NOT find new chapters. Use
   * {@link #fetchChapterList(int)} to find new chapters.
   *
   * @param mangaId the ID of the manga for which to get the chapter list
   * @return the list of Chapter objects representing the chapters of the manga
   */
  public List<Chapter> getChapterList(int mangaId) {
    return mangaClient.getChapters(mangaId);
  }

  /**
   * Retrieves the list of chapters for a manga from the server. This method finds new chapters and
   * updates the cache. Use {@link #getChapterList(int)} to retrieve the cached list of chapters.
   *
   * @param mangaId the ID of the manga for which to get the chapter list
   * @return the list of Chapter objects representing the chapters of the manga
   */
  public List<Chapter> fetchChapterList(int mangaId) {
    return mangaClient.fetchChapterList(mangaId);
  }

  @Cacheable(value = "chapter", key = "#chapterId", unless = "#result.pageCount == -1")
  public Chapter getChapter(int chapterId) {
    return mangaClient.getChapter(chapterId);
  }

  /**
   * Sets a chapter as read.
   *
   * @param chapterId the ID of the chapter to be set as read
   * @return {@code true} if the chapter was successfully set as read, {@code false} otherwise
   */
  public boolean setChapterRead(int chapterId, int mangaId) {
    try {

      boolean updated = mangaClient.setChapterRead(chapterId);

      if (!updated) {
        return false;
      }

      suwayomiTrackingService.trackProgress(mangaId);

      return true;

    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Sets a chapter as unread.
   *
   * @param chapterId the ID of the chapter to be set as unread
   * @return {@code true} if the chapter was successfully set as unread, {@code false} otherwise
   */
  public boolean setChapterUnread(int chapterId) {
    try {
      return mangaClient.setChapterUnread(chapterId);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Retrieves the full information of a manga specified by its ID.
   *
   * @param mangaId the ID of the manga to retrieve
   * @return a Manga object containing the full data of the manga
   */
  @Cacheable(value = "manga", key = "#mangaId")
  public Manga getManga(long mangaId) {
    return mangaClient.getManga(mangaId);
  }

  /**
   * Adds a manga to a category.
   *
   * @param mangaId the ID of the manga to be added
   * @param categoryId the ID of the category to add the manga to
   */
  public void addMangaToCategory(int mangaId, int categoryId) {
    mangaClient.addMangaToCategories(List.of(categoryId), mangaId);
  }

  /**
   * Removes a manga from a category.
   *
   * @param mangaId the ID of the manga to be removed
   * @param categoryId the ID of the category to remove the manga from
   */
  public void removeMangaFromCategory(int mangaId, int categoryId) {
    mangaClient.removeMangaFromCategories(List.of(categoryId), mangaId);
  }

  public void moveMangaToCategory(int mangaId, int newCategoryId, int oldCategoryId) {
    addMangaToCategory(mangaId, newCategoryId);
    removeMangaFromCategory(mangaId, oldCategoryId);
  }

  /**
   * Downloads a single chapter with the specified chapter ID.
   *
   * @param chapterId the ID of the {@link Chapter} to download
   * @return true if the download was successful, false otherwise
   */
  public boolean downloadSingleChapter(int chapterId) {
    return downloadClient.downloadChapters(List.of(chapterId));
  }

  /**
   * Downloads multiple chapters of manga.
   *
   * @param chapterIds the IDs of the chapters to download
   * @return true if downloading was queued, false otherwise
   */
  public boolean downloadMultipleChapter(List<Integer> chapterIds) {
    return downloadClient.downloadChapters(List.copyOf(chapterIds));
  }

  /**
   * Deletes a single downloaded chapter of a manga.
   *
   * @param chapterId the ID of the chapter to delete
   * @return true if the chapter was successfully deleted, false otherwise
   */
  public boolean deleteSingleChapter(int chapterId) {
    return downloadClient.deleteChapter(chapterId);
  }

  public List<String> getChapterPages(int chapterId) {
    return mangaClient.getChapterPages(chapterId);
  }

  /**
   * Retrieves the list of {@link Manga} in the user's library.
   *
   * @return the list of manga in the library
   */
  public List<Manga> getLibraryManga() {
    return mangaClient.getLibraryManga();
  }

  /**
   * Adds a listener to the download change event tracker.
   *
   * @param chapterId The id of the chapter to listen for
   * @param callback The callback to run when the chapter is downloaded
   */
  public void addDownloadTrackListener(int chapterId, Runnable callback) {
    Disposable.Composite cancellation = Disposables.composite();

    var subscription =
        downloadChangeEventTracker.subscribe(
            events ->
                events.forEach(
                    event -> {
                      if (event.chapter().id() != chapterId) {
                        return;
                      }

                      if (event.progress() != 1) {
                        return;
                      }

                      callback.run();
                      cancellation.dispose();
                    }));

    cancellation.add(subscription);
  }
}
