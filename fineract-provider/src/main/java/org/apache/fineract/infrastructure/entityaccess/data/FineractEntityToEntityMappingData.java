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

import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FineractEntityToEntityMappingData implements Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private Long mapId;

    @SuppressWarnings("unused")
    private Long relationId;

    @SuppressWarnings("unused")
    private Long fromId;

    @SuppressWarnings("unused")
    private Long toId;

    @SuppressWarnings("unused")
    private LocalDate startDate;

    @SuppressWarnings("unused")
    private LocalDate endDate;

    @SuppressWarnings("unused")
    private String fromEntity;

    @SuppressWarnings("unused")
    private String toEntity;

    public static FineractEntityToEntityMappingData getRelatedEntities(final Long mapId, final Long relationId, final Long fromId,
            final Long toId, final LocalDate startDate, final LocalDate endDate, final String fromEntity, final String toEntity) {

        return new FineractEntityToEntityMappingData().setMapId(mapId).setRelationId(relationId).setFromId(fromId).setToId(toId)
                .setStartDate(startDate).setEndDate(endDate).setFromEntity(fromEntity).setToEntity(toEntity);

    }

    public static FineractEntityToEntityMappingData getRelatedEntities(final Long relationId, final Long fromId, final Long toId,
            final LocalDate startDate, final LocalDate endDate) {
        final Long mapId = null;
        final String fromEntity = null;
        final String toEntity = null;

        return new FineractEntityToEntityMappingData().setMapId(mapId).setRelationId(relationId).setFromId(fromId).setToId(toId)
                .setStartDate(startDate).setEndDate(endDate).setFromEntity(fromEntity).setToEntity(toEntity);
    }

}
