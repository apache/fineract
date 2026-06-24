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
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanAccountFraudTest extends FeignLoanTestBase {

    private static final double AMOUNT = 100.0;

    @Test
    public void testMarkLoanAsFraud() {
        LocalDate todaysDate = Utils.getLocalDateOfTenant();
        String operationDate = Utils.dateFormatter.format(todaysDate);

        runAt(operationDate, () -> {

            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());

            PostLoansRequest applyRequest = applyLoanRequest(clientId, loanProductId, operationDate, AMOUNT, 1);
            Long loanId = applyForLoan(applyRequest);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);

            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());

            changeLoanFraudState(loanId, true);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());
            String statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            changeLoanFraudState(loanId, false);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            approveLoan(loanId, LoanRequestBuilders.approveLoan(AMOUNT, operationDate));

            changeLoanFraudState(loanId, true);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            changeLoanFraudState(loanId, false);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            disburseLoan(loanId, BigDecimal.valueOf(AMOUNT), operationDate);

            changeLoanFraudState(loanId, true);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());

            changeLoanFraudState(loanId, false);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());

            changeLoanFraudState(loanId, true);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            undoDisbursement(loanId);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);
        });
    }
}
