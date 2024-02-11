/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;
import online.hatsunemiku.tachideskvaadinui.services.client.ExtensionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExtensionService {

  private final ExtensionClient extensionClient;

  @Autowired
  public ExtensionService(ExtensionClient extensionClient) {
    this.extensionClient = extensionClient;
  }

  public List<Extension> getExtensions() {
    var extensions = extensionClient.getExtensions();

    extensions.sort(
        (o1, o2) -> {
          // installed extensions first
          // extensions with updates first
          // alphabetical order
          if (o1.isInstalled() && !o2.isInstalled()) {
            return -1;
          } else if (!o1.isInstalled() && o2.isInstalled()) {
            return 1;
          } else {
            if (o1.isHasUpdate() && !o2.isHasUpdate()) {
              return -1;
            } else if (!o1.isHasUpdate() && o2.isHasUpdate()) {
              return 1;
            } else {
              return o1.getName().compareTo(o2.getName());
            }
          }
        });

    return extensions;
  }

  public boolean installExtension(String extensionId) {
    return extensionClient.installExtension(extensionId);
  }

  public boolean uninstallExtension(String extensionId) {
    return extensionClient.uninstallExtension(extensionId);
  }

  public boolean updateExtension(String extensionId) {
    return extensionClient.updateExtension(extensionId);
  }
}
