/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.data.tracking.TrackerTokens;
import online.hatsunemiku.tachideskvaadinui.utils.PathUtils;
import online.hatsunemiku.tachideskvaadinui.utils.ProfileUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * This class is responsible for managing tracking data. It handles the serialization and
 * deserialization of tokens and trackers, which are stored in JSON files. The tokens are used for
 * authentication with tracking services, while the trackers keep track of individual manga.
 *
 * <p>The serialization and deserialization processes are automatically executed upon the creation
 * and destruction of the service, respectively.
 *
 * @author aless2003
 */
@Service
@Slf4j
public class TrackingDataService {

  private final ObjectMapper mapper;
  private final Path tokenFile;
  private final Path trackerFile;
  private final HashMap<Long, Tracker> mangaTrackers = new HashMap<>();
  @Getter private TrackerTokens tokens;

  public TrackingDataService(ObjectMapper mapper, Environment env) {
    this.mapper = mapper;

    Path projectDirPath;
    if (ProfileUtils.isDev(env)) {
      projectDirPath = PathUtils.getDevDir();
    } else {
      projectDirPath = PathUtils.getProjectDir();
    }

    this.tokenFile = projectDirPath.resolve("tokens.json");
    this.trackerFile = projectDirPath.resolve("trackers.json");

    deserializeTokens();
    deserializeTrackers();
  }

  private void deserializeTokens() {
    if (!Files.exists(tokenFile)) {
      tokens = new TrackerTokens();
      serializeTokens();
      return;
    }

    if (Files.isDirectory(tokenFile)) {
      log.error("Tokens file is a directory");
      return;
    }

    try (var in = Files.newInputStream(tokenFile)) {
      tokens = mapper.readValue(in, TrackerTokens.class);
      if (tokens == null) {
        tokens = new TrackerTokens();
        serializeTokens();
      }
    } catch (StreamReadException e) {
      log.error("Failed to deserialize tokens, because the stream was already closed", e);
      throw new RuntimeException(e);
    } catch (DatabindException e) {
      log.error("Failed to deserialize tokens, because the data binding failed", e);
      throw new RuntimeException(e);
    } catch (IOException e) {
      log.error("Failed to deserialize tokens", e);
      throw new RuntimeException(e);
    }
  }

  @PreDestroy
  private void serializeTokens() {
    log.info("Serializing Tokens");

    if (Files.notExists(tokenFile)) {
      try {
        log.info("Creating tokens file");

        Path parent = tokenFile.getParent();

        if (Files.notExists(parent)) {
          Files.createDirectories(parent);
          log.info("Created tokens parent directory");
        }

        Files.createFile(tokenFile);
        log.info("Created tokens file");
      } catch (IOException e) {
        log.error("Failed to create tokens file", e);
        throw new RuntimeException(e);
      }
    }

    try (var out = Files.newOutputStream(tokenFile)) {
      mapper.writeValue(out, tokens);
    } catch (StreamWriteException e) {
      log.error("Failed to serialize tokens, because the stream was already closed", e);
      throw new RuntimeException(e);
    } catch (DatabindException e) {
      log.error("Failed to serialize tokens, because the data binding failed", e);
      throw new RuntimeException(e);
    } catch (IOException e) {
      log.error("Failed to serialize tokens", e);
      throw new RuntimeException(e);
    }
  }

  private void deserializeTrackers() {
    if (!Files.exists(trackerFile)) {
      return;
    }

    if (Files.isDirectory(trackerFile)) {
      log.error("Trackers file is a directory");
      return;
    }

    try (var in = Files.newInputStream(trackerFile)) {

      TypeReference<HashMap<Long, Tracker>> typeRef = new TypeReference<>() {};

      HashMap<Long, Tracker> tempTrackers = mapper.readValue(in, typeRef);
      if (tempTrackers == null) {
        return;
      }
      mangaTrackers.putAll(tempTrackers);
    } catch (StreamReadException e) {
      log.error("Failed to deserialize trackers, because the stream was already closed", e);
      throw new RuntimeException(e);
    } catch (DatabindException e) {
      log.error("Failed to deserialize trackers, because the data binding failed", e);
      throw new RuntimeException(e);
    } catch (IOException e) {
      log.error("Failed to deserialize trackers", e);
      throw new RuntimeException(e);
    }
  }

  @PreDestroy
  private void serializeTrackers() {
    log.info("Serializing Trackers");

    try (var out = Files.newOutputStream(trackerFile)) {
      mapper.writeValue(out, mangaTrackers);
    } catch (StreamWriteException e) {
      log.error("Failed to serialize trackers, because the stream was already closed", e);
      throw new RuntimeException(e);
    } catch (DatabindException e) {
      log.error("Failed to serialize trackers, because the data binding failed", e);
      throw new RuntimeException(e);
    } catch (IOException e) {
      log.error("Failed to serialize trackers", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Retrieves the tracker for a specific manga.
   *
   * @param mangaId The ID of the manga for which the tracker is to be retrieved.
   * @return The {@link Tracker} object for the specified manga.
   */
  public Tracker getTracker(long mangaId) {
    mangaTrackers.putIfAbsent(mangaId, new Tracker(mangaId));

    var tracker = mangaTrackers.get(mangaId);

    // Fix for old trackers without manga ID
    if (tracker.getMangaId() == 0) {
      tracker.setMangaId(mangaId);
    }

    return tracker;
  }
}
