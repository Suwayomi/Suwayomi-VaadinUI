package online.hatsunemiku.tachideskvaadinui.services;

import java.net.URI;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.services.client.MangaClient;
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
    Settings settings = settingsService.getSettings();

    URI baseUrl = URI.create(settings.getUrl());

    mangaClient.addMangaToLibrary(baseUrl, mangaId);
  }

  public void removeMangaFromLibrary(long mangaId) {
    Settings settings = settingsService.getSettings();

    URI baseUrl = URI.create(settings.getUrl());

    mangaClient.removeMangaFromLibrary(baseUrl, mangaId);
  }
}
