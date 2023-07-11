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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.client.models.GetPaymentTypesPaymentTypeIdResponse;
import org.apache.fineract.client.models.PostPaymentTypesRequest;
import org.apache.fineract.client.models.PostPaymentTypesResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class RepaymentWithPostDatedChecksTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private final SimpleDateFormat dateFormatterStandard = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
    private LoanTransactionHelper loanTransactionHelper;
    private PaymentTypeHelper paymentTypeHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.paymentTypeHelper = new PaymentTypeHelper();
    }

    @Test
    public void testRepaymentWithPostDatedChecks() {
        this.loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

        Calendar meetingCalendar = Calendar.getInstance();
        meetingCalendar.set(2012, 3, 4);

        final String disbursalDate = this.dateFormatterStandard.format(meetingCalendar.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        Assertions.assertNotNull(loanProductID, "Could not create Loan Product");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "8000");
        Assertions.assertNotNull(loanID, "Could not create Loan Account");

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        // Test for loan account is created, can be approved
        this.loanTransactionHelper.approveLoan(disbursalDate, loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        // Get repayments Template for Repayment
        ArrayList<HashMap> installmentData = this.loanTransactionHelper.getRepayments(loanID);
        Assertions.assertNotNull(installmentData, "Empty Installment Data Template");

        // Get repayments for Disburse
        installmentData = this.loanTransactionHelper.getRepayments(loanID);
        Assertions.assertNotNull(installmentData, "Empty Installment Data");
        List<HashMap> postDatedChecks = new ArrayList<>();
        Gson gson = new Gson();

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        // Get the first installment date
        ArrayList installmentDate = (ArrayList) installmentData.get(0).get("date");
        Assertions.assertNotNull(installmentDate);
        Assertions.assertEquals(3, installmentDate.size());
        Calendar calendar = Calendar.getInstance();
        calendar.set((Integer) installmentDate.get(0), (Integer) installmentDate.get(1) - 1, (Integer) installmentDate.get(2));
        final String LOAN_REPAYMENT_DATE = dateFormat.format(calendar.getTime());
        Float firstInstallmentAmount = (Float) installmentData.get(0).get("amount");

        for (int i = 0; i < installmentData.size(); i++) {
            String result = gson.toJson(installmentData.get(i));
            JsonObject reportObject = JsonParser.parseString(result).getAsJsonObject();
            final Integer installmentId = reportObject.get("installmentId").getAsInt();
            final BigDecimal amount = reportObject.get("amount").getAsBigDecimal();
            postDatedChecks.add(postDatedCheck(installmentId, amount));
        }

        Assertions.assertNotNull(postDatedChecks);

        // Test for loan account approved can be disbursed
        this.loanTransactionHelper.disburseLoanWithPostDatedChecks(disbursalDate, loanID, BigDecimal.valueOf(8000), postDatedChecks);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // Create payment type PDC - Post Dated Checks
        String name = "PDC";
        String description = PaymentTypeHelper.randomNameGenerator("PDC", 15);
        Boolean isCashPayment = false;
        Integer position = 1;

        PostPaymentTypesResponse paymentTypesResponse = paymentTypeHelper.createPaymentType(
                new PostPaymentTypesRequest().name(name).description(description).isCashPayment(isCashPayment).position(position));
        Long paymentTypeId = paymentTypesResponse.getResourceId();
        Assertions.assertNotNull(paymentTypeId);
        paymentTypeHelper.verifyPaymentTypeCreatedOnServer(paymentTypeId);
        GetPaymentTypesPaymentTypeIdResponse paymentTypeResponse = paymentTypeHelper.retrieveById(paymentTypeId);
        Assertions.assertEquals(name, paymentTypeResponse.getName());

        // Repay for the installment 1 using post dated check
        HashMap postDatedCheck = this.loanTransactionHelper.getPostDatedCheck(loanID, Integer.valueOf(1));
        Assertions.assertNotNull(postDatedCheck);
        Assertions.assertNotNull(Float.valueOf(String.valueOf(postDatedCheck.get("amount"))));

        this.loanTransactionHelper.makeRepaymentWithPDC(LOAN_REPAYMENT_DATE, firstInstallmentAmount, loanID, paymentTypeId);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String proposedAmount) {
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));
        final String loanApplication = new LoanApplicationTestBuilder().withPrincipal(proposedAmount).withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate("04 April 2012")
                .withCollaterals(collaterals).withSubmittedOnDate("02 April 2012")
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<String, String>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

    private HashMap<String, String> postDatedCheck(final Integer installmentId, final BigDecimal amount) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("installmentId", installmentId.toString());
        map.put("name", "AMANA BANK");
        map.put("amount", amount.toString());
        map.put("accountNo", "900400500621");
        map.put("checkNo", Utils.uniqueRandomNumberGenerator(9).toString());

        return map;
    }

}
