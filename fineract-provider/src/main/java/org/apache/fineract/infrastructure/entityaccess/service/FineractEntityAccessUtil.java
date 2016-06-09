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
import java.util.Date;
import java.util.Iterator;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.entityaccess.FineractEntityAccessConstants;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelation;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelationRepositoryWrapper;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMapping;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingRepository;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityType;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FineractEntityAccessUtil {
    
    private final PlatformSecurityContext context;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final FineractEntityAccessWriteService fineractEntityAccessWriteService;
    private final FineractEntityAccessReadService fineractEntityAccessReadService;
    private final FineractEntityRelationRepositoryWrapper fineractEntityRelationRepositoryWrapper;
    private final FineractEntityToEntityMappingRepository fineractEntityToEntityMappingRepository;

    @Autowired
    public FineractEntityAccessUtil (
    		final PlatformSecurityContext context,
    		final GlobalConfigurationRepositoryWrapper globalConfigurationRepository,
            final FineractEntityAccessWriteService fineractEntityAccessWriteService,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final CodeValueRepositoryWrapper codeValueRepository,
            final FineractEntityAccessReadService fineractEntityAccessReadService,
            final FineractEntityRelationRepositoryWrapper fineractEntityRelationRepositoryWrapper,
            final FineractEntityToEntityMappingRepository fineractEntityToEntityMappingRepository) {
    	this.context = context;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.fineractEntityAccessWriteService = fineractEntityAccessWriteService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.codeValueRepository = codeValueRepository;
        this.fineractEntityAccessReadService = fineractEntityAccessReadService;
        this.fineractEntityRelationRepositoryWrapper = fineractEntityRelationRepositoryWrapper;
        this.fineractEntityToEntityMappingRepository = fineractEntityToEntityMappingRepository;
    }

	
	@Transactional
	public void checkConfigurationAndAddProductResrictionsForUserOffice (
			final FineractEntityAccessType fineractEntityAccessType,
			final Long productOrChargeId) {
		
		AppUser thisUser = this.context.authenticatedUser();
		
		// check if the office specific products are enabled. If yes, then save this product or charge against a specific office
        // i.e. this product or charge is specific for this office.
		
        final GlobalConfigurationProperty property = this.globalConfigurationRepository
        		.findOneByNameWithNotFoundDetection(
        				FineractEntityAccessConstants.GLOBAL_CONFIG_FOR_OFFICE_SPECIFIC_PRODUCTS);
        if (property.isEnabled() ) {
        	// If this property is enabled, then Fineract need to restrict access to this loan product to only the office of the current user            	
            final GlobalConfigurationProperty restrictToUserOfficeProperty = this.globalConfigurationRepository
            		.findOneByNameWithNotFoundDetection(
            				FineractEntityAccessConstants.GLOBAL_CONFIG_FOR_RESTRICT_PRODUCTS_TO_USER_OFFICE);
            
            if (restrictToUserOfficeProperty.isEnabled() ) {
            	final Long officeId = thisUser.getOffice().getId();
            					Date startDateFormapping = null;
            					Date endDateFormapping = null;
            					FineractEntityRelation fineractEntityRelation = fineractEntityRelationRepositoryWrapper
            							.findOneByCodeName(fineractEntityAccessType.toStr());
            					Long relId = fineractEntityRelation.getId();
            					final FineractEntityRelation mapId = this.fineractEntityRelationRepositoryWrapper
            							.findOneWithNotFoundDetection(relId);
            					final FineractEntityToEntityMapping newMap = FineractEntityToEntityMapping.newMap(mapId, officeId,
            							productOrChargeId, startDateFormapping, endDateFormapping);
            					this.fineractEntityToEntityMappingRepository.save(newMap);
            				}
        }
		
	}
	
	public String getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled (
			FineractEntityType fineractEntityType) {
		String inClause = "";
		
		final GlobalConfigurationProperty property = this.globalConfigurationRepository
        		.findOneByNameWithNotFoundDetection(
        				FineractEntityAccessConstants.GLOBAL_CONFIG_FOR_OFFICE_SPECIFIC_PRODUCTS);
		
        if (property.isEnabled() ) {
        	// Get 'SQL In Clause' for fetching only products/charges that are relevant for current user's office
        	if (fineractEntityType.equals(FineractEntityType.SAVINGS_PRODUCT)) {
        		inClause = fineractEntityAccessReadService.
        				getSQLQueryInClauseIDList_ForSavingsProductsForOffice (
        				this.context.authenticatedUser().getOffice().getId(), false);
        	} else if (fineractEntityType.equals(FineractEntityType.LOAN_PRODUCT)) {
        		inClause = fineractEntityAccessReadService.
        				getSQLQueryInClauseIDList_ForLoanProductsForOffice (
        				this.context.authenticatedUser().getOffice().getId(), false);
        	} else if (fineractEntityType.equals(FineractEntityType.CHARGE)) {
        		inClause = fineractEntityAccessReadService.
        				getSQLQueryInClauseIDList_ForChargesForOffice(
        				this.context.authenticatedUser().getOffice().getId(), false);
        	}
        }
		return inClause;
	}
	
}