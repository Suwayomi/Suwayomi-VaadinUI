/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig implements CacheManagerCustomizer<CaffeineCacheManager> {

  @Override
  public void customize(CaffeineCacheManager cacheManager) {
    cacheManager.setAllowNullValues(false);

    Duration expiry = Duration.ofMinutes(10);

    Caffeine<Object, Object> cache =
        Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(expiry);

    cacheManager.setCaffeine(cache);
  }
}
