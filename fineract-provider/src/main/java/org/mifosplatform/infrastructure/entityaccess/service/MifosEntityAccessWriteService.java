/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.service;

import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface MifosEntityAccessWriteService {

    CommandProcessingResult createEntityAccess(final JsonCommand command);

    CommandProcessingResult createEntityToEntityMapping(final Long relId,final JsonCommand command);

    CommandProcessingResult updateEntityToEntityMapping(final Long mapId, final JsonCommand command);

    CommandProcessingResult deleteEntityToEntityMapping(final Long mapId);

    void addNewEntityAccess(final String entityType, final Long entityId, final CodeValue accessType, final String secondEntityType,
            final Long secondEntityId);

    /*
     * CommandProcessingResult updateEntityAccess ( final Long entityAccessId,
     * final JsonCommand command);
     * 
     * CommandProcessingResult removeEntityAccess ( final String entityType,
     * final Long entityId, final Long accessType, final String
     * secondEntityType, final Long secondEntityId);
     */
}