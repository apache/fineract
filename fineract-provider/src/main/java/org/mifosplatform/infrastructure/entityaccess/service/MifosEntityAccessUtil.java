/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.service;

import java.util.Collection;
import java.util.Iterator;

import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;


import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.entityaccess.MifosEntityAccessConstants;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityAccessType;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityType;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MifosEntityAccessUtil {
    
    private final PlatformSecurityContext context;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final MifosEntityAccessWriteService mifosEntityAccessWriteService;
    private final MifosEntityAccessReadService mifosEntityAccessReadService;

    @Autowired
    public MifosEntityAccessUtil (
    		final PlatformSecurityContext context,
    		final GlobalConfigurationRepositoryWrapper globalConfigurationRepository,
            final MifosEntityAccessWriteService mifosEntityAccessWriteService,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final CodeValueRepositoryWrapper codeValueRepository,
            final MifosEntityAccessReadService mifosEntityAccessReadService) {
    	this.context = context;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.mifosEntityAccessWriteService = mifosEntityAccessWriteService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.codeValueRepository = codeValueRepository;
        this.mifosEntityAccessReadService = mifosEntityAccessReadService;
    }

	
	@Transactional
	public void checkConfigurationAndAddProductResrictionsForUserOffice (
			final MifosEntityAccessType mifosEntityAccessType,
			final MifosEntityType mifosEntityType,
			final Long productOrChargeId) {
		
		AppUser thisUser = this.context.authenticatedUser();
		
		// check if the office specific products are enabled. If yes, then save this product or charge against a specific office
        // i.e. this product or charge is specific for this office.
		
        final GlobalConfigurationProperty property = this.globalConfigurationRepository
        		.findOneByNameWithNotFoundDetection(
        				MifosEntityAccessConstants.GLOBAL_CONFIG_FOR_OFFICE_SPECIFIC_PRODUCTS);
        if (property.isEnabled() ) {
        	// If this property is enabled, then Mifos need to restrict access to this loan product to only the office of the current user            	
            final GlobalConfigurationProperty restrictToUserOfficeProperty = this.globalConfigurationRepository
            		.findOneByNameWithNotFoundDetection(
            				MifosEntityAccessConstants.GLOBAL_CONFIG_FOR_RESTRICT_PRODUCTS_TO_USER_OFFICE);
            
            if (restrictToUserOfficeProperty.isEnabled() ) {
            	final Long officeId = thisUser.getOffice().getId();
            	Collection<CodeValueData> codevalues = codeValueReadPlatformService.retrieveCodeValuesByCode(
            			MifosEntityAccessConstants.ENTITY_ACCESS_CODENAME);
            	if (codevalues != null) {
            		Iterator<CodeValueData> iterator = codevalues.iterator();
            		while(iterator.hasNext()) {
            			CodeValueData oneCodeValue = iterator.next();
            			if ( (oneCodeValue != null) &&
            					(oneCodeValue.getName().equals(mifosEntityAccessType.toStr())) ) {
            				CodeValue cv = codeValueRepository.findOneByCodeNameAndLabelWithNotFoundDetection(
            						MifosEntityAccessConstants.ENTITY_ACCESS_CODENAME,
            						mifosEntityAccessType.toStr()
            						);
            				if (cv != null) {
            					mifosEntityAccessWriteService.addNewEntityAccess(
            							MifosEntityType.OFFICE.getType(), officeId,
            							cv,
            							mifosEntityType.getType(), productOrChargeId);
            				}
            			}
            		}
            	}
            }
        }
		
	}
	
	public String getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled (
			MifosEntityType mifosEntityType) {
		String inClause = "";
		
		final GlobalConfigurationProperty property = this.globalConfigurationRepository
        		.findOneByNameWithNotFoundDetection(
        				MifosEntityAccessConstants.GLOBAL_CONFIG_FOR_OFFICE_SPECIFIC_PRODUCTS);
		
        if (property.isEnabled() ) {
        	// Get 'SQL In Clause' for fetching only products/charges that are relevant for current user's office
        	if (mifosEntityType.equals(MifosEntityType.SAVINGS_PRODUCT)) {
        		inClause = mifosEntityAccessReadService.
        				getSQLQueryInClauseIDList_ForSavingsProductsForOffice (
        				this.context.authenticatedUser().getOffice().getId(), false);
        	} else if (mifosEntityType.equals(MifosEntityType.LOAN_PRODUCT)) {
        		inClause = mifosEntityAccessReadService.
        				getSQLQueryInClauseIDList_ForLoanProductsForOffice (
        				this.context.authenticatedUser().getOffice().getId(), false);
        	} else if (mifosEntityType.equals(MifosEntityType.CHARGE)) {
        		inClause = mifosEntityAccessReadService.
        				getSQLQueryInClauseIDList_ForChargesForOffice(
        				this.context.authenticatedUser().getOffice().getId(), false);
        	}
        }
		return inClause;
	}
	
}