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

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Test;

public class ProgressiveLoanCreditBalanceRefundTest extends BaseLoanIntegrationTest {

    Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    Long loanProductId = loanProductHelper.createLoanProduct(create4IProgressive()).getResourceId();

    @Test
    public void testAccrualCreationAfterCBRThenReverseRepayment() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> reverseRepaymentIdRef = new AtomicReference<>();
        runAt("13 February 2021", () -> {
            loanIdRef.set(applyAndApproveProgressiveLoan(clientId, loanProductId, "13 February 2021", 300.0, 37.56, 12, null));

            disburseLoan(loanIdRef.get(), BigDecimal.valueOf(300.0), "13 February 2021");

            verifyRepaymentSchedule(loanIdRef.get(), //
                    installment(300.0, null, "13 February 2021"), //
                    installment(20.98, 9.39, 30.37, false, "13 March 2021"), //
                    installment(21.64, 8.73, 30.37, false, "13 April 2021"), //
                    installment(22.31, 8.06, 30.37, false, "13 May 2021"), //
                    installment(23.01, 7.36, 30.37, false, "13 June 2021"), //
                    installment(23.73, 6.64, 30.37, false, "13 July 2021"), //
                    installment(24.48, 5.89, 30.37, false, "13 August 2021"), //
                    installment(25.24, 5.13, 30.37, false, "13 September 2021"), //
                    installment(26.03, 4.34, 30.37, false, "13 October 2021"), //
                    installment(26.85, 3.52, 30.37, false, "13 November 2021"), //
                    installment(27.69, 2.68, 30.37, false, "13 December 2021"), //
                    installment(28.55, 1.82, 30.37, false, "13 January 2022"), //
                    installment(29.49, 0.92, 30.41, false, "13 February 2022") //
            );

            loanTransactionHelper.makeLoanRepayment(loanIdRef.get(), "Repayment", "13 February 2021", 60.0);
            Long repaymentId = loanTransactionHelper.makeLoanRepayment(loanIdRef.get(), "Repayment", "13 February 2021", 40.0)
                    .getResourceId();
            reverseRepaymentIdRef.set(repaymentId);
            loanTransactionHelper.makeLoanRepayment(loanIdRef.get(), "MerchantIssuedRefund", "13 February 2021", 300.0);

            verifyRepaymentSchedule(loanIdRef.get(), //
                    installment(300.0, null, "13 February 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 March 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 April 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 May 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 June 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 July 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 August 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 September 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 October 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 November 2021"), //
                    installment(26.67, 0.0, 0.0, true, "13 December 2021"), //
                    installment(0.0, 0.0, 0.0, true, "13 January 2022"), //
                    installment(0.0, 0.0, 0.0, true, "13 February 2022") //
            );
            verifyTransactions(loanIdRef.get(), //
                    transaction(300.0, "Disbursement", "13 February 2021", 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(60.0, "Repayment", "13 February 2021", 240.0, 60.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(40.0, "Repayment", "13 February 2021", 200.0, 40.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(300.0, "Merchant Issued Refund", "13 February 2021", 0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 100.0, false) //
            );
        });

        runAt("19 February 2021", () -> {
            final Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            loanTransactionHelper.makeLoanRepayment(loanId, "CreditBalanceRefund", "19 February 2021", 100.0);
            // 0 overpaid
            verifyRepaymentSchedule(loanId, //
                    installment(300.0, null, "13 February 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 March 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 April 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 May 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 June 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 July 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 August 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 September 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 October 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 November 2021"), //
                    installment(26.67, 0.0, 0.0, true, "13 December 2021"), //
                    installment(0.0, 0.0, 0.0, true, "13 January 2022"), //
                    installment(0.0, 0.0, 0.0, true, "13 February 2022") //
            );
            verifyTransactions(loanId, //
                    transaction(300.0, "Disbursement", "13 February 2021", 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(60.0, "Repayment", "13 February 2021", 240.0, 60.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(40.0, "Repayment", "13 February 2021", 200.0, 40.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(300.0, "Merchant Issued Refund", "13 February 2021", 0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 100.0, false), //
                    transaction(100.0, "Credit Balance Refund", "19 February 2021", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 100.0, false) //
            );
        });
        runAt("23 February 2021", () -> {
            final Long loanId = loanIdRef.get();
            final Long reverseRepaymentId = reverseRepaymentIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            loanTransactionHelper.reverseLoanTransaction(loanId, reverseRepaymentId, "23 February 2021");
            // 40 outstanding
            verifyRepaymentSchedule(loanId, //
                    installment(300.0, null, "13 February 2021"), //
                    installment(130.37, 0.98, 40.98, false, "13 March 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 April 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 May 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 June 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 July 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 August 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 September 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 October 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 November 2021"), //
                    installment(26.67, 0.0, 0.0, true, "13 December 2021"), //
                    installment(0.0, 0.0, 0.0, true, "13 January 2022"), //
                    installment(0.0, 0.0, 0.0, true, "13 February 2022") //
            );

            verifyTransactions(loanId, //
                    transaction(300.0, "Disbursement", "13 February 2021", 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(60.0, "Repayment", "13 February 2021", 240.0, 60.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(40.0, "Repayment", "13 February 2021", 200.0, 40.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(300.0, "Merchant Issued Refund", "13 February 2021", 0.0, 240.0, 0.0, 0.0, 0.0, 0.0, 60.0, false), //
                    transaction(100.0, "Credit Balance Refund", "19 February 2021", 40.0, 40.0, 0.0, 0.0, 0.0, 0.0, 60.0, false) //
            );
        });
        runAt("24 February 2021", () -> {
            final Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            verifyTransactions(loanId, //
                    transaction(300.0, "Disbursement", "13 February 2021", 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(60.0, "Repayment", "13 February 2021", 240.0, 60.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(40.0, "Repayment", "13 February 2021", 200.0, 40.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(300.0, "Merchant Issued Refund", "13 February 2021", 0.0, 240.0, 0.0, 0.0, 0.0, 0.0, 60.0, false), //
                    transaction(100.0, "Credit Balance Refund", "19 February 2021", 40.0, 40.0, 0.0, 0.0, 0.0, 0.0, 60.0, false), //
                    transaction(0.18, "Accrual", "23 February 2021", 0.0, 0.0, 0.18, 0.0, 0.0, 0.0, 0.0, false) //
            );
        });
        runAt("28 February 2021", () -> {
            final Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            verifyTransactions(loanId, //
                    transaction(300.0, "Disbursement", "13 February 2021", 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(60.0, "Repayment", "13 February 2021", 240.0, 60.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(40.0, "Repayment", "13 February 2021", 200.0, 40.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(300.0, "Merchant Issued Refund", "13 February 2021", 0.0, 240.0, 0.0, 0.0, 0.0, 0.0, 60.0, false), //
                    transaction(100.0, "Credit Balance Refund", "19 February 2021", 40.0, 40.0, 0.0, 0.0, 0.0, 0.0, 60.0, false), //
                    transaction(0.18, "Accrual", "23 February 2021", 0.0, 0.0, 0.18, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.04, "Accrual", "24 February 2021", 0.0, 0.0, 0.04, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.05, "Accrual", "25 February 2021", 0.0, 0.0, 0.05, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.04, "Accrual", "26 February 2021", 0.0, 0.0, 0.04, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.05, "Accrual", "27 February 2021", 0.0, 0.0, 0.05, 0.0, 0.0, 0.0, 0.0, false) //
            );
        });
    }

    @Test
    public void testAccrualCreationAfterCBRThenReverseRepaymentThenRepayment() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> reverseRepaymentIdRef = new AtomicReference<>();
        runAt("13 February 2021", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "13 February 2021", 300.0, 37.56, 12, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(300.0), "13 February 2021");

            verifyRepaymentSchedule(loanId, //
                    installment(300.0, null, "13 February 2021"), //
                    installment(20.98, 9.39, 30.37, false, "13 March 2021"), //
                    installment(21.64, 8.73, 30.37, false, "13 April 2021"), //
                    installment(22.31, 8.06, 30.37, false, "13 May 2021"), //
                    installment(23.01, 7.36, 30.37, false, "13 June 2021"), //
                    installment(23.73, 6.64, 30.37, false, "13 July 2021"), //
                    installment(24.48, 5.89, 30.37, false, "13 August 2021"), //
                    installment(25.24, 5.13, 30.37, false, "13 September 2021"), //
                    installment(26.03, 4.34, 30.37, false, "13 October 2021"), //
                    installment(26.85, 3.52, 30.37, false, "13 November 2021"), //
                    installment(27.69, 2.68, 30.37, false, "13 December 2021"), //
                    installment(28.55, 1.82, 30.37, false, "13 January 2022"), //
                    installment(29.49, 0.92, 30.41, false, "13 February 2022") //
            );
            Long resourceId = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "13 February 2021", 100.0).getResourceId();
            reverseRepaymentIdRef.set(resourceId);

            loanTransactionHelper.makeLoanRepayment(loanId, "MerchantIssuedRefund", "13 February 2021", 300.0);

            verifyRepaymentSchedule(loanId, //
                    installment(300.0, null, "13 February 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 March 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 April 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 May 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 June 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 July 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 August 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 September 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 October 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 November 2021"), //
                    installment(26.67, 0.0, 0.0, true, "13 December 2021"), //
                    installment(0.0, 0.0, 0.0, true, "13 January 2022"), //
                    installment(0.0, 0.0, 0.0, true, "13 February 2022") //
            );
            verifyTransactions(loanId, //
                    transaction(300.0, "Disbursement", "13 February 2021", 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0, "Repayment", "13 February 2021", 200.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(300.0, "Merchant Issued Refund", "13 February 2021", 0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 100.0, false) //
            );

        });
        runAt("19 February 2021", () -> {
            final Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            loanTransactionHelper.makeLoanRepayment(loanId, "CreditBalanceRefund", "19 February 2021", 100.0);
            verifyRepaymentSchedule(loanId, //
                    installment(300.0, null, "13 February 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 March 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 April 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 May 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 June 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 July 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 August 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 September 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 October 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 November 2021"), //
                    installment(26.67, 0.0, 0.0, true, "13 December 2021"), //
                    installment(0.0, 0.0, 0.0, true, "13 January 2022"), //
                    installment(0.0, 0.0, 0.0, true, "13 February 2022") //
            );
            verifyTransactions(loanId, //
                    transaction(300.0, "Disbursement", "13 February 2021", 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0, "Repayment", "13 February 2021", 200.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(300.0, "Merchant Issued Refund", "13 February 2021", 0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 100.0, false), //
                    transaction(100.0, "Credit Balance Refund", "19 February 2021", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 100.0, false) //
            );
        });
        runAt("23 February 2021", () -> {
            final Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);

            final Long reverseRepaymentId = reverseRepaymentIdRef.get();
            loanTransactionHelper.reverseLoanTransaction(loanId, reverseRepaymentId, "23 February 2021");
            verifyRepaymentSchedule(loanId, //
                    installment(300.0, null, "13 February 2021"), //
                    installment(130.37, 2.46, 102.46, false, "13 March 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 April 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 May 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 June 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 July 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 August 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 September 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 October 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 November 2021"), //
                    installment(26.67, 0.0, 0.0, true, "13 December 2021"), //
                    installment(0.0, 0.0, 0.0, true, "13 January 2022"), //
                    installment(0.0, 0.0, 0.0, true, "13 February 2022") //
            );
            verifyTransactions(loanId, //
                    transaction(300.0, "Disbursement", "13 February 2021", 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0, "Repayment", "13 February 2021", 200.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(300.0, "Merchant Issued Refund", "13 February 2021", 0.0, 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0, "Credit Balance Refund", "19 February 2021", 100.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, false) //
            );
        });
        runAt("24 February 2021", () -> {
            final Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            verifyTransactions(loanId, //
                    transaction(300.0, "Disbursement", "13 February 2021", 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0, "Repayment", "13 February 2021", 200.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(300.0, "Merchant Issued Refund", "13 February 2021", 0.0, 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0, "Credit Balance Refund", "19 February 2021", 100.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.45, "Accrual", "23 February 2021", 0.0, 0.0, 0.45, 0.0, 0.0, 0.0, 0.0, false) //
            );
        });
        runAt("28 February 2021", () -> {
            final Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "28 February 2021", 101.01);
            verifyRepaymentSchedule(loanId, //
                    installment(300.0, null, "13 February 2021"), //
                    installment(130.37, 1.01, 0.0, true, "13 March 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 April 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 May 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 June 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 July 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 August 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 September 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 October 2021"), //
                    installment(30.37, 0.0, 0.0, true, "13 November 2021"), //
                    installment(26.67, 0.0, 0.0, true, "13 December 2021"), //
                    installment(0.0, 0.0, 0.0, true, "13 January 2022"), //
                    installment(0.0, 0.0, 0.0, true, "13 February 2022") //
            );
            verifyTransactions(loanId, //
                    transaction(300.0, "Disbursement", "13 February 2021", 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0, "Repayment", "13 February 2021", 200.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(300.0, "Merchant Issued Refund", "13 February 2021", 0.0, 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0, "Credit Balance Refund", "19 February 2021", 100.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.45, "Accrual", "23 February 2021", 0.0, 0.0, 0.45, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.11, "Accrual", "24 February 2021", 0.0, 0.0, 0.11, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.11, "Accrual", "25 February 2021", 0.0, 0.0, 0.11, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.11, "Accrual", "26 February 2021", 0.0, 0.0, 0.11, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.11, "Accrual", "27 February 2021", 0.0, 0.0, 0.11, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(101.01, "Repayment", "28 February 2021", 0.0, 100.0, 1.01, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.12, "Accrual", "28 February 2021", 0.0, 0.0, 0.12, 0.0, 0.0, 0.0, 0.0, false) //
            );
        });
    }
}
