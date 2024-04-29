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

import static java.lang.Boolean.TRUE;
import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanDelinquencyDetailsNextPaymentDateConfigurationTest extends BaseLoanIntegrationTest {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);

    @Test
    public void testNextPaymentDateForUnpaidInstallmentsWithNPlusOneTest() {
        runAt("01 November 2023", () -> {
            try {
                // update Global configuration for next payment date
                GlobalConfigurationHelper.updateLoanNextPaymentDateConfiguration(this.requestSpec, this.responseSpec,
                        "next-unpaid-due-date");
                // Create Client
                Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

                // Create Loan Product
                Long loanProductId = createLoanProductWith25PctDownPaymentAndDelinquencyBucket(false, true, false, 0);

                // Apply and Approve Loan
                Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 November 2023", 1000.0, 3, req -> {
                    req.submittedOnDate("01 November 2023");
                    req.setLoanTermFrequency(45);
                    req.setRepaymentEvery(15);
                    req.setGraceOnArrearsAgeing(0);
                });

                // Loan amount Disbursement
                disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 November 2023");

                // verify repayment schedule
                verifyRepaymentSchedule(loanId, //
                        installment(1000.0, null, "01 November 2023"), //
                        installment(250.0, false, "01 November 2023"), //
                        installment(250.0, false, "16 November 2023"), //
                        installment(250.0, false, "01 December 2023"), //
                        installment(250.0, false, "16 December 2023") //
                );

                // delinquency next payment date for 01 Nov Business date
                verifyLoanDelinquencyNextPaymentDate(loanId, "01 November 2023", false);

                // Update business date
                businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("13 November 2023")
                        .dateFormat(DATETIME_PATTERN).locale("en"));

                // delinquency next payment date for 13 Nov Business date
                verifyLoanDelinquencyNextPaymentDate(loanId, "16 November 2023", false);

                businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("16 November 2023")
                        .dateFormat(DATETIME_PATTERN).locale("en"));

                // delinquency next payment date for 16 Nov Business date
                verifyLoanDelinquencyNextPaymentDate(loanId, "01 December 2023", false);

                businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("01 December 2023")
                        .dateFormat(DATETIME_PATTERN).locale("en"));

                // delinquency next payment date for 01 Dec Business date
                verifyLoanDelinquencyNextPaymentDate(loanId, "16 December 2023", false);

                // add charge with due date after loan maturity date (N + 1)
                Long loanChargeId = addCharge(loanId, false, 50, "23 December 2023");

                // verify repayment schedule
                verifyRepaymentSchedule(loanId, //
                        installment(1000.0, null, "01 November 2023"), //
                        installment(250.0, false, "01 November 2023"), //
                        installment(250.0, false, "16 November 2023"), //
                        installment(250.0, false, "01 December 2023"), //
                        installment(250.0, false, "16 December 2023"), //
                        installment(0.0, 0.0, 50.0, 50.0, false, "23 December 2023") //
                );

                businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("17 December 2023")
                        .dateFormat(DATETIME_PATTERN).locale("en"));

                // delinquency next payment date for 17 Dec Business date N + 1
                verifyLoanDelinquencyNextPaymentDate(loanId, "23 December 2023", false);

                businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("25 December 2023")
                        .dateFormat(DATETIME_PATTERN).locale("en"));

            } finally {
                // reset global config
                GlobalConfigurationHelper.updateLoanNextPaymentDateConfiguration(this.requestSpec, this.responseSpec,
                        "earliest-unpaid-date");
            }

        });
    }

    @Test
    public void testNextPaymentDateFor2Paid1PartiallyPaidInstallmentsWithNPlusOneTest() {
        runAt("01 November 2023", () -> {
            try {
                // update Global configuration for next payment date
                GlobalConfigurationHelper.updateLoanNextPaymentDateConfiguration(this.requestSpec, this.responseSpec,
                        "next-unpaid-due-date");
                // Create Client
                Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

                // Create Loan Product with auto downpayment enabled
                Long loanProductId = createLoanProductWith25PctDownPaymentAndDelinquencyBucket(true, true, false, 0);

                // Apply and Approve Loan
                Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 November 2023", 1000.0, 3, req -> {
                    req.submittedOnDate("01 November 2023");
                    req.setLoanTermFrequency(45);
                    req.setRepaymentEvery(15);
                    req.setGraceOnArrearsAgeing(0);
                });

                // Loan amount Disbursement
                disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 November 2023");

                // verify repayment schedule
                verifyRepaymentSchedule(loanId, //
                        installment(1000.0, null, "01 November 2023"), //
                        installment(250.0, true, "01 November 2023"), //
                        installment(250.0, false, "16 November 2023"), //
                        installment(250.0, false, "01 December 2023"), //
                        installment(250.0, false, "16 December 2023") //
                );

                // delinquency next payment date for 01 Nov Business date with auto paid downpayment installment
                verifyLoanDelinquencyNextPaymentDate(loanId, "16 November 2023", false);

                // Update business date
                businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("13 November 2023")
                        .dateFormat(DATETIME_PATTERN).locale("en"));

                // delinquency next payment date for 13 Nov Business date
                verifyLoanDelinquencyNextPaymentDate(loanId, "16 November 2023", false);

                // pay 16 Nov Installment
                addRepaymentForLoan(loanId, 250.0, "13 November 2023");

                // verify repayment schedule
                verifyRepaymentSchedule(loanId, //
                        installment(1000.0, null, "01 November 2023"), //
                        installment(250.0, true, "01 November 2023"), //
                        installment(250.0, true, "16 November 2023"), //
                        installment(250.0, false, "01 December 2023"), //
                        installment(250.0, false, "16 December 2023")//
                );

                // delinquency next payment date for 13 Nov Business date after paying 16 November Installment
                verifyLoanDelinquencyNextPaymentDate(loanId, "01 December 2023", false);

                businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("16 November 2023")
                        .dateFormat(DATETIME_PATTERN).locale("en"));

                // delinquency next payment date for 16 Nov Business date
                verifyLoanDelinquencyNextPaymentDate(loanId, "01 December 2023", false);

                // partially pay 01 December installment
                addRepaymentForLoan(loanId, 100.0, "16 November 2023");

                // verify repayment schedule
                verifyRepaymentSchedule(loanId, //
                        installment(1000.0, null, "01 November 2023"), //
                        installment(250.0, true, "01 November 2023"), //
                        installment(250.0, true, "16 November 2023"), //
                        installment(250.0, 0.0, 150.0, false, "01 December 2023"), //
                        installment(250.0, false, "16 December 2023")//
                );

                // delinquency next payment date for 16 Nov Business date after partial payment of 01 Dec installment
                verifyLoanDelinquencyNextPaymentDate(loanId, "01 December 2023", false);

                businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("01 December 2023")
                        .dateFormat(DATETIME_PATTERN).locale("en"));

                // delinquency next payment date for 01 December Business date
                verifyLoanDelinquencyNextPaymentDate(loanId, "16 December 2023", false);

                // add charge with due date after loan maturity date (N + 1)
                Long loanChargeId = addCharge(loanId, false, 50, "23 December 2023");

                // verify repayment schedule
                verifyRepaymentSchedule(loanId, //
                        installment(1000.0, null, "01 November 2023"), //
                        installment(250.0, true, "01 November 2023"), //
                        installment(250.0, true, "16 November 2023"), //
                        installment(250.0, 0.0, 150.0, false, "01 December 2023"), //
                        installment(250.0, false, "16 December 2023"), //
                        installment(0.0, 0.0, 50.0, 50.0, false, "23 December 2023") //
                );

                businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("17 December 2023")
                        .dateFormat(DATETIME_PATTERN).locale("en"));

                // delinquency next payment date for 17 Dec Business date N + 1
                verifyLoanDelinquencyNextPaymentDate(loanId, "23 December 2023", false);

                businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("25 December 2023")
                        .dateFormat(DATETIME_PATTERN).locale("en"));
            } finally {
                // reset global config
                GlobalConfigurationHelper.updateLoanNextPaymentDateConfiguration(this.requestSpec, this.responseSpec,
                        "earliest-unpaid-date");
            }

        });
    }

    private void verifyLoanDelinquencyNextPaymentDate(Long loanId, String nextPaymentDate, boolean verifyNull) {
        GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        Assertions.assertNotNull(loan.getDelinquent());
        if (!verifyNull) {
            Assertions.assertNotNull(loan.getDelinquent().getNextPaymentDueDate());
            assertThat(loan.getDelinquent().getNextPaymentDueDate().isEqual(LocalDate.parse(nextPaymentDate, dateTimeFormatter)));
        } else {
            Assertions.assertNull(loan.getDelinquent().getNextPaymentDueDate());
        }

    }

    private Long createLoanProductWith25PctDownPaymentAndDelinquencyBucket(boolean autoDownPaymentEnabled, boolean multiDisburseEnabled,
            boolean installmentLevelDelinquencyEnabled, Integer graceOnArrearsAging) {
        // Create DelinquencyBuckets
        Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                Pair.of(1, 3), //
                Pair.of(4, 10), //
                Pair.of(11, 60), //
                Pair.of(61, null)//
        ));
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setDelinquencyBucketId(delinquencyBucketId.longValue());
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.setEnableDownPayment(true);
        product.setGraceOnArrearsAgeing(graceOnArrearsAging);

        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE);
        product.setEnableAutoRepaymentForDownPayment(autoDownPaymentEnabled);
        product.setEnableInstallmentLevelDelinquency(installmentLevelDelinquencyEnabled);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());

        Long loanProductId = loanProductResponse.getResourceId();

        assertEquals(TRUE, getLoanProductsProductIdResponse.getEnableDownPayment());
        assertNotNull(getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment());
        assertEquals(0, getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment().compareTo(DOWN_PAYMENT_PERCENTAGE));
        assertEquals(autoDownPaymentEnabled, getLoanProductsProductIdResponse.getEnableAutoRepaymentForDownPayment());
        assertEquals(multiDisburseEnabled, getLoanProductsProductIdResponse.getMultiDisburseLoan());
        return loanProductId;

    }
}
