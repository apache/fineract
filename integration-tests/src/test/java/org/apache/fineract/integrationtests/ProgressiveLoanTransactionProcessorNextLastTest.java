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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Test;

public class ProgressiveLoanTransactionProcessorNextLastTest extends BaseLoanIntegrationTest {

    private final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

    @Test
    public void testPartialEarlyRepaymentWithNextLast() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2024", () -> {
            Long progressiveLoanInterestRecalculationNextLastId = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(true).loanScheduleProcessingType("HORIZONTAL")
                            .paymentAllocation(
                                    List.of(createPaymentAllocation("DEFAULT", FuturePaymentAllocationRule.NEXT_LAST_INSTALLMENT))))
                    .getResourceId();
            Long loanId = applyAndApproveProgressiveLoan(clientId, progressiveLoanInterestRecalculationNextLastId, "1 January 2024", 100.0,
                    65.7, 6, null);
            loanIdRef.set(loanId);

            loanTransactionHelper.disburseLoan(loanId, "1 January 2024", 100.0);
            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"),
                    installment(14.52, 5.48, 20.0, false, "01 February 2024"), //
                    installment(15.32, 4.68, 20.0, false, "01 March 2024"), //
                    installment(16.16, 3.84, 20.0, false, "01 April 2024"), //
                    installment(17.04, 2.96, 20.0, false, "01 May 2024"), //
                    installment(17.98, 2.02, 20.0, false, "01 June 2024"), //
                    installment(18.98, 1.04, 20.02, false, "01 July 2024"));

            // should pay to first installment - edge case coming from implementation
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "1 January 2024", 5.0);
            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"), //
                    installment(14.8, 5.2, 15.0, false, "01 February 2024"), //
                    installment(15.34, 4.66, 20.0, false, "01 March 2024"), //
                    installment(16.18, 3.82, 20.0, false, "01 April 2024"), //
                    installment(17.06, 2.94, 20.0, false, "01 May 2024"), //
                    installment(18.0, 2.0, 20.0, false, "01 June 2024"), //
                    installment(18.62, 1.02, 19.64, false, "01 July 2024"));
        });
        runAt("31 January 2024", () -> {
            Long loanId = loanIdRef.get();

            // test the repayment before the due date. Should go to 1st installment.
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "31 January 2024", 4.0);
            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"), //
                    installment(14.81, 5.19, 11.0, false, "01 February 2024"), //
                    installment(15.34, 4.66, 20.0, false, "01 March 2024"), //
                    installment(16.18, 3.82, 20.0, false, "01 April 2024"), //
                    installment(17.06, 2.94, 20.0, false, "01 May 2024"), //
                    installment(18.0, 2.0, 20.0, false, "01 June 2024"), //
                    installment(18.61, 1.02, 19.63, false, "01 July 2024"));

            // test the repayment before the due date. Should go to 1st installment, and rest to last installment.
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "31 January 2024", 20.0);
            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"),
                    installment(14.97, 5.03, 0.0, true, "01 February 2024"), installment(15.7, 4.3, 20.0, false, "01 March 2024"),
                    installment(16.7, 3.3, 20.0, false, "01 April 2024"), installment(17.61, 2.39, 20.0, false, "01 May 2024"),
                    installment(18.58, 1.42, 20.0, false, "01 June 2024"), installment(16.44, 0.41, 7.85, false, "01 July 2024"));
        });
        runAt("1 March 2024", () -> {
            Long loanId = loanIdRef.get();
            // test repayment on due date. should repay 2nd installment normally and rest should go to last installment.
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "1 March 2024", 26.0);
            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"),
                    installment(14.97, 5.03, 0.0, true, "01 February 2024"), installment(15.7, 4.3, 0.0, true, "01 March 2024"),
                    installment(17.03, 2.97, 14.0, false, "01 April 2024"), installment(17.63, 2.37, 20.0, false, "01 May 2024"),
                    installment(18.59, 1.41, 20.0, false, "01 June 2024"), installment(16.08, 0.39, 7.47, false, "01 July 2024"));
        });
        runAt("2 March 2024", () -> {
            Long loanId = loanIdRef.get();
            // verify multiple partial repayment for "current" installment
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "2 March 2024", 7.0);
            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"),
                    installment(14.97, 5.03, 0.0, true, "01 February 2024"), installment(15.7, 4.3, 0.0, true, "01 March 2024"),
                    installment(17.4, 2.6, 7.0, false, "01 April 2024"), installment(17.65, 2.35, 20.0, false, "01 May 2024"),
                    installment(18.62, 1.38, 20.0, false, "01 June 2024"), installment(15.66, 0.36, 7.02, false, "01 July 2024"));
            // verify multiple partial repayment for "current" installment
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "2 March 2024", 7.0);
            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"),
                    installment(14.97, 5.03, 0.0, true, "01 February 2024"), installment(15.7, 4.3, 0.0, true, "01 March 2024"),
                    installment(19.9, 0.1, 0.0, true, "01 April 2024"), installment(15.65, 4.35, 20.0, false, "01 May 2024"),
                    installment(18.64, 1.36, 20.0, false, "01 June 2024"), installment(15.14, 0.34, 6.48, false, "01 July 2024"));
            // verify next then last installment logic.
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "2 March 2024", 22.0);
            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"),
                    installment(14.97, 5.03, 0.0, true, "01 February 2024"), installment(15.7, 4.3, 0.0, true, "01 March 2024"),
                    installment(19.9, 0.1, 0.0, true, "01 April 2024"), installment(18.02, 1.98, 20.0, false, "01 May 2024"),
                    installment(11.41, 0.02, 0.43, false, "01 June 2024"), installment(20.0, 0.0, 0.0, true, "01 July 2024"));
            // verify last installment logic.
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "2 March 2024", 22.0);
            verifyRepaymentSchedule(loanId, installment(100.0, null, "01 January 2024"),
                    installment(14.97, 5.03, 0.0, true, "01 February 2024"), installment(15.7, 4.3, 0.0, true, "01 March 2024"),
                    installment(19.9, 0.1, 0.0, true, "01 April 2024"), installment(9.43, 0.0, 0.0, true, "01 May 2024"),
                    installment(20.0, 0.0, 0.0, true, "01 June 2024"), installment(20.0, 0.0, 0.0, true, "01 July 2024"));
        });
    }

}
