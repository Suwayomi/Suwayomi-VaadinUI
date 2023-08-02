package online.hatsunemiku.tachideskvaadinui.services.client;

import java.net.URI;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mangaClient", url = "http://localhost:8080")
public interface MangaClient {

  @GetMapping("/api/v1/manga/{mangaId}/library")
  void addMangaToLibrary(URI baseUrl, @PathVariable long mangaId);

  @DeleteMapping("/api/v1/manga/{mangaId}/library")
  void removeMangaFromLibrary(URI baseUrl, @PathVariable long mangaId);

  //{{_.base_url}}/api/v1/manga/{{_.mangaId}}/chapters
  @GetMapping("/api/v1/manga/{mangaId}/chapters")
  List<Chapter> getChapterList(URI baseUrl, @PathVariable long mangaId);

  @GetMapping("/api/v1/manga/{mangaId}/chapter/{chapterIndex}")
  Chapter getChapter(URI baseUrl, @PathVariable long mangaId, @PathVariable int chapterIndex);
}
