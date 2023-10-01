/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.settings.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UrlChangeEvent extends ApplicationEvent {

  private final String url;

  public UrlChangeEvent(Object source, String url) {
    super(source);
    this.url = url;
  }
}
