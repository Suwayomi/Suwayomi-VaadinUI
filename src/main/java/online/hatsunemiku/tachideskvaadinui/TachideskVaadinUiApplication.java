/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui;

import java.io.IOException;
import java.net.ServerSocket;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.utils.BrowserUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main class of the application. This class is responsible for starting the Spring Boot
 * application.
 */
@Slf4j
@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
public class TachideskVaadinUiApplication {

  /**
   * The main method of the application. This method is responsible for starting the Spring Boot
   * application.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {
    boolean headless = Boolean.parseBoolean(System.getProperty("vaaui.headless"));

    if (isRunningAlready()) {
      log.warn("Application is already running.");
      runAlreadyRunningTasks(headless);
      System.exit(0);
    }

    log.info("Is headless: {}", headless);

    SpringApplication app =
        new SpringApplicationBuilder(TachideskVaadinUiApplication.class).headless(headless).build();

    app.run(args);
  }

  /**
   * Runs the tasks that should be run when the application is already running.
   *
   * @param headless Whether the application is running in headless mode
   */
  private static void runAlreadyRunningTasks(boolean headless) {
    if (!headless) {
      log.info("Opening browser...");

      try {
        BrowserUtils.openBrowser("http://localhost:3901");
      } catch (IOException e) {
        log.error("Failed to open browser.", e);
      }
    }
  }

  /**
   * Checks if the application is already running.
   *
   * @return {@code true} if the application is already running, {@code false} otherwise
   */
  private static boolean isRunningAlready() {
    int port = 3901;
    try (ServerSocket ignored =
        new ServerSocket(
            port)) { // skipcq: JAVA-S1011 - Not a security risk, because the socket is closed
      // immediately.
      return false;
    } catch (IOException e) {
      return true;
    }
  }
}
