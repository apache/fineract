package org.mifosplatform.infrastructure.cache.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.infrastructure.cache.CacheApiConstants;
import org.mifosplatform.infrastructure.cache.CacheEnumerations;
import org.mifosplatform.infrastructure.cache.data.CacheData;
import org.mifosplatform.infrastructure.cache.domain.CacheType;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
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
@Component(value = "runtimeDelegatingCacheManager")
public class RuntimeDelegatingCacheManager implements CacheManager {

    private final EhCacheCacheManager ehcacheCacheManager;
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

    public Collection<CacheData> retrieveAll() {

        final boolean ehcacheEnabled = false;
        final boolean noCacheEnabled = !ehcacheEnabled;
        final boolean distributedCacheEnabled = false;

        final EnumOptionData noCacheType = CacheEnumerations.cacheType(CacheType.NO_CACHE);
        final EnumOptionData singleNodeCacheType = CacheEnumerations.cacheType(CacheType.SINGLE_NODE);
        final EnumOptionData multiNodeCacheType = CacheEnumerations.cacheType(CacheType.MULTI_NODE);

        final CacheData noCache = CacheData.instance(noCacheType, noCacheEnabled);
        final CacheData singleNodeCache = CacheData.instance(singleNodeCacheType, ehcacheEnabled);
        final CacheData distributedCache = CacheData.instance(multiNodeCacheType, distributedCacheEnabled);

        final Collection<CacheData> caches = Arrays.asList(noCache, singleNodeCache, distributedCache);
        return caches;
    }

    public Map<String, Object> switchToCache(final boolean ehcacheEnabled, final CacheType toCacheType) {

        final Map<String, Object> changes = new HashMap<String, Object>();

        final boolean noCacheEnabled = !ehcacheEnabled;
        final boolean distributedCacheEnabled = !ehcacheEnabled;

        switch (toCacheType) {
            case INVALID:
            break;
            case NO_CACHE:
                if (!noCacheEnabled) {
                    changes.put(CacheApiConstants.cacheTypeParameter, toCacheType.getValue());
                    this.currentCacheManager = this.noOpCacheManager;
                }
            break;
            case SINGLE_NODE:
                if (!ehcacheEnabled) {
                    changes.put(CacheApiConstants.cacheTypeParameter, toCacheType.getValue());
                    clearEhCache();
                    this.currentCacheManager = this.ehcacheCacheManager;
                }
            break;
            case MULTI_NODE:
                if (!distributedCacheEnabled) {
                    changes.put(CacheApiConstants.cacheTypeParameter, toCacheType.getValue());
                }
            break;
        }

        return changes;
    }

    private void clearEhCache() {
        this.ehcacheCacheManager.getCacheManager().clearAll();
    }
}