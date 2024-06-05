/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.services.client.LibUpdateClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.graphql.client.FieldAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LibUpdateService {

  private final LibUpdateClient client;
  private final MangaService mangaService;
  private final Lock lock = new ReentrantLock();

  public LibUpdateService(LibUpdateClient client, MangaService mangaService) {
    this.client = client;
    this.mangaService = mangaService;
  }

  @CacheEvict(
      value = {"manga"},
      allEntries = true)
  public boolean fetchUpdate(UI ui) {

    if (!lock.tryLock()) {
      return false;
    }

    if (mangaService.getLibraryManga().isEmpty()) {
      lock.unlock();
      throw new IllegalStateException("No Manga in Library");
    }

    boolean fetchUpdate;
    try {
      fetchUpdate = client.fetchUpdate();
    } catch (Exception e) {
      log.error("Could not fetch update", e);
      lock.unlock();
      return false;
    }

    if (!fetchUpdate) {
      lock.unlock();
      return false;
    }

    var manga = mangaService.getLibraryManga();

    for (var m : manga) {
      try {
        mangaService.fetchChapterList(m.getId());
      } catch (FieldAccessException e) {
        if (ui == null) {
          log.debug(e.getMessage());
          continue;
        }

        Notification notification = new Notification(e.getMessage(), 5000, Position.BOTTOM_END);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        ui.access(notification::open);
      } catch (Exception e) {
        log.error("Could not fetch chapter list for manga {}", m.getId(), e);
        lock.unlock();
        return false;
      }
    }

    lock.unlock();
    return true;
  }

  @Scheduled(initialDelay = 1, fixedRate = 30, timeUnit = TimeUnit.MINUTES)
  protected void scheduledUpdate() {
    boolean success;
    try {
      success = fetchUpdate(null);
    } catch (Exception e) {
      log.info("Won't update, no Manga in Library", e);
      return;
    }

    if (success) {
      log.info("Library update started");
    } else {
      log.debug("Could not start library update");
    }
  }
}
