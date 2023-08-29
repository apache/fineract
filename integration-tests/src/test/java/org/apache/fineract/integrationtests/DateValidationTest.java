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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositAccountHelper;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositProductHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.interoperation.InteropHelper;
import org.apache.fineract.interoperation.domain.InteropInitiatorType;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.apache.fineract.interoperation.domain.InteropTransactionScenario;
import org.apache.fineract.interoperation.util.InteropUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class DateValidationTest {

    public static final String WHOLE_TERM = "1";
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private ResponseSpecification responseSpec;
    private ResponseSpecification errorResponseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private InteropHelper interopHelper;
    private AccountHelper accountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.errorResponseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        this.interopHelper = new InteropHelper(requestSpec, errorResponseSpec);
        this.accountHelper = new AccountHelper(requestSpec, responseSpec);
    }

    @Test
    public void testShouldFailIfDateIsInvalid() {

        String invalidDate = "31 June 2022";

        PostClientsRequest postClientsRequest = ClientHelper.defaultClientCreationRequest();

        PostClientsResponse client = clientHelper.createClient(postClientsRequest);
        Long clientId = client.getClientId();

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withAccountingRuleAsNone().withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30")
                .withDaysInYear("365").withMoratorium("0", "0").withInArrearsTolerance("1001").withMultiDisburse()
                .withDisallowExpectedDisbursements(true).build(null);
        final Integer loanProductID = loanTransactionHelper.getLoanProductId(loanProductJSON);

        loanTransactionHelper = new LoanTransactionHelper(requestSpec, errorResponseSpec);

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(invalidDate).withSubmittedOnDate("01 March 2022").withLoanType("individual")
                .build(clientId.toString(), loanProductID.toString(), null);
        HashMap<String, Object> response = (HashMap) loanTransactionHelper.createLoanAccount(loanApplicationJSON, "");
        List<HashMap<String, Object>> errors = (List) response.get("errors");
        assertNotNull(errors);
        HashMap<String, Object> error = errors.get(0);
        assertNotNull(error);
        assertEquals(
                "The parameter `expectedDisbursementDate` is invalid based on the dateFormat: `dd MMMM yyyy` and locale: `en_GB` provided:",
                error.get("developerMessage"));
    }

    @Test
    public void testShouldFailWithInvalidDateTime() {
        String requestCode = UUID.randomUUID().toString();
        InteropTransactionRole role = InteropTransactionRole.PAYER;
        String requestBody = buildRequestBody(requestCode, role);
        String response = interopHelper.postTransactionRequest(requestCode, role, requestBody);
        HashMap<String, Object> map = new Gson().fromJson(response, new TypeToken<HashMap<String, Object>>() {}.getType());
        List<Map<String, Object>> errors = (List) map.get("errors");
        assertNotNull(errors);
        Map<String, Object> error = errors.get(0);
        assertNotNull(error);
        assertEquals("The parameter `expiration` is invalid based on the dateFormat: `dd MMMM yyyy HH:mm:ss` and locale: `en` provided:",
                error.get("developerMessage"));
    }

    @Test
    public void testShouldFailWithInvalidMonthDay() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter(Locale.US);

        LocalDate todaysDate = LocalDate.now(ZoneId.systemDefault()).minusMonths(3);
        final String VALID_FROM = todaysDate.format(formatter);
        todaysDate = todaysDate.plusYears(10);
        final String VALID_TO = todaysDate.format(formatter);

        todaysDate = LocalDate.now(ZoneId.systemDefault()).minusMonths(1);
        final String SUBMITTED_ON_DATE = todaysDate.format(formatter);

        PostClientsRequest postClientsRequest = ClientHelper.defaultClientCreationRequest();

        Long clientId = clientHelper.createClient(postClientsRequest).getClientId();
        Assertions.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, assetAccount, liabilityAccount, incomeAccount,
                expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        final Integer maturityInstructionId = 400;
        String response = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                maturityInstructionId, getCharges());
        HashMap<String, Object> map = new Gson().fromJson(response, new TypeToken<HashMap<String, Object>>() {}.getType());
        List<Map<String, Object>> errors = (List) map.get("errors");
        assertNotNull(errors);
        Map<String, Object> error = errors.get(0);
        assertNotNull(error);
        assertEquals("The parameter `feeOnMonthDay` is invalid based on the monthDayFormat: `dd MMM` and locale: `en_GB` provided:",
                error.get("developerMessage"));
    }

    private String buildRequestBody(final String requestCode, final InteropTransactionRole role) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(InteropUtil.PARAM_TRANSACTION_CODE, UUID.randomUUID().toString());
        map.put(InteropUtil.PARAM_REQUEST_CODE, requestCode);
        map.put(InteropUtil.PARAM_ACCOUNT_ID, UUID.randomUUID().toString());
        map.put(InteropUtil.PARAM_TRANSACTION_ROLE, role);
        map.put(InteropUtil.PARAM_EXPIRATION, "31 November 2022 11:11:11");
        map.put(InteropUtil.PARAM_LOCALE, "en");
        map.put(InteropUtil.PARAM_DATE_FORMAT, "dd MMMM yyyy HH:mm:ss");

        HashMap<String, Object> amountMap = new HashMap<>();
        amountMap.put(InteropUtil.PARAM_AMOUNT, "10");
        amountMap.put(InteropUtil.PARAM_CURRENCY, "EUR");
        map.put(InteropUtil.PARAM_AMOUNT, amountMap);

        HashMap<String, Object> typeMap = new HashMap<>();
        typeMap.put(InteropUtil.PARAM_SCENARIO, InteropTransactionScenario.PAYMENT);
        typeMap.put(InteropUtil.PARAM_INITIATOR, InteropTransactionRole.PAYEE);
        typeMap.put(InteropUtil.PARAM_INITIATOR_TYPE, InteropInitiatorType.CONSUMER);
        map.put(InteropUtil.PARAM_TRANSACTION_TYPE, typeMap);

        return new Gson().toJson(map);
    }

    private Integer createFixedDepositProduct(final String validFrom, final String validTo, Account... accounts) {
        log.info("------------------------------CREATING NEW FIXED DEPOSIT PRODUCT ---------------------------------------");
        FixedDepositProductHelper fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        fixedDepositProductHelper = fixedDepositProductHelper.withAccountingRuleAsCashBased(accounts);
        final String fixedDepositProductJSON = fixedDepositProductHelper.withPeriodRangeChart() //
                .build(validFrom, validTo, true);
        return FixedDepositProductHelper.createFixedDepositProduct(fixedDepositProductJSON, requestSpec, responseSpec);
    }

    private String applyForFixedDepositApplication(final String clientID, final String productID, final String submittedOnDate,
            final Integer maturityInstructionId, final List<HashMap<String, String>> charges) {
        log.info("--------------------------------APPLYING FOR FIXED DEPOSIT ACCOUNT --------------------------------");
        final String fixedDepositApplicationJSON = new FixedDepositAccountHelper(this.requestSpec, this.errorResponseSpec) //
                .withSubmittedOnDate(submittedOnDate).withMaturityInstructionId(maturityInstructionId).withCharges(charges)
                .build(clientID, productID, WHOLE_TERM);
        return FixedDepositAccountHelper.applyFixedDepositApplication(fixedDepositApplicationJSON, this.requestSpec,
                this.errorResponseSpec);
    }

    private List<HashMap<String, String>> getCharges() {
        List<HashMap<String, String>> list = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        map.put("feeOnMonthDay", "31 June");
        list.add(map);
        return list;
    }
}
