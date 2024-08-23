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

    public static final String ENABLE_BUSINESS_DATE = "enable_business_date";
    public static final String ENABLE_AUTOMATIC_COB_DATE_ADJUSTMENT = "enable_automatic_cob_date_adjustment";
    public static final String EXTERNAL_EVENTS_PURGE_DAYS = "purge-external-events-older-than-days";

    public static final String PROCESSED_COMMANDS_PURGE_DAYS = "purge-processed-commands-older-than-days";
    private static final String DAYS_BEFORE_REPAYMENT_IS_DUE = "days-before-repayment-is-due";
    private static final String DAYS_AFTER_REPAYMENT_IS_OVERDUE = "days-after-repayment-is-overdue";
    private static final String ENABLE_EXTERNAL_ID_AUTO_GENERATION = "enable-auto-generated-external-id";
    private static final String ENABLE_ADDRESS = "Enable-Address";
    private static final String ENABLE_COB_BULK_EVENT = "enable-cob-bulk-event";
    private static final String EXTERNAL_EVENT_BATCH_SIZE = "external-event-batch-size";

    private static final String REPORT_EXPORT_S3_FOLDER_NAME = "report-export-s3-folder-name";

    public static final String CHARGE_ACCRUAL_DATE_CRITERIA = "charge-accrual-date";
    public static final String NEXT_PAYMENT_DUE_DATE = "next-payment-due-date";

    private final PermissionRepository permissionRepository;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final PlatformCacheRepository cacheTypeRepository;

    @Override
    public boolean isMakerCheckerEnabledForTask(final String taskPermissionCode) {
        if (StringUtils.isBlank(taskPermissionCode)) {
            throw new PermissionNotFoundException(taskPermissionCode);
        }
        final String makerCheckerConfigurationProperty = "maker-checker";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(makerCheckerConfigurationProperty);
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
        return getGlobalConfigurationPropertyData("enable-same-maker-checker").isEnabled();
    }

    @Override
    public boolean isAmazonS3Enabled() {
        return getGlobalConfigurationPropertyData("amazon-S3").isEnabled();
    }

    @Override
    public boolean isRescheduleFutureRepaymentsEnabled() {
        final String rescheduleRepaymentsConfigurationProperty = "reschedule-future-repayments";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(rescheduleRepaymentsConfigurationProperty);
        return property.isEnabled();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.fineract.infrastructure.configuration.domain. ConfigurationDomainService#isHolidaysEnabled()
     */
    @Override
    public boolean isRescheduleRepaymentsOnHolidaysEnabled() {
        final String holidaysConfigurationProperty = "reschedule-repayments-on-holidays";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(holidaysConfigurationProperty);
        return property.isEnabled();
    }

    @Override
    public boolean allowTransactionsOnHolidayEnabled() {
        final String allowTransactionsOnHolidayProperty = "allow-transactions-on-holiday";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(allowTransactionsOnHolidayProperty);
        return property.isEnabled();
    }

    @Override
    public boolean allowTransactionsOnNonWorkingDayEnabled() {
        final String propertyName = "allow-transactions-on-non_workingday";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public boolean isConstraintApproachEnabledForDatatables() {
        final String propertyName = "constraint_approach_for_datatables";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
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
        final String propertyName = "penalty-wait-period";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();
    }

    @Override
    public Long retrieveGraceOnPenaltyPostingPeriod() {
        final String propertyName = "grace-on-penalty-posting";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();
    }

    @Override
    public boolean isPasswordForcedResetEnable() {
        final String propertyName = "force-password-reset-days";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public Long retrievePasswordLiveTime() {
        final String propertyName = "force-password-reset-days";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();
    }

    @Override
    public Long retrieveOpeningBalancesContraAccount() {
        final String propertyName = "office-opening-balances-contra-account";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();
    }

    @Override
    public boolean isSavingsInterestPostingAtCurrentPeriodEnd() {
        final String propertyName = "savings-interest-posting-current-period-end";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public Integer retrieveFinancialYearBeginningMonth() {
        final String propertyName = "financial-year-beginning-month";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        if (property.isEnabled()) {
            return property.getValue().intValue();
        }
        return 1;
    }

    @Override
    public Integer retrieveMinAllowedClientsInGroup() {
        final String propertyName = "min-clients-in-group";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        if (property.isEnabled()) {
            return property.getValue().intValue();
        }
        return null;
    }

    @Override
    public Integer retrieveMaxAllowedClientsInGroup() {
        final String propertyName = "max-clients-in-group";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        if (property.isEnabled()) {
            return property.getValue().intValue();
        }
        return null;
    }

    @Override
    public boolean isMeetingMandatoryForJLGLoans() {
        final String propertyName = "meetings-mandatory-for-jlg-loans";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public int getRoundingMode() {
        final String propertyName = "rounding-mode";
        int defaultValue = 6; // 6 Stands for HALF-EVEN
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
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
        final String propertyName = "backdate-penalties-enabled";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public boolean isOrganisationstartDateEnabled() {
        final String propertyName = "organisation-start-date";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public LocalDate retrieveOrganisationStartDate() {
        final String propertyName = "organisation-start-date";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getDateValue();
    }

    @Override
    public boolean isPaymentTypeApplicableForDisbursementCharge() {
        final String propertyName = "paymenttype-applicable-for-disbursement-charges";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public boolean isSkippingMeetingOnFirstDayOfMonthEnabled() {
        return getGlobalConfigurationPropertyData("skip-repayment-on-first-day-of-month").isEnabled();
    }

    @Override
    public boolean isFirstRepaymentDateAfterRescheduleAllowedOnHoliday() {
        return getGlobalConfigurationPropertyData("loan-reschedule-is-first-payday-allowed-on-holiday").isEnabled();
    }

    @Override
    public boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI() {
        return getGlobalConfigurationPropertyData("is-interest-to-be-recovered-first-when-greater-than-emi").isEnabled();
    }

    @Override
    public boolean isPrincipalCompoundingDisabledForOverdueLoans() {
        return getGlobalConfigurationPropertyData("is-principal-compounding-disabled-for-overdue-loans").isEnabled();
    }

    @Override
    public Long retreivePeriodInNumberOfDaysForSkipMeetingDate() {
        final String propertyName = "skip-repayment-on-first-day-of-month";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();

    }

    @Override
    public boolean isInterestChargedFromDateSameAsDisbursementDate() {
        final String propertyName = "interest-charged-from-date-same-as-disbursal-date";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled() {
        final String propertyName = "change-emi-if-repaymentdate-same-as-disbursementdate";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public boolean isDailyTPTLimitEnabled() {
        final String propertyName = "daily-tpt-limit";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
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
        final String propertyName = "allow-backdated-transaction-before-interest-posting";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return !property.isEnabled();

    }

    @Override
    public boolean isRelaxingDaysConfigForPivotDateEnabled() {
        final String propertyName = "allow-backdated-transaction-before-interest-posting-date-for-days";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public Long retrieveRelaxingDaysConfigForPivotDate() {
        final String propertyName = "allow-backdated-transaction-before-interest-posting-date-for-days";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
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
        return getGlobalConfigurationPropertyData("sub-rates").isEnabled();
    }

    @Override
    public String getAccountMappingForPaymentType() {
        final String propertyName = "account-mapping-for-payment-type";
        String defaultValue = "Asset"; // 1 Stands for Account mapped from asset only
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
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
        final String propertyName = "account-mapping-for-charge";
        String defaultValue = "Income"; // 1 Stands for Account mapped from income only
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
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
        final String propertyName = "fixed-deposit-transfer-interest-next-day-for-period-end-posting";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public boolean isBusinessDateEnabled() {
        return getGlobalConfigurationPropertyData(ENABLE_BUSINESS_DATE).isEnabled();
    }

    @Override
    public boolean isCOBDateAdjustmentEnabled() {
        return getGlobalConfigurationPropertyData(ENABLE_AUTOMATIC_COB_DATE_ADJUSTMENT).isEnabled();
    }

    @Override
    public boolean isReversalTransactionAllowed() {
        final String propertyName = "enable-post-reversal-txns-for-reverse-transactions";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public Long retrieveExternalEventsPurgeDaysCriteria() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(EXTERNAL_EVENTS_PURGE_DAYS);
        return property.getValue();

    }

    @Override
    public Long retrieveProcessedCommandsPurgeDaysCriteria() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(PROCESSED_COMMANDS_PURGE_DAYS);
        return property.getValue();

    }

    @Override
    public Long retrieveRepaymentDueDays() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(DAYS_BEFORE_REPAYMENT_IS_DUE);
        return property.getValue();
    }

    @Override
    public Long retrieveRepaymentOverdueDays() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(DAYS_AFTER_REPAYMENT_IS_OVERDUE);
        return property.getValue();
    }

    @Override
    public boolean isExternalIdAutoGenerationEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(ENABLE_EXTERNAL_ID_AUTO_GENERATION);
        return property.isEnabled();
    }

    @Override
    public boolean isAddressEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(ENABLE_ADDRESS);
        return property.isEnabled();
    }

    @Override
    public boolean isCOBBulkEventEnabled() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(ENABLE_COB_BULK_EVENT);
        return property.isEnabled();
    }

    @Override
    public Long retrieveExternalEventBatchSize() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(EXTERNAL_EVENT_BATCH_SIZE);
        return property.getValue();
    }

    @Override
    public String retrieveReportExportS3FolderName() {
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(REPORT_EXPORT_S3_FOLDER_NAME);
        return property.getStringValue();
    }

    @Override
    public String getAccrualDateConfigForCharge() {
        String defaultValue = "due-date";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(CHARGE_ACCRUAL_DATE_CRITERIA);
        String value = property.getStringValue();
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public String getNextPaymentDateConfigForLoan() {
        String defaultValue = "earliest-unpaid-date";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(NEXT_PAYMENT_DUE_DATE);
        String value = property.getStringValue();
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

}
