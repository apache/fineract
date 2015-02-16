/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.data;

import java.io.Serializable;
import java.util.Date;

public class MifosEntityToEntityMappingData implements Serializable {

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

    private MifosEntityToEntityMappingData(final Long mapId, final Long relationId, final Long fromId, final Long toId,
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

    public static MifosEntityToEntityMappingData getRelatedEntities(final Long mapId, final Long relationId, final Long fromId,
            final Long toId, final Date startDate, final Date endDate ,final String fromEntity, final String toEntity) {


        return new MifosEntityToEntityMappingData(mapId, relationId, fromId, toId, startDate, endDate, fromEntity, toEntity);

    }

    public static MifosEntityToEntityMappingData getRelatedEntities(final Long relationId, final Long fromId, final Long toId,
            final Date startDate, final Date endDate) {
        final Long mapId = null;
        final String fromEntity = null;
        final String toEntity = null;

        return new MifosEntityToEntityMappingData(mapId, relationId, fromId, toId, startDate, endDate, fromEntity, toEntity);

    }

}
