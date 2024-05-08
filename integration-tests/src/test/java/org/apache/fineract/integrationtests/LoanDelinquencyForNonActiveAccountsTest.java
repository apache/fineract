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

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.client.models.GetLoansLoanIdLoanInstallmentLevelDelinquency;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanDelinquencyForNonActiveAccountsTest extends BaseLoanIntegrationTest {

    private SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(this.requestSpec);

    @Test
    public void testDelinquencyCalculationsForRejectedLoanAccount() {
        runAt("06 May 2024", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create DelinquencyBuckets
            Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                    Pair.of(1, 10), //
                    Pair.of(11, 30), //
                    Pair.of(31, 60), //
                    Pair.of(61, null)//
            ));

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            loanProductsRequest.setEnableInstallmentLevelDelinquency(true);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "06 May 2024", 1000.0, 4);

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", null, null);

            // Update Business Date
            updateBusinessDate("17 June 2024");

            // Undo Approval
            undoLoanApproval(loanId);

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", null, null);

            // Reject Loan
            rejectLoan(loanId, "17 June 2024");

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", null, null);
        });
    }

    @Test
    public void testDelinquencyCalculationsForRejectedLoanAccountCOBTest() {
        runAt("06 May 2024", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create DelinquencyBuckets
            Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                    Pair.of(1, 10), //
                    Pair.of(11, 30), //
                    Pair.of(31, 60), //
                    Pair.of(61, null)//
            ));

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            loanProductsRequest.setEnableInstallmentLevelDelinquency(true);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "06 May 2024", 1000.0, 4);

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", null, null);

            // Update Business Date
            updateBusinessDate("17 June 2024");

            // Undo Approval
            undoLoanApproval(loanId);

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", null, null);

            // Reject Loan
            rejectLoan(loanId, "17 June 2024");

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", null, null);

            // Update Business Date
            updateBusinessDate("18 June 2024");

            // execute COB
            schedulerJobHelper.executeAndAwaitJob("Loan COB");

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", null, null);

        });
    }

    @Test
    public void testDelinquencyCalculationsForClosedLoanAccount() {
        runAt("06 May 2024", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create DelinquencyBuckets
            Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                    Pair.of(1, 10), //
                    Pair.of(11, 30), //
                    Pair.of(31, 60), //
                    Pair.of(61, null)//
            ));

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            loanProductsRequest.setEnableInstallmentLevelDelinquency(true);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "06 May 2024", 1000.0, 4);

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", null, null);

            // Update Business Date
            updateBusinessDate("17 June 2024");

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000), "06 May 2024");

            verifyDelinquency(loanId, 12, "250.0", null, null, //
                    delinquency(11, 30, "250.0"));

            // re-pay Loan
            addRepaymentForLoan(loanId, 1000.0, "17 June 2024");

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", "17 June 2024", "1000.0");
        });
    }

    @Test
    public void testDelinquencyCalculationsForOverPaidLoanAccount() {
        runAt("06 May 2024", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create DelinquencyBuckets
            Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                    Pair.of(1, 10), //
                    Pair.of(11, 30), //
                    Pair.of(31, 60), //
                    Pair.of(61, null)//
            ));

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            loanProductsRequest.setEnableInstallmentLevelDelinquency(true);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "06 May 2024", 1000.0, 4);

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", null, null);

            // Update Business Date
            updateBusinessDate("17 June 2024");

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000), "06 May 2024");

            verifyDelinquency(loanId, 12, "250.0", null, null, //
                    delinquency(11, 30, "250.0"));

            // over-pay Loan
            addRepaymentForLoan(loanId, 1200.0, "17 June 2024");

            // Delinquency Calculations
            verifyDelinquency(loanId, 0, "0.0", "17 June 2024", "1200.0");
        });
    }

    private void verifyDelinquency(Long loanId, Integer loanLevelDelinquentDays, String loanLevelDelinquentAmount,
            String expectedLastRepaymentDate, String expectedLastRepaymentAmount,
            InstallmentLevelDelinquencyAPIIntegrationTests.DelinquencyData... expectedInstallmentLevelDelinquencyData) {
        GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        assertThat(loan.getDelinquent()).isNotNull();
        List<GetLoansLoanIdLoanInstallmentLevelDelinquency> installmentLevelDelinquency = loan.getDelinquent()
                .getInstallmentLevelDelinquency();

        assertThat(loan.getDelinquent().getDelinquentDays()).isEqualTo(loanLevelDelinquentDays);
        assertThat(loan.getDelinquent().getDelinquentAmount()).isEqualByComparingTo(Double.valueOf(loanLevelDelinquentAmount));
        if (expectedLastRepaymentDate != null && expectedLastRepaymentAmount != null) {
            assertThat(loan.getDelinquent().getLastRepaymentDate()).isNotNull();
            Assertions.assertEquals(expectedLastRepaymentDate, loan.getDelinquent().getLastRepaymentDate().format(dateTimeFormatter));
            assertThat(loan.getDelinquent().getLastRepaymentAmount()).isNotNull();
            assertThat(loan.getDelinquent().getLastRepaymentAmount()).isEqualByComparingTo(Double.valueOf(expectedLastRepaymentAmount));
        }

        if (expectedInstallmentLevelDelinquencyData != null && expectedInstallmentLevelDelinquencyData.length > 0) {
            assertThat(installmentLevelDelinquency).isNotNull();
            assertThat(installmentLevelDelinquency).hasSize(expectedInstallmentLevelDelinquencyData.length);
            for (int i = 0; i < expectedInstallmentLevelDelinquencyData.length; i++) {
                assertThat(installmentLevelDelinquency.get(i).getMaximumAgeDays())
                        .isEqualTo(expectedInstallmentLevelDelinquencyData[i].maxAgeDays);
                assertThat(installmentLevelDelinquency.get(i).getMinimumAgeDays())
                        .isEqualTo(expectedInstallmentLevelDelinquencyData[i].minAgeDays);
                assertThat(installmentLevelDelinquency.get(i).getDelinquentAmount())
                        .isEqualByComparingTo(expectedInstallmentLevelDelinquencyData[i].delinquentAmount);
            }
        } else {
            assertThat(installmentLevelDelinquency).isNull();
        }
    }

    @AllArgsConstructor
    public static class DelinquencyData {

        Integer minAgeDays;
        Integer maxAgeDays;
        BigDecimal delinquentAmount;
    }

    private static InstallmentLevelDelinquencyAPIIntegrationTests.DelinquencyData delinquency(Integer minAgeDays, Integer maxAgeDays,
            String delinquentAmount) {
        return new InstallmentLevelDelinquencyAPIIntegrationTests.DelinquencyData(minAgeDays, maxAgeDays, new BigDecimal(delinquentAmount));
    }
}
