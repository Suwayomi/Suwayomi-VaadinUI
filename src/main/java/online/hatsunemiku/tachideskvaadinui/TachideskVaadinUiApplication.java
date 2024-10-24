/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui;

import lombok.extern.slf4j.Slf4j;
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

    log.info("Is headless: {}", headless);

    SpringApplication app =
        new SpringApplicationBuilder(TachideskVaadinUiApplication.class).headless(headless).build();

    app.run(args);
  }
}
