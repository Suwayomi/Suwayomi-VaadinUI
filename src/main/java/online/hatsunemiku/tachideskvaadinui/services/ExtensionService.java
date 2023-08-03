package online.hatsunemiku.tachideskvaadinui.services;

import com.vaadin.flow.component.UI;
import java.net.URI;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;
import online.hatsunemiku.tachideskvaadinui.services.client.ExtensionClient;
import online.hatsunemiku.tachideskvaadinui.view.ServerStartView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExtensionService {

  private final RestTemplate client;
  private final SettingsService settingsService;
  private final ExtensionClient extensionClient;

  @Autowired
  public ExtensionService(RestTemplate client, SettingsService settingsService, ExtensionClient extensionClient) {
    this.client = client;
    this.settingsService = settingsService;
    this.extensionClient = extensionClient;
  }

  public List<Extension> getExtensions() {

    Settings settings = settingsService.getSettings();

    String url = settings.getUrl() + "/api/v1/extension/list";

    Extension[] extensions = new Extension[0];
    try {
      extensions = client.getForObject(url, Extension[].class);
    } catch (RestClientException e) {
      UI.getCurrent().navigate(ServerStartView.class);
    }

    if (extensions == null) {
      return List.of();
    }

    return List.of(extensions);
  }

  public HttpStatusCode installExtension(String pkgName) {
    Settings settings = settingsService.getSettings();

    String url = settings.getUrl() + "/api/v1/extension/install/" + pkgName;

    try {
      var response = client.getForEntity(url, Void.class);

      return response.getStatusCode();
    } catch (RestClientException e) {
      return HttpStatusCode.valueOf(500);
    }
  }

  public HttpStatusCode uninstallExtension(String pkgName) {
    Settings settings = settingsService.getSettings();

    String url = settings.getUrl() + "/api/v1/extension/uninstall/" + pkgName;

    try {
      var response = client.getForEntity(url, Void.class);

      return response.getStatusCode();
    } catch (RestClientException e) {
      return HttpStatusCode.valueOf(500);
    }
  }

  public void updateExtension(String pkgName) {
    Settings settings = settingsService.getSettings();

    URI baseUrl = URI.create(settings.getUrl());

    try {
      extensionClient.updateExtension(baseUrl, pkgName);
    } catch (RestClientException e) {
      UI.getCurrent().navigate(ServerStartView.class);
    }
  }
}
