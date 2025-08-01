/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.webpush.WebPush;
import com.vaadin.flow.server.webpush.WebPushMessage;
import com.vaadin.flow.server.webpush.WebPushSubscription;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.utils.PathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Service for sending web push notifications via the WebPush API.
 *
 * @version 1.12.0
 * @since 1.12.0
 */
@Slf4j
@Service
public class WebPushService {

  private final ObjectMapper mapper;
  private final Path subscriptionFile;

  @Value("${vaadin.webpush.publicKey}")
  private String publicKey;

  @Value("${vaadin.webpush.privateKey}")
  private String privateKey;

  @Value("${vaadin.webpush.subject}")
  private String subject;

  private WebPushSubscription subscription;
  private WebPush webPush;

  /**
   * Creates a new {@link WebPushService} instance.
   *
   * @param mapper The {@link ObjectMapper} used to serialize the subscription
   * @param env The {@link Environment} used to get the project directory
   */
  public WebPushService(ObjectMapper mapper, Environment env) {
    this.mapper = mapper;

    Path projectDirPath;
    projectDirPath = PathUtils.getResolvedProjectPath(env);

    subscriptionFile = projectDirPath.resolve("subscription.json");

    if (Files.notExists(subscriptionFile)) {
      return;
    }

    try (var in = Files.newInputStream(subscriptionFile)) {
      subscription = mapper.readValue(in, WebPushSubscription.class);
    } catch (IOException e) {
      log.error("Could not read subscription file", e);
      throw new RuntimeException(e);
    }
  }

  private WebPush getWebPush() {
    if (webPush == null) {
      webPush = new WebPush(publicKey, privateKey, subject);
    }

    return webPush;
  }

  /**
   * Subscribes the client to the push notification service.
   *
   * @param ui The {@link UI} to subscribe with
   */
  public void subscribe(UI ui) {
    getWebPush().subscribe(ui, this::setSubscription);
  }

  /**
   * Sends a notification to the subscribed client.
   *
   * @param title The title of the notification
   * @param message The message of the notification
   */
  public void notify(String title, String message) {

    if (subscription == null) {
      log.debug("No subscription available");
      return;
    }

    WebPushMessage pushMessage = new WebPushMessage(title, message);

    getWebPush().sendNotification(subscription, pushMessage);
  }

  private void setSubscription(WebPushSubscription subscription) {
    log.info("Adding subscription: {}", subscription.endpoint());
    this.subscription = subscription;
  }

  /** Removes the active subscription from the service. */
  public void removeSubscription() {
    log.info("Removing subscription: {}", subscription.endpoint());
    this.subscription = null;
  }

  /**
   * Updates the subscription to a new instance.
   *
   * @param subscription The new subscription to update to
   */
  public void updateSubscription(WebPushSubscription subscription) {
    this.subscription = subscription;
  }

  /**
   * Checks if a subscription already exists on the client side and fetches it if it does.
   *
   * @param ui The {@link UI} to check the subscription for
   */
  public void checkExistingSubscription(UI ui) {

    var webPush = getWebPush();

    webPush.subscriptionExists(
        ui,
        registered -> {
          if (registered) {
            webPush.fetchExistingSubscription(ui, this::updateSubscription);
          }
        });
  }

  /**
   * Handles the serialization of the subscription object to a file before the service is destroyed.
   */
  @PreDestroy
  public void destroy() {
    if (webPush == null) {
      return;
    }

    try (var out = Files.newOutputStream(subscriptionFile)) {
      mapper.writeValue(out, subscription);
    } catch (IOException e) {
      log.error("Could not write subscription file", e);
      throw new RuntimeException(e);
    }
  }
}
