package org.mifosplatform.integrationtests.common.charges;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.portfolio.charge.domain.ChargeAppliesTo;
import org.mifosplatform.portfolio.charge.domain.ChargeCalculationType;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class ChargesHelper {

    private static final String CHARGES_URL = "/mifosng-provider/api/v1/charges";
    private static final String CREATE_CHARGES_URL = CHARGES_URL + "?" + Utils.TENANT_IDENTIFIER;

    private final static boolean active = true;
    private final static String amount = "100";
    private final static Integer chargeAppliesTo = ChargeAppliesTo.SAVINGS.getValue();
    private final static Integer chargeCalculationType = ChargeCalculationType.FLAT.getValue();
    private final static String currencyCode = "USD";
    public final static String feeOnMonthDay = "04 March";
    private final static String monthDayFormat = "dd MMM";
    
    private static String getChargeName(){
        return Utils.randomNameGenerator("Charge_", 6);
    }

    public static String getSavingsAnnualFeeJSON() {
        final HashMap<String, Object> map = populateDefaults();
        map.put("feeOnMonthDay", ChargesHelper.feeOnMonthDay);
        map.put("chargeTimeType", ChargeTimeType.ANNUAL_FEE.getValue());
        String chargesCreateJson = new Gson().toJson(map);
        return chargesCreateJson;
    }
    
    public static String getSavingsMonthlyFeeJSON() {
        final HashMap<String, Object> map = populateDefaults();
        map.put("feeOnMonthDay", ChargesHelper.feeOnMonthDay);
        map.put("chargeTimeType", ChargeTimeType.MONTHLY_FEE.getValue());
        map.put("feeInterval", 2);
        String chargesCreateJson = new Gson().toJson(map);
        return chargesCreateJson;
    }

    public static String getSavingsWithdrawalFeeJSON() {
        final HashMap<String, Object> map = populateDefaults();
        map.put("chargeTimeType", ChargeTimeType.WITHDRAWAL_FEE.getValue());
        String chargesCreateJson = new Gson().toJson(map);
        return chargesCreateJson;
    }

    
    public static HashMap<String, Object> populateDefaults(){
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("active", ChargesHelper.active);
        map.put("amount", ChargesHelper.amount);
        map.put("chargeAppliesTo", ChargesHelper.chargeAppliesTo);
        map.put("chargeCalculationType", ChargesHelper.chargeCalculationType);
        map.put("currencyCode", ChargesHelper.currencyCode);
        map.put("locale", CommonConstants.locale);
        map.put("monthDayFormat", ChargesHelper.monthDayFormat);
        map.put("name", ChargesHelper.getChargeName());
        return map;
    }

    public static Integer createCharges(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,final String request) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CHARGES_URL, request, "resourceId");
    }
    
 
    
}