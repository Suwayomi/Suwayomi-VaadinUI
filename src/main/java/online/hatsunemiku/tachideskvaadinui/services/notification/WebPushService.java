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
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Subscription;
import online.hatsunemiku.tachideskvaadinui.utils.PathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebPushService {

  private final ObjectMapper mapper;
  @Value("${vaadin.webpush.publicKey}")
  private String publicKey;
  @Value("${vaadin.webpush.privateKey}")
  private String privateKey;
  @Value("${vaadin.webpush.subject}")
  private String subject;

  private Subscription subscription;

  private WebPush webPush;
  private final Path subscriptionFile;

  public WebPushService(ObjectMapper mapper, Environment env) {
    this.mapper = mapper;

    Path projectDirPath;
    projectDirPath = PathUtils.getResolvedProjectPath(env);

    subscriptionFile = projectDirPath.resolve("subscription.json");

    if (Files.notExists(subscriptionFile)) {
      return;
    }

    try (var in = Files.newInputStream(subscriptionFile)) {
      subscription = mapper.readValue(in, Subscription.class);
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

  public void subscribe(UI ui) {
    getWebPush().subscribe(ui, this::setSubscription);
  }

  public void notify(String title, String message) {

    if (subscription == null) {
      log.debug("No subscription available");
      return;
    }

    WebPushMessage pushMessage = new WebPushMessage(title, message);

    getWebPush().sendNotification(subscription, pushMessage);
  }

  private void setSubscription(Subscription subscription) {
    log.info("Adding subscription: {}", subscription.endpoint());
    this.subscription = subscription;
  }

  public void removeSubscription() {
    log.info("Removing subscription: {}", subscription.endpoint());
    this.subscription = null;
  }

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
