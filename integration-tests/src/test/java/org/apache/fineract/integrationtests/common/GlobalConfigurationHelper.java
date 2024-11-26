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
package org.apache.fineract.integrationtests.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetGlobalConfigurationsResponse;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.junit.jupiter.api.Assertions;

@SuppressWarnings({ "unused", "rawtypes" })
@Slf4j
@RequiredArgsConstructor
public class GlobalConfigurationHelper extends IntegrationTest {

    private static final Gson GSON = new JSON().getGson();

    public GetGlobalConfigurationsResponse getAllGlobalConfigurations() {
        log.info("------------------------ RETRIEVING ALL GLOBAL CONFIGURATIONS -------------------------");
        return ok(fineract().globalConfigurations.retrieveConfiguration(false));
    }

    public GlobalConfigurationPropertyData getGlobalConfigurationByName(final String configName) {
        log.info("------------------------ RETRIEVING GLOBAL CONFIGURATION BY NAME -------------------------");
        return ok(fineract().globalConfigurations.retrieveOneByName(configName));
    }

    // TODO: This is quite a bad pattern and adds a lot of time to individual test executions
    public void resetAllDefaultGlobalConfigurations() {

        GetGlobalConfigurationsResponse actualGlobalConfigurations = getAllGlobalConfigurations();
        final ArrayList<HashMap> defaults = getAllDefaultGlobalConfigurations();
        int changedNo = 0;
        for (int i = 0; i < actualGlobalConfigurations.getGlobalConfiguration().size(); i++) {

            HashMap defaultGlobalConfiguration = defaults.get(i);
            GlobalConfigurationPropertyData actualGlobalConfiguration = actualGlobalConfigurations.getGlobalConfiguration().get(i);

            if (!isMatching(defaultGlobalConfiguration, actualGlobalConfiguration)) {

                /**
                 * Cannot update trapDoor global configurations because
                 * {@link org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyCannotBeModfied}
                 * will be thrown.
                 */
                if ((Boolean) defaultGlobalConfiguration.get("trapDoor")) {
                    continue;
                }

                // Currently only values and enabled flags are modified by the
                // integration test suite.
                // If any other column is modified by the integration test suite in
                // the future, it needs to be reset here.
                final String configName = (String) defaultGlobalConfiguration.get("name");
                final Long configDefaultValue = (Long) defaultGlobalConfiguration.get("value");

                updateGlobalConfiguration(configName, new PutGlobalConfigurationsRequest().value(configDefaultValue)
                        .enabled((Boolean) defaultGlobalConfiguration.get("enabled")));
                changedNo++;
            }
        }
        log.info("--------------------------------- UPDATED GLOBAL CONFIG ENTRY SIZE: {} ---------------------------------------------",
                changedNo);
    }

    private static boolean isMatching(HashMap o1, GlobalConfigurationPropertyData o2) {
        return o1.get("name").equals(o2.getName()) && o1.get("value").equals(o2.getValue()) && o1.get("enabled").equals(o2.getEnabled())
                && o1.get("trapDoor").equals(o2.getTrapDoor());
    }

    public void verifyAllDefaultGlobalConfigurations() {

        ArrayList<HashMap> expectedGlobalConfigurations = getAllDefaultGlobalConfigurations();
        GetGlobalConfigurationsResponse actualGlobalConfigurations = getAllGlobalConfigurations();

        Assertions.assertEquals(56, expectedGlobalConfigurations.size());
        Assertions.assertEquals(56, actualGlobalConfigurations.getGlobalConfiguration().size());

        for (int i = 0; i < expectedGlobalConfigurations.size(); i++) {

            HashMap expectedGlobalConfiguration = expectedGlobalConfigurations.get(i);
            GlobalConfigurationPropertyData actualGlobalConfiguration = actualGlobalConfigurations.getGlobalConfiguration().get(i);

            final String assertionFailedMessage = "Assertion failed for configName:<" + expectedGlobalConfiguration.get("name") + ">";
            Assertions.assertEquals(expectedGlobalConfiguration.get("name"), actualGlobalConfiguration.getName(), assertionFailedMessage);
            Assertions.assertEquals(expectedGlobalConfiguration.get("value"), actualGlobalConfiguration.getValue(), assertionFailedMessage);
            Assertions.assertEquals(expectedGlobalConfiguration.get("enabled"), actualGlobalConfiguration.getEnabled(),
                    assertionFailedMessage);
            Assertions.assertEquals(expectedGlobalConfiguration.get("trapDoor"), actualGlobalConfiguration.getTrapDoor(),
                    assertionFailedMessage);
        }
    }

