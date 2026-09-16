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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanLastRepaymentDetailsTest extends FeignLoanTestBase {

    private final FeignDelinquencyHelper delinquencyHelper = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void loanLastRepaymentDetailsTestClosedLoan() {
        final String loanExternalIdStr = UUID.randomUUID().toString();
        final Long loanId = createClientWithLoan(loanExternalIdStr);

        makeLoanRepayment(loanExternalIdStr, repaymentOf("5 September 2022", 500.0));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        verifyLastRepayment(loanDetails, 500.00, LocalDate.of(2022, 9, 5));

        makeLoanRepayment(loanExternalIdStr, repaymentOf("6 September 2022", 500.0));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getClosedObligationsMet());
        verifyLastRepayment(loanDetails, 500.00, LocalDate.of(2022, 9, 6));
    }

    @Test
    public void loanLastRepaymentDetailsTestOverpaidLoan() {
        final String loanExternalIdStr = UUID.randomUUID().toString();
        final Long loanId = createClientWithLoan(loanExternalIdStr);

        makeLoanRepayment(loanExternalIdStr, repaymentOf("5 September 2022", 500.0));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        verifyLastRepayment(loanDetails, 500.00, LocalDate.of(2022, 9, 5));

        makeLoanRepayment(loanExternalIdStr, repaymentOf("6 September 2022", 600.0));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getOverpaid());
        verifyLastRepayment(loanDetails, 600.00, LocalDate.of(2022, 9, 6));
    }

    private Long createClientWithLoan(final String loanExternalIdStr) {
        final Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().buildRequest(null, delinquencyBucketId));
        assertNotNull(loanProductId);

        return createLoanAccount(clientId, loanProductId, loanExternalIdStr);
    }

    private void verifyLastRepayment(final GetLoansLoanIdResponse loanDetails, final Double expectedAmount, final LocalDate expectedDate) {
        assertNotNull(loanDetails);
        assertNotNull(loanDetails.getDelinquent());
        assertNotNull(loanDetails.getDelinquent().getLastRepaymentAmount());
        assertEquals(expectedAmount, Utils.getDoubleValue(loanDetails.getDelinquent().getLastRepaymentAmount()));
        assertNotNull(loanDetails.getDelinquent().getLastRepaymentDate());
        assertEquals(expectedDate, loanDetails.getDelinquent().getLastRepaymentDate());
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {
        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, "01 September 2022", 1000.0, 1)//
                .expectedDisbursementDate("03 September 2022")//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .externalId(externalId);

        final Long loanId = loanHelper.applyForLoan(application).getLoanId();
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "02 September 2022"));
        disburseLoan(loanId, "03 September 2022", 1000.0);
        return loanId;
    }

    private PostLoansLoanIdTransactionsRequest repaymentOf(final String transactionDate, final Double amount) {
        return new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate(transactionDate)
                .locale(LoanTestData.LOCALE).transactionAmount(amount);
    }
}
