package online.hatsunemiku.tachideskvaadinui.services;

import com.vaadin.flow.component.UI;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import online.hatsunemiku.tachideskvaadinui.view.ServerStartView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExtensionService {

  private final RestTemplate client;

  @Autowired
  public ExtensionService(RestTemplate client) {
    this.client = client;
  }

  public List<Extension> getExtensions() {

    Settings settings = SerializationUtils.deseralizeSettings();

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
    Settings settings = SerializationUtils.deseralizeSettings();

    String url = settings.getUrl() + "/api/v1/extension/install/" + pkgName;

    try {
      var response = client.getForEntity(url, Void.class);

      return response.getStatusCode();
    } catch (RestClientException e) {
      return HttpStatusCode.valueOf(500);
    }
  }

  public HttpStatusCode uninstallExtension(String pkgName) {
    Settings settings = SerializationUtils.deseralizeSettings();

    String url = settings.getUrl() + "/api/v1/extension/uninstall/" + pkgName;

    try {
      var response = client.getForEntity(url, Void.class);

      return response.getStatusCode();
    } catch (RestClientException e) {
      return HttpStatusCode.valueOf(500);
    }
  }
}