    /**
     * Helper method to get the current default instance data of the /configurations endpoint. Used to reset and verify
     * that no global configuration affects state between integration tests.
     *
     * @see <a href= "https://issues.apache.org/jira/browse/FINERACT-722">FINERACT-722</a> This is a quick, fail fast
     *      and early implementation to resolve this issue. TODO: A more robust future solution would be isolating all
     *      integration test state using Spring Framework's integration test infrastructure for transaction commits and
     *      rollbacks.
     */
    private static ArrayList<HashMap> getAllDefaultGlobalConfigurations() {

        ArrayList<HashMap> defaults = new ArrayList<>();

        HashMap<String, Object> makerCheckerDefault = new HashMap<>();
        makerCheckerDefault.put("name", GlobalConfigurationConstants.MAKER_CHECKER);
        makerCheckerDefault.put("value", 0L);
        makerCheckerDefault.put("enabled", false);
        makerCheckerDefault.put("trapDoor", false);
        defaults.add(makerCheckerDefault);

        HashMap<String, Object> amazonS3Default = new HashMap<>();
        amazonS3Default.put("name", GlobalConfigurationConstants.AMAZON_S3);
        amazonS3Default.put("value", 0L);
        amazonS3Default.put("enabled", false);
        amazonS3Default.put("trapDoor", false);
        defaults.add(amazonS3Default);

        HashMap<String, Object> rescheduleFuturePaymentsDefault = new HashMap<>();
        rescheduleFuturePaymentsDefault.put("name", GlobalConfigurationConstants.RESCHEDULE_FUTURE_REPAYMENTS);
        rescheduleFuturePaymentsDefault.put("value", 0L);
        rescheduleFuturePaymentsDefault.put("enabled", true);
        rescheduleFuturePaymentsDefault.put("trapDoor", false);
        defaults.add(rescheduleFuturePaymentsDefault);

        HashMap<String, Object> rescheduleRepaymentsOnHolidaysDefault = new HashMap<>();
        rescheduleRepaymentsOnHolidaysDefault.put("id", 6);
        rescheduleRepaymentsOnHolidaysDefault.put("name", GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS);
        rescheduleRepaymentsOnHolidaysDefault.put("value", 0L);
        rescheduleRepaymentsOnHolidaysDefault.put("enabled", false);
        rescheduleRepaymentsOnHolidaysDefault.put("trapDoor", false);
        defaults.add(rescheduleRepaymentsOnHolidaysDefault);

        HashMap<String, Object> allowTransactionsOnHolidayDefault = new HashMap<>();
        allowTransactionsOnHolidayDefault.put("name", GlobalConfigurationConstants.ALLOW_TRANSACTIONS_ON_HOLIDAY);
        allowTransactionsOnHolidayDefault.put("value", 0L);
        allowTransactionsOnHolidayDefault.put("enabled", false);
        allowTransactionsOnHolidayDefault.put("trapDoor", false);
        defaults.add(allowTransactionsOnHolidayDefault);

        HashMap<String, Object> allowTransactionsOnNonWorkingDayDefault = new HashMap<>();
        allowTransactionsOnNonWorkingDayDefault.put("name", GlobalConfigurationConstants.ALLOW_TRANSACTIONS_ON_NON_WORKING_DAY);
        allowTransactionsOnNonWorkingDayDefault.put("value", 0L);
        allowTransactionsOnNonWorkingDayDefault.put("enabled", false);
        allowTransactionsOnNonWorkingDayDefault.put("trapDoor", false);
        defaults.add(allowTransactionsOnNonWorkingDayDefault);

        HashMap<String, Object> constraintApproachForDataTablesDefault = new HashMap<>();
        constraintApproachForDataTablesDefault.put("name", GlobalConfigurationConstants.CONSTRAINT_APPROACH_FOR_DATATABLES);
        constraintApproachForDataTablesDefault.put("value", 0L);
        constraintApproachForDataTablesDefault.put("enabled", false);
        constraintApproachForDataTablesDefault.put("trapDoor", false);
        defaults.add(constraintApproachForDataTablesDefault);

        HashMap<String, Object> penaltyWaitPeriodDefault = new HashMap<>();
        penaltyWaitPeriodDefault.put("name", GlobalConfigurationConstants.PENALTY_WAIT_PERIOD);
        penaltyWaitPeriodDefault.put("value", 2L);
        penaltyWaitPeriodDefault.put("enabled", true);
        penaltyWaitPeriodDefault.put("trapDoor", false);
        defaults.add(penaltyWaitPeriodDefault);

        HashMap<String, Object> forcePasswordResetDaysDefault = new HashMap<>();
        forcePasswordResetDaysDefault.put("name", GlobalConfigurationConstants.FORCE_PASSWORD_RESET_DAYS);
        forcePasswordResetDaysDefault.put("value", 0L);
        forcePasswordResetDaysDefault.put("enabled", false);
        forcePasswordResetDaysDefault.put("trapDoor", false);
        defaults.add(forcePasswordResetDaysDefault);

        HashMap<String, Object> graceOnPenaltyPostingDefault = new HashMap<>();
        graceOnPenaltyPostingDefault.put("name", GlobalConfigurationConstants.GRACE_ON_PENALTY_POSTING);
        graceOnPenaltyPostingDefault.put("value", 0L);
        graceOnPenaltyPostingDefault.put("enabled", true);
        graceOnPenaltyPostingDefault.put("trapDoor", false);
        defaults.add(graceOnPenaltyPostingDefault);

        HashMap<String, Object> savingsInterestPostingCurrentPeriodEndDefault = new HashMap<>();
        savingsInterestPostingCurrentPeriodEndDefault.put("name", GlobalConfigurationConstants.SAVINGS_INTEREST_POSTING_CURRENT_PERIOD_END);
        savingsInterestPostingCurrentPeriodEndDefault.put("value", 0L);
        savingsInterestPostingCurrentPeriodEndDefault.put("enabled", false);
        savingsInterestPostingCurrentPeriodEndDefault.put("trapDoor", false);
        defaults.add(savingsInterestPostingCurrentPeriodEndDefault);

        HashMap<String, Object> financialYearBeginningMonthDefault = new HashMap<>();
        financialYearBeginningMonthDefault.put("name", GlobalConfigurationConstants.FINANCIAL_YEAR_BEGINNING_MONTH);
        financialYearBeginningMonthDefault.put("value", 1L);
        financialYearBeginningMonthDefault.put("enabled", true);
        financialYearBeginningMonthDefault.put("trapDoor", false);
        defaults.add(financialYearBeginningMonthDefault);

        HashMap<String, Object> minClientsInGroupDefault = new HashMap<>();
        minClientsInGroupDefault.put("name", GlobalConfigurationConstants.MIN_CLIENTS_IN_GROUP);
        minClientsInGroupDefault.put("value", 5L);
        minClientsInGroupDefault.put("enabled", false);
        minClientsInGroupDefault.put("trapDoor", false);
        defaults.add(minClientsInGroupDefault);

        HashMap<String, Object> maxClientsInGroupDefault = new HashMap<>();
        maxClientsInGroupDefault.put("name", GlobalConfigurationConstants.MAX_CLIENTS_IN_GROUP);
        maxClientsInGroupDefault.put("value", 5L);
        maxClientsInGroupDefault.put("enabled", false);
        maxClientsInGroupDefault.put("trapDoor", false);
        defaults.add(maxClientsInGroupDefault);

        HashMap<String, Object> meetingsMandatoryForJlgLoansDefault = new HashMap<>();
        meetingsMandatoryForJlgLoansDefault.put("name", GlobalConfigurationConstants.MEETINGS_MANDATORY_FOR_JLG_LOANS);
        meetingsMandatoryForJlgLoansDefault.put("value", 0L);
        meetingsMandatoryForJlgLoansDefault.put("enabled", false);
        meetingsMandatoryForJlgLoansDefault.put("trapDoor", false);
        defaults.add(meetingsMandatoryForJlgLoansDefault);

        HashMap<String, Object> officeSpecificProductsEnabledDefault = new HashMap<>();
        officeSpecificProductsEnabledDefault.put("name", GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED);
        officeSpecificProductsEnabledDefault.put("value", 0L);
        officeSpecificProductsEnabledDefault.put("enabled", false);
        officeSpecificProductsEnabledDefault.put("trapDoor", false);
        defaults.add(officeSpecificProductsEnabledDefault);

        HashMap<String, Object> restrictProductsToUserOfficeDefault = new HashMap<>();
        restrictProductsToUserOfficeDefault.put("name", GlobalConfigurationConstants.RESTRICT_PRODUCTS_TO_USER_OFFICE);
        restrictProductsToUserOfficeDefault.put("value", 0L);
        restrictProductsToUserOfficeDefault.put("enabled", false);
        restrictProductsToUserOfficeDefault.put("trapDoor", false);
        defaults.add(restrictProductsToUserOfficeDefault);

        HashMap<String, Object> officeOpeningBalancesContraAccountDefault = new HashMap<>();
        officeOpeningBalancesContraAccountDefault.put("name", GlobalConfigurationConstants.OFFICE_OPENING_BALANCES_CONTRA_ACCOUNT);
        officeOpeningBalancesContraAccountDefault.put("value", 0L);
        officeOpeningBalancesContraAccountDefault.put("enabled", true);
        officeOpeningBalancesContraAccountDefault.put("trapDoor", false);
        defaults.add(officeOpeningBalancesContraAccountDefault);

        HashMap<String, Object> roundingModeDefault = new HashMap<>();
        roundingModeDefault.put("name", GlobalConfigurationConstants.ROUNDING_MODE);
        roundingModeDefault.put("value", 6L);
        roundingModeDefault.put("enabled", true);
        roundingModeDefault.put("trapDoor", true);
        defaults.add(roundingModeDefault);

        HashMap<String, Object> backDatePenaltiesEnabledDefault = new HashMap<>();
        backDatePenaltiesEnabledDefault.put("name", GlobalConfigurationConstants.BACKDATE_PENALTIES_ENABLED);
        backDatePenaltiesEnabledDefault.put("value", 0L);
        backDatePenaltiesEnabledDefault.put("enabled", true);
        backDatePenaltiesEnabledDefault.put("trapDoor", false);
        defaults.add(backDatePenaltiesEnabledDefault);

        HashMap<String, Object> organisationStartDateDefault = new HashMap<>();
        organisationStartDateDefault.put("name", GlobalConfigurationConstants.ORGANISATION_START_DATE);
        organisationStartDateDefault.put("value", 0L);
        organisationStartDateDefault.put("enabled", false);
        organisationStartDateDefault.put("trapDoor", false);
        defaults.add(organisationStartDateDefault);

        HashMap<String, Object> paymentTypeApplicableForDisbursementChargesDefault = new HashMap<>();
        paymentTypeApplicableForDisbursementChargesDefault.put("name",
                GlobalConfigurationConstants.PAYMENT_TYPE_APPLICABLE_FOR_DISBURSEMENT_CHARGES);
        paymentTypeApplicableForDisbursementChargesDefault.put("value", 0L);
        paymentTypeApplicableForDisbursementChargesDefault.put("enabled", false);
        paymentTypeApplicableForDisbursementChargesDefault.put("trapDoor", false);
        defaults.add(paymentTypeApplicableForDisbursementChargesDefault);

        HashMap<String, Object> interestChargedFromDateSameAsDisbursalDateDefault = new HashMap<>();
        interestChargedFromDateSameAsDisbursalDateDefault.put("name",
                GlobalConfigurationConstants.INTEREST_CHARGED_FROM_DATE_SAME_AS_DISBURSAL_DATE);
        interestChargedFromDateSameAsDisbursalDateDefault.put("value", 0L);
        interestChargedFromDateSameAsDisbursalDateDefault.put("enabled", false);
        interestChargedFromDateSameAsDisbursalDateDefault.put("trapDoor", false);
        defaults.add(interestChargedFromDateSameAsDisbursalDateDefault);

        HashMap<String, Object> skipRepaymentOnFirstDayOfMonthDefault = new HashMap<>();
        skipRepaymentOnFirstDayOfMonthDefault.put("name", GlobalConfigurationConstants.SKIP_REPAYMENT_ON_FIRST_DAY_OF_MONTH);
        skipRepaymentOnFirstDayOfMonthDefault.put("value", 14L);
        skipRepaymentOnFirstDayOfMonthDefault.put("enabled", false);
        skipRepaymentOnFirstDayOfMonthDefault.put("trapDoor", false);
        defaults.add(skipRepaymentOnFirstDayOfMonthDefault);

        HashMap<String, Object> changeEmiIfRepaymentDateSameAsDisbursementDateDefault = new HashMap<>();
        changeEmiIfRepaymentDateSameAsDisbursementDateDefault.put("name",
                GlobalConfigurationConstants.CHANGE_EMI_IF_REPAYMENT_DATE_SAME_AS_DISBURSEMENT_DATE);
        changeEmiIfRepaymentDateSameAsDisbursementDateDefault.put("value", 0L);
        changeEmiIfRepaymentDateSameAsDisbursementDateDefault.put("enabled", true);
        changeEmiIfRepaymentDateSameAsDisbursementDateDefault.put("trapDoor", false);
        defaults.add(changeEmiIfRepaymentDateSameAsDisbursementDateDefault);

        HashMap<String, Object> dailyTptLimitDefault = new HashMap<>();
        dailyTptLimitDefault.put("name", GlobalConfigurationConstants.DAILY_TPT_LIMIT);
        dailyTptLimitDefault.put("value", 0L);
        dailyTptLimitDefault.put("enabled", false);
        dailyTptLimitDefault.put("trapDoor", false);
        defaults.add(dailyTptLimitDefault);

        HashMap<String, Object> enableAddressDefault = new HashMap<>();
        enableAddressDefault.put("name", GlobalConfigurationConstants.ENABLE_ADDRESS);
        enableAddressDefault.put("value", 0L);
        enableAddressDefault.put("enabled", false);
        enableAddressDefault.put("trapDoor", false);
        defaults.add(enableAddressDefault);

        HashMap<String, Object> enableSubRatesDefault = new HashMap<>();
        enableSubRatesDefault.put("name", GlobalConfigurationConstants.SUB_RATES);
        enableSubRatesDefault.put("value", 0L);
        enableSubRatesDefault.put("enabled", false);
        enableSubRatesDefault.put("trapDoor", false);
        defaults.add(enableSubRatesDefault);

        HashMap<String, Object> isFirstPaydayAllowedOnHoliday = new HashMap<>();
        isFirstPaydayAllowedOnHoliday.put("name", GlobalConfigurationConstants.LOAN_RESCHEDULE_IS_FIRST_PAYDAY_ALLOWED_ON_HOLIDAY);
        isFirstPaydayAllowedOnHoliday.put("value", 0L);
        isFirstPaydayAllowedOnHoliday.put("enabled", false);
        isFirstPaydayAllowedOnHoliday.put("trapDoor", false);
        defaults.add(isFirstPaydayAllowedOnHoliday);

        HashMap<String, Object> isAccountMappedForPayment = new HashMap<>();
        isAccountMappedForPayment.put("name", GlobalConfigurationConstants.ACCOUNT_MAPPING_FOR_PAYMENT_TYPE);
        isAccountMappedForPayment.put("value", 0L);
        isAccountMappedForPayment.put("enabled", true);
        isAccountMappedForPayment.put("trapDoor", false);
        isAccountMappedForPayment.put("string_value", "Asset");
        defaults.add(isAccountMappedForPayment);

        HashMap<String, Object> isAccountMappedForCharge = new HashMap<>();
        isAccountMappedForCharge.put("name", GlobalConfigurationConstants.ACCOUNT_MAPPING_FOR_CHARGE);
        isAccountMappedForCharge.put("value", 0L);
        isAccountMappedForCharge.put("enabled", true);
        isAccountMappedForCharge.put("trapDoor", false);
        isAccountMappedForCharge.put("string_value", "Income");
        defaults.add(isAccountMappedForCharge);

        HashMap<String, Object> isNextDayFixedDepositInterestTransferEnabledForPeriodEnd = new HashMap<>();
        isNextDayFixedDepositInterestTransferEnabledForPeriodEnd.put("name",
                GlobalConfigurationConstants.FIXED_DEPOSIT_TRANSFER_INTEREST_NEXT_DAY_FOR_PERIOD_END_POSTING);
        isNextDayFixedDepositInterestTransferEnabledForPeriodEnd.put("value", 0L);
        isNextDayFixedDepositInterestTransferEnabledForPeriodEnd.put("enabled", false);
        isNextDayFixedDepositInterestTransferEnabledForPeriodEnd.put("trapDoor", false);
        defaults.add(isNextDayFixedDepositInterestTransferEnabledForPeriodEnd);

        HashMap<String, Object> isAllowedBackDatedTransactionsBeforeInterestPostingDate = new HashMap<>();
        isAllowedBackDatedTransactionsBeforeInterestPostingDate.put("name",
                GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING);
        isAllowedBackDatedTransactionsBeforeInterestPostingDate.put("value", 0L);
        isAllowedBackDatedTransactionsBeforeInterestPostingDate.put("enabled", true);
        isAllowedBackDatedTransactionsBeforeInterestPostingDate.put("trapDoor", false);
        defaults.add(isAllowedBackDatedTransactionsBeforeInterestPostingDate);

        HashMap<String, Object> isAllowedBackDatedTransactionsBeforeInterestPostingDateForDays = new HashMap<>();
        isAllowedBackDatedTransactionsBeforeInterestPostingDateForDays.put("name",
                GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING_DATE_FOR_DAYS);
        isAllowedBackDatedTransactionsBeforeInterestPostingDateForDays.put("value", 0L);
        isAllowedBackDatedTransactionsBeforeInterestPostingDateForDays.put("enabled", false);
        isAllowedBackDatedTransactionsBeforeInterestPostingDateForDays.put("trapDoor", false);
        defaults.add(isAllowedBackDatedTransactionsBeforeInterestPostingDateForDays);

        HashMap<String, Object> isClientAccountNumberLengthModify = new HashMap<>();
        isClientAccountNumberLengthModify.put("name", GlobalConfigurationConstants.CUSTOM_ACCOUNT_NUMBER_LENGTH);
        isClientAccountNumberLengthModify.put("value", 0L);
        isClientAccountNumberLengthModify.put("enabled", false);
        isClientAccountNumberLengthModify.put("trapDoor", false);
        defaults.add(isClientAccountNumberLengthModify);

        HashMap<String, Object> isAccountNumberRandomGenerated = new HashMap<>();
        isAccountNumberRandomGenerated.put("name", GlobalConfigurationConstants.RANDOM_ACCOUNT_NUMBER);
        isAccountNumberRandomGenerated.put("value", 0L);
        isAccountNumberRandomGenerated.put("enabled", false);
        isAccountNumberRandomGenerated.put("trapDoor", false);
        defaults.add(isAccountNumberRandomGenerated);

        HashMap<String, Object> isInterestAppropriationEnabled = new HashMap<>();
        isInterestAppropriationEnabled.put("name", GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI);
        isInterestAppropriationEnabled.put("value", 0L);
        isInterestAppropriationEnabled.put("enabled", false);
        isInterestAppropriationEnabled.put("trapDoor", false);
        defaults.add(isInterestAppropriationEnabled);

        HashMap<String, Object> isPrincipalCompoundingDisabled = new HashMap<>();
        isPrincipalCompoundingDisabled.put("name", GlobalConfigurationConstants.IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS);
        isPrincipalCompoundingDisabled.put("value", 0L);
        isPrincipalCompoundingDisabled.put("enabled", false);
        isPrincipalCompoundingDisabled.put("trapDoor", false);
        defaults.add(isPrincipalCompoundingDisabled);

        HashMap<String, Object> isBusinessDateEnabled = new HashMap<>();
        isBusinessDateEnabled.put("name", GlobalConfigurationConstants.ENABLE_BUSINESS_DATE);
        isBusinessDateEnabled.put("value", 0L);
        isBusinessDateEnabled.put("enabled", false);
        isBusinessDateEnabled.put("trapDoor", false);
        defaults.add(isBusinessDateEnabled);

        HashMap<String, Object> isAutomaticCOBDateAdjustmentEnabled = new HashMap<>();
        isAutomaticCOBDateAdjustmentEnabled.put("name", GlobalConfigurationConstants.ENABLE_AUTOMATIC_COB_DATE_ADJUSTMENT);
        isAutomaticCOBDateAdjustmentEnabled.put("value", 0L);
        isAutomaticCOBDateAdjustmentEnabled.put("enabled", true);
        isAutomaticCOBDateAdjustmentEnabled.put("trapDoor", false);
        defaults.add(isAutomaticCOBDateAdjustmentEnabled);

        HashMap<String, Object> isReversalTransactionAllowed = new HashMap<>();
        isReversalTransactionAllowed.put("name", GlobalConfigurationConstants.ENABLE_POST_REVERSAL_TXNS_FOR_REVERSE_TRANSACTIONS);
        isReversalTransactionAllowed.put("value", 0L);
        isReversalTransactionAllowed.put("enabled", false);
        isReversalTransactionAllowed.put("trapDoor", false);
        defaults.add(isReversalTransactionAllowed);

        HashMap<String, Object> purgeExternalEventsOlderThanDaysDefault = new HashMap<>();
        purgeExternalEventsOlderThanDaysDefault.put("name", GlobalConfigurationConstants.PURGE_EXTERNAL_EVENTS_OLDER_THAN_DAYS);
        purgeExternalEventsOlderThanDaysDefault.put("value", 30L);
        purgeExternalEventsOlderThanDaysDefault.put("enabled", false);
        purgeExternalEventsOlderThanDaysDefault.put("trapDoor", false);
        defaults.add(purgeExternalEventsOlderThanDaysDefault);

        HashMap<String, Object> loanRepaymentDueDaysDefault = new HashMap<>();
        loanRepaymentDueDaysDefault.put("name", GlobalConfigurationConstants.DAYS_BEFORE_REPAYMENT_IS_DUE);
        loanRepaymentDueDaysDefault.put("value", 1L);
        loanRepaymentDueDaysDefault.put("enabled", false);
        loanRepaymentDueDaysDefault.put("trapDoor", false);
        defaults.add(loanRepaymentDueDaysDefault);

        HashMap<String, Object> loanRepaymentOverdueDaysDefault = new HashMap<>();
        loanRepaymentOverdueDaysDefault.put("name", GlobalConfigurationConstants.DAYS_AFTER_REPAYMENT_IS_OVERDUE);
        loanRepaymentOverdueDaysDefault.put("value", 1L);
        loanRepaymentOverdueDaysDefault.put("enabled", false);
        loanRepaymentOverdueDaysDefault.put("trapDoor", false);
        defaults.add(loanRepaymentOverdueDaysDefault);

        HashMap<String, Object> isAutomaticExternalIdGenerationEnabled = new HashMap<>();
        isAutomaticExternalIdGenerationEnabled.put("name", GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID);
        isAutomaticExternalIdGenerationEnabled.put("value", 0L);
        isAutomaticExternalIdGenerationEnabled.put("enabled", false);
        isAutomaticExternalIdGenerationEnabled.put("trapDoor", false);
        defaults.add(isAutomaticExternalIdGenerationEnabled);

        HashMap<String, Object> purgeProcessCommandDaysDefault = new HashMap<>();
        purgeProcessCommandDaysDefault.put("name", GlobalConfigurationConstants.PURGE_PROCESSED_COMMANDS_OLDER_THAN_DAYS);
        purgeProcessCommandDaysDefault.put("value", 30L);
        purgeProcessCommandDaysDefault.put("enabled", false);
        purgeProcessCommandDaysDefault.put("trapDoor", false);
        defaults.add(purgeProcessCommandDaysDefault);

        HashMap<String, Object> isCOBBulkEventEnabled = new HashMap<>();
        isCOBBulkEventEnabled.put("name", GlobalConfigurationConstants.ENABLE_COB_BULK_EVENT);
        isCOBBulkEventEnabled.put("value", 0L);
        isCOBBulkEventEnabled.put("enabled", false);
        isCOBBulkEventEnabled.put("trapDoor", false);
        defaults.add(isCOBBulkEventEnabled);

        HashMap<String, Object> externalEventBatchSize = new HashMap<>();
        externalEventBatchSize.put("name", GlobalConfigurationConstants.EXTERNAL_EVENT_BATCH_SIZE);
        externalEventBatchSize.put("value", 1000L);
        externalEventBatchSize.put("enabled", false);
        externalEventBatchSize.put("trapDoor", false);
        defaults.add(externalEventBatchSize);

        HashMap<String, Object> reportExportS3FolderName = new HashMap<>();
        reportExportS3FolderName.put("name", GlobalConfigurationConstants.REPORT_EXPORT_S3_FOLDER_NAME);
        reportExportS3FolderName.put("value", 0L);
        reportExportS3FolderName.put("enabled", true);
        reportExportS3FolderName.put("trapDoor", false);
        defaults.add(reportExportS3FolderName);

        HashMap<String, Object> loanArrearsDelinquencyDisplayData = new HashMap<>();
        loanArrearsDelinquencyDisplayData.put("name", GlobalConfigurationConstants.LOAN_ARREARS_DELINQUENCY_DISPLAY_DATA);
        loanArrearsDelinquencyDisplayData.put("value", 0L);
        loanArrearsDelinquencyDisplayData.put("enabled", true);
        loanArrearsDelinquencyDisplayData.put("trapDoor", false);
        defaults.add(loanArrearsDelinquencyDisplayData);

        HashMap<String, Object> accrualForChargeDate = new HashMap<>();
        accrualForChargeDate.put("name", GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE);
        accrualForChargeDate.put("value", 0L);
        accrualForChargeDate.put("enabled", true);
        accrualForChargeDate.put("trapDoor", false);
        accrualForChargeDate.put("string_value", "due-date");
        defaults.add(accrualForChargeDate);

        HashMap<String, Object> assetExternalizationOfNonActiveLoans = new HashMap<>();
        assetExternalizationOfNonActiveLoans.put("name", GlobalConfigurationConstants.ASSET_EXTERNALIZATION_OF_NON_ACTIVE_LOANS);
        assetExternalizationOfNonActiveLoans.put("value", 0L);
        assetExternalizationOfNonActiveLoans.put("enabled", true);
        assetExternalizationOfNonActiveLoans.put("trapDoor", false);
        defaults.add(assetExternalizationOfNonActiveLoans);

        HashMap<String, Object> enableSameMakerChecker = new HashMap<>();
        enableSameMakerChecker.put("name", GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER);
        enableSameMakerChecker.put("value", 0L);
        enableSameMakerChecker.put("enabled", false);
        enableSameMakerChecker.put("trapDoor", false);
        defaults.add(enableSameMakerChecker);

        HashMap<String, Object> nextPaymentDateConfigForLoan = new HashMap<>();
        nextPaymentDateConfigForLoan.put("name", GlobalConfigurationConstants.NEXT_PAYMENT_DUE_DATE);
        nextPaymentDateConfigForLoan.put("value", 0L);
        nextPaymentDateConfigForLoan.put("enabled", true);
        nextPaymentDateConfigForLoan.put("trapDoor", false);
        nextPaymentDateConfigForLoan.put("string_value", "earliest-unpaid-date");
        defaults.add(nextPaymentDateConfigForLoan);

        HashMap<String, Object> enablePaymentHubIntegrationConfig = new HashMap<>();
        enablePaymentHubIntegrationConfig.put("name", GlobalConfigurationConstants.ENABLE_PAYMENT_HUB_INTEGRATION);
        enablePaymentHubIntegrationConfig.put("value", 0L);
        enablePaymentHubIntegrationConfig.put("enabled", false);
        enablePaymentHubIntegrationConfig.put("trapDoor", false);
        enablePaymentHubIntegrationConfig.put("string_value", "enable payment hub integration");
        defaults.add(enablePaymentHubIntegrationConfig);

        HashMap<String, Object> enableImmediateChargeAccrualPostMaturity = new HashMap<>();
        enableImmediateChargeAccrualPostMaturity.put("name", GlobalConfigurationConstants.ENABLE_IMMEDIATE_CHARGE_ACCRUAL_POST_MATURITY);
        enableImmediateChargeAccrualPostMaturity.put("value", 0L);
        enableImmediateChargeAccrualPostMaturity.put("enabled", false);
        enableImmediateChargeAccrualPostMaturity.put("trapDoor", false);
        defaults.add(enableImmediateChargeAccrualPostMaturity);

        return defaults;
    }

    public PutGlobalConfigurationsResponse updateGlobalConfiguration(final String configName, PutGlobalConfigurationsRequest request) {
        log.info("---------------------------------UPDATE VALUE FOR GLOBAL CONFIG---------------------------------------------");
        return ok(fineract().globalConfigurations.updateConfigurationByName(configName, request));
    }

    public void updateGlobalConfigurationInternal(final String configName, final Long value) {
        log.info("---------------------------UPDATE VALUE FOR GLOBAL CONFIG (internal) ---------------------------------------");
        ok(fineract().legacy.updateGlobalConfiguration(configName, value));
    }

    public void manageConfigurations(final String configurationName, final boolean enabled) {
        GlobalConfigurationPropertyData configuration = getGlobalConfigurationByName(configurationName);
        assertNotNull(configuration);
        updateGlobalConfiguration(configurationName, new PutGlobalConfigurationsRequest().enabled(enabled));
        GlobalConfigurationPropertyData updatedConfiguration = getGlobalConfigurationByName(configurationName);
        assertEquals(updatedConfiguration.getEnabled(), enabled);
    }
}
