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
package org.apache.fineract.adhocquery.service;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.adhocquery.api.AdHocJsonInputParams;
import org.apache.fineract.adhocquery.domain.AdHoc;
import org.apache.fineract.adhocquery.domain.AdHocRepository;
import org.apache.fineract.adhocquery.exception.AdHocNotFoundException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class AdHocWritePlatformServiceJpaRepositoryImpl implements AdHocWritePlatformService {

    private final PlatformSecurityContext context;
    private final AdHocRepository adHocRepository;
    private final AdHocDataValidator adHocCommandFromApiJsonDeserializer;
    private final SqlValidator sqlValidator;

    @Transactional
    @Override
    public CommandProcessingResult createAdHocQuery(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.adHocCommandFromApiJsonDeserializer.validateForCreate(command.json());

            String commandQuery = command.stringValueOfParameterNamed(AdHocJsonInputParams.QUERY.getValue());

            sqlValidator.validate("adhoc", commandQuery);

            final AdHoc entity = newAdHoc(command);
            this.adHocRepository.saveAndFlush(entity);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(entity.getId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleDataIntegrityIssues(command, throwable, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }
    }

    private static AdHoc newAdHoc(final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed(AdHocJsonInputParams.NAME.getValue());

        final String query = command.stringValueOfParameterNamed(AdHocJsonInputParams.QUERY.getValue());
        final String tableName = command.stringValueOfParameterNamed(AdHocJsonInputParams.TABLENAME.getValue());
        final String tableFields = command.stringValueOfParameterNamed(AdHocJsonInputParams.TABLEFIELDS.getValue());
        final String email = command.stringValueOfParameterNamed(AdHocJsonInputParams.EMAIL.getValue());
        final Long reportRunFrequency = command.longValueOfParameterNamed(AdHocJsonInputParams.REPORT_RUN_FREQUENCY.getValue());
        final Long reportRunEvery = command.longValueOfParameterNamed(AdHocJsonInputParams.REPORT_RUN_EVERY.getValue());
        final boolean active = command.booleanPrimitiveValueOfParameterNamed(AdHocJsonInputParams.ACTIVE.getValue());
        return new AdHoc().setName(StringUtils.defaultIfEmpty(name, null)).setQuery(StringUtils.defaultIfEmpty(query, null))
                .setTableName(StringUtils.defaultIfEmpty(tableName, null)).setTableFields(StringUtils.defaultIfEmpty(tableFields, null))
                .setEmail(StringUtils.defaultIfEmpty(email, null)).setReportRunFrequency(reportRunFrequency)
                .setReportRunEvery(reportRunEvery).setActive(active);
    }

    private static Map<String, Object> applyChanges(final AdHoc adHoc, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, adHoc.getName())) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            adHoc.setName(newValue);
        }

        final String descriptionParamName = "query";
        if (command.isChangeInStringParameterNamed(descriptionParamName, adHoc.getQuery())) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            adHoc.setQuery(newValue);
        }
        final String tableName = "tableName";
        if (command.isChangeInStringParameterNamed(tableName, adHoc.getTableName())) {
            final String newValue = command.stringValueOfParameterNamed(tableName);
            actualChanges.put(tableName, newValue);
            adHoc.setTableName(newValue);
        }
        final String tableField = "tableFields";
        if (command.isChangeInStringParameterNamed(tableField, adHoc.getTableFields())) {
            final String newValue = command.stringValueOfParameterNamed(tableField);
            actualChanges.put(tableField, newValue);
            adHoc.setTableFields(newValue);
        }
        final String email = "email";
        if (command.isChangeInStringParameterNamed(email, adHoc.getEmail())) {
            final String newValue = command.stringValueOfParameterNamed(email);
            actualChanges.put(email, newValue);
            adHoc.setEmail(newValue);
        }
        final String reportRunFrequency = "reportRunFrequency";
        if (command.isChangeInLongParameterNamed(reportRunFrequency, adHoc.getReportRunFrequency())) {
            final Long newValue = command.longValueOfParameterNamed(reportRunFrequency);
            actualChanges.put(reportRunFrequency, newValue);
            adHoc.setReportRunFrequency(newValue);
        }
        final String reportRunEvery = "reportRunEvery";
        if (command.isChangeInLongParameterNamed(reportRunEvery, adHoc.getReportRunEvery())) {
            final Long newValue = command.longValueOfParameterNamed(reportRunEvery);
            actualChanges.put(reportRunEvery, newValue);
            adHoc.setReportRunEvery(newValue);
        }
        final String paramisActive = AdHocJsonInputParams.ACTIVE.getValue();
        if (command.isChangeInBooleanParameterNamed(paramisActive, adHoc.isActive())) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(paramisActive);
            actualChanges.put(paramisActive, newValue);
            adHoc.setActive(Boolean.TRUE.equals(newValue));
        }
        return actualChanges;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause,
            final NonTransientDataAccessException dve) {
        if (realCause.getMessage().contains("unq_name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.adhocquery.duplicate.name",
                    "AdHocQuery with name `" + name + "` already exists", "name", name);
        }
        log.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.adhocquery.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult updateAdHocQuery(final Long adHocId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.adHocCommandFromApiJsonDeserializer.validateForUpdate(command.json());

            final AdHoc adHoc = this.adHocRepository.findById(adHocId).orElseThrow(() -> new AdHocNotFoundException(adHocId));

            final Map<String, Object> changes = applyChanges(adHoc, command);
            if (!changes.isEmpty()) {
                this.adHocRepository.saveAndFlush(adHoc);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(adHocId) //
                    .with(changes) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleDataIntegrityIssues(command, throwable, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }
    }

    /**
     * Method for Delete adhoc
     */
    @Transactional
    @Override
    public CommandProcessingResult deleteAdHocQuery(Long adHocId) {

        try {
            /**
             * Checking the adhocQuery present in DB or not using adHocId
             */
            final AdHoc adHoc = this.adHocRepository.findById(adHocId).orElseThrow(() -> new AdHocNotFoundException(adHocId));

            this.adHocRepository.delete(adHoc);
            return new CommandProcessingResultBuilder() //
                    .withEntityId(adHocId) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            throw ErrorHandler.getMappable(e, "error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
    }

    /**
     * Method for disabling the adhocquery
     */
    @Transactional
    @Override
    public CommandProcessingResult disableAdHocQuery(Long adHocId) {
        try {
            /**
             * Checking the adhocquery present in DB or not using adHocId
             */
            final AdHoc adHoc = this.adHocRepository.findById(adHocId).orElseThrow(() -> new AdHocNotFoundException(adHocId));
            adHoc.setActive(true);
            this.adHocRepository.save(adHoc);
            return new CommandProcessingResultBuilder() //
                    .withEntityId(adHocId) //
                    .build();

        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            throw ErrorHandler.getMappable(e, "error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
    }

    /**
     * Method for Enabling the Active
     */
    @Transactional
    @Override
    public CommandProcessingResult enableAdHocQuery(Long adHocId) {
        try {
            /**
             * Checking the adHoc present in DB or not using id
             */
            final AdHoc adHoc = this.adHocRepository.findById(adHocId).orElseThrow(() -> new AdHocNotFoundException(adHocId));
            adHoc.setActive(false);
            this.adHocRepository.save(adHoc);
            return new CommandProcessingResultBuilder() //
                    .withEntityId(adHocId) //
                    .build();

        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            throw ErrorHandler.getMappable(e, "error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
    }
}
