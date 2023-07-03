package online.hatsunemiku.tachideskvaadinui.services.client;

import java.net.URI;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mangaClient", url = "http://localhost:8080")
public interface MangaClient {

  @GetMapping("/api/v1/manga/{mangaId}/library")
  public void addMangaToLibrary(URI baseUrl, @PathVariable long mangaId);

  @DeleteMapping("/api/v1/manga/{mangaId}/library")
  public void removeMangaFromLibrary(URI baseUrl, @PathVariable long mangaId);
}
