package org.mifosplatform.infrastructure.entityaccess.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.entityaccess.data.MifosEntityAccessData;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityAccessType;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityType;


public interface MifosEntityAccessReadService {
	Collection<MifosEntityAccessData> retrieveEntityAccessFor (Long entityId, MifosEntityType type, 
			MifosEntityAccessType accessType, MifosEntityType secondType,
			boolean includeAllOffices);

	String getSQLQueryInClause_WithListOfIDsForEntityAccess (Long entityId, MifosEntityType firstEntityType,
			MifosEntityAccessType accessType, MifosEntityType secondEntityType,
			boolean includeAllOffices);
	
	String getSQLQueryInClauseIDList_ForLoanProductsForOffice (Long loanProductId,
			boolean includeAllOffices);

	String getSQLQueryInClauseIDList_ForSavingsProductsForOffice(
			Long savingsProductId, boolean includeAllOffices);

	String getSQLQueryInClauseIDList_ForChargesForOffice(Long officeId,
			boolean includeAllOffices);
}
