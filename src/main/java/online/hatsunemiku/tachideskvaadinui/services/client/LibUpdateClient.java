package online.hatsunemiku.tachideskvaadinui.services.client;

import java.net.URI;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "lib-update-client", url = "http://localhost:8080")
public interface LibUpdateClient {

  @PostMapping("/api/v1/update/fetch")
  ResponseEntity<Void> fetchUpdate(URI baseUrl);
}
