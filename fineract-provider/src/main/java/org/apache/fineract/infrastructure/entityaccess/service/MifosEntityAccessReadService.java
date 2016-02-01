/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.entityaccess.data.MifosEntityAccessData;
import org.mifosplatform.infrastructure.entityaccess.data.MifosEntityRelationData;
import org.mifosplatform.infrastructure.entityaccess.data.MifosEntityToEntityMappingData;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityAccessType;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityType;

public interface MifosEntityAccessReadService {

    Collection<MifosEntityAccessData> retrieveEntityAccessFor(Long entityId, MifosEntityType type, MifosEntityAccessType accessType,
            MifosEntityType secondType, boolean includeAllOffices);

    String getSQLQueryInClause_WithListOfIDsForEntityAccess(Long entityId, MifosEntityType firstEntityType,
            MifosEntityAccessType accessType, MifosEntityType secondEntityType, boolean includeAllOffices);

    String getSQLQueryInClauseIDList_ForLoanProductsForOffice(Long loanProductId, boolean includeAllOffices);

    String getSQLQueryInClauseIDList_ForSavingsProductsForOffice(Long savingsProductId, boolean includeAllOffices);

    String getSQLQueryInClauseIDList_ForChargesForOffice(Long officeId, boolean includeAllOffices);

    Collection<MifosEntityRelationData> retrieveAllSupportedMappingTypes();

    Collection<MifosEntityToEntityMappingData> retrieveOneMapping(Long mapId);

    Collection<MifosEntityToEntityMappingData> retrieveEntityToEntityMappings(Long mapId, Long fromoId, Long toId);

}
