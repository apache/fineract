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
package org.apache.fineract.portfolio.client.adapter;

import static java.util.Objects.nonNull;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.documentmanagement.adapter.EntityImageIdAdapter;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
@Deprecated
class ClientImageIdAdapter implements EntityImageIdAdapter {

    private static final String ENTITY_TYPE = "clients";

    private final ClientRepository repository;

    @Override
    public boolean accept(String entityType) {
        return ENTITY_TYPE.equalsIgnoreCase(entityType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImageIdResult> get(Long entityId) {
        return repository.findById(entityId).filter(client -> nonNull(client.getImageId()))
                .map(client -> ImageIdResult.builder().id(client.getImageId()).displayName(client.getDisplayName()).build());
    }

    @Override
    @Transactional
    public Optional<ImageIdResult> set(Long entityId, Long imageId) {
        final var result = get(entityId);

        if (imageId == null) {
            repository.removeImageId(entityId);
        } else {
            repository.updateByIdAndImageId(entityId, imageId);
        }

        return result;
    }
}
