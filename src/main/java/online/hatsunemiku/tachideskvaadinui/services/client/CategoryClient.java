package online.hatsunemiku.tachideskvaadinui.services.client;

import feign.Headers;
import java.net.URI;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "category-service", url = "http://localhost:8080")
public interface CategoryClient {

  @PostMapping(value = "/api/v1/category", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @Headers("Content-Type: application/x-www-form-urlencoded")
  void createCategory(URI baseUrl, @RequestBody Map<String, ?> formParams);


}
