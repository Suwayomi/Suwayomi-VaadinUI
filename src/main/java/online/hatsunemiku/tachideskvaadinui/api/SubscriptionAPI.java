/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Subscription;
import online.hatsunemiku.tachideskvaadinui.services.notification.WebPushService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * API for updating the web push subscription of the user.
 *
 * @version 1.12.0
 * @since 1.12.0
 */
@Slf4j
@RestController("/api/subscription")
public class SubscriptionAPI {

  private WebPushService webPushService;

  public SubscriptionAPI(WebPushService webPushService) {
    this.webPushService = webPushService;
  }

  @PostMapping("update")
  public void updateSubscription(@RequestBody SubscriptionUpdateRequest request) {
    log.info("Updating subscription");

    Subscription newSubscription = request._new();

    log.info("replacing old subscription with new subscription");

    webPushService.updateSubscription(newSubscription);

  }

  public record SubscriptionUpdateRequest(
      @JsonProperty("old") Subscription old,
      @JsonProperty("new") Subscription _new
  ) {

  }

}
