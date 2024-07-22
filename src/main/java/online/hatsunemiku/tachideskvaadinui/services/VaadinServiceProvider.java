/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Service;

@Service
public class VaadinServiceProvider implements VaadinServiceInitListener {

  private static VaadinService vaadinService;

  @Override
  public void serviceInit(ServiceInitEvent event) {
    vaadinService = event.getSource();
  }

  public static VaadinService getCurrentService() {
    return vaadinService;
  }
}
