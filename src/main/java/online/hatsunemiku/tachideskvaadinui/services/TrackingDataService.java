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
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrackingDataService {
  private final ObjectMapper mapper;
  private final Path tokenFile;
  private final Path trackerFile;
  @Getter private TrackerTokens tokens;
  private final HashMap<Long, Tracker> mangaTrackers = new HashMap<>();

  public TrackingDataService(ObjectMapper mapper) {
    this.mapper = mapper;

    Path projectDirPath = PathUtils.getProjectDir();

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

  public Tracker getTracker(long mangaId) {
    mangaTrackers.putIfAbsent(mangaId, new Tracker());
    return mangaTrackers.get(mangaId);
  }
}
