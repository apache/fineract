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

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanAccountFraudTest extends BaseLoanIntegrationTest {

    private static final double AMOUNT = 100.0;
    private static final String COMMAND = "markAsFraud";
    private LocalDate todaysDate;
    private String operationDate;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.todaysDate = Utils.getLocalDateOfTenant();
        this.operationDate = Utils.dateFormatter.format(this.todaysDate);
    }

    @Test
    public void testMarkLoanAsFraud() {
        runAt(operationDate, () -> {

            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            PostLoanProductsRequest loanProductsRequest = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            PostLoansResponse postLoansResponse = loanTransactionHelper
                    .applyLoan(applyLoanRequest(clientId, loanProductResponse.getResourceId(), operationDate, AMOUNT, 1));
            Integer loanId = postLoansResponse.getLoanId().intValue();

            GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);

            // Default values Not Null and False
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());

            String payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", "true");
            PutLoansLoanIdResponse putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, COMMAND, payload, responseSpec);
            assertNotNull(putLoansLoanIdResponse);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());
            String statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", "false");
            putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, COMMAND, payload, responseSpec);
            assertNotNull(putLoansLoanIdResponse);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            // Approve the Loan active
            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(AMOUNT, operationDate));
            assertNotNull(approvedLoanResult);

            payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", "true");
            putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, COMMAND, payload, responseSpec);
            assertNotNull(putLoansLoanIdResponse);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", "false");
            putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, COMMAND, payload, responseSpec);
            assertNotNull(putLoansLoanIdResponse);

            // Default values Not Null and False
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            disburseLoan(loanId.longValue(), BigDecimal.valueOf(AMOUNT), operationDate);

            // Mark On the Fraud
            payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", "true");
            putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, COMMAND, payload, responseSpec);
            assertNotNull(putLoansLoanIdResponse);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());

            // Mark Off the Fraud
            payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", "false");
            putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, COMMAND, payload, this.responseSpec);
            assertNotNull(putLoansLoanIdResponse);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());

            payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", "true");
            putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, COMMAND, payload, responseSpec);
            assertNotNull(putLoansLoanIdResponse);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            undoDisbursement(loanId);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);
        });
    }
}
