package online.hatsunemiku.tachideskvaadinui.utils;

import static org.springframework.http.HttpMethod.GET;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@UtilityClass
public class CategoryUtils {

  private static final Logger logger = LoggerFactory.getLogger(CategoryUtils.class);

  public static boolean deleteCategory(RestTemplate rest, Settings settings, int categoryId) {
    String categoryEndpoint = settings.getUrl() + "/api/v1/category/" + categoryId;

    try {
      rest.delete(categoryEndpoint);
      return true;
    } catch (RestClientException e) {
      logger.error("Failed to delete category", e);
      return false;
    }
  }

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
