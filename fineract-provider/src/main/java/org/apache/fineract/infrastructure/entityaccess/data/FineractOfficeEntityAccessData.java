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
package org.apache.fineract.infrastructure.entityaccess.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingIdsView;

public record FineractOfficeEntityAccessData(boolean allEntitiesGranted, Set<Long> restrictedEntityIds,
        Set<Long> grantedEntityIds) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Long ALL_ENTITIES_ID = 0L;

    public static FineractOfficeEntityAccessData from(final Collection<FineractEntityToEntityMappingIdsView> mappings,
            final Collection<Long> officeIds) {
        boolean allEntitiesGranted = false;
        final Set<Long> restrictedEntityIds = new HashSet<>();
        final Set<Long> grantedEntityIds = new HashSet<>();
        for (FineractEntityToEntityMappingIdsView mapping : mappings) {
            if (mapping.getToId() == null) {
                continue;
            }
            final boolean grantedToOffice = ALL_ENTITIES_ID.equals(mapping.getFromId()) || officeIds.contains(mapping.getFromId());
            if (ALL_ENTITIES_ID.equals(mapping.getToId())) {
                allEntitiesGranted |= grantedToOffice;
            } else {
                restrictedEntityIds.add(mapping.getToId());
                if (grantedToOffice) {
                    grantedEntityIds.add(mapping.getToId());
                }
            }
        }
        return new FineractOfficeEntityAccessData(allEntitiesGranted, Set.copyOf(restrictedEntityIds), Set.copyOf(grantedEntityIds));
    }

    public boolean isVisible(final Long entityId) {
        return allEntitiesGranted || !restrictedEntityIds.contains(entityId) || grantedEntityIds.contains(entityId);
    }
}
