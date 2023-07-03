package online.hatsunemiku.tachideskvaadinui.services;

import java.net.URI;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.services.client.MangaClient;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MangaService {

  private final MangaClient mangaClient;

  @Autowired
  public MangaService(MangaClient mangaClient) {
    this.mangaClient = mangaClient;
  }

  public void addMangaToLibrary(long mangaId) {
    Settings settings = SerializationUtils.deseralizeSettings();

    URI baseUrl = URI.create(settings.getUrl());

    mangaClient.addMangaToLibrary(baseUrl, mangaId);
  }

  public void removeMangaFromLibrary(long mangaId) {
    Settings settings = SerializationUtils.deseralizeSettings();

    URI baseUrl = URI.create(settings.getUrl());

    mangaClient.removeMangaFromLibrary(baseUrl, mangaId);
  }

}
