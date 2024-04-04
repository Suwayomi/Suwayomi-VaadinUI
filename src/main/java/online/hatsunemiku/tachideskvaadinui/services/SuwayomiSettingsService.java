/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import java.util.ArrayList;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.ExtensionRepo;
import online.hatsunemiku.tachideskvaadinui.services.client.suwayomi.SuwayomiSettingsClient;
import org.springframework.stereotype.Service;

/**
 * Responsible for managing Suwayomi server settings. This class is an abstraction over the {@link
 * SuwayomiSettingsClient} class and provides methods for updating and retrieving Suwayomi server
 * settings.
 */
@Service
public class SuwayomiSettingsService {

  private final SuwayomiSettingsClient client;

  /**
   * Creates a new instance of the {@link SuwayomiSettingsService} class.
   *
   * @param client the {@link SuwayomiSettingsClient} used for making API requests to the Suwayomi
   *     Server.
   */
  public SuwayomiSettingsService(SuwayomiSettingsClient client) {
    this.client = client;
  }

  public boolean addExtensionRepo(String extensionRepoUrl) {
    var extensionRepos = getExtensionRepos();
    var extensionRepo = new ExtensionRepo(extensionRepoUrl);
    extensionRepos.add(extensionRepo);

    var repos = extensionRepos.stream().map(ExtensionRepo::getUrl).toList();

    return client.updateExtensionRepos(repos);
  }

  public boolean removeExtensionRepo(String extensionRepoUrl) {
    var extensionRepos = getExtensionRepos();
    var extensionRepo = new ExtensionRepo(extensionRepoUrl);
    extensionRepos.remove(extensionRepo);

    var repos = extensionRepos.stream().map(ExtensionRepo::getUrl).toList();
    return client.updateExtensionRepos(repos);
  }

  public boolean resetExtensionRepos() {
    List<String> extensionRepos = List.of();

    return client.updateExtensionRepos(extensionRepos);
  }

  public List<ExtensionRepo> getExtensionRepos() {
    return new ArrayList<>(client.getExtensionRepos().stream().map(ExtensionRepo::new).toList());
  }
}
