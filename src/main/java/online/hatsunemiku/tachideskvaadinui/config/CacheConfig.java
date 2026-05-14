/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CaffeineCacheManager caffeineCacheManager() {
    CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();

    Duration expireAfterWrite = Duration.ofMinutes(10);

    Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(expireAfterWrite);

    caffeineCacheManager.setCaffeine(caffeine);
    return caffeineCacheManager;
  }
}
