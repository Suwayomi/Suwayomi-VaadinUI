/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.startup;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import online.hatsunemiku.tachideskvaadinui.data.InitData;
import online.hatsunemiku.tachideskvaadinui.data.server.event.ServerStartedEvent;
import online.hatsunemiku.tachideskvaadinui.services.SuwayomiSettingsService;
import online.hatsunemiku.tachideskvaadinui.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class FirstInitService {

  private static final Logger log = LoggerFactory.getLogger(FirstInitService.class);
  private final Path projectDir;
  private final ObjectMapper objectMapper;
  private final SuwayomiSettingsService suwayomiSettingsService;

  public FirstInitService(
      Environment env, ObjectMapper objectMapper, SuwayomiSettingsService suwayomiSettingsService) {
    projectDir = PathUtils.getResolvedProjectPath(env);

    this.objectMapper = objectMapper;
    this.suwayomiSettingsService = suwayomiSettingsService;
  }

  @EventListener(ServerStartedEvent.class)
  private void init() {
    Path checkFile = projectDir.resolve("initCheck");

    if (Files.notExists(checkFile)) {
      try {
        Files.createFile(checkFile);
        objectMapper.writeValue(checkFile.toFile(), new InitData());
      } catch (Exception e) {
        log.error("Couldn't create the Initialization check file", e);
        return;
      }
    }

    try {
      InitData initData = objectMapper.readValue(checkFile.toFile(), InitData.class);

      if (!initData.isSuwayomiSettings()) {
        initSuwayomiSettings(initData);
      }

      try {
        objectMapper.writeValue(checkFile.toFile(), initData);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void initSuwayomiSettings(InitData initData) {
    boolean success = suwayomiSettingsService.resetExtensionRepos();

    if (!success) {
      throw new RuntimeException("Couldn't reset the extension repos");
    }

    initData.setSuwayomiSettings(true);
  }
}
