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
package org.apache.fineract.integrationtests.interoperation;

import static org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL;
import static org.apache.fineract.integrationtests.interoperation.InteropHelper.PARAM_ACCOUNT_BALANCE;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.internal.path.ObjectConverter;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.interoperation.domain.InteropActionState;
import org.apache.fineract.interoperation.domain.InteropIdentifierType;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.apache.fineract.interoperation.util.InteropUtil;
import org.apache.fineract.interoperation.util.MathUtil;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteropTest {

    private final static Logger log = LoggerFactory.getLogger(InteropTest.class);

    private final static String MIN_INTEREST_CALCULATON_BALANCE = null;
    private final static String MIN_REQUIRED_BALANCE = null;
    private final static String MIN_OPENING_BALANCE = "100000.0";
    private final static boolean ENFORCE_MIN_REQUIRED_BALANCE = false;
    private final static MathContext MATHCONTEXT  = new MathContext(12, RoundingMode.HALF_EVEN);

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private ResponseSpecification responseClientErrorSpec;
    private ResponseSpecification responseErrorSpec;

    private AccountHelper accountHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private InteropHelper interopHelper;

    private Integer clientId;
    private Integer savingsProductId;
    private Integer savingsId;
    private Integer chargeId;
    private String requestCode;
    private String quoteCode;
    private String transferCode;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());

        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        responseClientErrorSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        responseErrorSpec = new ResponseSpecBuilder().expectStatusCode(500).build();

        String savingsExternalId = UUID.randomUUID().toString();
        String transactionCode = UUID.randomUUID().toString();

        accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);
        interopHelper = new InteropHelper(requestSpec, responseSpec, savingsExternalId, transactionCode);
    }

    @Test
    public void testInteroperation() {
        createClient();
        createSavingsProduct();
        createCharge();
        openSavingsAccount();

        testParties();
        testRequests();
        testQuotes();
        testTransfers();
    }

    private void createClient() {
        clientId = ClientHelper.createClient(requestSpec, responseSpec);
        Assert.assertNotNull(clientId);
    }

    private void createSavingsProduct() {
        log.debug("------------------------------ Create Interoperable Saving Product ---------------------------------------");

        Account[] accounts = {accountHelper.createAssetAccount(), accountHelper.createIncomeAccount(), accountHelper.createExpenseAccount(),
                accountHelper.createLiabilityAccount()};

        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final String savingsProductJSON = savingsProductHelper
                .withCurrencyCode(interopHelper.getCurrency())
                .withNominalAnnualInterestRate(BigDecimal.ZERO)
                .withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsMonthly()
                .withInterestCalculationPeriodTypeAsDailyBalance()
                .withMinBalanceForInterestCalculation(MIN_INTEREST_CALCULATON_BALANCE)
                .withMinRequiredBalance(MIN_REQUIRED_BALANCE)
                .withEnforceMinRequiredBalance(Boolean.toString(ENFORCE_MIN_REQUIRED_BALANCE))
                .withMinimumOpenningBalance(MIN_OPENING_BALANCE)
                .withAccountingRuleAsCashBased(accounts)
                .build();
        savingsProductId = SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
        Assert.assertNotNull(savingsProductId);

        log.debug("Sucessfully created Interoperable Saving Product (id: {})", savingsProductId);
    }

    private void createCharge() {
        chargeId = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getSavingsJSON(interopHelper.getFee().toString(),
                interopHelper.getCurrency(), ChargeTimeType.WITHDRAWAL_FEE));
        Assert.assertNotNull(chargeId);
    }

    private void openSavingsAccount() {
        log.debug("------------------------------ Create Interoperable Saving Account ---------------------------------------");
        savingsId = savingsAccountHelper.applyForSavingsApplicationWithExternalId(clientId, savingsProductId, ACCOUNT_TYPE_INDIVIDUAL, interopHelper.getAccountExternalId(), true);
        Assert.assertNotNull(savingsId);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(requestSpec, responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        if (chargeId != null) {
            savingsAccountHelper.addChargesForSavings(savingsId, chargeId, false, interopHelper.getFee());
        }

        log.debug("Sucessfully created Interoperable Saving Account (id: {})", savingsId);
    }

    private void testParties() {
        String idValue = UUID.randomUUID().toString();
        String accountId = interopHelper.postParty(InteropIdentifierType.MSISDN, idValue);
        Assert.assertEquals(interopHelper.getAccountExternalId(), accountId);

        interopHelper.setResponseSpec(responseErrorSpec);
        accountId = interopHelper.postParty(InteropIdentifierType.MSISDN, idValue);
        Assert.assertNull(accountId);
        interopHelper.setResponseSpec(responseSpec);

        accountId = interopHelper.getParty(InteropIdentifierType.MSISDN, idValue);
        Assert.assertEquals(interopHelper.getAccountExternalId(), accountId);

        accountId = interopHelper.deleteParty(InteropIdentifierType.MSISDN, idValue);
        Assert.assertEquals(interopHelper.getAccountExternalId(), accountId);

        interopHelper.setResponseSpec(responseErrorSpec);
        accountId = interopHelper.getParty(InteropIdentifierType.MSISDN, idValue);
        Assert.assertNull(accountId);
        interopHelper.setResponseSpec(responseSpec);
    }

    private void testRequests() {
        requestCode = UUID.randomUUID().toString();
        String response = interopHelper.postTransactionRequest(requestCode, InteropTransactionRole.PAYER);
        JsonPath json = JsonPath.from(response);
        Assert.assertEquals(requestCode, json.getString(InteropUtil.PARAM_REQUEST_CODE));
        Assert.assertEquals(InteropActionState.ACCEPTED.toString(), json.getString(InteropHelper.PARAM_ACTION_STATE));

        interopHelper.setResponseSpec(responseClientErrorSpec);
        interopHelper.postTransactionRequest(requestCode, InteropTransactionRole.PAYEE);
        interopHelper.setResponseSpec(responseSpec);
    }

    private void testQuotes() {
        // payer
        quoteCode = UUID.randomUUID().toString();
        String response = interopHelper.postQuote(quoteCode, InteropTransactionRole.PAYER);
        JsonPath json = JsonPath.from(response);
        Assert.assertEquals(quoteCode, json.getString(InteropUtil.PARAM_QUOTE_CODE));
        Assert.assertEquals(InteropActionState.ACCEPTED.toString(), json.getString(InteropHelper.PARAM_ACTION_STATE));

        Map<Object, Object> fee = json.getMap(InteropUtil.PARAM_FSP_FEE);
        Assert.assertNotNull(fee);
        BigDecimal feeAmount = ObjectConverter.convertObjectTo(fee.get(InteropUtil.PARAM_AMOUNT), BigDecimal.class);
        Assert.assertTrue("Quote fee expected: " + interopHelper.getFee() + ", actual: " + feeAmount, MathUtil.isEqualTo(interopHelper.getFee(), feeAmount));
        Assert.assertEquals(interopHelper.getCurrency(), fee.get(InteropUtil.PARAM_CURRENCY));

        // payee
        response = interopHelper.postQuote(quoteCode, InteropTransactionRole.PAYEE);
        json = JsonPath.from(response);
        Assert.assertEquals(quoteCode, json.getString(InteropUtil.PARAM_QUOTE_CODE));
        Assert.assertEquals(InteropActionState.ACCEPTED.toString(), json.getString(InteropHelper.PARAM_ACTION_STATE));

        fee = json.getMap(InteropUtil.PARAM_FSP_FEE);
        if (fee != null) {
            feeAmount = ObjectConverter.convertObjectTo(fee.get(InteropUtil.PARAM_AMOUNT), BigDecimal.class);
            Assert.assertTrue("PAYEE Quote fee expected: " + BigDecimal.ZERO + ", actual: " + feeAmount, MathUtil.isZero(feeAmount));
        }
    }

    private void testTransfers() {
        String savings = (String) savingsAccountHelper.getSavingsAccountDetail(savingsId, null);
        JsonPath savingsJson = JsonPath.from(savings);
        BigDecimal onHold = ObjectConverter.convertObjectTo(savingsJson.get(SavingsApiConstants.savingsAmountOnHold), BigDecimal.class);
        BigDecimal balance = ObjectConverter.convertObjectTo(savingsJson.get(PARAM_ACCOUNT_BALANCE), BigDecimal.class);

        transferCode = UUID.randomUUID().toString();
        String response = interopHelper.prepareTransfer(transferCode);
        JsonPath json = JsonPath.from(response);
        Assert.assertEquals(transferCode, json.getString(InteropUtil.PARAM_TRANSFER_CODE));
        Assert.assertEquals(InteropActionState.ACCEPTED.toString(), json.getString(InteropHelper.PARAM_ACTION_STATE));

        // prepare
        savings = (String) savingsAccountHelper.getSavingsAccountDetail(savingsId, null);
        log.debug("Response Interoperable GET Saving: {}", savings);
        savingsJson = JsonPath.from(savings);
        BigDecimal onHold2 = ObjectConverter.convertObjectTo(savingsJson.get(SavingsApiConstants.savingsAmountOnHold), BigDecimal.class);
        BigDecimal balance2 = ObjectConverter.convertObjectTo(savingsJson.get(PARAM_ACCOUNT_BALANCE), BigDecimal.class);

        BigDecimal transferAmount = interopHelper.getTransferAmount();
        BigDecimal expectedHold = MathUtil.add(onHold, transferAmount, MATHCONTEXT);
        Assert.assertTrue("On hold amount expected: " + expectedHold + ", actual: " + onHold2, MathUtil.isEqualTo(expectedHold, onHold2));
        BigDecimal expectedBalance = MathUtil.subtract(balance, transferAmount, MATHCONTEXT);
        Assert.assertTrue("Balance amount expected: " + expectedBalance + ", actual: " + balance2, MathUtil.isEqualTo(expectedBalance, balance2));

        // payer
        response = interopHelper.createTransfer(transferCode, InteropTransactionRole.PAYER);
        json = JsonPath.from(response);
        Assert.assertEquals(transferCode, json.getString(InteropUtil.PARAM_TRANSFER_CODE));
        Assert.assertEquals(InteropActionState.ACCEPTED.toString(), json.getString(InteropHelper.PARAM_ACTION_STATE));

        savings = (String) savingsAccountHelper.getSavingsAccountDetail(savingsId, null);
        log.debug("Response Interoperable GET Saving: {}", savings);
        savingsJson = JsonPath.from(savings);
        BigDecimal onHold3 = ObjectConverter.convertObjectTo(savingsJson.get(SavingsApiConstants.savingsAmountOnHold), BigDecimal.class);
        BigDecimal balance3 = ObjectConverter.convertObjectTo(savingsJson.get(PARAM_ACCOUNT_BALANCE), BigDecimal.class);
        Assert.assertTrue("On hold amount expected: " + onHold + ", actual: " + onHold3, MathUtil.isEqualTo(onHold, onHold3));
        Assert.assertTrue("Balance amount expected: " + expectedBalance + ", actual: " + balance3, MathUtil.isEqualTo(expectedBalance, balance3));

        // payee
        response = interopHelper.createTransfer(transferCode, InteropTransactionRole.PAYEE);
        json = JsonPath.from(response);
        Assert.assertEquals(transferCode, json.getString(InteropUtil.PARAM_TRANSFER_CODE));
        Assert.assertEquals(InteropActionState.ACCEPTED.toString(), json.getString(InteropHelper.PARAM_ACTION_STATE));

        savings = (String) savingsAccountHelper.getSavingsAccountDetail(savingsId, null);
        log.debug("Response Interoperable GET Saving: {}", savings);
        savingsJson = JsonPath.from(savings);
        BigDecimal onHold4 = ObjectConverter.convertObjectTo(savingsJson.get(SavingsApiConstants.savingsAmountOnHold), BigDecimal.class);
        BigDecimal balance4 = ObjectConverter.convertObjectTo(savingsJson.get(PARAM_ACCOUNT_BALANCE), BigDecimal.class);
        expectedBalance = MathUtil.subtract(balance, interopHelper.getFee(), MATHCONTEXT);
        Assert.assertTrue("On hold amount expected: " + onHold + ", actual: " + onHold4, MathUtil.isEqualTo(onHold, onHold4));
        Assert.assertTrue("Balance amount expected: " + expectedBalance + ", actual: " + balance4, MathUtil.isEqualTo(balance, balance4));
    }
}
