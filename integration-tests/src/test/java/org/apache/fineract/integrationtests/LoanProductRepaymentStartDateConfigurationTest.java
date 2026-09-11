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

import java.time.LocalDate;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanProductRepaymentStartDateConfigurationTest extends FeignLoanTestBase {

    private static final Integer REPAYMENT_START_DATE_TYPE_DISBURSEMENT_DATE = 1;
    private static final Integer REPAYMENT_START_DATE_TYPE_SUBMITTED_ON_DATE = 2;

    private final FeignDelinquencyHelper delinquencyHelper = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void loanProductWithRepaymentStartDateTypeConfigurationCreateAndModifyTest() {
        // create product with repayment start date configuration, get , modify

        // Delinquency Bucket
        final Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

        // create loan product with repayment start date configuration
        Long loanProductId = createLoanProductWithRepaymentStartDateTypeConfiguration(delinquencyBucketId,
                REPAYMENT_START_DATE_TYPE_SUBMITTED_ON_DATE);

        GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(REPAYMENT_START_DATE_TYPE_SUBMITTED_ON_DATE,
                getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
        assertEquals("repaymentStartDateType.submittedOnDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());

        // modify loan product repayment start date configuration to disbursement date

        PutLoanProductsProductIdResponse loanProductModifyResponse = updateRepaymentStartDateType(getLoanProductsProductResponse.getId());
        assertNotNull(loanProductModifyResponse);

        getLoanProductsProductResponse = retrieveLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(REPAYMENT_START_DATE_TYPE_DISBURSEMENT_DATE,
                getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
        assertEquals("repaymentStartDateType.disbursementDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());

    }

    @Test
    public void loanProductWithNoRepaymentStartDateTypeConfigurationDefaultsToDisbursementDateTest() {
        // create loan product with no configuration for repayment start date and verify that it is disbursement date by
        // default
        // Delinquency Bucket
        final Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

        // create loan product with repayment start date configuration
        Long loanProductId = createLoanProductWithRepaymentStartDateTypeConfiguration(delinquencyBucketId, null);

        GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(REPAYMENT_START_DATE_TYPE_DISBURSEMENT_DATE,
                getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
        assertEquals("repaymentStartDateType.disbursementDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());
    }

    @Test
    public void loanAccountWithLoanProductRepaymentStartDateTypeAsSubmittedOnDateScheduleTest() {
        // create loan account with product with repayment start date type configuration as submitted on date, verify
        // repayment schedule is according to submitted on date, before and after disbursements
        runAt("2023-03-03", () -> {

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = createClient();

            // Loan Product creation with repayment start date type configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
                    REPAYMENT_START_DATE_TYPE_SUBMITTED_ON_DATE);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(REPAYMENT_START_DATE_TYPE_SUBMITTED_ON_DATE,
                    getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
            assertEquals("repaymentStartDateType.submittedOnDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());

            // create loan account with submitted date as business date (03 March 2023) and expected disbursement date
            // as future date (07 March 2023)
            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);

            // verify loan schedule is according to submitted on date

            assertNotNull(loanDetails.getRepaymentSchedule());
            // loan term
            assertEquals(92L, loanDetails.getRepaymentSchedule().getLoanTermInDays());

            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));

            // first period [2023-03-03 to 2023-04-03]
            verifyPeriod(loanDetails, 1, 1, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3), 333.33);

            // second period [2023-04-03 to 2023-05-03]
            verifyPeriod(loanDetails, 2, 2, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3), 333.33);

            // third period [2023-05-03 to 2023-06-03]
            verifyPeriod(loanDetails, 3, 3, LocalDate.of(2023, 5, 3), LocalDate.of(2023, 6, 3), 333.34);

            // first disbursement on a future date (7 March 2023)

            updateBusinessDate("07 March 2023");

            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(500.0, "07 March 2023"));

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to submitted on date after first disbursement
            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());
            // loan term
            assertEquals(92L, loanDetails.getRepaymentSchedule().getLoanTermInDays());
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalDisbursed()));

            // first period [2023-03-03 to 2023-04-03]
            verifyPeriod(loanDetails, 1, 1, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3), 166.67);

            // second period [2023-04-03 to 2023-05-03]
            verifyPeriod(loanDetails, 2, 2, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3), 166.67);

            // third period [2023-05-03 to 2023-06-03]
            verifyPeriod(loanDetails, 3, 3, LocalDate.of(2023, 5, 3), LocalDate.of(2023, 6, 3), 166.66);

            // second disbursement next month (7 April 2023)

            updateBusinessDate("07 April 2023");

            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(500.0, "07 April 2023"));

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to submitted on date after second disbursement

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());
            // loan term
            assertEquals(92L, loanDetails.getRepaymentSchedule().getLoanTermInDays());
            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalDisbursed()));

            // first period [2023-03-03 to 2023-04-03]
            verifyPeriod(loanDetails, 1, 1, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3), 333.33);

            // second period [2023-04-03 to 2023-05-03]
            verifyPeriod(loanDetails, 3, 2, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3), 333.33);

            // third period [2023-05-03 to 2023-06-03]
            verifyPeriod(loanDetails, 4, 3, LocalDate.of(2023, 5, 3), LocalDate.of(2023, 6, 3), 333.34);
        });

    }

    @Test
    public void loanAccountWithLoanProductRepaymentStartDateTypeAsDisbursementDateScheduleTest() {
        // create loan account with loan product with repayment start date type configuration as disbursement date ,
        // verify repayment schedule is as per disbursement date before and after disbursements

        runAt("2023-03-03", () -> {

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = createClient();

            // Loan Product creation with repayment date type configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
                    REPAYMENT_START_DATE_TYPE_DISBURSEMENT_DATE);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(REPAYMENT_START_DATE_TYPE_DISBURSEMENT_DATE,
                    getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
            assertEquals("repaymentStartDateType.disbursementDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());

            // create loan account with submitted date as business date (03 March 2023) and expected disbursement date
            // (07 March 2023)
            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);

            // verify loan schedule is according to disbursement date

            assertNotNull(loanDetails.getRepaymentSchedule());

            // loan term
            assertEquals(92L, loanDetails.getRepaymentSchedule().getLoanTermInDays());

            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));

            // first period [2023-03-07 to 2023-04-07]
            verifyPeriod(loanDetails, 1, 1, LocalDate.of(2023, 3, 7), LocalDate.of(2023, 4, 7), 333.33);

            // second period [2023-04-07 to 2023-05-07]
            verifyPeriod(loanDetails, 2, 2, LocalDate.of(2023, 4, 7), LocalDate.of(2023, 5, 7), 333.33);

            // third period [2023-05-07 to 2023-06-07]
            verifyPeriod(loanDetails, 3, 3, LocalDate.of(2023, 5, 7), LocalDate.of(2023, 6, 7), 333.34);

            // first disbursement (7 March 2023)

            updateBusinessDate("07 March 2023");

            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(500.0, "07 March 2023"));

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to disbursement date
            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // loan term
            assertEquals(92L, loanDetails.getRepaymentSchedule().getLoanTermInDays());
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalDisbursed()));

            // first period [2023-03-07 to 2023-04-07]
            verifyPeriod(loanDetails, 1, 1, LocalDate.of(2023, 3, 7), LocalDate.of(2023, 4, 7), 166.67);

            // second period [2023-04-07 to 2023-05-07]
            verifyPeriod(loanDetails, 2, 2, LocalDate.of(2023, 4, 7), LocalDate.of(2023, 5, 7), 166.67);

            // third period [2023-05-07 to 2023-06-07]
            verifyPeriod(loanDetails, 3, 3, LocalDate.of(2023, 5, 7), LocalDate.of(2023, 6, 7), 166.66);

            // second disbursement next month (7 April 2023)

            updateBusinessDate("07 April 2023");

            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(500.0, "07 April 2023"));

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to disbursement after second disbursement

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // loan term
            assertEquals(92L, loanDetails.getRepaymentSchedule().getLoanTermInDays());
            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalDisbursed()));

            // first period [2023-03-07 to 2023-04-07]
            verifyPeriod(loanDetails, 2, 1, LocalDate.of(2023, 3, 7), LocalDate.of(2023, 4, 7), 333.33);

            // second period [2023-04-07 to 2023-05-07]
            verifyPeriod(loanDetails, 3, 2, LocalDate.of(2023, 4, 7), LocalDate.of(2023, 5, 7), 333.33);

            // third period [2023-05-07 to 2023-06-07]
            verifyPeriod(loanDetails, 4, 3, LocalDate.of(2023, 5, 7), LocalDate.of(2023, 6, 7), 333.34);
        });

    }

    private void verifyPeriod(GetLoansLoanIdResponse loanDetails, int index, int expectedPeriod, LocalDate expectedFromDate,
            LocalDate expectedDueDate, double expectedInstallmentAmount) {
        var period = loanDetails.getRepaymentSchedule().getPeriods().get(index);
        assertEquals(expectedPeriod, period.getPeriod());
        assertEquals(expectedFromDate, period.getFromDate());
        assertEquals(expectedDueDate, period.getDueDate());
        assertEquals(expectedInstallmentAmount, Utils.getDoubleValue(period.getTotalInstallmentAmountForPeriod()));
    }

    private PutLoanProductsProductIdResponse updateRepaymentStartDateType(Long id) {
        // repayment start date configuration
        final PutLoanProductsProductIdRequest requestModifyLoan = new PutLoanProductsProductIdRequest()
                .repaymentStartDateType(REPAYMENT_START_DATE_TYPE_DISBURSEMENT_DATE).locale("en");
        return updateLoanProduct(id, requestModifyLoan);
    }

    private Long createLoanProductWithRepaymentStartDateTypeConfiguration(final Long delinquencyBucketId,
            final Integer repaymentStartDateType) {
        return createLoanProduct(
                new LoanProductTestBuilder().withRepaymentStartDateType(repaymentStartDateType).buildRequest(null, delinquencyBucketId));
    }

    private Long createLoanAccountMultipleRepaymentsDisbursement(final Long clientId, final Long loanProductId, final String externalId) {
        final Long loanId = applyForLoan(LoanRequestBuilders.applyLoan(clientId, loanProductId, "03 March 2023", 1000.0, 3)//
                .expectedDisbursementDate("07 March 2023")//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .externalId(externalId));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "03 March 2023"));
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
            final Integer repaymentStartDateType) {
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("3").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withRepaymentStartDateType(repaymentStartDateType).buildRequest());
        return retrieveLoanProduct(loanProductId);
    }

}
