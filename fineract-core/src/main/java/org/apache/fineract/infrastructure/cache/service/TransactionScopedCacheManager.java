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
package org.apache.fineract.infrastructure.cache.service;

import org.springframework.cache.CacheManager;

/**
 * A {@link CacheManager} whose entries only live for the duration of a database transaction.
 * <p>
 * The transaction manager does not call these hooks directly: {@link RuntimeDelegatingCacheManager} forwards them, and
 * only while this manager is the active one. That keeps the transaction-scoped clearing from wiping caches that belong
 * to another mode (for example the TTL-based single-node cache), which share the same underlying cache instances.
 */
public interface TransactionScopedCacheManager extends CacheManager {

    /** Called right after a transaction has begun on the current thread. */
    void afterBegin();

    /** Called after a transaction has completed (committed or rolled back) on the current thread. */
    void afterCompletion();
}
