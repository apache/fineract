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
package org.apache.fineract.organisation.provisioning.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.provisioning.constants.ProvisioningCriteriaConstants;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCriteriaDefinitionData;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCriteria;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCriteriaRepository;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCategoryNotFoundException;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCriteriaCannotBeDeletedException;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCriteriaNotFoundException;
import org.apache.fineract.organisation.provisioning.serialization.ProvisioningCriteriaDefinitionJsonDeserializer;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class ProvisioningCriteriaWritePlatformServiceJpaRepositoryImpl implements ProvisioningCriteriaWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ProvisioningCriteriaWritePlatformServiceJpaRepositoryImpl.class);

    private final ProvisioningCriteriaDefinitionJsonDeserializer fromApiJsonDeserializer;
    private final ProvisioningCriteriaAssembler provisioningCriteriaAssembler;
    private final ProvisioningCriteriaRepository provisioningCriteriaRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final GLAccountRepository glAccountRepository;
    private final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService ;
    
    @Autowired
    public ProvisioningCriteriaWritePlatformServiceJpaRepositoryImpl(final ProvisioningCriteriaDefinitionJsonDeserializer fromApiJsonDeserializer,
            final ProvisioningCriteriaAssembler provisioningCriteriaAssembler, final ProvisioningCriteriaRepository provisioningCriteriaRepository,
            final FromJsonHelper fromApiJsonHelper,
            final GLAccountRepository glAccountRepository,
            final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.provisioningCriteriaAssembler = provisioningCriteriaAssembler;
        this.provisioningCriteriaRepository = provisioningCriteriaRepository;
        this.fromApiJsonHelper = fromApiJsonHelper ;
        this.glAccountRepository = glAccountRepository ; 
        this.provisioningEntriesReadPlatformService = provisioningEntriesReadPlatformService ;
    }

    @Override
    public CommandProcessingResult createProvisioningCriteria(JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateForCreate(command.json());
            ProvisioningCriteria provisioningCriteria = provisioningCriteriaAssembler.fromParsedJson(command.parsedJson());
            this.provisioningCriteriaRepository.save(provisioningCriteria);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(provisioningCriteria.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult deleteProvisioningCriteria(Long criteriaId) {
        ProvisioningCriteria criteria = this.provisioningCriteriaRepository.findOne(criteriaId) ;
        if(criteria == null) {
            throw new ProvisioningCriteriaNotFoundException(criteriaId) ;
        }
        if(this.provisioningEntriesReadPlatformService.retrieveProvisioningEntryDataByCriteriaId(criteriaId) != null) {
            throw new ProvisioningCriteriaCannotBeDeletedException(criteriaId) ;
        }
        this.provisioningCriteriaRepository.delete(criteriaId) ;
        return new CommandProcessingResultBuilder().withEntityId(criteriaId).build();
    }

    @Override
    public CommandProcessingResult updateProvisioningCriteria(final Long criteriaId, JsonCommand command) {
    	try {
    		this.fromApiJsonDeserializer.validateForUpdate(command.json());
            ProvisioningCriteria provisioningCriteria = provisioningCriteriaRepository.findOne(criteriaId) ;
            if(provisioningCriteria == null) {
                throw new ProvisioningCategoryNotFoundException(criteriaId) ;
            }
            List<LoanProduct> products = this.provisioningCriteriaAssembler.parseLoanProducts(command.parsedJson()) ;
            final Map<String, Object> changes = provisioningCriteria.update(command, products) ;
            if(!changes.isEmpty()) {
                updateProvisioningCriteriaDefinitions(provisioningCriteria, command) ;
                provisioningCriteriaRepository.saveAndFlush(provisioningCriteria) ;    
            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(provisioningCriteria.getId()).build();	
    	} catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    private void updateProvisioningCriteriaDefinitions(ProvisioningCriteria provisioningCriteria, JsonCommand command) {
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(command.parsedJson().getAsJsonObject());
        JsonArray jsonProvisioningCriteria = this.fromApiJsonHelper.extractJsonArrayNamed(
                ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM, command.parsedJson());
        for (JsonElement element : jsonProvisioningCriteria) {
            JsonObject jsonObject = element.getAsJsonObject();
            Long id = this.fromApiJsonHelper.extractLongNamed("id", jsonObject) ;
            Long categoryId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_CATEOGRYID_PARAM, jsonObject);
            Long minimumAge = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_MINIMUM_AGE_PARAM, jsonObject);
            Long maximumAge = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_MAXIMUM_AGE_PARAM, jsonObject);
            BigDecimal provisioningpercentage = this.fromApiJsonHelper.extractBigDecimalNamed(ProvisioningCriteriaConstants.JSON_PROVISIONING_PERCENTAGE_PARAM,
                    jsonObject, locale);
            Long liabilityAccountId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_LIABILITY_ACCOUNT_PARAM, jsonObject);
            Long expenseAccountId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_EXPENSE_ACCOUNT_PARAM, jsonObject);
            GLAccount liabilityAccount = glAccountRepository.findOne(liabilityAccountId);
            GLAccount expenseAccount = glAccountRepository.findOne(expenseAccountId);
            String categoryName = null ;
            String liabilityAccountName = null ;
            String expenseAccountName = null ;
            ProvisioningCriteriaDefinitionData data = new ProvisioningCriteriaDefinitionData(id, categoryId, 
                    categoryName, minimumAge, maximumAge, provisioningpercentage, 
                    liabilityAccount.getId(), liabilityAccount.getGlCode(), liabilityAccountName, expenseAccount.getId(), expenseAccount.getGlCode(), expenseAccountName) ;
            provisioningCriteria.update(data, liabilityAccount, expenseAccount) ;
        }
    }
    
    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("criteria_name")) {
            final String name = command.stringValueOfParameterNamed("criteria_name");
            throw new PlatformDataIntegrityException("error.msg.provisioning.duplicate.criterianame", "Provisioning Criteria with name `"
                    + name + "` already exists", "category name", name);
        }else if (realCause.getMessage().contains("product_id")) {
			throw new PlatformDataIntegrityException(
					"error.msg.provisioning.product.id(s).already.associated.existing.criteria",
					"The selected products already associated with another Provisioning Criteria");
		}
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.provisioning.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
