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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentScheduleInstallment;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetPostDatedChecks;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PaymentTypeData;
import org.apache.fineract.client.models.PostLoansLoanIdPostDatedCheckData;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class RepaymentWithPostDatedChecksTest extends FeignLoanTestBase {

    private static final String DISBURSAL_DATE = "04 April 2012";
    private static final Double PROPOSED_AMOUNT = 8000.0;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);
    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testRepaymentWithPostDatedChecks() {
        final Long clientId = createClient();
        assertNotNull(clientId);
        assertEquals(clientId, clientHelper.getClient(clientId).getId(), "ERROR IN CREATING THE CLIENT");

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().buildRequest(null));
        assertNotNull(loanProductId, "Could not create Loan Product");

        final Long loanId = applyForLoanApplication(clientId, loanProductId);
        assertNotNull(loanId, "Could not create Loan Account");

        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(PROPOSED_AMOUNT, DISBURSAL_DATE));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);

        final List<GetLoansLoanIdRepaymentScheduleInstallment> installmentData = transactionHelper
                .retrieveTransactionTemplate(loanId, "disburse", null, null, null).getLoanRepaymentScheduleInstallments();
        assertNotNull(installmentData, "Empty Installment Data Template");

        final LocalDate firstInstallmentDate = installmentData.get(0).getDate();
        assertNotNull(firstInstallmentDate);
        final String loanRepaymentDate = dateFormatter.format(firstInstallmentDate);
        final BigDecimal firstInstallmentAmount = installmentData.get(0).getAmount();

        final List<PostLoansLoanIdPostDatedCheckData> postDatedChecks = new ArrayList<>();
        for (GetLoansLoanIdRepaymentScheduleInstallment installment : installmentData) {
            postDatedChecks.add(postDatedCheck(installment.getInstallmentId(), installment.getAmount()));
        }
        assertNotNull(postDatedChecks);

        disburseLoan(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(DISBURSAL_DATE)//
                .transactionAmount(BigDecimal.valueOf(PROPOSED_AMOUNT))//
                .note("DISBURSE NOTE")//
                .postDatedChecks(postDatedChecks)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        final String name = "PDC";
        final Long paymentTypeId = PaymentTypeHelper.createPaymentType(new PaymentTypeCreateRequest().name(name)
                .description(PaymentTypeHelper.randomNameGenerator("PDC", 15)).isCashPayment(false).position(1L)).getResourceId();
        assertNotNull(paymentTypeId);
        PaymentTypeHelper.verifyPaymentTypeCreatedOnServer(paymentTypeId);
        final PaymentTypeData paymentTypeResponse = PaymentTypeHelper.retrieveById(paymentTypeId);
        assertEquals(name, paymentTypeResponse.getName());

        final GetPostDatedChecks postDatedCheck = loanHelper.getPostDatedCheck(loanId, 1);
        assertNotNull(postDatedCheck);
        assertNotNull(postDatedCheck.getAmount());

        makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest()//
                .transactionDate(loanRepaymentDate)//
                .transactionAmount(firstInstallmentAmount.doubleValue())//
                .paymentTypeId(paymentTypeId)//
                .note("Repayment Made!!!")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);

        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, "02 April 2012", PROPOSED_AMOUNT, 5)//
                .expectedDisbursementDate(DISBURSAL_DATE)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));

        return loanHelper.applyForLoan(application).getLoanId();
    }

    private PostLoansLoanIdPostDatedCheckData postDatedCheck(final Integer installmentId, final BigDecimal amount) {
        return new PostLoansLoanIdPostDatedCheckData()//
                .installmentId(installmentId)//
                .name("AMANA BANK")//
                .amount(amount)//
                .accountNo(900400500621L)//
                .checkNo(Utils.uniqueRandomNumberGenerator(9).longValue());
    }
}
