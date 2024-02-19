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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.CreditAllocationOrder;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

public class LoanDownPaymentTransactionChargebackTest extends BaseLoanIntegrationTest {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);

    @Test
    public void loanDownPaymentTransactionChargebackTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(false);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // make down payment
            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = loanTransactionHelper.makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 March 2023").locale("en")
                            .transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            // chargeback down payment transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId.intValue(),
                    downPaymentTransaction_1.getResourceId(), "50.00", 0, responseSpec);

            reviewLoanTransactionRelations(loanId.intValue(), downPaymentTransaction_1.getResourceId(), 1, Double.valueOf("750.00"));
            reviewLoanTransactionRelations(loanId.intValue(), chargebackTransactionId, 0, Double.valueOf("800.00"));

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, true, "01 March 2023"), //
                    installment(300.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // verify journal entries for chargeback transaction
            GetJournalEntriesTransactionIdResponse journalEntries = journalEntryHelper
                    .getJournalEntries("L" + chargebackTransactionId.toString());
            assertEquals(2L, journalEntries.getTotalFilteredRecords());
            assertEquals(50.0, journalEntries.getPageItems().get(0).getAmount());
            assertEquals("CREDIT", journalEntries.getPageItems().get(0).getEntryType().getValue());

            assertEquals(50.0, journalEntries.getPageItems().get(1).getAmount());
            assertEquals("DEBIT", journalEntries.getPageItems().get(1).getEntryType().getValue());

        });
    }

    @Test
    public void loanDownPaymentTransactionChargebackForAdvancedPaymentAllocationTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // make down payment
            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = loanTransactionHelper.makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 March 2023").locale("en")
                            .transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            // chargeback down payment transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId.intValue(),
                    downPaymentTransaction_1.getResourceId(), "50.00", 0, responseSpec);

            reviewLoanTransactionRelations(loanId.intValue(), downPaymentTransaction_1.getResourceId(), 1, Double.valueOf("750.00"));
            reviewLoanTransactionRelations(loanId.intValue(), chargebackTransactionId, 0, Double.valueOf("800.00"));

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, true, "01 March 2023"), //
                    installment(300.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // verify journal entries for chargeback transaction
            GetJournalEntriesTransactionIdResponse journalEntries = journalEntryHelper
                    .getJournalEntries("L" + chargebackTransactionId.toString());
            assertEquals(2L, journalEntries.getTotalFilteredRecords());
            assertEquals(50.0, journalEntries.getPageItems().get(0).getAmount());
            assertEquals("CREDIT", journalEntries.getPageItems().get(0).getEntryType().getValue());

            assertEquals(50.0, journalEntries.getPageItems().get(1).getAmount());
            assertEquals("DEBIT", journalEntries.getPageItems().get(1).getEntryType().getValue());

        });
    }

    @Test
    @Ignore
    public void loanDownPaymentTransactionChargebackWithChargesForAdvancedPaymentAllocationTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(true);

            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));
            final String penaltyCharge1AddedDate = "16 March 2023";

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // make down payment
            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = loanTransactionHelper.makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 March 2023").locale("en")
                            .transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            PostLoansLoanIdChargesResponse penaltyResponse = loanTransactionHelper.addLoanCharge(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(penalty.longValue()).amount(10.0).dueDate(penaltyCharge1AddedDate).dateFormat(DATETIME_PATTERN).locale("en"));
            assertNotNull(penaltyResponse.getResourceId());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("16 March 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            PostLoansLoanIdTransactionsResponse repaymentResponse = loanTransactionHelper.makeLoanRepayment("16 March 2023", 260.0f,
                    loanId.intValue());
            assertNotNull(repaymentResponse.getResourceId());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("25 March 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // chargeback down payment transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId.intValue(),
                    repaymentResponse.getResourceId(), "260.00", 0, responseSpec);

            reviewLoanTransactionRelations(loanId.intValue(), chargebackTransactionId, 0, Double.valueOf("760.00"));

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, 0.0, 0.0, 0.0, 0.0, true, "01 March 2023"), //
                    installment(250.0, 0.0, 0.0, 10.0, 0.0, true, "16 March 2023"), //
                    installment(500.0, 0.0, 0.0, 10.0, 510.0, false, "31 March 2023"), //
                    installment(250.0, 0.0, 0.0, 0.0, 250.0, false, "15 April 2023")//
            );
        });
    }

    @Test
    @Ignore
    public void loanDownPaymentTransactionChargebackWithMultipleFeesAndPartialChargebackForAdvancedPaymentAllocationTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPaymentWithCreditAllocation(true,
                    getCreditAllocationOrder(AllocationType.FEE, AllocationType.PRINCIPAL, AllocationType.INTEREST,
                            AllocationType.PENALTY));

            Integer fee1 = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "15", false));
            final String feeCharge1AddedDate = "14 March 2023";

            Integer fee2 = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "5", false));
            final String feeCharge2AddedDate = "16 March 2023";

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // make down payment
            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = loanTransactionHelper.makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 March 2023").locale("en")
                            .transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            PostLoansLoanIdChargesResponse fee1Response = loanTransactionHelper.addLoanCharge(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(fee1.longValue()).amount(15.0).dueDate(feeCharge1AddedDate).dateFormat(DATETIME_PATTERN).locale("en"));
            assertNotNull(fee1Response.getResourceId());

            PostLoansLoanIdChargesResponse fee2Response = loanTransactionHelper.addLoanCharge(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(fee2.longValue()).amount(5.0).dueDate(feeCharge2AddedDate).dateFormat(DATETIME_PATTERN).locale("en"));
            assertNotNull(fee2Response.getResourceId());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("16 March 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            PostLoansLoanIdTransactionsResponse repaymentResponse = loanTransactionHelper.makeLoanRepayment("16 March 2023", 110.0f,
                    loanId.intValue());
            assertNotNull(repaymentResponse.getResourceId());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("25 March 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // chargeback down payment transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId.intValue(),
                    repaymentResponse.getResourceId(), "5.00", 0, responseSpec);

            reviewLoanTransactionRelations(loanId.intValue(), chargebackTransactionId, 0, Double.valueOf("665.00"));

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, 0.0, 0.0, 0.0, 0.0, true, "01 March 2023"), //
                    installment(250.0, 0.0, 20.0, 0.0, 160.0, false, "16 March 2023"), //
                    installment(250.0, 0.0, 5.0, 0.0, 255.0, false, "31 March 2023"), //
                    installment(250.0, 0.0, 0.0, 0.0, 250.0, false, "15 April 2023")//
            );
        });
    }

    @Test
    @Ignore
    public void loanDownPaymentTransactionChargebackWithMultipleFeesAndPartialChargeback2ForAdvancedPaymentAllocationTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPaymentWithCreditAllocation(true,
                    getCreditAllocationOrder(AllocationType.FEE, AllocationType.PRINCIPAL, AllocationType.INTEREST,
                            AllocationType.PENALTY));

            Integer fee1 = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "15", false));
            final String feeCharge1AddedDate = "14 March 2023";

            Integer fee2 = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "5", false));
            final String feeCharge2AddedDate = "16 March 2023";

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // make down payment
            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = loanTransactionHelper.makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 March 2023").locale("en")
                            .transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            PostLoansLoanIdChargesResponse fee1Response = loanTransactionHelper.addLoanCharge(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(fee1.longValue()).amount(15.0).dueDate(feeCharge1AddedDate).dateFormat(DATETIME_PATTERN).locale("en"));
            assertNotNull(fee1Response.getResourceId());

            PostLoansLoanIdChargesResponse fee2Response = loanTransactionHelper.addLoanCharge(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(fee2.longValue()).amount(5.0).dueDate(feeCharge2AddedDate).dateFormat(DATETIME_PATTERN).locale("en"));
            assertNotNull(fee2Response.getResourceId());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("16 March 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            PostLoansLoanIdTransactionsResponse repaymentResponse = loanTransactionHelper.makeLoanRepayment("16 March 2023", 110.0f,
                    loanId.intValue());
            assertNotNull(repaymentResponse.getResourceId());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("25 March 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // chargeback down payment transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId.intValue(),
                    repaymentResponse.getResourceId(), "50.00", 0, responseSpec);

            reviewLoanTransactionRelations(loanId.intValue(), chargebackTransactionId, 0, Double.valueOf("710.00"));

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, 0.0, 0.0, 0.0, 0.0, true, "01 March 2023"), //
                    installment(250.0, 0.0, 20.0, 0.0, 160.0, false, "16 March 2023"), //
                    installment(280.0, 0.0, 20.0, 0.0, 300.0, false, "31 March 2023"), //
                    installment(250.0, 0.0, 0.0, 0.0, 250.0, false, "15 April 2023")//
            );
        });
    }

    @Test
    @Ignore
    public void loanDownPaymentTransactionChargebackWithPartialChargebackForAdvancedPaymentAllocationTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPaymentWithCreditAllocation(true,
                    getCreditAllocationOrder(AllocationType.PRINCIPAL, AllocationType.INTEREST, AllocationType.PENALTY,
                            AllocationType.FEE));

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));
            final String feeChargeAddedDate = "14 March 2023";

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // make down payment
            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = loanTransactionHelper.makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 March 2023").locale("en")
                            .transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            PostLoansLoanIdChargesResponse fee1Response = loanTransactionHelper.addLoanCharge(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(fee.longValue()).amount(10.0).dueDate(feeChargeAddedDate).dateFormat(DATETIME_PATTERN).locale("en"));
            assertNotNull(fee1Response.getResourceId());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("16 March 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            PostLoansLoanIdTransactionsResponse repaymentResponse = loanTransactionHelper.makeLoanRepayment("16 March 2023", 100.0f,
                    loanId.intValue());
            assertNotNull(repaymentResponse.getResourceId());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("25 March 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // chargeback down payment transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId.intValue(),
                    repaymentResponse.getResourceId(), "95.00", 0, responseSpec);

            reviewLoanTransactionRelations(loanId.intValue(), chargebackTransactionId, 0, Double.valueOf("755.00"));

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, 0.0, 0.0, 0.0, 0.0, true, "01 March 2023"), //
                    installment(250.0, 0.0, 10.0, 0.0, 160.0, false, "16 March 2023"), //
                    installment(340.0, 0.0, 5.0, 0.0, 345.0, false, "31 March 2023"), //
                    installment(250.0, 0.0, 0.0, 0.0, 250.0, false, "15 April 2023")//
            );
        });
    }

    @Test
    @Ignore
    public void loanDownPaymentTransactionChargebackWithPenaltyAndFeePartialChargebackForAdvancedPaymentAllocationTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPaymentWithCreditAllocation(true,
                    getCreditAllocationOrder(AllocationType.PENALTY, AllocationType.FEE, AllocationType.PRINCIPAL,
                            AllocationType.INTEREST));

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));
            final String feeChargeAddedDate = "14 March 2023";

            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));
            final String penaltyChargeAddedDate = "16 March 2023";

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // make down payment
            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = loanTransactionHelper.makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 March 2023").locale("en")
                            .transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            PostLoansLoanIdChargesResponse feeResponse = loanTransactionHelper.addLoanCharge(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(fee.longValue()).amount(10.0).dueDate(feeChargeAddedDate).dateFormat(DATETIME_PATTERN).locale("en"));
            assertNotNull(feeResponse.getResourceId());

            PostLoansLoanIdChargesResponse penaltyResponse = loanTransactionHelper.addLoanCharge(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(penalty.longValue()).amount(10.0).dueDate(penaltyChargeAddedDate).dateFormat(DATETIME_PATTERN).locale("en"));
            assertNotNull(penaltyResponse.getResourceId());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("16 March 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            PostLoansLoanIdTransactionsResponse repaymentResponse = loanTransactionHelper.makeLoanRepayment("16 March 2023", 100.0f,
                    loanId.intValue());
            assertNotNull(repaymentResponse.getResourceId());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("25 March 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // chargeback down payment transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId.intValue(),
                    repaymentResponse.getResourceId(), "95.00", 0, responseSpec);

            reviewLoanTransactionRelations(loanId.intValue(), chargebackTransactionId, 0, Double.valueOf("765.00"));

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, 0.0, 0.0, 0.0, 0.0, true, "01 March 2023"), //
                    installment(250.0, 0.0, 10.0, 10.0, 170.0, false, "16 March 2023"), //
                    installment(325.0, 0.0, 10.0, 10.0, 345.0, false, "31 March 2023"), //
                    installment(250.0, 0.0, 0.0, 0.0, 250.0, false, "15 April 2023")//
            );
        });
    }

    private Long createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(boolean isAdvancedPaymentStrategy) {
        return createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPaymentWithCreditAllocation(isAdvancedPaymentStrategy,
                Collections.emptyList());
    }

    private Long createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPaymentWithCreditAllocation(
            boolean isAdvancedPaymentStrategy, List<CreditAllocationOrder> creditAllocationOrder) {
        boolean multiDisburseEnabled = true;
        PostLoanProductsRequest product = isAdvancedPaymentStrategy
                ? createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation(creditAllocationOrder)
                : createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.setNumberOfRepayments(3);
        product.setRepaymentEvery(15);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE);
        product.setEnableAutoRepaymentForDownPayment(false);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());
        assertNotNull(getLoanProductsProductIdResponse);
        return loanProductResponse.getResourceId();
    }

    protected PostLoanProductsRequest createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation(
            List<CreditAllocationOrder> creditAllocationOrder) {
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);
        CreditAllocationData chargebackAllocation = createCreditAllocation(creditAllocationOrder);

        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy")//
                .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()) //
                .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()) //
                .addPaymentAllocationItem(defaultAllocation).addCreditAllocationItem(chargebackAllocation);
    }

    private CreditAllocationData createCreditAllocation(List<CreditAllocationOrder> creditAllocationOrder) {
        CreditAllocationData creditAllocationData = new CreditAllocationData();
        creditAllocationData.setTransactionType("CHARGEBACK");
        creditAllocationData.setCreditAllocationOrder(creditAllocationOrder.isEmpty()
                ? getCreditAllocationOrder(AllocationType.PENALTY, AllocationType.FEE, AllocationType.INTEREST, AllocationType.PRINCIPAL)
                : creditAllocationOrder);

        return creditAllocationData;
    }

    private AdvancedPaymentData createDefaultPaymentAllocation(String futureInstallmentAllocationRule) {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }

    private List<CreditAllocationOrder> getCreditAllocationOrder(AllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(cat -> {
            CreditAllocationOrder creditAllocationOrder = new CreditAllocationOrder();
            creditAllocationOrder.setCreditAllocationRule(cat.name());
            creditAllocationOrder.setOrder(integer.getAndIncrement());
            return creditAllocationOrder;
        }).toList();
    }

    private void reviewLoanTransactionRelations(final Integer loanId, final Long transactionId, final Integer expectedSize,
            final Double outstandingBalance) {

        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper.getLoanTransaction(loanId,
                transactionId.intValue());
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());
        assertEquals(expectedSize, getLoansTransactionResponse.getTransactionRelations().size());
        // Outstanding amount
        assertEquals(outstandingBalance, getLoansTransactionResponse.getOutstandingLoanBalance());
    }
}
