package site.weather.global.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {
	@Bean
	public CaffeineCacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("weatherCache", "cityCache");
		cacheManager.setCaffeine(caffeineCacheBuilder());
		cacheManager.setAsyncCacheMode(true);
		return cacheManager;
	}

	@Bean
	public Caffeine<Object, Object> caffeineCacheBuilder() {
		return Caffeine.newBuilder()
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(100);
	}
}
