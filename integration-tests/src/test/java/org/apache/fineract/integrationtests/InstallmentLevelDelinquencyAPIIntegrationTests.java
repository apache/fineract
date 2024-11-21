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

import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetLoansLoanIdLoanInstallmentLevelDelinquency;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class InstallmentLevelDelinquencyAPIIntegrationTests extends BaseLoanIntegrationTest {

    private SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(this.requestSpec);

    @Test
    public void testInstallmentLevelDelinquencyFourRangesInTheBucket() {
        runAt("31 May 2023", () -> {
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
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), // 120 days delinquent -> range4
                    installment(312.0, false, "02 March 2023"), // 90 days delinquent -> range4
                    installment(312.0, false, "01 April 2023"), // 60 days delinquent -> range3
                    installment(314.0, false, "01 May 2023") // 30 days delinquent -> range2
            );

            // since the current day is 31 May 2023, therefore all the installments are delinquent
            verifyDelinquency(loanId, 120, "1250.0", //
                    delinquency(11, 30, "314.0"), // 4th installment
                    delinquency(31, 60, "312.0"), // 3rd installment
                    delinquency(61, null, "624.0") // 1st installment + 2nd installment
            );

            // Repayment of the first two installments
            addRepaymentForLoan(loanId, 626.0, "31 May 2023");
            verifyDelinquency(loanId, 60, "624.0", //
                    delinquency(11, 30, "314.0"), // 4th installment
                    delinquency(31, 60, "310.0") // 3rd installment
            );

            // Partial repayment
            addRepaymentForLoan(loanId, 100.0, "31 May 2023");
            verifyDelinquency(loanId, 60, "524.0", //
                    delinquency(11, 30, "314.0"), // 4th installment
                    delinquency(31, 60, "210.0") // 3rd installment
            );

            // Repay the loan fully
            addRepaymentForLoan(loanId, 524.0, "31 May 2023");
            verifyDelinquency(loanId, 0, "0.0");
        });
    }

    @Test
    public void testInstallmentLevelDelinquencyTwoRangesInTheBucket() {
        runAt("31 May 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create DelinquencyBuckets
            Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                    Pair.of(1, 60), //
                    Pair.of(61, null)//
            ));

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            loanProductsRequest.setEnableInstallmentLevelDelinquency(true);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), // 120 days delinquent -> range2
                    installment(312.0, false, "02 March 2023"), // 90 days delinquent -> range2
                    installment(312.0, false, "01 April 2023"), // 60 days delinquent -> range1
                    installment(314.0, false, "01 May 2023") // 30 days delinquent -> range1
            );

            verifyDelinquency(loanId, 120, "1250.0", //
                    delinquency(1, 60, "626.0"), // 4th installment
                    delinquency(61, null, "624.0") // 1st installment + 2nd installment
            );

            // repay the first installment
            addRepaymentForLoan(loanId, 313.0, "31 May 2023");

            verifyDelinquency(loanId, 90, "937.0", //
                    delinquency(1, 60, "626.0"), // 4th installment
                    delinquency(61, null, "311.0") // 1st installment + 2nd installment
            );

        });
    }

    @Test
    public void testInstallmentLevelDelinquencyIsTurnedOff() {
        runAt("31 May 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create DelinquencyBuckets
            Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                    Pair.of(1, 60), //
                    Pair.of(61, null)//
            ));

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            loanProductsRequest.setEnableInstallmentLevelDelinquency(false);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), // 120 days delinquent -> range2
                    installment(312.0, false, "02 March 2023"), // 90 days delinquent -> range2
                    installment(312.0, false, "01 April 2023"), // 60 days delinquent -> range1
                    installment(314.0, false, "01 May 2023") // 30 days delinquent -> range1
            );

            // this should be empty as the installment level delinquency is not enabled for this loan
            verifyDelinquency(loanId, 120, "1250.0");
        });
    }

    @Test
    public void testInstallmentLevelDelinquencyUpdatedWhenCOBIsExecuted() {
        runAt("01 February 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create DelinquencyBuckets
            Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                    Pair.of(1, 1), //
                    Pair.of(2, null)//
            ));

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            loanProductsRequest.setEnableInstallmentLevelDelinquency(true);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), installment(312.0, false, "31 January 2023"),
                    installment(312.0, false, "02 March 2023"), installment(312.0, false, "01 April 2023"),
                    installment(314.0, false, "01 May 2023"));

            // The first installment falls into the first range
            verifyDelinquency(loanId, 1, "312.0", //
                    delinquency(1, 1, "312.0") // 4th installment
            );

            // Let's go one day ahead in the time
            updateBusinessDateAndExecuteCOBJob("2 February 2023");

            // The first installment is not two days delinquent and therefore falls into the second range
            verifyDelinquency(loanId, 2, "312.0", //
                    delinquency(2, null, "312.0") // 4th installment
            );
        });
    }

    @Test
    public void testInstallmentLevelDelinquencyTurnedOnForProductAndOffForLoan() {
        runAt("31 May 2023", () -> {
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
            // set installment level delinquency as true
            loanProductsRequest.setEnableInstallmentLevelDelinquency(true);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan, turn loan level installment delinquency as false
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4,
                    req -> req.setEnableInstallmentLevelDelinquency(false));

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), // 120 days delinquent -> range4
                    installment(312.0, false, "02 March 2023"), // 90 days delinquent -> range4
                    installment(312.0, false, "01 April 2023"), // 60 days delinquent -> range3
                    installment(314.0, false, "01 May 2023") // 30 days delinquent -> range2
            );

            // since the installment level delinquency is overridden and set as false for loan application, therefore it
            // is not calculated
            verifyDelinquency(loanId, 120, "1250.0");
        });

    }

    @Test
    public void testInstallmentLevelDelinquencyTurnedOffForProductAndOnForLoan() {
        runAt("31 May 2023", () -> {
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
            // set installment level delinquency as false
            loanProductsRequest.setEnableInstallmentLevelDelinquency(false);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan, turn loan level installment delinquency as true
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4,
                    req -> req.setEnableInstallmentLevelDelinquency(true));

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), // 120 days delinquent -> range4
                    installment(312.0, false, "02 March 2023"), // 90 days delinquent -> range4
                    installment(312.0, false, "01 April 2023"), // 60 days delinquent -> range3
                    installment(314.0, false, "01 May 2023") // 30 days delinquent -> range2
            );

            // since the installment level delinquency is overridden and set as true for loan application, therefore it
            // is calculated
            verifyDelinquency(loanId, 120, "1250.0", //
                    delinquency(11, 30, "314.0"), // 4th installment
                    delinquency(31, 60, "312.0"), // 3rd installment
                    delinquency(61, null, "624.0") // 1st installment + 2nd installment
            );
        });

    }

    @Test
    public void testLoanInheritsInstallmentLevelSettingFromLoanProductIfNotSet() {
        runAt("31 May 2023", () -> {
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
            // set installment level delinquency as true
            loanProductsRequest.setEnableInstallmentLevelDelinquency(true);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan, do not set installment level delinquency
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), // 120 days delinquent -> range4
                    installment(312.0, false, "02 March 2023"), // 90 days delinquent -> range4
                    installment(312.0, false, "01 April 2023"), // 60 days delinquent -> range3
                    installment(314.0, false, "01 May 2023") // 30 days delinquent -> range2
            );

            // since the installment level delinquency is inherited from loan product, therefore it
            // is calculated
            verifyDelinquency(loanId, 120, "1250.0", //
                    delinquency(11, 30, "314.0"), // 4th installment
                    delinquency(31, 60, "312.0"), // 3rd installment
                    delinquency(61, null, "624.0") // 1st installment + 2nd installment
            );
        });

    }

    @Test
    public void tesInstallmentLevelSettingForLoanWithLoanProductWithoutDelinquencyBucketValidation() {

        runAt("31 May 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply For Loan with installment level delinquency setting
            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.applyLoan(applyLoanRequest(clientId, loanProductResponse.getResourceId(), "01 January 2023",
                            1250.0, 4, req -> req.setEnableInstallmentLevelDelinquency(true))));

            Assertions.assertTrue(callFailedRuntimeException.getMessage().contains(
                    "Installment level delinquency cannot be enabled for a loan if Delinquency bucket is not configured for loan product"));

        });

    }

    @Test
    public void testLoanInstallmentLevelSettingModification() {
        runAt("31 May 2023", () -> {
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

            // set delinquency bucket
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply for loan
            Long loanId = loanTransactionHelper
                    .applyLoan(applyLoanRequest(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4))
                    .getResourceId();

            // verify installment level delinquency setting for loan
            GetLoansLoanIdResponse loanResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
            assertThat(loanResponse.getEnableInstallmentLevelDelinquency()).isFalse();

            // Modify installment level delinquency as true for loan
            PutLoansLoanIdResponse loansModificationResponse = loanTransactionHelper.modifyApplicationForLoan(loanId, "modify",
                    new PutLoansLoanIdRequest().clientId(clientId).productId(loanProductResponse.getResourceId()).loanType("individual")
                            .enableInstallmentLevelDelinquency(true).locale("en").dateFormat(DATETIME_PATTERN));

            // verify installment level delinquency setting for loan
            loanResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
            assertThat(loanResponse.getEnableInstallmentLevelDelinquency()).isTrue();

            // Approve Loan
            loanTransactionHelper.approveLoan(loanId, approveLoanRequest(1250.0, "01 January 2023"));

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), // 120 days delinquent -> range4
                    installment(312.0, false, "02 March 2023"), // 90 days delinquent -> range4
                    installment(312.0, false, "01 April 2023"), // 60 days delinquent -> range3
                    installment(314.0, false, "01 May 2023") // 30 days delinquent -> range2
            );

            // since the installment level delinquency is modified as true for loan, therefore it
            // is calculated
            verifyDelinquency(loanId, 120, "1250.0", //
                    delinquency(11, 30, "314.0"), // 4th installment
                    delinquency(31, 60, "312.0"), // 3rd installment
                    delinquency(61, null, "624.0") // 1st installment + 2nd installment
            );

        });

    }

    @Test
    public void tesInstallmentLevelSettingModificationForLoanWithLoanProductWithoutDelinquencyBucketValidation() {

        runAt("31 May 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product without delinquency bucket
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply for loan
            Long loanId = loanTransactionHelper
                    .applyLoan(applyLoanRequest(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4))
                    .getResourceId();

            // Modify Loan with installment level delinquency setting
            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.modifyApplicationForLoan(loanId, "modify",
                            new PutLoansLoanIdRequest().clientId(clientId).productId(loanProductResponse.getResourceId())
                                    .loanType("individual").enableInstallmentLevelDelinquency(true).locale("en")
                                    .dateFormat(DATETIME_PATTERN)));

            Assertions.assertTrue(callFailedRuntimeException.getMessage().contains(
                    "Installment level delinquency cannot be enabled for a loan if Delinquency bucket is not configured for loan product"));

        });
    }

    @Test
    public void testCalculateRepaymentScheduleWorksWithInstallmentLevelDelinquencySetting() {
        runAt("31 May 2023", () -> {
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

            // set installment level delinquency as false
            loanProductsRequest.setEnableInstallmentLevelDelinquency(false);
            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // run calculateLoanSchedule command works, while Applying for loan with installment level delinquency
            PostLoansResponse loansResponse = loanTransactionHelper
                    .calculateRepaymentScheduleForApplyLoan(applyLoanRequest(clientId, loanProductResponse.getResourceId(),
                            "01 January 2023", 1250.0, 4, req -> req.setEnableInstallmentLevelDelinquency(true)), "calculateLoanSchedule");

            assertThat(loansResponse).isNotNull();
            assertNotNull(loansResponse.getPeriods());
            assertThat(loansResponse.getPeriods().size()).isEqualTo(5);

        });

    }

    @Test
    public void tesInstallmentLevelSettingForLoanProductWithoutDelinquencyBucketValidation() {

        runAt("31 May 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product without delinquency bucket
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            // set installment level delinquency as true
            loanProductsRequest.setEnableInstallmentLevelDelinquency(true);

            // Create loan product with installment level delinquency setting
            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanProductHelper.createLoanProduct(loanProductsRequest));

            Assertions.assertTrue(callFailedRuntimeException.getMessage()
                    .contains("Installment level delinquency cannot be enabled if Delinquency bucket is not configured for loan product"));

        });

    }

    @Test
    public void tesUpdateInstallmentLevelSettingForLoanProductWithoutDelinquencyBucketValidation() {

        runAt("31 May 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product without delinquency bucket
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Update loan product with installment level delinquency setting
            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanProductHelper.updateLoanProductById(loanProductResponse.getResourceId(),
                            new PutLoanProductsProductIdRequest().enableInstallmentLevelDelinquency(true).locale("en")));

            Assertions.assertTrue(callFailedRuntimeException.getMessage()
                    .contains("Installment level delinquency cannot be enabled if Delinquency bucket is not configured for loan product"));

        });

    }

    private void updateBusinessDateAndExecuteCOBJob(String date) {
        businessDateHelper.updateBusinessDate(
                new BusinessDateRequest().type(BUSINESS_DATE.getName()).date(date).dateFormat(DATETIME_PATTERN).locale("en"));
        schedulerJobHelper.executeAndAwaitJob("Loan COB");
    }

    @AllArgsConstructor
    public static class DelinquencyData {

        Integer minAgeDays;
        Integer maxAgeDays;
        BigDecimal delinquentAmount;
    }

    private static DelinquencyData delinquency(Integer minAgeDays, Integer maxAgeDays, String delinquentAmount) {
        return new DelinquencyData(minAgeDays, maxAgeDays, new BigDecimal(delinquentAmount));
    }

    private void verifyDelinquency(Long loanId, Integer loanLevelDelinquentDays, String loanLevelDelinquentAmount,
            DelinquencyData... expectedInstallmentLevelDelinquencyData) {
        GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        assertThat(loan.getDelinquent()).isNotNull();
        List<GetLoansLoanIdLoanInstallmentLevelDelinquency> installmentLevelDelinquency = loan.getDelinquent()
                .getInstallmentLevelDelinquency();

        assertThat(loan.getDelinquent().getDelinquentDays()).isEqualTo(loanLevelDelinquentDays);
        assertThat(loan.getDelinquent().getDelinquentAmount()).isEqualByComparingTo(Double.valueOf(loanLevelDelinquentAmount));

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

}
