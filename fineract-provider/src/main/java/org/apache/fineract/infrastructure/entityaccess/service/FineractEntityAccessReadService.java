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
package org.apache.fineract.infrastructure.entityaccess.service;

import java.util.Collection;

import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityRelationData;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityToEntityMappingData;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityType;

public interface FineractEntityAccessReadService {

	Collection<FineractEntityToEntityMappingData> retrieveEntityAccessFor(FineractEntityType firstEntityType,
			   		final Long relId, final  Long fromEntityId,boolean includeAllSubOffices);

	String getSQLQueryInClause_WithListOfIDsForEntityAccess( FineractEntityType firstEntityType,
			             final Long relId,final Long fromEntityId ,boolean includeAllOffices);

    String getSQLQueryInClauseIDList_ForLoanProductsForOffice(Long loanProductId, boolean includeAllOffices);

    String getSQLQueryInClauseIDList_ForSavingsProductsForOffice(Long savingsProductId, boolean includeAllOffices);

    String getSQLQueryInClauseIDList_ForChargesForOffice(Long officeId, boolean includeAllOffices);

    Collection<FineractEntityRelationData> retrieveAllSupportedMappingTypes();

    Collection<FineractEntityToEntityMappingData> retrieveOneMapping(Long mapId);

    Collection<FineractEntityToEntityMappingData> retrieveEntityToEntityMappings(Long mapId, Long fromoId, Long toId);

}
