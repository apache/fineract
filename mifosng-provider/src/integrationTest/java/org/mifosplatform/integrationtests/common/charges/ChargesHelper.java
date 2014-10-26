/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.charges;

import java.util.ArrayList;
import java.util.HashMap;

import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ChargesHelper {

    private static final String CHARGES_URL = "/mifosng-provider/api/v1/charges";
    private static final String CREATE_CHARGES_URL = CHARGES_URL + "?" + Utils.TENANT_IDENTIFIER;

    private static final Integer CHARGE_APPLIES_TO_LOAN = 1;
    private static final Integer CHARGE_APPLIES_TO_SAVINGS = 2;

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

    private final static boolean active = true;
    private final static boolean penalty = true;
    private final static String amount = "100";
    private final static String currencyCode = "USD";
    public final static String feeOnMonthDay = "04 March";
    private final static String monthDayFormat = "dd MMM";

    public static String getSavingsSpecifiedDueDateJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("chargeTimeType", CHARGE_SPECIFIED_DUE_DATE);
        map.put("feeInterval", 2);
        String chargesCreateJson = new Gson().toJson(map);
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getSavingsActivationFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("chargeTimeType", CHARGE_SAVINGS_ACTIVATION_FEE);
        String chargesCreateJson = new Gson().toJson(map);
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getSavingsWithdrawalFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("chargeTimeType", CHARGE_WITHDRAWAL_FEE);
        String chargesCreateJson = new Gson().toJson(map);
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getSavingsAnnualFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("feeOnMonthDay", ChargesHelper.feeOnMonthDay);
        map.put("chargeTimeType", CHARGE_ANNUAL_FEE);
        String chargesCreateJson = new Gson().toJson(map);
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getSavingsMonthlyFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("feeOnMonthDay", ChargesHelper.feeOnMonthDay);
        map.put("chargeTimeType", CHARGE_MONTHLY_FEE);
        map.put("feeInterval", 2);
        String chargesCreateJson = new Gson().toJson(map);
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }
    
    public static String getSavingsWeeklyFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("chargeTimeType", WEEKLY_FEE);
        map.put("feeInterval", 1);
        String chargesCreateJson = new Gson().toJson(map);
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getSavingsOverdraftFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForSavings();
        map.put("chargeTimeType", CHARGE_OVERDRAFT_FEE);
        String chargesCreateJson = new Gson().toJson(map);
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }

    public static HashMap<String, Object> populateDefaultsForSavings() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("active", ChargesHelper.active);
        map.put("amount", ChargesHelper.amount);
        map.put("chargeAppliesTo", ChargesHelper.CHARGE_APPLIES_TO_SAVINGS);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT);
        map.put("currencyCode", ChargesHelper.currencyCode);
        map.put("locale", CommonConstants.locale);
        map.put("monthDayFormat", ChargesHelper.monthDayFormat);
        map.put("name", Utils.randomNameGenerator("Charge_Savings_", 6));
        return map;
    }

    public static String getLoanDisbursementJSON() {
        return getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, ChargesHelper.amount);
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
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getLoanSpecifiedDueDateJSON() {
        return getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, ChargesHelper.amount, ChargesHelper.penalty);
    }

    public static String getLoanSpecifiedDueDateJSON(final Integer chargeCalculationType, final String amount, boolean penalty) {
        return getLoanSpecifiedDueDateJSON(chargeCalculationType, amount, penalty, ChargesHelper.CHARGE_PAYMENT_MODE_REGULAR);
    }

    public static String getLoanSpecifiedDueDateJSON(final Integer chargeCalculationType, final String amount, final boolean penalty,
            final Integer paymentMode) {
        final HashMap<String, Object> map = populateDefaultsForLoan();
        map.put("chargeTimeType", CHARGE_SPECIFIED_DUE_DATE);
        map.put("chargePaymentMode", paymentMode);
        map.put("penalty", penalty);
        map.put("amount", amount);
        map.put("chargeCalculationType", chargeCalculationType);

        String chargesCreateJson = new Gson().toJson(map);
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getLoanSpecifiedDueDateWithAccountTransferJSON(final Integer chargeCalculationType, final String amount,
            boolean penalty) {
        return getLoanSpecifiedDueDateJSON(chargeCalculationType, amount, penalty, ChargesHelper.CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
    }

    public static String getLoanSpecifiedDueDateWithAccountTransferJSON() {
        return getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, ChargesHelper.amount, ChargesHelper.penalty,
                ChargesHelper.CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
    }

    public static String getLoanInstallmentJSON(final Integer chargeCalculationType, final String amount, boolean penalty) {
        return getLoanInstallmentJSON(chargeCalculationType, amount, penalty, ChargesHelper.CHARGE_PAYMENT_MODE_REGULAR);
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
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }

    public static String getLoanInstallmentWithAccountTransferJSON(final Integer chargeCalculationType, final String amount, boolean penalty) {
        return getLoanInstallmentJSON(chargeCalculationType, amount, penalty, ChargesHelper.CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
    }

    public static String getLoanInstallmentFeeJSON() {
        return getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, ChargesHelper.amount, ChargesHelper.penalty);
    }

    public static String getLoanOverdueFeeJSON() {
        final HashMap<String, Object> map = populateDefaultsForLoan();
        map.put("penalty", ChargesHelper.penalty);
        map.put("chargePaymentMode", ChargesHelper.CHARGE_PAYMENT_MODE_REGULAR);
        map.put("chargeTimeType", CHARGE_OVERDUE_INSTALLMENT_FEE);
        map.put("feeFrequency", ChargesHelper.CHARGE_FEE_FREQUENCY_MONTHS);
        map.put("feeOnMonthDay", ChargesHelper.feeOnMonthDay);
        map.put("feeInterval", 2);
        String chargesCreateJson = new Gson().toJson(map);
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }
    
    public static String getLoanOverdueFeeJSONWithCalculattionTypePercentage() {
        final HashMap<String, Object> map = populateDefaultsForLoan();
        map.put("penalty", ChargesHelper.penalty);
        map.put("amount", "10");
        map.put("chargePaymentMode", ChargesHelper.CHARGE_PAYMENT_MODE_REGULAR);
        map.put("chargeTimeType", CHARGE_OVERDUE_INSTALLMENT_FEE);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST);
        String chargesCreateJson = new Gson().toJson(map);
        System.out.println(chargesCreateJson);
        return chargesCreateJson;
    }

    public static HashMap<String, Object> populateDefaultsForLoan() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("active", ChargesHelper.active);
        map.put("amount", ChargesHelper.amount);
        map.put("chargeAppliesTo", ChargesHelper.CHARGE_APPLIES_TO_LOAN);
        map.put("chargeCalculationType", ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT);
        map.put("currencyCode", ChargesHelper.currencyCode);
        map.put("locale", CommonConstants.locale);
        map.put("monthDayFormat", ChargesHelper.monthDayFormat);
        map.put("name", Utils.randomNameGenerator("Charge_Loans_", 6));
        return map;
    }

    public static Integer createCharges(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String request) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CHARGES_URL, request, "resourceId");
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
        map.put("locale", CommonConstants.locale);
        map.put("amount", "200.0");
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getModifyWithdrawalFeeSavingsChargeJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("chargeCalculationType", CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getModifyChargeAsPecentageAmountJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("chargeCalculationType", CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT);
        map.put("chargePaymentMode", CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getModifyChargeAsPecentageLoanAmountWithInterestJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("chargeCalculationType", CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST);
        map.put("chargePaymentMode", CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getModifyChargeAsPercentageInterestJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("chargeCalculationType", CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST);
        map.put("chargePaymentMode", CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getModifyChargeFeeFrequencyAsDaysJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("feeFrequency", ChargesHelper.CHARGE_FEE_FREQUENCY_DAYS);
        map.put("feeInterval", 2);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getModifyChargeFeeFrequencyAsWeeksJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("feeFrequency", ChargesHelper.CHARGE_FEE_FREQUENCY_WEEKS);
        map.put("feeInterval", 2);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getModifyChargeFeeFrequencyAsMonthsJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("feeFrequency", ChargesHelper.CHARGE_FEE_FREQUENCY_MONTHS);
        map.put("feeInterval", 2);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getModifyChargeFeeFrequencyAsYearsJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("feeFrequency", ChargesHelper.CHARGE_FEE_FREQUENCY_YEARS);
        map.put("feeInterval", 2);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

}