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
import java.util.Date;

public class FineractEntityToEntityMappingData implements Serializable {

    @SuppressWarnings("unused")
    private Long mapId;

    @SuppressWarnings("unused")
    private Long relationId;

    @SuppressWarnings("unused")
    private Long fromId;

    @SuppressWarnings("unused")
    private Long toId;

    @SuppressWarnings("unused")
    private Date startDate;

    @SuppressWarnings("unused")
    private Date endDate;

    @SuppressWarnings("unused")
    private final String fromEntity;

    @SuppressWarnings("unused")
    private final String toEntity;

    private FineractEntityToEntityMappingData(final Long mapId, final Long relationId, final Long fromId, final Long toId,
            final Date startDate, final Date endDate, final String fromEntity, final String toEntity) {
        this.mapId = mapId;
        this.relationId = relationId;
        this.fromId = fromId;
        this.toId = toId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fromEntity = fromEntity;
        this.toEntity = toEntity;
    }

    public static FineractEntityToEntityMappingData getRelatedEntities(final Long mapId, final Long relationId, final Long fromId,
            final Long toId, final Date startDate, final Date endDate ,final String fromEntity, final String toEntity) {


        return new FineractEntityToEntityMappingData(mapId, relationId, fromId, toId, startDate, endDate, fromEntity, toEntity);

    }

    public static FineractEntityToEntityMappingData getRelatedEntities(final Long relationId, final Long fromId, final Long toId,
            final Date startDate, final Date endDate) {
        final Long mapId = null;
        final String fromEntity = null;
        final String toEntity = null;

        return new FineractEntityToEntityMappingData(mapId, relationId, fromId, toId, startDate, endDate, fromEntity, toEntity);

    }

	public Long getRelationId() {
		return relationId;
	}

	public Long getToId() {
		return toId;
	}

	public String getFromEntity() {
		return fromEntity;
	}

    
}
