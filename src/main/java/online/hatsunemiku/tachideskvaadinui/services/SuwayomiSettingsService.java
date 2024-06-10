/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import com.helger.commons.url.URLValidator;
import java.util.ArrayList;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.settings.FlareSolverrSettings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.ExtensionRepo;
import online.hatsunemiku.tachideskvaadinui.services.client.suwayomi.SuwayomiSettingsClient;
import org.springframework.stereotype.Service;

/**
 * Responsible for managing Suwayomi server settings. This class is an abstraction over the
 * {@link SuwayomiSettingsClient} class and provides methods for updating and retrieving Suwayomi
 * server settings.
 */
@Service
public class SuwayomiSettingsService {

  private final SuwayomiSettingsClient client;

  /**
   * Creates a new instance of the {@link SuwayomiSettingsService} class.
   *
   * @param client the {@link SuwayomiSettingsClient} used for making API requests to the Suwayomi
   *               Server.
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

  /**
   * Updates whether FlareSolverr is used by the Suwayomi Server.
   *
   * @param status whether to enable or disable FlareSolverr.
   * @return {@code true} if the status was updated successfully, {@code false} otherwise.
   */
  public boolean updateFlareSolverrEnabledStatus(boolean status) {
    return client.updateFlareSolverrEnabledStatus(status);
  }

  /**
   * Updates the URL to the FlareSolverr Server on the Suwayomi Server.
   *
   * @param url the URL to the FlareSolverr Server.
   * @return {@code true} if the URL was updated successfully, {@code false} otherwise.
   * @throws IllegalArgumentException if the URL is invalid.
   */
  public boolean updateFlareSolverrUrl(String url) throws IllegalArgumentException {
    //check if url is valid
    boolean valid = URLValidator.isValid(url);

    if (!valid) {
      throw new IllegalArgumentException("Invalid URL");
    }

    return client.updateFlareSolverrUrl(url);
  }

  /**
   * Retrieves the FlareSolverr settings on the Suwayomi Server.
   *
   * @return the {@link FlareSolverrSettings} object representing the FlareSolverr settings on the
   * server.
   */
  public FlareSolverrSettings getFlareSolverrSettings() {
    return client.getFlareSolverrSettings();
  }
}
