package online.hatsunemiku.tachideskvaadinui.config;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import java.util.List;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig implements CacheManagerCustomizer<CaffeineCacheManager> {

  @Override
  public void customize(CaffeineCacheManager cacheManager) {
    cacheManager.setCacheNames(List.of("extensions"));

    CaffeineSpec spec = CaffeineSpec.parse("maximumSize=1500,expireAfterWrite=30m");

    cacheManager.setCaffeineSpec(spec);
  }
}
