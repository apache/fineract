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
package org.apache.fineract.commands.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdempotencyKeyResolver {

    private final FineractRequestContextHolder fineractRequestContextHolder;

    private final IdempotencyKeyGenerator randomKeyGenerator;

    private final DeterministicIdempotencyKeyGenerator deterministicGenerator;

    public record ResolvedKey(String key, boolean isDeterministic) {
    }

    public ResolvedKey resolveWithMeta(CommandWrapper wrapper) {
        // 1. Explicit key from wrapper (client-provided header)
        if (wrapper.getIdempotencyKey() != null) {
            return new ResolvedKey(wrapper.getIdempotencyKey(), false);
        }
        // 2. Internal retry — key already stored in request context
        Optional<String> attributeKey = getAttribute();
        if (attributeKey.isPresent()) {
            return new ResolvedKey(attributeKey.get(), false);
        }
        // 3. No JSON body — cannot hash, use random key
        if (wrapper.getJson() == null || wrapper.getJson().isBlank()) {
            return new ResolvedKey(randomKeyGenerator.create(), false);
        }
        // 4. No clientId and no entityId — system-level operation (e.g. global
        // config update, business date change). These have no per-caller scope
        // so the same payload from different scenarios within the same 5-minute
        // window would collide. Fall back to random key to avoid false cache hits.
        if (wrapper.getClientId() == null && wrapper.getEntityId() == null && wrapper.getJobName() == null) {
            return new ResolvedKey(randomKeyGenerator.create(), false);
        }
        // 5. Global configuration updates — same configId + same payload (e.g.
        // enabled=true) collides across scenarios within the same 5-minute window
        // since entityId is the configId not a client-scoped resource.
        String href = wrapper.getHref() != null ? wrapper.getHref() : "";
        if (href.startsWith("/configurations/")) {
            return new ResolvedKey(randomKeyGenerator.create(), false);
        }

        // 6. Job commands — always use random key since jobs must run every invocation
        // even with the same payload (e.g. same loan IDs for COB across different business dates)
        if (wrapper.getJobName() != null && !wrapper.getJobName().isBlank()) {
            return new ResolvedKey(randomKeyGenerator.create(), false);
        }

        // 7. Account transfers — the ONLY operation where deterministic idempotency
        // is genuinely needed. A network timeout during a transfer means the client
        // cannot know if money was moved. Retrying with a random key would create
        // a duplicate transfer. Deterministic key ensures the retry returns the
        // cached result instead of moving money twice.
        String entityName = wrapper.getEntityName() != null ? wrapper.getEntityName().toUpperCase() : "";
        String actionName = wrapper.getActionName() != null ? wrapper.getActionName().toUpperCase() : "";
        boolean isAccountTransfer = actionName.equals("CREATE") && entityName.equals("ACCOUNTTRANSFER");
        if (!isAccountTransfer) {
            return new ResolvedKey(randomKeyGenerator.create(), false);
        }

        // 8. Account transfer — generate deterministic key to prevent duplicate transfers
        String deterministicKey = deterministicGenerator.generate(wrapper.getJson(), buildContext(wrapper));
        fineractRequestContextHolder.setAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_ATTRIBUTE, deterministicKey);
        return new ResolvedKey(deterministicKey, true);
    }

    public String resolve(CommandWrapper wrapper) {
        return resolveWithMeta(wrapper).key();
    }

    private String buildContext(CommandWrapper wrapper) {
        return wrapper.getActionName() + ":" + wrapper.getEntityName() + ":" + wrapper.getHref() + ":" + wrapper.getClientId() + ":"
                + wrapper.getJobName();
    }

    private Optional<String> getAttribute() {
        return Optional.ofNullable(fineractRequestContextHolder.getAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_ATTRIBUTE))
                .map(String::valueOf);

    }
}
