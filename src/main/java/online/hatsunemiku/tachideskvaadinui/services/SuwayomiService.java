/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import java.util.Optional;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.ServerVersion;
import online.hatsunemiku.tachideskvaadinui.services.client.suwayomi.SuwayomiMetaClient;
import org.springframework.stereotype.Service;

/**
 * Responsible for retrieving data from the Suwayomi Server. This class is an abstraction over the
 * {@link SuwayomiMetaClient} class and provides methods for retrieving data from the Suwayomi
 * Server.
 */
@Service
public class SuwayomiService {

  private final SuwayomiMetaClient metaClient;

  public SuwayomiService(SuwayomiMetaClient metaClient) {
    this.metaClient = metaClient;
  }

  public Optional<ServerVersion> getServerVersion() {
    try {
      return Optional.of(metaClient.getServerVersion());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
