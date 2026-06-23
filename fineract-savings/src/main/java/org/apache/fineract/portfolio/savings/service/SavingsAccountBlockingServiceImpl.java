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
package org.apache.fineract.portfolio.savings.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountSubStatusEnum;

/**
 * Default implementation of {@link SavingsAccountBlockingService}. The method bodies were extracted from
 * {@code SavingsAccount}; behaviour is intentionally unchanged.
 */
public class SavingsAccountBlockingServiceImpl implements SavingsAccountBlockingService {

    @Override
    public Map<String, Object> block(final SavingsAccount account) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(account.depositAccountType().resourceName() + SavingsApiConstants.blockAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        account.setSubStatus(SavingsAccountSubStatusEnum.BLOCK.getValue());
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(account.getSubStatus()));

        return actualChanges;
    }

    @Override
    public Map<String, Object> unblock(final SavingsAccount account) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(account.depositAccountType().resourceName() + SavingsApiConstants.unblockAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);

        }

        final SavingsAccountSubStatusEnum currentSubStatus = SavingsAccountSubStatusEnum.fromInt(account.getSubStatus());
        if (!SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.subStatusParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.blocked.state");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        account.setSubStatus(SavingsAccountSubStatusEnum.NONE.getValue());
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(account.getSubStatus()));
        return actualChanges;
    }

    @Override
    public Map<String, Object> blockCredits(final SavingsAccount account, final Integer currentSubstatus) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(account.depositAccountType().resourceName() + SavingsApiConstants.blockCreditsAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);
        }
        if (SavingsAccountSubStatusEnum.BLOCK.hasStateOf(SavingsAccountSubStatusEnum.fromInt(currentSubstatus))) {

            baseDataValidator.reset().parameter(SavingsApiConstants.subStatusParamName)
                    .value(SavingsAccountSubStatusEnum.fromInt(currentSubstatus)).failWithCodeNoParameterAddedToErrorCode("currently.set");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (SavingsAccountSubStatusEnum.BLOCK_DEBIT.hasStateOf(SavingsAccountSubStatusEnum.fromInt(currentSubstatus))) {
            account.setSubStatus(SavingsAccountSubStatusEnum.BLOCK.getValue());
        } else {
            account.setSubStatus(SavingsAccountSubStatusEnum.BLOCK_CREDIT.getValue());
        }
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(account.getSubStatus()));

        return actualChanges;
    }

    @Override
    public Map<String, Object> unblockCredits(final SavingsAccount account) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(account.depositAccountType().resourceName() + SavingsApiConstants.unblockCreditsAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);
        }

        final SavingsAccountSubStatusEnum currentSubStatus = SavingsAccountSubStatusEnum.fromInt(account.getSubStatus());
        if (!(SavingsAccountSubStatusEnum.BLOCK_CREDIT.hasStateOf(currentSubStatus)
                || SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus))) {
            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode("credits.are.not.blocked");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus)) {
            account.setSubStatus(SavingsAccountSubStatusEnum.BLOCK_DEBIT.getValue());
        } else {
            account.setSubStatus(SavingsAccountSubStatusEnum.NONE.getValue());
        }
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(account.getSubStatus()));
        return actualChanges;
    }

    @Override
    public Map<String, Object> blockDebits(final SavingsAccount account, final Integer currentSubstatus) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(account.depositAccountType().resourceName() + SavingsApiConstants.blockDebitsAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);

        }
        if (SavingsAccountSubStatusEnum.BLOCK.hasStateOf(SavingsAccountSubStatusEnum.fromInt(currentSubstatus))) {

            baseDataValidator.reset().parameter(SavingsApiConstants.subStatusParamName)
                    .value(SavingsAccountSubStatusEnum.fromInt(currentSubstatus)).failWithCodeNoParameterAddedToErrorCode("currently.set");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (SavingsAccountSubStatusEnum.BLOCK_CREDIT.hasStateOf(SavingsAccountSubStatusEnum.fromInt(currentSubstatus))) {
            account.setSubStatus(SavingsAccountSubStatusEnum.BLOCK.getValue());
        } else {
            account.setSubStatus(SavingsAccountSubStatusEnum.BLOCK_DEBIT.getValue());
        }
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(account.getSubStatus()));

        return actualChanges;
    }

    @Override
    public Map<String, Object> unblockDebits(final SavingsAccount account) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(account.depositAccountType().resourceName() + SavingsApiConstants.unblockDebitsAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);

        }

        final SavingsAccountSubStatusEnum currentSubStatus = SavingsAccountSubStatusEnum.fromInt(account.getSubStatus());
        if (!(SavingsAccountSubStatusEnum.BLOCK_DEBIT.hasStateOf(currentSubStatus)
                || SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus))) {
            baseDataValidator.reset().parameter(SavingsApiConstants.subStatusParamName)
                    .failWithCodeNoParameterAddedToErrorCode("debits.are.not.blocked");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus)) {
            account.setSubStatus(SavingsAccountSubStatusEnum.BLOCK_CREDIT.getValue());
        } else {
            account.setSubStatus(SavingsAccountSubStatusEnum.NONE.getValue());
        }
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(account.getSubStatus()));
        return actualChanges;
    }
}
