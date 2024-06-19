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

import java.time.LocalDate;
import org.apache.fineract.infrastructure.cache.domain.CacheType;

public interface ConfigurationDomainService {

    boolean isMakerCheckerEnabledForTask(String taskPermissionCode);

    boolean isSameMakerCheckerEnabled();

    boolean isAmazonS3Enabled();

    boolean isRescheduleFutureRepaymentsEnabled();

    boolean isRescheduleRepaymentsOnHolidaysEnabled();

    boolean allowTransactionsOnHolidayEnabled();

    boolean allowTransactionsOnNonWorkingDayEnabled();

    boolean isConstraintApproachEnabledForDatatables();

    boolean isEhcacheEnabled();

    void updateCache(CacheType cacheType);

    Long retrievePenaltyWaitPeriod();

    boolean isPasswordForcedResetEnable();

    Long retrievePasswordLiveTime();

    Long retrieveGraceOnPenaltyPostingPeriod();

    Long retrieveOpeningBalancesContraAccount();

    boolean isSavingsInterestPostingAtCurrentPeriodEnd();

    Integer retrieveFinancialYearBeginningMonth();

    Integer retrieveMinAllowedClientsInGroup();

    Integer retrieveMaxAllowedClientsInGroup();

    boolean isMeetingMandatoryForJLGLoans();

    int getRoundingMode();

    boolean isBackdatePenaltiesEnabled();

    boolean isOrganisationstartDateEnabled();

    LocalDate retrieveOrganisationStartDate();

    boolean isPaymentTypeApplicableForDisbursementCharge();

    boolean isInterestChargedFromDateSameAsDisbursementDate();

    boolean isSkippingMeetingOnFirstDayOfMonthEnabled();

    boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI();

    boolean isPrincipalCompoundingDisabledForOverdueLoans();

    Long retreivePeriodInNumberOfDaysForSkipMeetingDate();

    boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled();

    boolean isDailyTPTLimitEnabled();

    Long getDailyTPTLimit();

    void removeGlobalConfigurationPropertyDataFromCache(String propertyName);

    boolean isSMSOTPDeliveryEnabled();

    boolean isEmailOTPDeliveryEnabled();

    Integer retrieveOTPCharacterLength();

    Integer retrieveOTPLiveTime();

    boolean isSubRatesEnabled();

    boolean isFirstRepaymentDateAfterRescheduleAllowedOnHoliday();

    String getAccountMappingForPaymentType();

    String getAccountMappingForCharge();

    boolean isNextDayFixedDepositInterestTransferEnabledForPeriodEnd();

    boolean retrievePivotDateConfig();

    boolean isRelaxingDaysConfigForPivotDateEnabled();

    Long retrieveRelaxingDaysConfigForPivotDate();

    boolean isBusinessDateEnabled();

    boolean isCOBDateAdjustmentEnabled();

    boolean isReversalTransactionAllowed();

    Long retrieveExternalEventsPurgeDaysCriteria();

    Long retrieveProcessedCommandsPurgeDaysCriteria();

    Long retrieveRepaymentDueDays();

    Long retrieveRepaymentOverdueDays();

    boolean isExternalIdAutoGenerationEnabled();

    boolean isAddressEnabled();

    boolean isCOBBulkEventEnabled();

    Long retrieveExternalEventBatchSize();

    String retrieveReportExportS3FolderName();

    String getAccrualDateConfigForCharge();

    String getNextPaymentDateConfigForLoan();

}
