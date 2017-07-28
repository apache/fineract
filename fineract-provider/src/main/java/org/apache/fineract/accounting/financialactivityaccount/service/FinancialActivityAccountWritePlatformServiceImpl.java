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
package org.apache.fineract.accounting.financialactivityaccount.service;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import org.apache.fineract.accounting.financialactivityaccount.api.FinancialActivityAccountsJsonInputParams;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.financialactivityaccount.exception.DuplicateFinancialActivityAccountFoundException;
import org.apache.fineract.accounting.financialactivityaccount.exception.FinancialActivityAccountInvalidException;
import org.apache.fineract.accounting.financialactivityaccount.serialization.FinancialActivityAccountDataValidator;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class FinancialActivityAccountWritePlatformServiceImpl implements FinancialActivityAccountWritePlatformService {

    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository;
    private final FinancialActivityAccountDataValidator fromApiJsonDeserializer;
    private final GLAccountRepositoryWrapper glAccountRepositoryWrapper;
    private final static Logger logger = LoggerFactory.getLogger(FinancialActivityAccountWritePlatformServiceImpl.class);

    @Autowired
    public FinancialActivityAccountWritePlatformServiceImpl(
            final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository,
            final FinancialActivityAccountDataValidator fromApiJsonDeserializer, final GLAccountRepositoryWrapper glAccountRepositoryWrapper) {
        this.financialActivityAccountRepository = financialActivityAccountRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.glAccountRepositoryWrapper = glAccountRepositoryWrapper;
    }

    @Override
    public CommandProcessingResult createFinancialActivityAccountMapping(JsonCommand command) {
        try {

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Integer financialActivityId = command
                    .integerValueSansLocaleOfParameterNamed(FinancialActivityAccountsJsonInputParams.FINANCIAL_ACTIVITY_ID.getValue());
            final Long accountId = command.longValueOfParameterNamed(FinancialActivityAccountsJsonInputParams.GL_ACCOUNT_ID.getValue());
            final GLAccount glAccount = glAccountRepositoryWrapper.findOneWithNotFoundDetection(accountId);
            FinancialActivityAccount financialActivityAccount = FinancialActivityAccount.createNew(glAccount, financialActivityId);

            validateFinancialActivityAndAccountMapping(financialActivityAccount);
            this.financialActivityAccountRepository.save(financialActivityAccount);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(financialActivityAccount.getId()) //
                    .build();
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            handleFinancialActivityAccountDataIntegrityIssues(command, dataIntegrityViolationException.getMostSpecificCause(), dataIntegrityViolationException);
            return CommandProcessingResult.empty();
        }catch(final PersistenceException ee) {
        	Throwable throwable = ExceptionUtils.getRootCause(ee.getCause()) ;
        	handleFinancialActivityAccountDataIntegrityIssues(command, throwable, ee);
            return CommandProcessingResult.empty();
        }
    }

    /**
     * Validate that the GL Account is appropriate for the particular Financial
     * Activity Type
     **/
    private void validateFinancialActivityAndAccountMapping(FinancialActivityAccount financialActivityAccount) {
        FINANCIAL_ACTIVITY financialActivity = FINANCIAL_ACTIVITY.fromInt(financialActivityAccount.getFinancialActivityType());
        GLAccount glAccount = financialActivityAccount.getGlAccount();
        if (!financialActivity.getMappedGLAccountType().getValue().equals(glAccount.getType())) { throw new FinancialActivityAccountInvalidException(
                financialActivity, glAccount); }
    }

    @Override
    public CommandProcessingResult updateGLAccountActivityMapping(Long financialActivityAccountId, JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            final FinancialActivityAccount financialActivityAccount = this.financialActivityAccountRepository
                    .findOneWithNotFoundDetection(financialActivityAccountId);
            Map<String, Object> changes = findChanges(command, financialActivityAccount);

            if (changes.containsKey(FinancialActivityAccountsJsonInputParams.GL_ACCOUNT_ID.getValue())) {
                final Long accountId = command.longValueOfParameterNamed(FinancialActivityAccountsJsonInputParams.GL_ACCOUNT_ID.getValue());
                final GLAccount glAccount = glAccountRepositoryWrapper.findOneWithNotFoundDetection(accountId);
                financialActivityAccount.updateGlAccount(glAccount);
            }

            if (changes.containsKey(FinancialActivityAccountsJsonInputParams.FINANCIAL_ACTIVITY_ID.getValue())) {
                final Integer financialActivityId = command
                        .integerValueSansLocaleOfParameterNamed(FinancialActivityAccountsJsonInputParams.FINANCIAL_ACTIVITY_ID.getValue());
                financialActivityAccount.updateFinancialActivityType(financialActivityId);
            }

            if (!changes.isEmpty()) {
                validateFinancialActivityAndAccountMapping(financialActivityAccount);
                this.financialActivityAccountRepository.save(financialActivityAccount);
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(financialActivityAccountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            handleFinancialActivityAccountDataIntegrityIssues(command, dataIntegrityViolationException.getMostSpecificCause(), dataIntegrityViolationException);
            return CommandProcessingResult.empty();
        }catch(final PersistenceException ee) {
        	Throwable throwable = ExceptionUtils.getRootCause(ee.getCause()) ;
        	handleFinancialActivityAccountDataIntegrityIssues(command, throwable, ee);
        	return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult deleteGLAccountActivityMapping(Long financialActivityAccountId, JsonCommand command) {
        final FinancialActivityAccount financialActivityAccount = this.financialActivityAccountRepository
                .findOneWithNotFoundDetection(financialActivityAccountId);
        this.financialActivityAccountRepository.delete(financialActivityAccount);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(financialActivityAccountId) //
                .build();
    }

    private void handleFinancialActivityAccountDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("financial_activity_type")) {
            final Integer financialActivityId = command
                    .integerValueSansLocaleOfParameterNamed(FinancialActivityAccountsJsonInputParams.FINANCIAL_ACTIVITY_ID.getValue());
            throw new DuplicateFinancialActivityAccountFoundException(financialActivityId);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.glAccount.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource GL Account: " + realCause.getMessage());
    }

    public Map<String, Object> findChanges(JsonCommand command, FinancialActivityAccount financialActivityAccount) {

        Map<String, Object> changes = new HashMap<>();

        Long existingGLAccountId = financialActivityAccount.getGlAccount().getId();
        Integer financialActivityType = financialActivityAccount.getFinancialActivityType();

        // is the account Id changed?
        if (command.isChangeInLongParameterNamed(FinancialActivityAccountsJsonInputParams.GL_ACCOUNT_ID.getValue(), existingGLAccountId)) {
            final Long newValue = command.longValueOfParameterNamed(FinancialActivityAccountsJsonInputParams.GL_ACCOUNT_ID.getValue());
            changes.put(FinancialActivityAccountsJsonInputParams.GL_ACCOUNT_ID.getValue(), newValue);
        }

        // is the financial Activity changed
        if (command.isChangeInIntegerSansLocaleParameterNamed(FinancialActivityAccountsJsonInputParams.FINANCIAL_ACTIVITY_ID.getValue(),
                financialActivityType)) {
            final Integer newValue = command
                    .integerValueSansLocaleOfParameterNamed(FinancialActivityAccountsJsonInputParams.FINANCIAL_ACTIVITY_ID.getValue());
            changes.put(FinancialActivityAccountsJsonInputParams.FINANCIAL_ACTIVITY_ID.getValue(), newValue);
        }
        return changes;
    }
}
