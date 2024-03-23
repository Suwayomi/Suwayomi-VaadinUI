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

@Service
public class SuwayomiSettingsService {

  private final SuwayomiSettingsClient client;

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
