/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.security.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.TenantOidcConfig;
import org.springframework.stereotype.Service;

/**
 * Manages per-tenant OIDC configurations stored in the master database. Maintains an in-process cache keyed by
 * issuerUri to avoid a DB round-trip on every JWT validation. The cache is explicitly invalidated on save/delete, so
 * entries reflect the current DB state.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantOidcConfigService {

    private final TenantOidcConfigRepository repository;
    private final PasswordEncryptor passwordEncryptor;

    // In-process cache: issuerUri → config. Invalidated explicitly on write operations.
    private final ConcurrentHashMap<String, TenantOidcConfig> issuerCache = new ConcurrentHashMap<>();

    public Optional<TenantOidcConfig> findByIssuerUri(String issuerUri) {
        TenantOidcConfig cached = issuerCache.get(issuerUri);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<TenantOidcConfig> fromDb = repository.findByIssuerUri(issuerUri);
        fromDb.ifPresent(config -> issuerCache.put(issuerUri, config));
        return fromDb;
    }

    public Optional<TenantOidcConfig> findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId);
    }

    /**
     * Persists a new or updated OIDC config. Encrypts clientSecret before storing. Invalidates the issuer cache entry
     * so the next request fetches the updated config.
     */
    public TenantOidcConfig save(TenantOidcConfig config) {
        if (config.getClientSecret() != null && !config.getClientSecret().isBlank()) {
            config.setClientSecret(passwordEncryptor.encrypt(config.getClientSecret()));
        }
        TenantOidcConfig saved = repository.save(config);
        issuerCache.remove(saved.getIssuerUri());
        log.info("Saved OIDC config for tenant: {} (issuer: {})", saved.getTenantId(), saved.getIssuerUri());
        return saved;
    }

    /**
     * Returns the decrypted clientSecret for internal use (e.g., building an OAuth2 client). Never expose this value in
     * API responses.
     */
    public String decryptClientSecret(TenantOidcConfig config) {
        if (config.getClientSecret() == null || config.getClientSecret().isBlank()) {
            return null;
        }
        return passwordEncryptor.decrypt(config.getClientSecret());
    }

    public void deleteByTenantId(String tenantId) {
        repository.findByTenantId(tenantId).ifPresent(config -> {
            issuerCache.remove(config.getIssuerUri());
            repository.deleteByTenantId(tenantId);
            log.info("Deleted OIDC config for tenant: {}", tenantId);
        });
    }

    public void evictIssuer(String issuerUri) {
        if (issuerUri != null) {
            issuerCache.remove(issuerUri);
        }
    }

    /** Removes all cached entries — useful after bulk changes or on application events. */
    public void evictAll() {
        issuerCache.clear();
    }
}
