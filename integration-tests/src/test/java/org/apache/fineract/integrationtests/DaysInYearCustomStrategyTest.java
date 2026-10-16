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

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.DaysInYearCustomStrategyRawApi;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DaysInYearCustomStrategyTest extends FeignLoanTestBase {

    private static final Integer DAYS_IN_YEAR_ACTUAL = 1;
    private static final Integer DAYS_IN_YEAR_360 = 360;
    private static final String FEB_29_PERIOD_ONLY = "FEB_29_PERIOD_ONLY";
    private static final String FULL_LEAP_YEAR = "FULL_LEAP_YEAR";
    private static final String INVALID_STRATEGY = "NOT_A_STRATEGY";

    @Test
    public void test_HttpError_for_ValidationError_DaysInYearsCustomStrategy() {
        CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class, () -> createLoanProduct(
                create4IProgressive().daysInYearType(DAYS_IN_YEAR_360).daysInYearCustomStrategy(FEB_29_PERIOD_ONLY)));
        Assertions.assertEquals(403, exception.getStatus());
    }

    @Test
    public void test_Update_DaysInYearsCustomStrategy_Value() {
        Long loanProductId = createLoanProduct(
                create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL).daysInYearCustomStrategy(FEB_29_PERIOD_ONLY));

        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertEquals(FEB_29_PERIOD_ONLY, loanProduct.getDaysInYearCustomStrategy().getId());

        updateLoanProduct(loanProductId, new PutLoanProductsProductIdRequest().daysInYearCustomStrategy(FULL_LEAP_YEAR));
        loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertEquals(FULL_LEAP_YEAR, loanProduct.getDaysInYearCustomStrategy().getId());
    }

    @Test
    public void testFEB_29_PERIOD_ONLY() {
        Long clientId = createClient();
        runAt("1 January 2024", () -> {
            Long loanProductId = createLoanProduct(create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL)
                    .daysInYearCustomStrategy(FEB_29_PERIOD_ONLY));
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 10000.0, 99.99, 12, null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(10000), "1 January 2024");
            verifyRepaymentSchedule(loanId, //
                    installment(10000.0, null, "01 January 2024"), //
                    installment(519.92, 821.84, 1341.76, false, "01 February 2024"), //
                    installment(564.78, 776.98, 1341.76, false, "01 March 2024"), //
                    installment(609.07, 732.69, 1341.76, false, "01 April 2024"), //
                    installment(659.12, 682.64, 1341.76, false, "01 May 2024"), //
                    installment(713.29, 628.47, 1341.76, false, "01 June 2024"), //
                    installment(771.91, 569.85, 1341.76, false, "01 July 2024"), //
                    installment(835.35, 506.41, 1341.76, false, "01 August 2024"), //
                    installment(904.0, 437.76, 1341.76, false, "01 September 2024"), //
                    installment(978.3, 363.46, 1341.76, false, "01 October 2024"), //
                    installment(1058.7, 283.06, 1341.76, false, "01 November 2024"), //
                    installment(1145.71, 196.05, 1341.76, false, "01 December 2024"), //
                    installment(1239.85, 101.9, 1341.75, false, "01 January 2025") //
            );
        });
    }

    @Test
    public void testFULL_LEAP_YEAR() {
        Long clientId = createClient();
        runAt("1 January 2024", () -> {
            Long loanProductId = createLoanProduct(
                    create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL).daysInYearCustomStrategy(FULL_LEAP_YEAR));
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 10000.0, 99.99, 12, null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(10000), "1 January 2024");
            verifyRepaymentSchedule(loanId, //
                    installment(10000.0, null, "01 January 2024"), //
                    installment(521.04, 819.59, 1340.63, false, "01 February 2024"), //
                    installment(563.74, 776.89, 1340.63, false, "01 March 2024"), //
                    installment(609.95, 730.68, 1340.63, false, "01 April 2024"), //
                    installment(659.94, 680.69, 1340.63, false, "01 May 2024"), //
                    installment(714.03, 626.6, 1340.63, false, "01 June 2024"), //
                    installment(772.55, 568.08, 1340.63, false, "01 July 2024"), //
                    installment(835.86, 504.77, 1340.63, false, "01 August 2024"), //
                    installment(904.37, 436.26, 1340.63, false, "01 September 2024"), //
                    installment(978.49, 362.14, 1340.63, false, "01 October 2024"), //
                    installment(1058.69, 281.94, 1340.63, false, "01 November 2024"), //
                    installment(1145.46, 195.17, 1340.63, false, "01 December 2024"), //
                    installment(1235.88, 104.68, 1340.56, false, "01 January 2025") //
            );
        });
    }

    @Test
    public void test_ClientEnum_Has_Separate_Constants() {
        Assertions.assertEquals(2, PostLoansRequest.DaysInYearCustomStrategyEnum.values().length);
        Assertions.assertEquals(PostLoansRequest.DaysInYearCustomStrategyEnum.FULL_LEAP_YEAR,
                PostLoansRequest.DaysInYearCustomStrategyEnum.fromValue(FULL_LEAP_YEAR));
        Assertions.assertEquals(PostLoansRequest.DaysInYearCustomStrategyEnum.FEB_29_PERIOD_ONLY,
                PostLoansRequest.DaysInYearCustomStrategyEnum.fromValue(FEB_29_PERIOD_ONLY));
        Assertions.assertEquals(FEB_29_PERIOD_ONLY, PostLoansRequest.DaysInYearCustomStrategyEnum.FEB_29_PERIOD_ONLY.getValue());
    }

    @Test
    public void test_LoanLevel_DaysInYearCustomStrategy_Overrides_Product_Value() {
        Long clientId = createClient();
        runAt("1 January 2024", () -> {
            Long loanProductId = createLoanProduct(
                    create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL).daysInYearCustomStrategy(FULL_LEAP_YEAR));
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 10000.0, 99.99, 12,
                    request -> request.daysInYearCustomStrategy(PostLoansRequest.DaysInYearCustomStrategyEnum.FEB_29_PERIOD_ONLY));
            Assertions.assertEquals(FEB_29_PERIOD_ONLY, getLoanDetails(loanId).getDaysInYearCustomStrategy().getId());

            disburseLoan(loanId, BigDecimal.valueOf(10000), "1 January 2024");
            // Same schedule as testFEB_29_PERIOD_ONLY, although the product uses FULL_LEAP_YEAR
            verifyRepaymentSchedule(loanId, //
                    installment(10000.0, null, "01 January 2024"), //
                    installment(519.92, 821.84, 1341.76, false, "01 February 2024"), //
                    installment(564.78, 776.98, 1341.76, false, "01 March 2024"), //
                    installment(609.07, 732.69, 1341.76, false, "01 April 2024"), //
                    installment(659.12, 682.64, 1341.76, false, "01 May 2024"), //
                    installment(713.29, 628.47, 1341.76, false, "01 June 2024"), //
                    installment(771.91, 569.85, 1341.76, false, "01 July 2024"), //
                    installment(835.35, 506.41, 1341.76, false, "01 August 2024"), //
                    installment(904.0, 437.76, 1341.76, false, "01 September 2024"), //
                    installment(978.3, 363.46, 1341.76, false, "01 October 2024"), //
                    installment(1058.7, 283.06, 1341.76, false, "01 November 2024"), //
                    installment(1145.71, 196.05, 1341.76, false, "01 December 2024"), //
                    installment(1239.85, 101.9, 1341.75, false, "01 January 2025") //
            );
        });
    }

    @Test
    public void test_LoanLevel_DaysInYearCustomStrategy_Is_Used_By_Schedule_Preview() {
        Long clientId = createClient();
        runAt("1 January 2024", () -> {
            Long fullLeapYearProductId = createLoanProduct(
                    create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL).daysInYearCustomStrategy(FULL_LEAP_YEAR));
            Long feb29ProductId = createLoanProduct(create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL)
                    .daysInYearCustomStrategy(FEB_29_PERIOD_ONLY));

            BigDecimal fullLeapYearInterest = calculateLoanSchedule(
                    applyLP2ProgressiveLoanRequest(clientId, fullLeapYearProductId, "1 January 2024", 10000.0, 99.99, 12, null))
                    .getTotalInterestCharged();
            BigDecimal feb29Interest = calculateLoanSchedule(
                    applyLP2ProgressiveLoanRequest(clientId, feb29ProductId, "1 January 2024", 10000.0, 99.99, 12, null))
                    .getTotalInterestCharged();
            BigDecimal overriddenInterest = calculateLoanSchedule(
                    applyLP2ProgressiveLoanRequest(clientId, fullLeapYearProductId, "1 January 2024", 10000.0, 99.99, 12,
                            request -> request.daysInYearCustomStrategy(PostLoansRequest.DaysInYearCustomStrategyEnum.FEB_29_PERIOD_ONLY)))
                    .getTotalInterestCharged();

            Assertions.assertNotEquals(0, fullLeapYearInterest.compareTo(feb29Interest));
            Assertions.assertEquals(0, feb29Interest.compareTo(overriddenInterest));
        });
    }

    @Test
    public void test_LoanLevel_Invalid_DaysInYearCustomStrategy_Is_Rejected() {
        Long clientId = createClient();
        runAt("1 January 2024", () -> {
            Long loanProductId = createLoanProduct(
                    create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL).daysInYearCustomStrategy(FULL_LEAP_YEAR));
            // The generated client only accepts valid enum values, so the invalid one is sent as raw JSON
            ObjectNode body = ObjectMapperFactory.getShared()
                    .valueToTree(applyLP2ProgressiveLoanRequest(clientId, loanProductId, "1 January 2024", 10000.0, 99.99, 12, null));
            body.put("daysInYearCustomStrategy", INVALID_STRATEGY);

            CallFailedRuntimeException exception = FeignCalls.fail(() -> rawApi().submitLoanApplication(body.toString()));
            Assertions.assertEquals(400, exception.getStatus());
        });
    }

    @Test
    public void test_LoanLevel_DaysInYearCustomStrategy_With_NonActual_DaysInYearType_Is_Rejected() {
        Long clientId = createClient();
        runAt("1 January 2024", () -> {
            Long loanProductId = createLoanProduct(
                    create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL).daysInYearCustomStrategy(FULL_LEAP_YEAR));
            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyForLoan(applyLP2ProgressiveLoanRequest(clientId, loanProductId, "1 January 2024", 10000.0, 99.99, 12,
                            request -> request.daysInYearType(DAYS_IN_YEAR_360)
                                    .daysInYearCustomStrategy(PostLoansRequest.DaysInYearCustomStrategyEnum.FEB_29_PERIOD_ONLY))));
            Assertions.assertEquals(400, exception.getStatus());
        });
    }

    @Test
    public void test_Modify_Loan_DaysInYearCustomStrategy() {
        Long clientId = createClient();
        runAt("1 January 2024", () -> {
            Long loanProductId = createLoanProduct(
                    create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL).daysInYearCustomStrategy(FULL_LEAP_YEAR));
            Long loanId = applyForLoan(applyLP2ProgressiveLoanRequest(clientId, loanProductId, "1 January 2024", 10000.0, 99.99, 12, null));
            Assertions.assertEquals(FULL_LEAP_YEAR, getLoanDetails(loanId).getDaysInYearCustomStrategy().getId());

            // PUT /loans/{loanId} expects the full application, so resend the original terms with the new strategy
            modifyLoanApplication(loanId, "modify", new PutLoansLoanIdRequest().clientId(clientId).productId(loanProductId)
                    .loanType("individual").submittedOnDate("1 January 2024").expectedDisbursementDate("1 January 2024")
                    .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE)
                    .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                    .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS).interestRatePerPeriod(BigDecimal.valueOf(99.99))
                    .numberOfRepayments(12).principal(10000L).loanTermFrequency(12).repaymentEvery(1)
                    .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                    .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                    .interestType(LoanTestData.InterestType.DECLINING_BALANCE)
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)
                    .daysInYearCustomStrategy(PutLoansLoanIdRequest.DaysInYearCustomStrategyEnum.FEB_29_PERIOD_ONLY));
            Assertions.assertEquals(FEB_29_PERIOD_ONLY, getLoanDetails(loanId).getDaysInYearCustomStrategy().getId());

            approveLoan(loanId, approveLoanRequest(10000.0, "1 January 2024"));
            disburseLoan(loanId, BigDecimal.valueOf(10000), "1 January 2024");
            verifyRepaymentSchedule(loanId, //
                    installment(10000.0, null, "01 January 2024"), //
                    installment(519.92, 821.84, 1341.76, false, "01 February 2024"), //
                    installment(564.78, 776.98, 1341.76, false, "01 March 2024"), //
                    installment(609.07, 732.69, 1341.76, false, "01 April 2024"), //
                    installment(659.12, 682.64, 1341.76, false, "01 May 2024"), //
                    installment(713.29, 628.47, 1341.76, false, "01 June 2024"), //
                    installment(771.91, 569.85, 1341.76, false, "01 July 2024"), //
                    installment(835.35, 506.41, 1341.76, false, "01 August 2024"), //
                    installment(904.0, 437.76, 1341.76, false, "01 September 2024"), //
                    installment(978.3, 363.46, 1341.76, false, "01 October 2024"), //
                    installment(1058.7, 283.06, 1341.76, false, "01 November 2024"), //
                    installment(1145.71, 196.05, 1341.76, false, "01 December 2024"), //
                    installment(1239.85, 101.9, 1341.75, false, "01 January 2025") //
            );
        });
    }

    @Test
    public void test_Update_Product_Clears_DaysInYearCustomStrategy_When_Switching_To_360() {
        Long loanProductId = createLoanProduct(
                create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL).daysInYearCustomStrategy(FEB_29_PERIOD_ONLY));

        // The generated client drops null fields, so the explicit null is sent as raw JSON
        FeignCalls.ok(() -> rawApi().updateLoanProduct(loanProductId,
                "{\"daysInYearCustomStrategy\": null, \"daysInYearType\": 360, \"locale\": \"en\"}"));

        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNull(loanProduct.getDaysInYearCustomStrategy());
        Assertions.assertEquals(DAYS_IN_YEAR_360.longValue(), loanProduct.getDaysInYearType().getId());
    }

    @Test
    public void test_Update_Product_Cannot_Clear_DaysInYearCustomStrategy_For_Progressive_Actual() {
        Long loanProductId = createLoanProduct(
                create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL).daysInYearCustomStrategy(FEB_29_PERIOD_ONLY));

        CallFailedRuntimeException exception = FeignCalls
                .fail(() -> rawApi().updateLoanProduct(loanProductId, "{\"daysInYearCustomStrategy\": null}"));
        Assertions.assertEquals(400, exception.getStatus());
        Assertions.assertEquals(FEB_29_PERIOD_ONLY, retrieveLoanProduct(loanProductId).getDaysInYearCustomStrategy().getId());
    }

    @Test
    public void test_Update_Product_Invalid_DaysInYearCustomStrategy_Is_Rejected() {
        Long loanProductId = createLoanProduct(
                create4IProgressive().currencyCode("USD").daysInYearType(DAYS_IN_YEAR_ACTUAL).daysInYearCustomStrategy(FEB_29_PERIOD_ONLY));

        CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                () -> updateLoanProduct(loanProductId, new PutLoanProductsProductIdRequest().daysInYearCustomStrategy(INVALID_STRATEGY)));
        Assertions.assertEquals(400, exception.getStatus());
    }

    // The generated models cannot express an explicit null or an out-of-enum value, so these bodies are sent as raw
    // JSON
    private DaysInYearCustomStrategyRawApi rawApi() {
        return fineractClient().create(DaysInYearCustomStrategyRawApi.class);
    }
}
