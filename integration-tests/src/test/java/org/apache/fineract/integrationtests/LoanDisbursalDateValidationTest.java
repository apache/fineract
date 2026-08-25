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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class LoanDisbursalDateValidationTest extends FeignLoanTestBase {

    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void loanApplicationValidateDisbursalDate() {

        final Double proposedAmount = 5000.0;
        final String approveDate = "01 March 2014";
        final String disbursalDate = "02 March 2014";

        final Long clientId = createClient("01 January 2014");

        final Long loanProductId = createLoanProduct(
                new LoanProductTestBuilder().withSyncExpectedWithDisbursementDate(true).buildRequest(null));

        final Long loanId = applyForLoanApplication(clientId, loanProductId, proposedAmount);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(proposedAmount, approveDate));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);

        final BigDecimal netDisbursalAmount = getLoanDetails(loanId).getNetDisbursalAmount();
        final PostLoansLoanIdRequest disbursement = new PostLoansLoanIdRequest()//
                .actualDisbursementDate(disbursalDate)//
                .netDisbursalAmount(netDisbursalAmount)//
                .note("DISBURSE NOTE")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> disburseLoan(loanId, disbursement));

        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, "error.msg.actual.disbursement.date.does.not.match.with.expected.disbursal.date");
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, final Double proposedAmount) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);

        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, "26 February 2014", proposedAmount, 5)//
                .expectedDisbursementDate("01 March 2014")//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));
        return loanHelper.applyForLoan(application).getLoanId();
    }
}
