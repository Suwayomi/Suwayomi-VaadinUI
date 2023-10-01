/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.settings.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SettingsEventPublisher {

  private final ApplicationEventPublisher publisher;

  public SettingsEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  public void publishUrlChangeEvent(Object source, String url) {
    var event = new UrlChangeEvent(source, url);
    publisher.publishEvent(event);
  }
}
