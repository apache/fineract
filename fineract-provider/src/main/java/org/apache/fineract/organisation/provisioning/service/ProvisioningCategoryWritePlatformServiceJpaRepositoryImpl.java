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

import jakarta.persistence.PersistenceException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCategory;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCategoryRepository;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCategoryCannotBeDeletedException;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCategoryNotFoundException;
import org.apache.fineract.organisation.provisioning.serialization.ProvisioningCategoryDefinitionJsonDeserializer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaSystemException;

@Slf4j
@RequiredArgsConstructor
public class ProvisioningCategoryWritePlatformServiceJpaRepositoryImpl implements ProvisioningCategoryWritePlatformService {

    private final ProvisioningCategoryRepository provisioningCategoryRepository;

    private final ProvisioningCategoryDefinitionJsonDeserializer fromApiJsonDeserializer;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public CommandProcessingResult createProvisioningCateogry(JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateForCreate(command.json());
            final ProvisioningCategory provisioningCategory = ProvisioningCategory.fromJson(command);
            this.provisioningCategoryRepository.saveAndFlush(provisioningCategory);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(provisioningCategory.getId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult deleteProvisioningCateogry(JsonCommand command) {
        final Long categoryId = command.entityId();
        final ProvisioningCategory provisioningCategory = this.provisioningCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ProvisioningCategoryNotFoundException(categoryId));
        boolean isProvisioningCategoryInUse = isAnyLoanProductsAssociateWithThisProvisioningCategory(categoryId);
        if (isProvisioningCategoryInUse) {
            throw new ProvisioningCategoryCannotBeDeletedException(
                    "error.msg.provisioningcategory.cannot.be.deleted.it.is.already.used.in.loanproduct",
                    "This provisioning category cannot be deleted, it is already used in loan product");
        }
        this.provisioningCategoryRepository.delete(provisioningCategory);
        return new CommandProcessingResultBuilder() //
                .withEntityId(categoryId) //
                .build();
    }

    @Override
    public CommandProcessingResult updateProvisioningCategory(final Long categoryId, JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            final ProvisioningCategory provisioningCategoryForUpdate = this.provisioningCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ProvisioningCategoryNotFoundException(categoryId));
            final Map<String, Object> changes = provisioningCategoryForUpdate.update(command);
            if (!changes.isEmpty()) {
                this.provisioningCategoryRepository.saveAndFlush(provisioningCategoryForUpdate);
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(categoryId) //
                    .with(changes) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    private boolean isAnyLoanProductsAssociateWithThisProvisioningCategory(final Long categoryId) {
        // The category is in use when a provisioning criteria definition references it. The original code queried the
        // non-existent m_loanproduct_provisioning_details table; the category_id column actually lives on
        // m_provisioning_criteria_definition. EXISTS short-circuits at the first match instead of counting every row,
        // and maps cleanly to Boolean across PostgreSQL (native bool) and MySQL/MariaDB (BIGINT 1/0).
        final String sql = """
                select exists (
                    select 1
                    from m_provisioning_criteria_definition
                    where category_id = ?
                )
                """;
        final Boolean exists = this.jdbcTemplate.queryForObject(sql, Boolean.class, categoryId);
        return Boolean.TRUE.equals(exists);
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("category_name")) {
            final String name = command.stringValueOfParameterNamed("category_name");
            throw new PlatformDataIntegrityException("error.msg.provisioning.duplicate.categoryname",
                    "Provisioning Cateory with name `" + name + "` already exists", "category name", name);
        }
        log.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
