package org.mifosplatform.infrastructure.cache;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.stereotype.Component;

/**
 * At present this implementation of {@link CacheManager} just delegates to the
 * real {@link CacheManager} to use.
 *
 * By default it is {@link NoOpCacheManager} but we can change that by checking
 * some persisted configuration in the database on startup and allow user to
 * switch implementation through UI/API
 */
@Component
public class RuntimeDelegatingCacheManager implements CacheManager {

    private final CacheManager ehcacheCacheManager;
    private final CacheManager noOpCacheManager = new NoOpCacheManager();
    private CacheManager currentCacheManager = this.noOpCacheManager;

    @Autowired
    public RuntimeDelegatingCacheManager(final EhCacheCacheManager ehCacheCacheManager) {
        this.ehcacheCacheManager = ehCacheCacheManager;
    }

    @Override
    public Cache getCache(final String name) {
        return this.currentCacheManager.getCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return this.currentCacheManager.getCacheNames();
    }

    // TODO - Annuruddha - you need to create API to call to switch caching
    // approach from NoOp to Single instance Echache - we can tackle the
    // distributed cache through memcache then - didnt see the configuration for
    // that.
    // example method to switch caching mechanism used
    private void switchToEhCache() {
        this.currentCacheManager = this.ehcacheCacheManager;
    }
}