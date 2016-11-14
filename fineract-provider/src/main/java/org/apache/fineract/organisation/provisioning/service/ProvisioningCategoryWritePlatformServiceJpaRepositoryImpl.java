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

import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCategory;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCategoryRepository;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCategoryCannotBeDeletedException;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCategoryNotFoundException;
import org.apache.fineract.organisation.provisioning.serialization.ProvisioningCategoryDefinitionJsonDeserializer;
import org.apache.fineract.portfolio.charge.service.ChargeWritePlatformServiceJpaRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProvisioningCategoryWritePlatformServiceJpaRepositoryImpl implements ProvisioningCategoryWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ChargeWritePlatformServiceJpaRepositoryImpl.class);

    private final ProvisioningCategoryRepository provisioningCategoryRepository;

    private final ProvisioningCategoryDefinitionJsonDeserializer fromApiJsonDeserializer;
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public ProvisioningCategoryWritePlatformServiceJpaRepositoryImpl(final ProvisioningCategoryRepository provisioningCategoryRepository,
            final ProvisioningCategoryDefinitionJsonDeserializer fromApiJsonDeserializer, final RoutingDataSource dataSource) {
        this.provisioningCategoryRepository = provisioningCategoryRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public CommandProcessingResult createProvisioningCateogry(JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateForCreate(command.json());
            final ProvisioningCategory provisioningCategory = ProvisioningCategory.fromJson(command);
            this.provisioningCategoryRepository.save(provisioningCategory);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(provisioningCategory.getId())
                    .build();
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
    public CommandProcessingResult deleteProvisioningCateogry(JsonCommand command) {
        this.fromApiJsonDeserializer.validateForCreate(command.json());
        final ProvisioningCategory provisioningCategory = ProvisioningCategory.fromJson(command);
        boolean isProvisioningCategoryInUse = isAnyLoanProductsAssociateWithThisProvisioningCategory(provisioningCategory.getId()) ;
        if(isProvisioningCategoryInUse) {
            throw new ProvisioningCategoryCannotBeDeletedException(
                    "error.msg.provisioningcategory.cannot.be.deleted.it.is.already.used.in.loanproduct",
                    "This provisioning category cannot be deleted, it is already used in loan product");
        }
        this.provisioningCategoryRepository.delete(provisioningCategory);
        return new CommandProcessingResultBuilder().withEntityId(provisioningCategory.getId()).build();
    }

    @Override
    public CommandProcessingResult updateProvisioningCategory(final Long categoryId, JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            final ProvisioningCategory provisioningCategoryForUpdate = this.provisioningCategoryRepository.findOne(categoryId);
            if (provisioningCategoryForUpdate == null) { throw new ProvisioningCategoryNotFoundException(categoryId); }
            final Map<String, Object> changes = provisioningCategoryForUpdate.update(command);
            if (!changes.isEmpty()) {
                this.provisioningCategoryRepository.save(provisioningCategoryForUpdate);
            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(categoryId).with(changes).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    private boolean isAnyLoanProductsAssociateWithThisProvisioningCategory(final Long categoryID) {
        final String sql = "select if((exists (select 1 from m_loanproduct_provisioning_details lpd where lpd.category_id = ?)) = 1, 'true', 'false')";
        final String isLoansUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { categoryID });
        return new Boolean(isLoansUsingCharge);
    }
    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("category_name")) {
            final String name = command.stringValueOfParameterNamed("category_name");
            throw new PlatformDataIntegrityException("error.msg.provisioning.duplicate.categoryname", "Provisioning Cateory with name `"
                    + name + "` already exists", "category name", name);
        }
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

}
