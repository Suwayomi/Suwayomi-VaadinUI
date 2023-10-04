/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import feign.FeignException;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.client.DownloadClient;
import online.hatsunemiku.tachideskvaadinui.services.client.MangaClient;
import online.hatsunemiku.tachideskvaadinui.services.client.MangaClientGraph;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MangaService {

  private final MangaClient mangaClient;
  private final MangaClientGraph newMangaClient;
  // I chose to use the download client here as you only really use it in combination with Manga and
  // not on its own
  private final DownloadClient downloadClient;
  private final SettingsService settingsService;

  @Autowired
  public MangaService(
      MangaClient mangaClient, DownloadClient downloadClient, SettingsService settingsService,
      MangaClientGraph newMangaClient) {
    this.mangaClient = mangaClient;
    this.downloadClient = downloadClient;
    this.settingsService = settingsService;
    this.newMangaClient = newMangaClient;
  }

  /**
   * Adds a manga to the library.
   *
   * @param mangaId the ID of the manga to be added
   * @return {@code true} if the manga was successfully added to the library, {@code false}
   * otherwise
   */
  public boolean addMangaToLibrary(int mangaId) {
    return newMangaClient.addMangaToLibrary(mangaId);
  }


  /**
   * Removes a manga from the library.
   *
   * @param mangaId the ID of the manga to be removed
   * @return {@code true} if the manga was successfully removed from the library, {@code false}
   * otherwise
   */
  public boolean removeMangaFromLibrary(int mangaId) {
    return newMangaClient.removeMangaFromLibrary(mangaId);
  }

  public List<Chapter> getChapterList(int mangaId) {
    return newMangaClient.getChapterList(mangaId);
  }

  @Cacheable(value = "chapter", key = "#mangaId + #chapterIndex")
  public Chapter getChapter(long mangaId, int chapterIndex) {
    URI baseUrl = getBaseUrl();

    return mangaClient.getChapter(baseUrl, mangaId, chapterIndex);
  }

  /**
   * Sets a chapter as read.
   *
   * @param chapterId the ID of the chapter to be set as read
   * @return {@code true} if the chapter was successfully set as read, {@code false} otherwise
   */
  public boolean setChapterRead(int chapterId) {
    try {
      return newMangaClient.setChapterRead(chapterId);
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
      return newMangaClient.setChapterUnread(chapterId);
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
    return newMangaClient.getManga(mangaId);
  }

  /**
   * Adds a manga to a category.
   *
   * @param mangaId    the ID of the manga to be added
   * @param categoryId the ID of the category to add the manga to
   */
  public void addMangaToCategory(long mangaId, long categoryId) {
    URI baseUrl = getBaseUrl();

    mangaClient.addMangaToCategory(baseUrl, mangaId, categoryId);
  }

  /**
   * Removes a manga from a category.
   *
   * @param mangaId    the ID of the manga to be removed
   * @param categoryId the ID of the category to remove the manga from
   */
  public void removeMangaFromCategory(long mangaId, long categoryId) {
    URI baseUrl = getBaseUrl();

    mangaClient.removeMangaFromCategory(baseUrl, mangaId, categoryId);
  }

  public void moveMangaToCategory(long mangaId, long newCategoryId, long oldCategoryId) {
    addMangaToCategory(mangaId, newCategoryId);
    removeMangaFromCategory(mangaId, oldCategoryId);
  }

  /**
   * Retrieves the base URL for the manga service.
   *
   * @return the base URL for the manga service
   * @throws NullPointerException if the URL is null
   */
  @NotNull
  private URI getBaseUrl() {
    Settings settings = settingsService.getSettings();

    return URI.create(settings.getUrl());
  }

  /**
   * Downloads a single chapter of a manga.
   *
   * @param mangaId      the ID of the manga to download
   * @param chapterIndex the index of the chapter to download
   * @return true if downloading was queued, false otherwise
   */
  public boolean downloadSingleChapter(int mangaId, int chapterIndex) {
    URI baseUrl = getBaseUrl();

    try {
      downloadClient.downloadSingleChapter(baseUrl, mangaId, chapterIndex);
      return true;
    } catch (FeignException e) {
      log.debug("Failed to download chapter", e);
      return false;
    } catch (Exception e) {
      log.error("Failed to download chapter", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Downloads multiple chapters of manga.
   *
   * @param chapterIds the IDs of the chapters to download
   * @return true if downloading was queued, false otherwise
   */
  public boolean downloadMultipleChapter(List<Integer> chapterIds) {
    URI baseUrl = getBaseUrl();

    try {
      var tempList = List.copyOf(chapterIds);
      var request = new DownloadClient.DownloadChapterRequest(tempList);
      downloadClient.downloadMultipleChapters(baseUrl, request);
      return true;
    } catch (FeignException e) {
      log.debug("Failed to download chapter", e);
      return false;
    } catch (Exception e) {
      log.error("Failed to download chapter", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Deletes a single chapter of manga.
   *
   * @param mangaId      the ID of the manga
   * @param chapterIndex the index of the chapter to delete
   * @return true if deletion was successful, false otherwise
   */
  public boolean deleteSingleChapter(int mangaId, int chapterIndex) {
    URI baseUrl = getBaseUrl();

    try {
      downloadClient.deleteSingleChapter(baseUrl, mangaId, chapterIndex);
      return true;
    } catch (FeignException e) {
      log.debug("Failed to delete chapter", e);
      return false;
    } catch (Exception e) {
      log.error("Failed to delete chapter", e);
      throw new RuntimeException(e);
    }
  }
}
