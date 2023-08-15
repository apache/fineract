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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanRepaymentScheduleWithDownPaymentTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ClientHelper clientHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanRepaymentScheduleWithSimpleDisbursementAndDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, false);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedDownPaymentAmount = 250.00;
        LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedRepaymentAmount = 750.00;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedDownPaymentAmount.equals(period.getTotalDueForPeriod()) //
                        && expectedDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
    }

    @Test
    public void loanRepaymentScheduleWithSimpleDisbursementAndAutoRepaymentDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = true;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, false);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedDownPaymentAmount = 250.00;
        LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedRepaymentAmount = 750.00;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedDownPaymentAmount.equals(period.getTotalPaidForPeriod()) //
                        && expectedDownPaymentDueDate.equals(period.getDueDate())));
        assertEquals(expectedRepaymentAmount, loanDetails.getSummary().getTotalOutstanding());
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductOneDisbursementAndDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedDownPaymentAmount = 250.00;
        LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedRepaymentAmount = 750.00;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedDownPaymentAmount.equals(period.getTotalDueForPeriod()) //
                        && expectedDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedFirstDownPaymentAmount = 175.00;
        LocalDate expectedFirstDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedSecondDownPaymentAmount = 75.00;
        LocalDate expectedSecondDownPaymentDueDate = LocalDate.of(2022, 9, 4);
        Double expectedRepaymentAmount = 750.00;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedFirstDownPaymentAmount.equals(period.getTotalDueForPeriod()) //
                        && expectedFirstDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream() //
                .anyMatch(period -> expectedSecondDownPaymentAmount.equals(period.getTotalDueForPeriod())
                        && expectedSecondDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndAutoRepaymentDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = true;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedFirstDownPaymentAmount = 175.00;
        LocalDate expectedFirstDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedSecondDownPaymentAmount = 75.00;
        LocalDate expectedSecondDownPaymentDueDate = LocalDate.of(2022, 9, 4);
        Double expectedRepaymentAmount = 750.00;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedFirstDownPaymentAmount.equals(period.getTotalPaidForPeriod()) //
                        && expectedFirstDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream() //
                .anyMatch(period -> expectedSecondDownPaymentAmount.equals(period.getTotalPaidForPeriod())
                        && expectedSecondDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
        assertEquals(expectedRepaymentAmount, loanDetails.getSummary().getTotalOutstanding());
    }

    private Integer createLoanProductWithDownPaymentConfiguration(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId, Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment,
            Boolean enableAutoRepaymentForDownPayment, boolean multiDisbursement) {
        HashMap<String, Object> loanProductMap;
        if (multiDisbursement) {
            loanProductMap = new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments() //
                    .withInterestTypeAsDecliningBalance().withMoratorium("", "").withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                    .withInterestTypeAsDecliningBalance() //
                    .withMultiDisburse() //
                    .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment) //
                    .withDisallowExpectedDisbursements(true) //
                    .build(null, delinquencyBucketId);
        } else {
            loanProductMap = new LoanProductTestBuilder() //
                    .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment) //
                    .build(null, delinquencyBucketId);
        }
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanProductId;
    }

    private Integer createApproveAndDisburseLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithTransactionAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

    private Integer createApproveAndDisburseTwiceLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("04 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithTransactionAmount("03 September 2022", loanId, "700");
        loanTransactionHelper.disburseLoanWithTransactionAmount("04 September 2022", loanId, "300");
        return loanId;
    }
}
