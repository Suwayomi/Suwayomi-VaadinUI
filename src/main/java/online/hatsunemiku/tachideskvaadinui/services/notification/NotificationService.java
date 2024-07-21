/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.MangaChapterCount;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.event.MangaUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.utils.PathUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

  private final String MANGA_SAVE_FILE = "mangaChapterState.json";
  private final Path projectDir;
  private final ObjectMapper mapper;
  private final WebPushService webPushService;
  private MangaChapterCount mangaChapterCount;

  public NotificationService(Environment env, ObjectMapper mapper, WebPushService webPushService) {
    projectDir = PathUtils.getResolvedProjectPath(env);
    this.mapper = mapper;
    this.webPushService = webPushService;
  }


  @EventListener(MangaUpdateEvent.class)
  public void notify(MangaUpdateEvent event) {
    if (event.isRunning()) {
      throw new IllegalArgumentException(
          "Manga Update event has reached NotificationService, while it's still running - this should not happen");
    }

    List<Manga> completedJobs = event.getCompletedJobs();
    if (completedJobs.isEmpty()) {
      return;
    }

    completedJobs.forEach(manga -> {
      var count = getNotificationData(manga);

      if (count == -1) {
        mangaChapterCount.addManga(manga.getId(), manga.getChapterCount());
        return;
      }

      if (count < manga.getChapterCount()) {
        mangaChapterCount.updateChapterCount(manga.getId(), manga.getChapterCount());
        String mangaTitle = manga.getTitle();
        String title = "New chapter available for " + mangaTitle;
        String message = "A new chapter is available for " + mangaTitle + "!";

        webPushService.notify(title, message);
      }
    });

  }

  private int getNotificationData(Manga manga) {
    if (mangaChapterCount == null) {
      loadNotificationData();
    }

    return mangaChapterCount.getChapterCount(manga.getId());
  }

  private void loadNotificationData() {
    var saveFile = projectDir.resolve(MANGA_SAVE_FILE);

    if (!Files.exists(saveFile)) {
      mangaChapterCount = new MangaChapterCount();
      return;
    }

    try (var in = Files.newInputStream(saveFile)) {
      mangaChapterCount = mapper.readValue(in, MangaChapterCount.class);
    } catch (Exception e) {
      String msg = "Couldn't read notification data from file";
      log.error(msg);
      throw new RuntimeException(msg, e);
    }
  }

  @PreDestroy
  public void saveNotificationData() {
    if (mangaChapterCount == null) {
      return;
    }

    var saveFile = projectDir.resolve(MANGA_SAVE_FILE);

    try (var out = Files.newOutputStream(saveFile)) {
      mapper.writeValue(out, mangaChapterCount);
    } catch (Exception e) {
      String msg = "Couldn't save notification data to file";
      log.error(msg);
      throw new RuntimeException(msg, e);
    }
  }

}
