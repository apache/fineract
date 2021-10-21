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

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unused", "rawtypes" })
public class GlobalConfigurationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalConfigurationHelper.class);
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public GlobalConfigurationHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static ArrayList<HashMap> getAllGlobalConfigurations(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String GET_ALL_GLOBAL_CONFIG_URL = "/fineract-provider/api/v1/configurations?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING ALL GLOBAL CONFIGURATIONS -------------------------");
        final HashMap<String, ArrayList<HashMap>> response = Utils.performServerGet(requestSpec, responseSpec, GET_ALL_GLOBAL_CONFIG_URL,
                "");
        return response.get("globalConfiguration");
    }

    public static HashMap getGlobalConfigurationById(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String configId) {
        final String GET_GLOBAL_CONFIG_BY_ID_URL = "/fineract-provider/api/v1/configurations/" + configId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING GLOBAL CONFIGURATION BY ID -------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_GLOBAL_CONFIG_BY_ID_URL, "");
    }

    public static void resetAllDefaultGlobalConfigurations(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {

        final ArrayList<HashMap> defaults = getAllDefaultGlobalConfigurations();
        for (HashMap configDefault : defaults) {

            /**
             * Cannot update trapDoor global configurations because
             * {@link org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyCannotBeModfied}
             * will be thrown.
             */
            if ((Boolean) configDefault.get("trapDoor")) {
                continue;
            }

            // Currently only values and enabled flags are modified by the
            // integration test suite.
            // If any other column is modified by the integration test suite in
            // the future, it needs to be reset here.
            final Integer configDefaultId = (Integer) configDefault.get("id");
            final Integer configDefaultValue = (Integer) configDefault.get("value");
            updateValueForGlobalConfiguration(requestSpec, responseSpec, configDefaultId.toString(), configDefaultValue.toString());
            updateEnabledFlagForGlobalConfiguration(requestSpec, responseSpec, configDefaultId.toString(),
                    (Boolean) configDefault.get("enabled"));
        }
    }

    public static void verifyAllDefaultGlobalConfigurations(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {

        ArrayList<HashMap> expectedGlobalConfigurations = getAllDefaultGlobalConfigurations();
        ArrayList<HashMap> actualGlobalConfigurations = getAllGlobalConfigurations(requestSpec, responseSpec);

        // There are currently 32 global configurations.
        Assertions.assertEquals(32, expectedGlobalConfigurations.size());
        Assertions.assertEquals(32, actualGlobalConfigurations.size());

        for (int i = 0; i < expectedGlobalConfigurations.size(); i++) {

            HashMap expectedGlobalConfiguration = expectedGlobalConfigurations.get(i);
            HashMap actualGlobalConfiguration = actualGlobalConfigurations.get(i);

            Assertions.assertEquals(expectedGlobalConfiguration.get("id"), actualGlobalConfiguration.get("id"));
            final String assertionFailedMessage = "Assertion failed for configID:<" + expectedGlobalConfiguration.get("id") + ">";
            Assertions.assertEquals(expectedGlobalConfiguration.get("name"), actualGlobalConfiguration.get("name"), assertionFailedMessage);
            Assertions.assertEquals(expectedGlobalConfiguration.get("value"), actualGlobalConfiguration.get("value"),
                    assertionFailedMessage);
            Assertions.assertEquals(expectedGlobalConfiguration.get("enabled"), actualGlobalConfiguration.get("enabled"),
                    assertionFailedMessage);
            Assertions.assertEquals(expectedGlobalConfiguration.get("trapDoor"), actualGlobalConfiguration.get("trapDoor"),
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
        makerCheckerDefault.put("id", 1);
        makerCheckerDefault.put("name", "maker-checker");
        makerCheckerDefault.put("value", 0);
        makerCheckerDefault.put("enabled", false);
        makerCheckerDefault.put("trapDoor", false);
        defaults.add(makerCheckerDefault);

        HashMap<String, Object> amazonS3Default = new HashMap<>();
        amazonS3Default.put("id", 4);
        amazonS3Default.put("name", "amazon-S3");
        amazonS3Default.put("value", 0);
        amazonS3Default.put("enabled", false);
        amazonS3Default.put("trapDoor", false);
        defaults.add(amazonS3Default);

        HashMap<String, Object> rescheduleFuturePaymentsDefault = new HashMap<>();
        rescheduleFuturePaymentsDefault.put("id", 5);
        rescheduleFuturePaymentsDefault.put("name", "reschedule-future-repayments");
        rescheduleFuturePaymentsDefault.put("value", 0);
        rescheduleFuturePaymentsDefault.put("enabled", true);
        rescheduleFuturePaymentsDefault.put("trapDoor", false);
        defaults.add(rescheduleFuturePaymentsDefault);

        HashMap<String, Object> rescheduleRepaymentsOnHolidaysDefault = new HashMap<>();
        rescheduleRepaymentsOnHolidaysDefault.put("id", 6);
        rescheduleRepaymentsOnHolidaysDefault.put("name", "reschedule-repayments-on-holidays");
        rescheduleRepaymentsOnHolidaysDefault.put("value", 0);
        rescheduleRepaymentsOnHolidaysDefault.put("enabled", false);
        rescheduleRepaymentsOnHolidaysDefault.put("trapDoor", false);
        defaults.add(rescheduleRepaymentsOnHolidaysDefault);

        HashMap<String, Object> allowTransactionsOnHolidayDefault = new HashMap<>();
        allowTransactionsOnHolidayDefault.put("id", 7);
        allowTransactionsOnHolidayDefault.put("name", "allow-transactions-on-holiday");
        allowTransactionsOnHolidayDefault.put("value", 0);
        allowTransactionsOnHolidayDefault.put("enabled", false);
        allowTransactionsOnHolidayDefault.put("trapDoor", false);
        defaults.add(allowTransactionsOnHolidayDefault);

        HashMap<String, Object> allowTransactionsOnNonWorkingDayDefault = new HashMap<>();
        allowTransactionsOnNonWorkingDayDefault.put("id", 8);
        allowTransactionsOnNonWorkingDayDefault.put("name", "allow-transactions-on-non_workingday");
        allowTransactionsOnNonWorkingDayDefault.put("value", 0);
        allowTransactionsOnNonWorkingDayDefault.put("enabled", false);
        allowTransactionsOnNonWorkingDayDefault.put("trapDoor", false);
        defaults.add(allowTransactionsOnNonWorkingDayDefault);

        HashMap<String, Object> constraintApproachForDataTablesDefault = new HashMap<>();
        constraintApproachForDataTablesDefault.put("id", 9);
        constraintApproachForDataTablesDefault.put("name", "constraint_approach_for_datatables");
        constraintApproachForDataTablesDefault.put("value", 0);
        constraintApproachForDataTablesDefault.put("enabled", false);
        constraintApproachForDataTablesDefault.put("trapDoor", false);
        defaults.add(constraintApproachForDataTablesDefault);

        HashMap<String, Object> penaltyWaitPeriodDefault = new HashMap<>();
        penaltyWaitPeriodDefault.put("id", 10);
        penaltyWaitPeriodDefault.put("name", "penalty-wait-period");
        penaltyWaitPeriodDefault.put("value", 2);
        penaltyWaitPeriodDefault.put("enabled", true);
        penaltyWaitPeriodDefault.put("trapDoor", false);
        defaults.add(penaltyWaitPeriodDefault);

        HashMap<String, Object> forcePasswordResetDaysDefault = new HashMap<>();
        forcePasswordResetDaysDefault.put("id", 11);
        forcePasswordResetDaysDefault.put("name", "force-password-reset-days");
        forcePasswordResetDaysDefault.put("value", 0);
        forcePasswordResetDaysDefault.put("enabled", false);
        forcePasswordResetDaysDefault.put("trapDoor", false);
        defaults.add(forcePasswordResetDaysDefault);

        HashMap<String, Object> graceOnPenaltyPostingDefault = new HashMap<>();
        graceOnPenaltyPostingDefault.put("id", 12);
        graceOnPenaltyPostingDefault.put("name", "grace-on-penalty-posting");
        graceOnPenaltyPostingDefault.put("value", 0);
        graceOnPenaltyPostingDefault.put("enabled", true);
        graceOnPenaltyPostingDefault.put("trapDoor", false);
        defaults.add(graceOnPenaltyPostingDefault);

        HashMap<String, Object> savingsInterestPostingCurrentPeriodEndDefault = new HashMap<>();
        savingsInterestPostingCurrentPeriodEndDefault.put("id", 15);
        savingsInterestPostingCurrentPeriodEndDefault.put("name", "savings-interest-posting-current-period-end");
        savingsInterestPostingCurrentPeriodEndDefault.put("value", 0);
        savingsInterestPostingCurrentPeriodEndDefault.put("enabled", false);
        savingsInterestPostingCurrentPeriodEndDefault.put("trapDoor", false);
        defaults.add(savingsInterestPostingCurrentPeriodEndDefault);

        HashMap<String, Object> financialYearBeginningMonthDefault = new HashMap<>();
        financialYearBeginningMonthDefault.put("id", 16);
        financialYearBeginningMonthDefault.put("name", "financial-year-beginning-month");
        financialYearBeginningMonthDefault.put("value", 1);
        financialYearBeginningMonthDefault.put("enabled", true);
        financialYearBeginningMonthDefault.put("trapDoor", false);
        defaults.add(financialYearBeginningMonthDefault);

        HashMap<String, Object> minClientsInGroupDefault = new HashMap<>();
        minClientsInGroupDefault.put("id", 17);
        minClientsInGroupDefault.put("name", "min-clients-in-group");
        minClientsInGroupDefault.put("value", 5);
        minClientsInGroupDefault.put("enabled", false);
        minClientsInGroupDefault.put("trapDoor", false);
        defaults.add(minClientsInGroupDefault);

        HashMap<String, Object> maxClientsInGroupDefault = new HashMap<>();
        maxClientsInGroupDefault.put("id", 18);
        maxClientsInGroupDefault.put("name", "max-clients-in-group");
        maxClientsInGroupDefault.put("value", 5);
        maxClientsInGroupDefault.put("enabled", false);
        maxClientsInGroupDefault.put("trapDoor", false);
        defaults.add(maxClientsInGroupDefault);

        HashMap<String, Object> meetingsMandatoryForJlgLoansDefault = new HashMap<>();
        meetingsMandatoryForJlgLoansDefault.put("id", 19);
        meetingsMandatoryForJlgLoansDefault.put("name", "meetings-mandatory-for-jlg-loans");
        meetingsMandatoryForJlgLoansDefault.put("value", 0);
        meetingsMandatoryForJlgLoansDefault.put("enabled", false);
        meetingsMandatoryForJlgLoansDefault.put("trapDoor", false);
        defaults.add(meetingsMandatoryForJlgLoansDefault);

        HashMap<String, Object> officeSpecificProductsEnabledDefault = new HashMap<>();
        officeSpecificProductsEnabledDefault.put("id", 20);
        officeSpecificProductsEnabledDefault.put("name", "office-specific-products-enabled");
        officeSpecificProductsEnabledDefault.put("value", 0);
        officeSpecificProductsEnabledDefault.put("enabled", false);
        officeSpecificProductsEnabledDefault.put("trapDoor", false);
        defaults.add(officeSpecificProductsEnabledDefault);

        HashMap<String, Object> restrictProductsToUserOfficeDefault = new HashMap<>();
        restrictProductsToUserOfficeDefault.put("id", 21);
        restrictProductsToUserOfficeDefault.put("name", "restrict-products-to-user-office");
        restrictProductsToUserOfficeDefault.put("value", 0);
        restrictProductsToUserOfficeDefault.put("enabled", false);
        restrictProductsToUserOfficeDefault.put("trapDoor", false);
        defaults.add(restrictProductsToUserOfficeDefault);

        HashMap<String, Object> officeOpeningBalancesContraAccountDefault = new HashMap<>();
        officeOpeningBalancesContraAccountDefault.put("id", 22);
        officeOpeningBalancesContraAccountDefault.put("name", "office-opening-balances-contra-account");
        officeOpeningBalancesContraAccountDefault.put("value", 0);
        officeOpeningBalancesContraAccountDefault.put("enabled", true);
        officeOpeningBalancesContraAccountDefault.put("trapDoor", false);
        defaults.add(officeOpeningBalancesContraAccountDefault);

        HashMap<String, Object> roundingModeDefault = new HashMap<>();
        roundingModeDefault.put("id", 23);
        roundingModeDefault.put("name", "rounding-mode");
        roundingModeDefault.put("value", 6);
        roundingModeDefault.put("enabled", true);
        roundingModeDefault.put("trapDoor", true);
        defaults.add(roundingModeDefault);

        HashMap<String, Object> backDatePenaltiesEnabledDefault = new HashMap<>();
        backDatePenaltiesEnabledDefault.put("id", 24);
        backDatePenaltiesEnabledDefault.put("name", "backdate-penalties-enabled");
        backDatePenaltiesEnabledDefault.put("value", 0);
        backDatePenaltiesEnabledDefault.put("enabled", true);
        backDatePenaltiesEnabledDefault.put("trapDoor", false);
        defaults.add(backDatePenaltiesEnabledDefault);

        HashMap<String, Object> organisationStartDateDefault = new HashMap<>();
        organisationStartDateDefault.put("id", 25);
        organisationStartDateDefault.put("name", "organisation-start-date");
        organisationStartDateDefault.put("value", 0);
        organisationStartDateDefault.put("enabled", false);
        organisationStartDateDefault.put("trapDoor", false);
        defaults.add(organisationStartDateDefault);

        HashMap<String, Object> paymentTypeApplicableForDisbursementChargesDefault = new HashMap<>();
        paymentTypeApplicableForDisbursementChargesDefault.put("id", 26);
        paymentTypeApplicableForDisbursementChargesDefault.put("name", "paymenttype-applicable-for-disbursement-charges");
        paymentTypeApplicableForDisbursementChargesDefault.put("value", 0);
        paymentTypeApplicableForDisbursementChargesDefault.put("enabled", false);
        paymentTypeApplicableForDisbursementChargesDefault.put("trapDoor", false);
        defaults.add(paymentTypeApplicableForDisbursementChargesDefault);

        HashMap<String, Object> interestChargedFromDateSameAsDisbursalDateDefault = new HashMap<>();
        interestChargedFromDateSameAsDisbursalDateDefault.put("id", 27);
        interestChargedFromDateSameAsDisbursalDateDefault.put("name", "interest-charged-from-date-same-as-disbursal-date");
        interestChargedFromDateSameAsDisbursalDateDefault.put("value", 0);
        interestChargedFromDateSameAsDisbursalDateDefault.put("enabled", false);
        interestChargedFromDateSameAsDisbursalDateDefault.put("trapDoor", false);
        defaults.add(interestChargedFromDateSameAsDisbursalDateDefault);

        HashMap<String, Object> skipRepaymentOnFirstDayOfMonthDefault = new HashMap<>();
        skipRepaymentOnFirstDayOfMonthDefault.put("id", 28);
        skipRepaymentOnFirstDayOfMonthDefault.put("name", "skip-repayment-on-first-day-of-month");
        skipRepaymentOnFirstDayOfMonthDefault.put("value", 14);
        skipRepaymentOnFirstDayOfMonthDefault.put("enabled", false);
        skipRepaymentOnFirstDayOfMonthDefault.put("trapDoor", false);
        defaults.add(skipRepaymentOnFirstDayOfMonthDefault);

        HashMap<String, Object> changeEmiIfRepaymentDateSameAsDisbursementDateDefault = new HashMap<>();
        changeEmiIfRepaymentDateSameAsDisbursementDateDefault.put("id", 29);
        changeEmiIfRepaymentDateSameAsDisbursementDateDefault.put("name", "change-emi-if-repaymentdate-same-as-disbursementdate");
        changeEmiIfRepaymentDateSameAsDisbursementDateDefault.put("value", 0);
        changeEmiIfRepaymentDateSameAsDisbursementDateDefault.put("enabled", true);
        changeEmiIfRepaymentDateSameAsDisbursementDateDefault.put("trapDoor", false);
        defaults.add(changeEmiIfRepaymentDateSameAsDisbursementDateDefault);

        HashMap<String, Object> dailyTptLimitDefault = new HashMap<>();
        dailyTptLimitDefault.put("id", 30);
        dailyTptLimitDefault.put("name", "daily-tpt-limit");
        dailyTptLimitDefault.put("value", 0);
        dailyTptLimitDefault.put("enabled", false);
        dailyTptLimitDefault.put("trapDoor", false);
        defaults.add(dailyTptLimitDefault);

        HashMap<String, Object> enableAddressDefault = new HashMap<>();
        enableAddressDefault.put("id", 31);
        enableAddressDefault.put("name", "Enable-Address");
        enableAddressDefault.put("value", 0);
        enableAddressDefault.put("enabled", false);
        enableAddressDefault.put("trapDoor", false);
        defaults.add(enableAddressDefault);

        HashMap<String, Object> enableSubRatesDefault = new HashMap<>();
        enableSubRatesDefault.put("id", 32);
        enableSubRatesDefault.put("name", "sub-rates");
        enableSubRatesDefault.put("value", 0);
        enableSubRatesDefault.put("enabled", false);
        enableSubRatesDefault.put("trapDoor", false);
        defaults.add(enableSubRatesDefault);

        HashMap<String, Object> isFirstPaydayAllowedOnHoliday = new HashMap<>();
        isFirstPaydayAllowedOnHoliday.put("id", 33);
        isFirstPaydayAllowedOnHoliday.put("name", "loan-reschedule-is-first-payday-allowed-on-holiday");
        isFirstPaydayAllowedOnHoliday.put("value", 0);
        isFirstPaydayAllowedOnHoliday.put("enabled", false);
        isFirstPaydayAllowedOnHoliday.put("trapDoor", false);
        defaults.add(isFirstPaydayAllowedOnHoliday);

        HashMap<String, Object> isInterestAppropriationEnabled = new HashMap<>();
        isInterestAppropriationEnabled.put("id", 34);
        isInterestAppropriationEnabled.put("name", "is-interest-to-be-appropriated-equally-when-greater-than-emi");
        isInterestAppropriationEnabled.put("value", 0);
        isInterestAppropriationEnabled.put("enabled", false);
        isInterestAppropriationEnabled.put("trapDoor", false);
        defaults.add(isInterestAppropriationEnabled);

        HashMap<String, Object> isAccountMappedForPayment = new HashMap<>();
        isAccountMappedForPayment.put("id", 35);
        isAccountMappedForPayment.put("name", "account-mapping-for-payment-type");
        isAccountMappedForPayment.put("value", 0);
        isAccountMappedForPayment.put("enabled", false);
        isAccountMappedForPayment.put("trapDoor", false);
        isAccountMappedForPayment.put("string_value", "Asset");
        defaults.add(isAccountMappedForPayment);

        HashMap<String, Object> isAccountMappedForCharge = new HashMap<>();
        isAccountMappedForCharge.put("id", 36);
        isAccountMappedForCharge.put("name", "account-mapping-for-charge");
        isAccountMappedForCharge.put("value", 0);
        isAccountMappedForCharge.put("enabled", false);
        isAccountMappedForCharge.put("trapDoor", false);
        isAccountMappedForCharge.put("string_value", "Income");
        defaults.add(isAccountMappedForCharge);

        return defaults;
    }

    public static Integer updateValueForGlobalConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String configId, final String value) {
        final String GLOBAL_CONFIG_UPDATE_URL = "/fineract-provider/api/v1/configurations/" + configId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------UPDATE VALUE FOR GLOBAL CONFIG---------------------------------------------");
        return Utils.performServerPut(requestSpec, responseSpec, GLOBAL_CONFIG_UPDATE_URL, updateGlobalConfigUpdateValueAsJSON(value),
                "resourceId");
    }

    public static Integer updateEnabledFlagForGlobalConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String configId, final Boolean enabled) {
        final String GLOBAL_CONFIG_UPDATE_URL = "/fineract-provider/api/v1/configurations/" + configId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------UPDATE GLOBAL CONFIG FOR ENABLED FLAG---------------------------------------------");
        return Utils.performServerPut(requestSpec, responseSpec, GLOBAL_CONFIG_UPDATE_URL,
                updateGlobalConfigUpdateEnabledFlagAsJSON(enabled), "resourceId");
    }

    public static ArrayList getGlobalConfigurationIsCacheEnabled(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String GET_IS_CACHE_GLOBAL_CONFIG_URL = "/fineract-provider/api/v1/caches?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING IS CACHE ENABLED GLOBAL CONFIGURATION -------------------------");
        final ArrayList<HashMap> response = Utils.performServerGet(requestSpec, responseSpec, GET_IS_CACHE_GLOBAL_CONFIG_URL, "");
        return response;
    }

    public static HashMap updateIsCacheEnabledForGlobalConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String cacheType) {
        final String IS_CACHE_GLOBAL_CONFIG_UPDATE_URL = "/fineract-provider/api/v1/caches?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------UPDATE GLOBAL CONFIG FOR IS CACHE ENABLED----------------------");
        return Utils.performServerPut(requestSpec, responseSpec, IS_CACHE_GLOBAL_CONFIG_UPDATE_URL,
                updateIsCacheEnabledGlobalConfigUpdateAsJSON(cacheType), "changes");
    }

    public static Object updatePasswordResetDaysForGlobalConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer configId, final String value, final String enabled,
            final String jsonAttributeToGetBack) {
        final String UPDATE_URL = "/fineract-provider/api/v1/configurations/" + configId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------UPDATE GLOBAL CONFIG FOR FORCE PASSWORD RESET DAYS----------------------");
        return Utils.performServerPut(requestSpec, responseSpec, UPDATE_URL, updatePasswordResetDaysGlobalConfigAsJSON(value, enabled),
                jsonAttributeToGetBack);
    }

    public static String updateGlobalConfigUpdateValueAsJSON(final String value) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("value", value);
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    public static String updatePasswordResetDaysGlobalConfigAsJSON(final String value, final String enabled) {
        final HashMap<String, String> map = new HashMap<>();
        if (value != null) {
            map.put("value", value);
        }
        map.put("enabled", enabled);
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    public static String updateGlobalConfigUpdateEnabledFlagAsJSON(final Boolean enabled) {
        final HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        map.put("enabled", enabled);
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    public static String updateIsCacheEnabledGlobalConfigUpdateAsJSON(final String cacheType) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("cacheType", cacheType);
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

}
