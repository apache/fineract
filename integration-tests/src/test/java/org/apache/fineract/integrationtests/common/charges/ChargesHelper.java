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
package org.apache.fineract.integrationtests.common.charges;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.client.models.PostChargesRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class ChargesHelper extends IntegrationTest {

    public ChargesHelper() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(ChargesHelper.class);
    private static final String CHARGES_URL = "/fineract-provider/api/v1/charges";
    private static final String CREATE_CHARGES_URL = CHARGES_URL + "?" + Utils.TENANT_IDENTIFIER;

    private static final Integer CHARGE_APPLIES_TO_LOAN = 1;
    private static final Integer CHARGE_APPLIES_TO_SAVINGS = 2;
    private static final Integer CHARGE_APPLIES_TO_CLIENT = 3;
    private static final Integer CHARGE_APPLIES_TO_SHARES = 4;

    private static final Integer CHARGE_DISBURSEMENT_FEE = 1;
    private static final Integer CHARGE_SPECIFIED_DUE_DATE = 2;
    private static final Integer CHARGE_SAVINGS_ACTIVATION_FEE = 3;
    private static final Integer CHARGE_WITHDRAWAL_FEE = 5;
    private static final Integer CHARGE_ANNUAL_FEE = 6;
    private static final Integer CHARGE_MONTHLY_FEE = 7;
    private static final Integer CHARGE_INSTALLMENT_FEE = 8;
    private static final Integer CHARGE_OVERDUE_INSTALLMENT_FEE = 9;
    private static final Integer CHARGE_OVERDRAFT_FEE = 10;
    private static final Integer WEEKLY_FEE = 11;
    private static final Integer SHAREACCOUNT_ACTIVATION = 13;
    private static final Integer SHARE_PURCHASE = 14;
    private static final Integer SHARE_REDEEM = 15;

    private static final Integer CHARGE_SAVINGS_NO_ACTIVITY_FEE = 16;

    private static final Integer CHARGE_CLIENT_SPECIFIED_DUE_DATE = 1;

    public static final Integer CHARGE_CALCULATION_TYPE_FLAT = 1;
    public static final Integer CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT = 2;
    public static final Integer CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST = 3;
    public static final Integer CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST = 4;

    private static final Integer CHARGE_PAYMENT_MODE_REGULAR = 0;
    private static final Integer CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER = 1;

    private static final Integer CHARGE_FEE_FREQUENCY_DAYS = 0;
    private static final Integer CHARGE_FEE_FREQUENCY_WEEKS = 1;
    private static final Integer CHARGE_FEE_FREQUENCY_MONTHS = 2;
    private static final Integer CHARGE_FEE_FREQUENCY_YEARS = 3;

    private static final boolean ACTIVE = true;
    private static final boolean PENALTY = true;
    private static final String AMOUNT = "100";
    private static final String CURRENCY_CODE = "USD";
    public static final String FEE_ON_MONTH_DAY = "04 March";
    private static final String MONTH_DAY_FORMAT = "dd MMM";

    private static final Gson GSON = new JSON().getGson();

    public static String getSavingsSpecifiedDueDateJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("chargeTimeType", CHARGE_SPECIFIED_DUE_DATE);
        map.put("feeInterval", 2);
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getSavingsActivationFeeJSON() {
        return getSavingsJSON(AMOUNT, CURRENCY_CODE, ChargeTimeType.SAVINGS_ACTIVATION);
    }

    public static String getSavingsNoActivityFeeJSON() {
        return getSavingsJSON(AMOUNT, CURRENCY_CODE, ChargeTimeType.SAVINGS_NOACTIVITY_FEE);
    }

    public static String getSavingsWithdrawalFeeJSON() {
        return getSavingsJSON(AMOUNT, CURRENCY_CODE, ChargeTimeType.WITHDRAWAL_FEE);
    }

    public static String getSavingsJSON(String amount, String currencyCode, ChargeTimeType timeType) {
        final HashMap<String, Object> map = populateDefaultsForSavings(amount.toString(), currencyCode);
        map.put("chargeTimeType", timeType.getValue());
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getSavingsAnnualFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("feeOnMonthDay", ChargesHelper.FEE_ON_MONTH_DAY);
        map.put("chargeTimeType", CHARGE_ANNUAL_FEE);
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getSavingsMonthlyFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("feeOnMonthDay", ChargesHelper.FEE_ON_MONTH_DAY);
        map.put("chargeTimeType", CHARGE_MONTHLY_FEE);
        map.put("feeInterval", 2);
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getSavingsWeeklyFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("chargeTimeType", WEEKLY_FEE);
        map.put("feeInterval", 1);
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getSavingsOverdraftFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("chargeTimeType", CHARGE_OVERDRAFT_FEE);
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static HashMap<String, Object> populateDefaultsForSavings() {
        return populateDefaultsForSavings(AMOUNT, CURRENCY_CODE);
    }

    public static HashMap<String, Object> populateDefaultsForSavings(String amount, String currencyCode) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("active", ChargesHelper.ACTIVE);
        map.put("amount", amount);
        map.put("chargeAppliesTo", ChargesHelper.CHARGE_APPLIES_TO_SAVINGS);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT);
        map.put("currencyCode", currencyCode);
        map.put("locale", CommonConstants.LOCALE);
        map.put("monthDayFormat", ChargesHelper.MONTH_DAY_FORMAT);
        map.put("name", Utils.uniqueRandomStringGenerator("Charge_Savings_", 6));
        return map;
    }

    public static String getLoanDisbursementJSON() {
        return getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, ChargesHelper.AMOUNT);
    }

    public static String getLoanDisbursementJSON(final Integer chargeCalculationType, final String amount) {
        return getLoanDisbursementJSON(chargeCalculationType, amount, ChargesHelper.CHARGE_PAYMENT_MODE_REGULAR);
    }

    public static String getLoanDisbursementAccountTransferJSON(final Integer chargeCalculationType, final String amount) {
        return getLoanDisbursementJSON(chargeCalculationType, amount, ChargesHelper.CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
    }

    public static String getLoanDisbursementJSON(final Integer chargeCalculationType, final String amount, final Integer paymentmode) {
        final HashMap<String, Object> map = populateDefaultsForLoan();
        map.put("chargeTimeType", CHARGE_DISBURSEMENT_FEE);
        map.put("chargePaymentMode", paymentmode);
        map.put("amount", amount);
        map.put("chargeCalculationType", chargeCalculationType);
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String paymentTypeCharge(Integer amount, final boolean enablePaymentType, final Long paymentTypeId) {
        return paymentTypeChargeJSON(amount, enablePaymentType, paymentTypeId);
    }

    public static String paymentTypeChargeJSON(Integer amount, final boolean enablePaymentType, final Long paymentTypeId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("active", ChargesHelper.ACTIVE);
        map.put("amount", amount);
        map.put("chargeAppliesTo", ChargesHelper.CHARGE_APPLIES_TO_SAVINGS);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT);
        map.put("currencyCode", ChargesHelper.CURRENCY_CODE);
        map.put("locale", CommonConstants.LOCALE);
        map.put("monthDayFormat", ChargesHelper.MONTH_DAY_FORMAT);
        map.put("name", Utils.uniqueRandomStringGenerator("Charge_Savings_", 6));
        map.put("chargeTimeType", CHARGE_WITHDRAWAL_FEE);
        map.put("enablePaymentType", enablePaymentType);
        map.put("paymentTypeId", paymentTypeId);
        String paymentTypeJson = new Gson().toJson(map);
        return paymentTypeJson;
    }

    public static String getLoanSpecifiedDueDateJSON() {
        return getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, ChargesHelper.AMOUNT, ChargesHelper.PENALTY);
    }

    public static String getLoanSpecifiedDueDateJSON(final Integer chargeCalculationType, final String amount, boolean penalty) {
        return getLoanSpecifiedDueDateJSON(chargeCalculationType, amount, penalty, ChargesHelper.CHARGE_PAYMENT_MODE_REGULAR);
    }

    public static String getLoanSpecifiedDueDateJSON(final Integer chargeCalculationType, final String amount, boolean penalty,
            String currencyCode) {
        return getLoanSpecifiedDueDateJSON(chargeCalculationType, amount, penalty, ChargesHelper.CHARGE_PAYMENT_MODE_REGULAR, currencyCode);
    }

    public static String getLoanSpecifiedDueDateJSON(final Integer chargeCalculationType, final String amount, final boolean penalty,
            final Integer paymentMode) {
        return getLoanSpecifiedDueDateJSON(chargeCalculationType, amount, penalty, paymentMode, ChargesHelper.CURRENCY_CODE);
    }

    public static String getLoanSpecifiedDueDateJSON(final Integer chargeCalculationType, final String amount, final boolean penalty,
            final Integer paymentMode, final String currencyCode) {
        final HashMap<String, Object> map = populateDefaultsForLoan();
        map.put("chargeTimeType", CHARGE_SPECIFIED_DUE_DATE);
        map.put("chargePaymentMode", paymentMode);
        map.put("penalty", penalty);
        map.put("amount", amount);
        map.put("chargeCalculationType", chargeCalculationType);
        map.put("currencyCode", currencyCode);

        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getLoanSpecificInstallmentFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForLoan();
        map.put("chargeTimeType", CHARGE_INSTALLMENT_FEE);
        map.put("chargePaymentMode", CHARGE_PAYMENT_MODE_REGULAR);
        map.put("penalty", false);
        map.put("amount", "1.500000");
        map.put("chargeCalculationType", CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST);

        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getLoanSpecifiedDueDateWithAccountTransferJSON(final Integer chargeCalculationType, final String amount,
            boolean penalty) {
        return getLoanSpecifiedDueDateJSON(chargeCalculationType, amount, penalty, ChargesHelper.CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
    }

    public static String getLoanSpecifiedDueDateWithAccountTransferJSON() {
        return getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, ChargesHelper.AMOUNT, ChargesHelper.PENALTY,
                ChargesHelper.CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
    }

    public static String getLoanInstallmentJSON(final Integer chargeCalculationType, final String amount, boolean penalty) {
        return getLoanInstallmentJSON(chargeCalculationType, amount, penalty, ChargesHelper.CHARGE_PAYMENT_MODE_REGULAR);
    }

    public static String getLoanOverdueInstallmentJSON(final String amount) {
        final HashMap<String, Object> map = populateDefaultsForLoan();
        map.put("chargeId", CHARGE_OVERDUE_INSTALLMENT_FEE);
        map.put("amount", amount);

        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getLoanInstallmentJSON(final Integer chargeCalculationType, final String amount, final boolean penalty,
            final Integer paymentMode) {
        final HashMap<String, Object> map = populateDefaultsForLoan();
        map.put("chargeTimeType", CHARGE_INSTALLMENT_FEE);
        map.put("chargePaymentMode", paymentMode);
        map.put("penalty", penalty);
        map.put("amount", amount);
        map.put("chargeCalculationType", chargeCalculationType);

        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getLoanInstallmentWithAccountTransferJSON(final Integer chargeCalculationType, final String amount,
            boolean penalty) {
        return getLoanInstallmentJSON(chargeCalculationType, amount, penalty, ChargesHelper.CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
    }

    public static String getLoanInstallmentFeeJSON() {
        return getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, ChargesHelper.AMOUNT, ChargesHelper.PENALTY);
    }

    public static String getShareAccountActivationChargeJson() {
        HashMap<String, Object> map = populateDefaultsShareActivationCharge();
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getShareAccountPurchaseChargeJson() {
        HashMap<String, Object> map = populateDefaultsSharePurchaseFlatCharge();
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getShareAccountRedeemChargeJson() {
        HashMap<String, Object> map = populateDefaultsShareRedeemFlatCharge();
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getLoanOverdueFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForLoan();
        map.put("penalty", ChargesHelper.PENALTY);
        map.put("chargePaymentMode", ChargesHelper.CHARGE_PAYMENT_MODE_REGULAR);
        map.put("chargeTimeType", CHARGE_OVERDUE_INSTALLMENT_FEE);
        map.put("feeFrequency", ChargesHelper.CHARGE_FEE_FREQUENCY_MONTHS);
        map.put("feeOnMonthDay", ChargesHelper.FEE_ON_MONTH_DAY);
        map.put("feeInterval", 2);
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getLoanOverdueFeeJSONWithCalculationTypePercentage(String penaltyPercentageAmount) {
        final HashMap<String, Object> map = populateDefaultsForLoan();
        map.put("penalty", ChargesHelper.PENALTY);
        map.put("amount", penaltyPercentageAmount);
        map.put("chargePaymentMode", ChargesHelper.CHARGE_PAYMENT_MODE_REGULAR);
        map.put("chargeTimeType", CHARGE_OVERDUE_INSTALLMENT_FEE);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST);
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("{}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static HashMap<String, Object> populateDefaultsForLoan() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("active", ChargesHelper.ACTIVE);
        map.put("amount", ChargesHelper.AMOUNT);
        map.put("chargeAppliesTo", ChargesHelper.CHARGE_APPLIES_TO_LOAN);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT);
        map.put("currencyCode", ChargesHelper.CURRENCY_CODE);
        map.put("locale", CommonConstants.LOCALE);
        map.put("monthDayFormat", ChargesHelper.MONTH_DAY_FORMAT);
        map.put("name", Utils.uniqueRandomStringGenerator("Charge_Loans_", 6));
        return map;
    }

    public static HashMap<String, Object> populateDefaultsClientCharge() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("active", ChargesHelper.ACTIVE);
        map.put("amount", ChargesHelper.AMOUNT);
        map.put("chargeAppliesTo", ChargesHelper.CHARGE_APPLIES_TO_CLIENT);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT);
        map.put("chargeTimeType", ChargesHelper.CHARGE_SPECIFIED_DUE_DATE);
        map.put("currencyCode", ChargesHelper.CURRENCY_CODE);
        map.put("locale", CommonConstants.LOCALE);
        map.put("monthDayFormat", ChargesHelper.MONTH_DAY_FORMAT);
        map.put("name", Utils.uniqueRandomStringGenerator("Charge_client_", 8));
        return map;
    }

    public static HashMap<String, Object> populateDefaultsShareActivationCharge() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("active", ChargesHelper.ACTIVE);
        map.put("amount", ChargesHelper.AMOUNT);
        map.put("chargeAppliesTo", ChargesHelper.CHARGE_APPLIES_TO_SHARES);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT);
        map.put("chargeTimeType", ChargesHelper.SHAREACCOUNT_ACTIVATION);
        map.put("currencyCode", ChargesHelper.CURRENCY_CODE);
        map.put("locale", CommonConstants.LOCALE);
        map.put("name", Utils.uniqueRandomStringGenerator("Charge_Share_Activation_", 8));
        return map;
    }

    public static HashMap<String, Object> populateDefaultsSharePurchaseFlatCharge() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("active", ChargesHelper.ACTIVE);
        map.put("amount", ChargesHelper.AMOUNT);
        map.put("chargeAppliesTo", ChargesHelper.CHARGE_APPLIES_TO_SHARES);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT);
        map.put("chargeTimeType", ChargesHelper.SHARE_PURCHASE);
        map.put("currencyCode", ChargesHelper.CURRENCY_CODE);
        map.put("locale", CommonConstants.LOCALE);
        map.put("name", Utils.uniqueRandomStringGenerator("Charge_Share_Purchase_", 8));
        return map;
    }

    public static HashMap<String, Object> populateDefaultsShareRedeemFlatCharge() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("active", ChargesHelper.ACTIVE);
        map.put("amount", ChargesHelper.AMOUNT);
        map.put("chargeAppliesTo", ChargesHelper.CHARGE_APPLIES_TO_SHARES);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT);
        map.put("chargeTimeType", ChargesHelper.SHARE_REDEEM);
        map.put("currencyCode", ChargesHelper.CURRENCY_CODE);
        map.put("locale", CommonConstants.LOCALE);
        map.put("name", Utils.uniqueRandomStringGenerator("Charge_Share_Redeem_", 8));
        return map;
    }

    public static Integer createCharges(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String request) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CHARGES_URL, request, "resourceId");
    }

    public static PostChargesResponse createLoanCharge(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String payload) {
        final String response = Utils.performServerPost(requestSpec, responseSpec, CREATE_CHARGES_URL, payload, null);
        return GSON.fromJson(response, PostChargesResponse.class);
    }

    public static ArrayList<HashMap> getCharges(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return (ArrayList) Utils.performServerGet(requestSpec, responseSpec, CHARGES_URL + "?" + Utils.TENANT_IDENTIFIER, "");
    }

    public static HashMap getChargeById(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer chargeId) {
        return Utils.performServerGet(requestSpec, responseSpec, CHARGES_URL + "/" + chargeId + "?" + Utils.TENANT_IDENTIFIER, "");
    }

    public static HashMap getChargeChanges(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer chargeId) {
        return Utils.performServerGet(requestSpec, responseSpec, CHARGES_URL + "/" + chargeId + "?" + Utils.TENANT_IDENTIFIER,
                CommonConstants.RESPONSE_CHANGES);
    }

    public static HashMap updateCharges(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer chargeId, final String request) {
        return Utils.performServerPut(requestSpec, responseSpec, CHARGES_URL + "/" + chargeId + "?" + Utils.TENANT_IDENTIFIER, request,
                CommonConstants.RESPONSE_CHANGES);
    }

    public static Integer deleteCharge(final ResponseSpecification responseSpec, final RequestSpecification requestSpec,
            final Integer chargeId) {
        return Utils.performServerDelete(requestSpec, responseSpec, CHARGES_URL + "/" + chargeId + "?" + Utils.TENANT_IDENTIFIER,
                CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public static String getModifyChargeJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("amount", 200.0);
        String json = new Gson().toJson(map);
        LOG.info("{}", json);
        return json;
    }

    public static String getModifyWithdrawalFeeSavingsChargeJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("chargeCalculationType", CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT);
        String json = new Gson().toJson(map);
        LOG.info("{}", json);
        return json;
    }

    public static String getModifyChargeAsPecentageAmountJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("chargeCalculationType", CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT);
        map.put("chargePaymentMode", CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
        String json = new Gson().toJson(map);
        LOG.info("{}", json);
        return json;
    }

    public static String getModifyChargeAsPecentageLoanAmountWithInterestJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("chargeCalculationType", CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST);
        map.put("chargePaymentMode", CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
        String json = new Gson().toJson(map);
        LOG.info("{}", json);
        return json;
    }

    public static String getModifyChargeAsPercentageInterestJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("chargeCalculationType", CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST);
        map.put("chargePaymentMode", CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
        String json = new Gson().toJson(map);
        LOG.info("{}", json);
        return json;
    }

    public static String getModifyChargeFeeFrequencyAsDaysJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("feeFrequency", ChargesHelper.CHARGE_FEE_FREQUENCY_DAYS);
        map.put("feeInterval", 2);
        String json = new Gson().toJson(map);
        LOG.info("{}", json);
        return json;
    }

    public static String getModifyChargeFeeFrequencyAsWeeksJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("feeFrequency", ChargesHelper.CHARGE_FEE_FREQUENCY_WEEKS);
        map.put("feeInterval", 2);
        String json = new Gson().toJson(map);
        LOG.info("{}", json);
        return json;
    }

    public static String getModifyChargeFeeFrequencyAsMonthsJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("feeFrequency", ChargesHelper.CHARGE_FEE_FREQUENCY_MONTHS);
        map.put("feeInterval", 2);
        String json = new Gson().toJson(map);
        LOG.info("{}", json);
        return json;
    }

    public static String getModifyChargeFeeFrequencyAsYearsJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("feeFrequency", ChargesHelper.CHARGE_FEE_FREQUENCY_YEARS);
        map.put("feeInterval", 2);
        String json = new Gson().toJson(map);
        LOG.info("{}", json);
        return json;
    }

    public static String getChargeSpecifiedDueDateJSON() {
        final HashMap<String, Object> map = populateDefaultsClientCharge();
        String chargesCreateJson = new Gson().toJson(map);
        LOG.info("chargesCreateJson: {}", chargesCreateJson);
        return chargesCreateJson;
    }

    public static String applyCharge(RequestSpecification requestSpec, ResponseSpecification responseSpec, String chargeId, String json) {
        return Utils.performServerPost(requestSpec, responseSpec, CHARGES_URL + "/" + chargeId + "?" + Utils.TENANT_IDENTIFIER, json,
                "status");

    }

    public PostChargesResponse createCharges(PostChargesRequest request) {
        return ok(fineract().charges.createCharge(request));
    }
}
