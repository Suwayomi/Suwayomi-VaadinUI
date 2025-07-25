/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vaadin.flow.server.webpush.WebPushSubscription;
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

  /**
   * Creates a new {@link SubscriptionAPI} instance.
   *
   * @param webPushService The {@link WebPushService} used to update the subscription
   */
  public SubscriptionAPI(WebPushService webPushService) {
    this.webPushService = webPushService;
  }

  /**
   * Updates the web push subscription of the user.
   *
   * @param request The request containing the old and new subscription
   */
  @PostMapping("update")
  public void updateSubscription(@RequestBody SubscriptionUpdateRequest request) {
    log.info("Updating subscription");

    var newSubscription = request._new();

    log.info("replacing old subscription with new subscription");

    webPushService.updateSubscription(newSubscription);
  }

  /**
   * Request for updating the web push subscription.
   *
   * @param old The old subscription
   * @param _new The new subscription
   * @version 1.12.0
   * @since 1.12.0
   */
  public record SubscriptionUpdateRequest(
      @JsonProperty("old") Subscription old, @JsonProperty("new") WebPushSubscription _new) {}
}
