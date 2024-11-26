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
package org.apache.fineract.infrastructure.configuration.domain;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.cache.domain.CacheType;
import org.apache.fineract.infrastructure.cache.domain.PlatformCache;
import org.apache.fineract.infrastructure.cache.domain.PlatformCacheRepository;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.useradministration.domain.Permission;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.apache.fineract.useradministration.exception.PermissionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationDomainServiceJpa implements ConfigurationDomainService {

    private final PermissionRepository permissionRepository;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final PlatformCacheRepository cacheTypeRepository;

    @Override
    public boolean isMakerCheckerEnabledForTask(final String taskPermissionCode) {
        if (StringUtils.isBlank(taskPermissionCode)) {
            throw new PermissionNotFoundException(taskPermissionCode);
        }
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(GlobalConfigurationConstants.MAKER_CHECKER);
        if (property.isEnabled()) {
            final Permission thisTask = this.permissionRepository.findOneByCode(taskPermissionCode);
            if (thisTask == null) {
                throw new PermissionNotFoundException(taskPermissionCode);
            }

            return thisTask.hasMakerCheckerEnabled();
        }
        return false;
    }

    @Override
    public boolean isSameMakerCheckerEnabled() {
        return getGlobalConfigurationPropertyData(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER).isEnabled();
    }

    @Override
    public boolean isAmazonS3Enabled() {
        return getGlobalConfigurationPropertyData(GlobalConfigurationConstants.AMAZON_S3).isEnabled();
    }

    @Override
    public boolean isRescheduleFutureRepaymentsEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.RESCHEDULE_FUTURE_REPAYMENTS);
        return property.isEnabled();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.fineract.infrastructure.configuration.domain. ConfigurationDomainService#isHolidaysEnabled()
     */
    @Override
    public boolean isRescheduleRepaymentsOnHolidaysEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS);
        return property.isEnabled();
    }

    @Override
    public boolean allowTransactionsOnHolidayEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ALLOW_TRANSACTIONS_ON_HOLIDAY);
        return property.isEnabled();
    }

    @Override
    public boolean allowTransactionsOnNonWorkingDayEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ALLOW_TRANSACTIONS_ON_NON_WORKING_DAY);
        return property.isEnabled();
    }

    @Override
    public boolean isConstraintApproachEnabledForDatatables() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.CONSTRAINT_APPROACH_FOR_DATATABLES);
        return property.isEnabled();
    }

    @Override
    public boolean isEhcacheEnabled() {
        return this.cacheTypeRepository.findById(1L).map(PlatformCache::isEhcacheEnabled).orElseThrow();
    }

    @Transactional
    @Override
    public void updateCache(final CacheType cacheType) {
        this.cacheTypeRepository.findById(1L).ifPresent(cache -> {
            cache.setCacheType(cacheType.getValue());
            this.cacheTypeRepository.save(cache);
        });
    }

    @Override
    public Long retrievePenaltyWaitPeriod() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.PENALTY_WAIT_PERIOD);
        return property.getValue();
    }

    @Override
    public Long retrieveGraceOnPenaltyPostingPeriod() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.GRACE_ON_PENALTY_POSTING);
        return property.getValue();
    }

    @Override
    public boolean isPasswordForcedResetEnable() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.FORCE_PASSWORD_RESET_DAYS);
        return property.isEnabled();
    }

    @Override
    public Long retrievePasswordLiveTime() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.FORCE_PASSWORD_RESET_DAYS);
        return property.getValue();
    }

    @Override
    public Long retrieveOpeningBalancesContraAccount() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.OFFICE_OPENING_BALANCES_CONTRA_ACCOUNT);
        return property.getValue();
    }

    @Override
    public boolean isSavingsInterestPostingAtCurrentPeriodEnd() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.SAVINGS_INTEREST_POSTING_CURRENT_PERIOD_END);
        return property.isEnabled();
    }

    @Override
    public Integer retrieveFinancialYearBeginningMonth() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.FINANCIAL_YEAR_BEGINNING_MONTH);
        if (property.isEnabled()) {
            return property.getValue().intValue();
        }
        return 1;
    }

    @Override
    public Integer retrieveMinAllowedClientsInGroup() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.MIN_CLIENTS_IN_GROUP);
        if (property.isEnabled()) {
            return property.getValue().intValue();
        }
        return null;
    }

    @Override
    public Integer retrieveMaxAllowedClientsInGroup() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.MAX_CLIENTS_IN_GROUP);
        if (property.isEnabled()) {
            return property.getValue().intValue();
        }
        return null;
    }

    @Override
    public boolean isMeetingMandatoryForJLGLoans() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.MEETINGS_MANDATORY_FOR_JLG_LOANS);
        return property.isEnabled();
    }

    @Override
    public int getRoundingMode() {
        int defaultValue = 6; // 6 Stands for HALF-EVEN
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(GlobalConfigurationConstants.ROUNDING_MODE);
        if (property.isEnabled()) {
            int value = property.getValue().intValue();
            if (value < 0 || value > 6) {
                return defaultValue;
            }
            return value;
        }
        return defaultValue;
    }

    @Override
    public boolean isBackdatePenaltiesEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.BACKDATE_PENALTIES_ENABLED);
        return property.isEnabled();
    }

    @Override
    public boolean isOrganisationstartDateEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ORGANISATION_START_DATE);
        return property.isEnabled();
    }

    @Override
    public LocalDate retrieveOrganisationStartDate() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ORGANISATION_START_DATE);
        return property.getDateValue();
    }

    @Override
    public boolean isPaymentTypeApplicableForDisbursementCharge() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.PAYMENT_TYPE_APPLICABLE_FOR_DISBURSEMENT_CHARGES);
        return property.isEnabled();
    }

    @Override
    public boolean isSkippingMeetingOnFirstDayOfMonthEnabled() {
        return getGlobalConfigurationPropertyData(GlobalConfigurationConstants.SKIP_REPAYMENT_ON_FIRST_DAY_OF_MONTH).isEnabled();
    }

    @Override
    public boolean isFirstRepaymentDateAfterRescheduleAllowedOnHoliday() {
        return getGlobalConfigurationPropertyData(GlobalConfigurationConstants.LOAN_RESCHEDULE_IS_FIRST_PAYDAY_ALLOWED_ON_HOLIDAY)
                .isEnabled();
    }

    @Override
    public boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI() {
        return getGlobalConfigurationPropertyData(GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI)
                .isEnabled();
    }

    @Override
    public boolean isPrincipalCompoundingDisabledForOverdueLoans() {
        return getGlobalConfigurationPropertyData(GlobalConfigurationConstants.IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS)
                .isEnabled();
    }

    @Override
    public Long retreivePeriodInNumberOfDaysForSkipMeetingDate() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.SKIP_REPAYMENT_ON_FIRST_DAY_OF_MONTH);
        return property.getValue();

    }

    @Override
    public boolean isInterestChargedFromDateSameAsDisbursementDate() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.INTEREST_CHARGED_FROM_DATE_SAME_AS_DISBURSAL_DATE);
        return property.isEnabled();
    }

    @Override
    public boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.CHANGE_EMI_IF_REPAYMENT_DATE_SAME_AS_DISBURSEMENT_DATE);
        return property.isEnabled();
    }

    @Override
    public boolean isDailyTPTLimitEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(GlobalConfigurationConstants.DAILY_TPT_LIMIT);
        return property.isEnabled();
    }

    @Override
    public Long getDailyTPTLimit() {
        final String propertyName = "daily-tpt-limit";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();
    }

    @Override
    public void removeGlobalConfigurationPropertyDataFromCache(final String propertyName) {
        globalConfigurationRepository.removeFromCache(propertyName);
    }

    @Override
    public boolean isSMSOTPDeliveryEnabled() {
        final String propertyName = "use-sms-for-2fa";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public boolean isEmailOTPDeliveryEnabled() {
        final String propertyName = "use-email-for-2fa";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public Integer retrieveOTPCharacterLength() {
        final String propertyName = "otp-character-length";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        int defaultValue = 6;
        int value = property.getValue().intValue();
        if (value < 1) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public Integer retrieveOTPLiveTime() {
        final String propertyName = "otp-validity-period";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        int defaultValue = 300;
        int value = property.getValue().intValue();
        if (value < 1) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public boolean retrievePivotDateConfig() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING);
        return !property.isEnabled();

    }

    @Override
    public boolean isRelaxingDaysConfigForPivotDateEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING_DATE_FOR_DAYS);
        return property.isEnabled();
    }

    @Override
    public Long retrieveRelaxingDaysConfigForPivotDate() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING_DATE_FOR_DAYS);
        if (property.getValue() == null) {
            return 0L;
        }
        return property.getValue();
    }

    @NotNull
    private GlobalConfigurationPropertyData getGlobalConfigurationPropertyData(final String propertyName) {
        return globalConfigurationRepository.findOneByNameWithNotFoundDetection(propertyName).toData();
    }

    @Override
    public boolean isSubRatesEnabled() {
        return getGlobalConfigurationPropertyData(GlobalConfigurationConstants.SUB_RATES).isEnabled();
    }

    @Override
    public String getAccountMappingForPaymentType() {
        String defaultValue = "Asset"; // 1 Stands for Account mapped from asset only
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ACCOUNT_MAPPING_FOR_PAYMENT_TYPE);
        if (property.isEnabled()) {
            String value = property.getStringValue();
            if (StringUtils.isBlank(value)) {
                return defaultValue;
            }
            return value;
        }
        return defaultValue;
    }

    @Override
    public String getAccountMappingForCharge() {
        String defaultValue = "Income"; // 1 Stands for Account mapped from income only
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ACCOUNT_MAPPING_FOR_CHARGE);
        if (property.isEnabled()) {
            String value = property.getStringValue();
            if (StringUtils.isBlank(value)) {
                return defaultValue;
            }
            return value;
        }
        return defaultValue;
    }

    @Override
    public boolean isNextDayFixedDepositInterestTransferEnabledForPeriodEnd() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.FIXED_DEPOSIT_TRANSFER_INTEREST_NEXT_DAY_FOR_PERIOD_END_POSTING);
        return property.isEnabled();
    }

    @Override
    public boolean isBusinessDateEnabled() {
        return getGlobalConfigurationPropertyData(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE).isEnabled();
    }

    @Override
    public boolean isCOBDateAdjustmentEnabled() {
        return getGlobalConfigurationPropertyData(GlobalConfigurationConstants.ENABLE_AUTOMATIC_COB_DATE_ADJUSTMENT).isEnabled();
    }

    @Override
    public boolean isReversalTransactionAllowed() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ENABLE_POST_REVERSAL_TXNS_FOR_REVERSE_TRANSACTIONS);
        return property.isEnabled();
    }

    @Override
    public Long retrieveExternalEventsPurgeDaysCriteria() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.PURGE_EXTERNAL_EVENTS_OLDER_THAN_DAYS);
        return property.getValue();

    }

    @Override
    public Long retrieveProcessedCommandsPurgeDaysCriteria() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.PURGE_PROCESSED_COMMANDS_OLDER_THAN_DAYS);
        return property.getValue();

    }

    @Override
    public Long retrieveRepaymentDueDays() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.DAYS_BEFORE_REPAYMENT_IS_DUE);
        return property.getValue();
    }

    @Override
    public Long retrieveRepaymentOverdueDays() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.DAYS_AFTER_REPAYMENT_IS_OVERDUE);
        return property.getValue();
    }

    @Override
    public boolean isExternalIdAutoGenerationEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID);
        return property.isEnabled();
    }

    @Override
    public boolean isAddressEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(GlobalConfigurationConstants.ENABLE_ADDRESS);
        return property.isEnabled();
    }

    @Override
    public boolean isCOBBulkEventEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.ENABLE_COB_BULK_EVENT);
        return property.isEnabled();
    }

    @Override
    public Long retrieveExternalEventBatchSize() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.EXTERNAL_EVENT_BATCH_SIZE);
        return property.getValue();
    }

    @Override
    public String retrieveReportExportS3FolderName() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.REPORT_EXPORT_S3_FOLDER_NAME);
        return property.getStringValue();
    }

    @Override
    public String getAccrualDateConfigForCharge() {
        String defaultValue = "due-date";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE);
        String value = property.getStringValue();
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public String getNextPaymentDateConfigForLoan() {
        String defaultValue = "earliest-unpaid-date";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(
                GlobalConfigurationConstants.NEXT_PAYMENT_DUE_DATE);
        String value = property.getStringValue();
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public boolean isImmediateChargeAccrualPostMaturityEnabled() {
        return getGlobalConfigurationPropertyData(GlobalConfigurationConstants.ENABLE_IMMEDIATE_CHARGE_ACCRUAL_POST_MATURITY).isEnabled();
    }
}
