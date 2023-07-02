package online.hatsunemiku.tachideskvaadinui.services;

import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SourceService {

  private final RestTemplate client;

  public SourceService(RestTemplate client) {
    this.client = client;
  }

  public List<Source> getSources() {

    Settings settings = SerializationUtils.deseralizeSettings();

    String url = settings.getUrl() + "/api/v1/source/list";

    Source[] sources = client.getForObject(url, Source[].class);

    if (sources == null) {
      return List.of();
    }

    return List.of(sources);
  }
}
