package online.hatsunemiku.tachideskvaadinui.config;

import static java.util.Arrays.asList;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig implements CacheManagerCustomizer<CaffeineCacheManager> {

  @Override
  public void customize(CaffeineCacheManager cacheManager) {
    cacheManager.setCacheNames(asList("extensions"));

    CaffeineSpec spec = CaffeineSpec.parse("maximumSize=1500,expireAfterWrite=30m");

    cacheManager.setCaffeineSpec(spec);
  }
}
