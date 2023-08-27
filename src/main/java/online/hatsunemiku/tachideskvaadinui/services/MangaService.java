package online.hatsunemiku.tachideskvaadinui.services;

import java.net.URI;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.client.MangaClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MangaService {

  private final MangaClient mangaClient;
  private final SettingsService settingsService;

  @Autowired
  public MangaService(MangaClient mangaClient, SettingsService settingsService) {
    this.mangaClient = mangaClient;
    this.settingsService = settingsService;
  }

  public void addMangaToLibrary(long mangaId) {
    URI baseUrl = getBaseUrl();

    mangaClient.addMangaToLibrary(baseUrl, mangaId);
  }

  public void removeMangaFromLibrary(long mangaId) {
    URI baseUrl = getBaseUrl();

    mangaClient.removeMangaFromLibrary(baseUrl, mangaId);
  }

  public List<Chapter> getChapterList(long mangaId) {
    URI baseUrl = getBaseUrl();

    return mangaClient.getChapterList(baseUrl, mangaId);
  }

  public Chapter getChapter(long mangaId, int chapterIndex) {
    URI baseUrl = getBaseUrl();

    return mangaClient.getChapter(baseUrl, mangaId, chapterIndex);
  }

  public boolean setChapterRead(long mangaId, int chapterIndex) {
    URI baseUrl = getBaseUrl();

    try {
      mangaClient.modifyReadStatus(baseUrl, mangaId, chapterIndex, true);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean setChapterUnread(long mangaId, int chapterIndex) {
    URI baseUrl = getBaseUrl();

    try {
      mangaClient.modifyReadStatus(baseUrl, mangaId, chapterIndex, false);
      return true;
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
  public Manga getMangaFull(long mangaId) {
    URI baseUrl = getBaseUrl();

    return mangaClient.getMangaFull(baseUrl, mangaId);
  }

  @NotNull
  private URI getBaseUrl() {
    Settings settings = settingsService.getSettings();

    return URI.create(settings.getUrl());
  }
}
