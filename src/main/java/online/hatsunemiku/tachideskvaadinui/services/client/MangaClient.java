package online.hatsunemiku.tachideskvaadinui.services.client;

import feign.Headers;
import java.net.URI;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(name = "mangaClient", url = "http://localhost:8080")
public interface MangaClient {

  @GetMapping("/api/v1/manga/{mangaId}/library")
  void addMangaToLibrary(URI baseUrl, @PathVariable long mangaId);

  @DeleteMapping("/api/v1/manga/{mangaId}/library")
  void removeMangaFromLibrary(URI baseUrl, @PathVariable long mangaId);

  // {{_.base_url}}/api/v1/manga/{{_.mangaId}}/chapters
  @GetMapping("/api/v1/manga/{mangaId}/chapters")
  List<Chapter> getChapterList(URI baseUrl, @PathVariable long mangaId);

  @GetMapping("/api/v1/manga/{mangaId}/chapter/{chapterIndex}")
  Chapter getChapter(URI baseUrl, @PathVariable long mangaId, @PathVariable int chapterIndex);

  @PatchMapping(
      value = "/api/v1/manga/{mangaId}/chapter/{chapterIndex}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Headers("Content-Type: multipart/form-data")
  HttpStatusCode modifyReadStatus(
      URI baseUrl,
      @PathVariable long mangaId,
      @PathVariable int chapterIndex,
      @RequestPart("read") boolean read);

  /**
   * Retrieves the full details of a manga based on the given manga ID.
   *
   * @param baseUrl The base URL of the API.
   * @param mangaId The ID of the manga to retrieve.
   * @return A Manga object representing the full data of the manga.
   */
  @GetMapping("/api/v1/manga/{mangaId}/full")
  Manga getMangaFull(URI baseUrl, @PathVariable long mangaId);
}
