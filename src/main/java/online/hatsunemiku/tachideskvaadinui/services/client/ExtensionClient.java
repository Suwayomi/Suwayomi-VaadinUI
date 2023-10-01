/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import java.net.URI;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "extensionClient", url = "http://localhost:8080")
public interface ExtensionClient {

  // {{_.base_url}}/api/v1/extension/update/{{_.pkgName}}
  @GetMapping("/api/v1/extension/update/{pkgName}")
  void updateExtension(URI baseUrl, @PathVariable String pkgName);
}
