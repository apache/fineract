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
package org.apache.fineract.investor.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAssetOwnerHelper {

    private final ExternalAssetOwnerRepository repository;

    // REQUIRES_NEW isolates the INSERT into a separate transaction and persistence context,
    // so a constraint violation does not corrupt the caller's session or mark the
    // outer transaction as rollback-only, allowing a safe retry.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long findOrCreateId(final ExternalId externalId) {
        return repository.findIdByExternalId(externalId).orElseGet(() -> {
            final ExternalAssetOwner owner = new ExternalAssetOwner();
            owner.setExternalId(externalId);
            return repository.saveAndFlush(owner).getId();
        });
    }
}
