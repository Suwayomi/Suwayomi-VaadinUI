/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.notification;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.webpush.WebPush;
import com.vaadin.flow.server.webpush.WebPushMessage;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebPushService {
  @Value("${vaadin.webpush.publicKey}")
  private String publicKey;
  @Value("${vaadin.webpush.privateKey}")
  private String privateKey;
  @Value("${vaadin.webpush.subject}")
  private String subject;

  //TODO figure out how to serialize this
  /**
   * Maps the key (endpoint as of now) to the subscription object.
   */
  private final Map<String, Subscription> endpointSubscriptions;

  private WebPush webPush;

  public WebPushService() {
    endpointSubscriptions = new HashMap<>();
  }

  private WebPush getWebPush() {
    if (webPush == null) {
      webPush = new WebPush(publicKey, privateKey, subject);
    }

    return webPush;
  }

  public void subscribe(UI ui) {
    getWebPush().subscribe(ui, this::addSubscription);
  }

  public void notifyAll(String title, String message) {
    WebPushMessage pushMessage = new WebPushMessage(title, message);

    endpointSubscriptions.values().forEach(subscription -> {
      getWebPush().sendNotification(subscription, pushMessage);
    });
  }

  private void addSubscription(Subscription subscription) {
    log.info("Adding subscription: {}", subscription.endpoint());
    endpointSubscriptions.put(subscription.endpoint(), subscription);
  }

  public void removeSubscription(Subscription subscription) {
    log.info("Removing subscription: {}", subscription.endpoint());
    endpointSubscriptions.remove(subscription.endpoint());
  }
}
