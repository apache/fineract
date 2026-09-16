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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanPayOffAddChargeWithRefundTest extends FeignLoanTestBase {

    private final FeignDelinquencyHelper delinquencyHelper = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient());

    private final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @Test
    public void loanAddChargeForPaidOffLoanWithRefundTest() {
        final String loanExternalIdStr = UUID.randomUUID().toString();

        final Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().buildRequest(null, delinquencyBucketId));
        assertNotNull(loanProductId);

        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

        makeLoanRepayment(loanExternalIdStr, transactionOf("4 September 2022", 1000.0));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        final int loanRepaymentScheduleSize = loanDetails.getRepaymentSchedule().getPeriods().size();
        assertTrue(loanDetails.getStatus().getClosedObligationsMet());

        makePayoutRefund(loanId, transactionOf("4 September 2022", 100.0));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getOverpaid());

        // apply charges on date before maturity date
        final Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);
        final String feeChargeAddedDate = dateFormatter.format(LocalDate.of(2022, 9, 4));
        assertNotNull(addLoanCharge(loanId, feeCharge, feeChargeAddedDate, 10.0).getResourceId());

        loanDetails = getLoanDetails(loanId);
        assertEquals(loanRepaymentScheduleSize, loanDetails.getRepaymentSchedule().getPeriods().size());

        // apply charges on date after maturity date
        final Long feeChargeAfterMaturity = createLoanSpecifiedDueDateCharge(10.0);
        final String feeChargeAfterMaturityAddedDate = dateFormatter.format(LocalDate.of(2022, 10, 4));
        assertNotNull(addLoanCharge(loanId, feeChargeAfterMaturity, feeChargeAfterMaturityAddedDate, 10.0).getResourceId());

        loanDetails = getLoanDetails(loanId);
        assertEquals(loanRepaymentScheduleSize + 1, loanDetails.getRepaymentSchedule().getPeriods().size());
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

    private PostLoansLoanIdTransactionsRequest transactionOf(final String transactionDate, final Double amount) {
        return new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate(transactionDate)
                .locale(LoanTestData.LOCALE).transactionAmount(amount);
    }
}
