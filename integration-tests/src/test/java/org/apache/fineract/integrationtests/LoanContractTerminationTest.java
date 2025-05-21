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
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanContractTerminationTest extends BaseLoanIntegrationTest {

    @Test
    public void testLoanContractTermination() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    500.0, 7.0, 6, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
        });

        runAt("2 February 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
        });

        runAt("3 February 2024", () -> {
            Long loanId = loanIdRef.get();

            loanTransactionHelper.moveLoanState(loanId,
                    new PostLoansLoanIdRequest().note("Contract Termination Test").externalId(Utils.randomStringGenerator("", 20)),
                    "contractTermination");

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(0.58, "Accrual", "01 February 2024"), //
                    transaction(100.62, "Contract Termination", "03 February 2024"), //
                    transaction(0.04, "Accrual", "03 February 2024") //
            );
        });
    }

    @Test
    public void testNegativeLoanContractTerminationInNoActiveLoan() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    500.0, 7.0, 3, null);
            loanIdRef.set(loanId);

            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.moveLoanState(loanId,
                            new PostLoansLoanIdRequest().note("Contract Termination Test").externalId(Utils.randomStringGenerator("", 20)),
                            "contractTermination"));

            Assertions.assertTrue(callFailedRuntimeException.getMessage()
                    .contains("Contract termination can not be applied, Loan Account is not Active"));
        });
    }

    @Test
    public void testNegativeLoanContractTerminationInNoProgressiveLoan() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(
                createOnePeriod30DaysPeriodicAccrualProduct(12.4).transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)
                        .loanScheduleType(LoanScheduleType.CUMULATIVE.toString()));

        runAt("1 January 2024", () -> {
            final Long loanId = applyAndApproveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024", 100.0, 6);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.moveLoanState(loanId,
                            new PostLoansLoanIdRequest().note("Contract Termination Test").externalId(Utils.randomStringGenerator("", 20)),
                            "contractTermination"));

            Assertions.assertTrue(callFailedRuntimeException.getMessage()
                    .contains("Contract termination can not be applied, Loan product schedule type is not Progressive"));
        });
    }

}
