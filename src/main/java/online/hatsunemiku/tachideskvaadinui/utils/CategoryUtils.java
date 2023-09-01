package online.hatsunemiku.tachideskvaadinui.utils;

import static org.springframework.http.HttpMethod.GET;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

@UtilityClass
public class CategoryUtils {

  public List<Category> getCategories(RestTemplate client, Settings settings) {
    String categoryEndpoint = settings.getUrl() + "/api/v1/category";

    ParameterizedTypeReference<List<Category>> typeRef = new ParameterizedTypeReference<>() {};

    List<Category> list = client.exchange(categoryEndpoint, GET, null, typeRef).getBody();

    if (list == null) {
      return new ArrayList<>();
    }

    return list;
  }
}
