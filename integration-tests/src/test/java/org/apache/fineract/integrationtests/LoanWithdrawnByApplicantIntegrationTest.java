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

import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class LoanWithdrawnByApplicantIntegrationTest extends BaseLoanIntegrationTest {

    @Test
    public void loanWithdrawnByApplicant() {
        final Long clientId = ClientHelper.createClient(new PostClientsRequest() //
                .officeId(1L) //
                .legalFormId(ClientHelper.LEGALFORM_ID_PERSON) //
                .firstname(Utils.randomFirstNameGenerator()) //
                .lastname(Utils.randomLastNameGenerator()) //
                .active(true) //
                .activationDate("01 January 2012") //
                .dateFormat(Utils.DATE_FORMAT) //
                .locale("en")) //
                .getResourceId();

        final Long loanProductId = loanTransactionHelper.createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()) //
                .getResourceId();

        final PostLoansResponse loanResponse = loanTransactionHelper
                .applyLoan(applyLoanRequest(clientId, loanProductId, "04 April 2012", 5000.0, 5));
        final Long loanId = loanResponse.getLoanId();

        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        loanTransactionHelper.withdrawnByApplicantLoan(loanId, //
                new PostLoansLoanIdRequest() //
                        .withdrawnOnDate("05 April 2012") //
                        .dateFormat(Utils.DATE_FORMAT) //
                        .locale("en"));

        verifyLoanStatus(loanId, LoanStatus.WITHDRAWN_BY_CLIENT);
    }
}
