package com.nextorm.portal.config;

import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URISyntaxException;

@EnableCaching
@Configuration
public class CacheConfig {
	@Bean
	public JCacheCacheManager cacheManager() throws URISyntaxException {
		CachingProvider cachingProvider = Caching.getCachingProvider(EhcacheCachingProvider.class.getName());
		CacheManager cacheManager = cachingProvider.getCacheManager(getClass().getResource("/ehcache.xml")
																			  .toURI(), getClass().getClassLoader());
		return new JCacheCacheManager(cacheManager);
	}
}
