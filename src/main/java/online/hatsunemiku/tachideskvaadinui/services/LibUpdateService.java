package online.hatsunemiku.tachideskvaadinui.services;

import java.net.URI;
import online.hatsunemiku.tachideskvaadinui.services.client.LibUpdateClient;
import org.springframework.stereotype.Service;

@Service
public class LibUpdateService {

  private final SettingsService settingsService;
  private final LibUpdateClient client;

  public LibUpdateService(SettingsService settingsService, LibUpdateClient client) {
    this.settingsService = settingsService;
    this.client = client;
  }

  public boolean fetchUpdate() {
    var settings = settingsService.getSettings();

    URI baseUrl = URI.create(settings.getUrl());

    var response = client.fetchUpdate(baseUrl);

    return response.getStatusCode().is2xxSuccessful();
  }
}
