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
package org.apache.fineract.integrationtests.guarantor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GuarantorData;
import org.apache.fineract.client.models.GuarantorFundingData;
import org.apache.fineract.client.models.GuarantorsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGuarantorHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class GuarantorTest extends FeignLoanTestBase {

    private static final BigDecimal SELF1_BALANCE = BigDecimal.valueOf(5000);
    private static final BigDecimal EXTERNAL1_BALANCE = BigDecimal.valueOf(5000);
    private static final BigDecimal EXTERNAL2_BALANCE = BigDecimal.valueOf(5000);
    private static final BigDecimal SELF1_GURANTEE = BigDecimal.valueOf(2000);
    private static final BigDecimal EXTERNAL1_GURANTEE = BigDecimal.valueOf(2000);
    private static final BigDecimal EXTERNAL2_GURANTEE = BigDecimal.valueOf(1000);

    private static final String SAVINGS_TRANSACTION_DATE = "01 March 2013";

    private static final String MIN_SELF_GUARANTEE_REQUIRED = "validation.msg.loan.guarantor.min.self.guarantee.required";
    private static final String MIN_EXTERNAL_GUARANTEE_REQUIRED = "validation.msg.loan.guarantor.min.external.guarantee.required";
    private static final String MANDATED_GUARANTEE_REQUIRED = "validation.msg.loan.guarantor.mandated.guarantee.required";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

    private final FeignGuarantorHelper guarantorHelper = new FeignGuarantorHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsHelper savingsHelper = new FeignSavingsHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsProductHelper savingsProductHelper = new FeignSavingsProductHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsTransactionHelper savingsTransactionHelper = new FeignSavingsTransactionHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final FeignGroupHelper groupHelper = new FeignGroupHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testGuarantor() {
        BigDecimal self1HoldFunds = BigDecimal.ZERO;
        BigDecimal external1HoldFunds = BigDecimal.ZERO;
        BigDecimal external2HoldFunds = BigDecimal.ZERO;

        final Long clientId = createVerifiedClient();
        final Long clientIdExternal = createVerifiedClient();
        final Long clientIdExternal2 = createVerifiedClient();

        final Long selfSavingsId = openSavingsAccount(clientId, SELF1_BALANCE);
        final Long externalSavingsId1 = openSavingsAccount(clientIdExternal, EXTERNAL1_BALANCE);
        final Long externalSavingsId2 = openSavingsAccount(clientIdExternal2, EXTERNAL2_BALANCE);

        final Long loanProductId = createLoanProductWithHoldFunds("50", "20", "20");
        final String loanDisbursementDate = daysAgo(7 * 4);
        final Long loanId = applyForCollateralisedLoan(clientId, loanProductId, loanDisbursementDate);
        assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        final Long externalGuarantor = guarantorHelper.createGuarantor(loanId, externalCustomer());
        assertNotNull(externalGuarantor);

        final Long withoutGuaranteeAmount = guarantorHelper.createGuarantor(loanId,
                existingCustomerWithoutGuaranteeAmount(clientIdExternal));
        assertNotNull(withoutGuaranteeAmount);

        List<String> errors = approveExpectingErrors(loanId, loanDisbursementDate);
        assertTrue(errors.contains(MIN_SELF_GUARANTEE_REQUIRED));
        assertTrue(errors.contains(MIN_EXTERNAL_GUARANTEE_REQUIRED));
        assertTrue(errors.contains(MANDATED_GUARANTEE_REQUIRED));

        final Long selfGuarantee = guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(clientId, selfSavingsId, SELF1_GURANTEE));
        verifySavingsOnHoldBalance(selfSavingsId, null);
        assertNotNull(selfGuarantee);

        errors = approveExpectingErrors(loanId, loanDisbursementDate);
        assertFalse(errors.contains(MIN_SELF_GUARANTEE_REQUIRED));
        assertTrue(errors.contains(MIN_EXTERNAL_GUARANTEE_REQUIRED));
        assertTrue(errors.contains(MANDATED_GUARANTEE_REQUIRED));

        final Long externalGuarantee1 = guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(clientIdExternal, externalSavingsId1, EXTERNAL1_GURANTEE));
        verifySavingsOnHoldBalance(externalSavingsId1, null);
        assertNotNull(externalGuarantee1);

        errors = approveExpectingErrors(loanId, loanDisbursementDate);
        assertFalse(errors.contains(MIN_SELF_GUARANTEE_REQUIRED));
        assertFalse(errors.contains(MIN_EXTERNAL_GUARANTEE_REQUIRED));
        assertTrue(errors.contains(MANDATED_GUARANTEE_REQUIRED));

        final Long externalGuarantee2 = guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(clientIdExternal2, externalSavingsId2, EXTERNAL2_GURANTEE));
        verifySavingsOnHoldBalance(externalSavingsId2, null);
        assertNotNull(externalGuarantee2);

        approveLoan(loanId, loanDisbursementDate);
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);
        self1HoldFunds = self1HoldFunds.add(SELF1_GURANTEE);
        external1HoldFunds = external1HoldFunds.add(EXTERNAL1_GURANTEE);
        external2HoldFunds = external2HoldFunds.add(EXTERNAL2_GURANTEE);
        verifySavingsOnHoldBalance(selfSavingsId, self1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId1, external1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId2, external2HoldFunds);

        undoApproval(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        verifySavingsOnHoldBalance(selfSavingsId, BigDecimal.ZERO);
        verifySavingsOnHoldBalance(externalSavingsId1, BigDecimal.ZERO);
        verifySavingsOnHoldBalance(externalSavingsId2, BigDecimal.ZERO);

        approveLoan(loanId, loanDisbursementDate);
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);
        verifySavingsOnHoldBalance(selfSavingsId, self1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId1, external1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId2, external2HoldFunds);

        disburseWithNetDisbursalAmount(loanId, loanDisbursementDate);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        // First repayment
        external1HoldFunds = external1HoldFunds.subtract(BigDecimal.valueOf(827.5867));
        external2HoldFunds = external2HoldFunds.subtract(BigDecimal.valueOf(413.7933));
        repayInstallment(loanId, 1, daysAgo(7 * 3));
        verifySavingsOnHoldBalance(selfSavingsId, self1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId1, external1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId2, external2HoldFunds);

        // Second repayment
        external1HoldFunds = external1HoldFunds.subtract(BigDecimal.valueOf(831.4067));
        external2HoldFunds = external2HoldFunds.subtract(BigDecimal.valueOf(415.7033333));
        repayInstallment(loanId, 2, daysAgo(7 * 2));
        verifySavingsOnHoldBalance(selfSavingsId, self1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId1, external1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId2, external2HoldFunds);

        // third repayment
        self1HoldFunds = self1HoldFunds.subtract(BigDecimal.valueOf(741.355));
        repayInstallment(loanId, 3, daysAgo(7));
        verifySavingsOnHoldBalance(selfSavingsId, self1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId1, BigDecimal.ZERO);
        verifySavingsOnHoldBalance(externalSavingsId2, BigDecimal.ZERO);

        // forth repayment
        repayInstallment(loanId, 3, daysAgo(0));
        verifySavingsOnHoldBalance(selfSavingsId, BigDecimal.ZERO);
        verifySavingsOnHoldBalance(externalSavingsId1, BigDecimal.ZERO);
        verifySavingsOnHoldBalance(externalSavingsId2, BigDecimal.ZERO);

        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getClosed);
    }

    @Test
    public void testGuarantor_UNDO_DISBURSAL() {
        BigDecimal self1HoldFunds = BigDecimal.ZERO;
        BigDecimal external1HoldFunds = BigDecimal.ZERO;
        BigDecimal external2HoldFunds = BigDecimal.ZERO;

        final Long clientId = createVerifiedClient();
        final Long clientIdExternal = createVerifiedClient();
        final Long clientIdExternal2 = createVerifiedClient();

        final Long selfSavingsId = openSavingsAccount(clientId, SELF1_BALANCE);
        final Long externalSavingsId1 = openSavingsAccount(clientIdExternal, EXTERNAL1_BALANCE);
        final Long externalSavingsId3 = openSavingsAccount(clientIdExternal, EXTERNAL1_BALANCE);
        final Long externalSavingsId2 = openSavingsAccount(clientIdExternal2, EXTERNAL2_BALANCE);

        final Long loanProductId = createLoanProductWithHoldFunds("50", "20", "20");
        final String loanDisbursementDate = daysAgo(7 * 4);
        final Long loanId = applyForCollateralisedLoan(clientId, loanProductId, loanDisbursementDate);
        assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        final Long externalGuarantor = guarantorHelper.createGuarantor(loanId, externalCustomer());
        assertNotNull(externalGuarantor);

        final Long withoutGuaranteeAmount = guarantorHelper.createGuarantor(loanId,
                existingCustomerWithoutGuaranteeAmount(clientIdExternal));
        assertNotNull(withoutGuaranteeAmount);

        List<String> errors = approveExpectingErrors(loanId, loanDisbursementDate);
        assertTrue(errors.contains(MIN_SELF_GUARANTEE_REQUIRED));
        assertTrue(errors.contains(MIN_EXTERNAL_GUARANTEE_REQUIRED));
        assertTrue(errors.contains(MANDATED_GUARANTEE_REQUIRED));

        final Long selfGuarantee = guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(clientId, selfSavingsId, SELF1_GURANTEE));
        verifySavingsOnHoldBalance(selfSavingsId, null);
        assertNotNull(selfGuarantee);

        errors = approveExpectingErrors(loanId, loanDisbursementDate);
        assertFalse(errors.contains(MIN_SELF_GUARANTEE_REQUIRED));
        assertTrue(errors.contains(MIN_EXTERNAL_GUARANTEE_REQUIRED));
        assertTrue(errors.contains(MANDATED_GUARANTEE_REQUIRED));

        final Long externalGuarantee1 = guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(clientIdExternal, externalSavingsId1, EXTERNAL1_GURANTEE));
        verifySavingsOnHoldBalance(externalSavingsId1, null);
        assertNotNull(externalGuarantee1);

        errors = approveExpectingErrors(loanId, loanDisbursementDate);
        assertFalse(errors.contains(MIN_SELF_GUARANTEE_REQUIRED));
        assertFalse(errors.contains(MIN_EXTERNAL_GUARANTEE_REQUIRED));
        assertTrue(errors.contains(MANDATED_GUARANTEE_REQUIRED));

        final Long externalGuarantee2 = guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(clientIdExternal2, externalSavingsId2, EXTERNAL2_GURANTEE));
        assertNotNull(externalGuarantee2);
        verifySavingsOnHoldBalance(externalSavingsId2, null);

        approveLoan(loanId, loanDisbursementDate);
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);
        self1HoldFunds = self1HoldFunds.add(SELF1_GURANTEE);
        external1HoldFunds = external1HoldFunds.add(EXTERNAL1_GURANTEE);
        external2HoldFunds = external2HoldFunds.add(EXTERNAL2_GURANTEE);
        verifySavingsOnHoldBalance(selfSavingsId, self1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId1, external1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId2, external2HoldFunds);

        assertEquals(externalGuarantor, guarantorHelper.deleteGuarantor(loanId, externalGuarantor).getResourceId());
        assertFalse(guarantorHelper.getGuarantor(loanId, externalGuarantor).getStatus());

        assertTrue(deleteExpectingErrors(loanId, withoutGuaranteeAmount, null).contains("error.msg.loan.guarantor.not.found"));
        assertEquals(4, guarantorHelper.getAllGuarantors(loanId).size());

        final Long fundDetailId = guarantorHelper.getGuarantor(loanId, externalGuarantee1).getGuarantorFundingDetails().get(0).getId();
        assertTrue(deleteExpectingErrors(loanId, externalGuarantee1, fundDetailId).contains(MIN_EXTERNAL_GUARANTEE_REQUIRED));

        final Long externalGuarantee3 = guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(clientIdExternal, externalSavingsId3, EXTERNAL1_GURANTEE));
        verifySavingsOnHoldBalance(externalSavingsId3, EXTERNAL1_GURANTEE);
        assertNotNull(externalGuarantee3);

        assertEquals(externalGuarantee3, guarantorHelper.deleteGuarantor(loanId, externalGuarantee3, fundDetailId).getResourceId());
        assertEquals(4, guarantorHelper.getAllGuarantors(loanId).size());

        final List<GuarantorFundingData> externalGuarantee1Details = guarantorHelper.getGuarantor(loanId, externalGuarantee1)
                .getGuarantorFundingDetails();
        assertEquals(2, externalGuarantee1Details.size());
        for (GuarantorFundingData fundingData : externalGuarantee1Details) {
            if (fundDetailId.equals(fundingData.getId())) {
                assertEquals("guarantorFundStatusType.withdrawn", fundingData.getStatus().getCode());
            }
        }

        disburseWithNetDisbursalAmount(loanId, loanDisbursementDate);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        // First repayment
        external1HoldFunds = external1HoldFunds.subtract(BigDecimal.valueOf(827.5867));
        external2HoldFunds = external2HoldFunds.subtract(BigDecimal.valueOf(413.7933));
        repayInstallment(loanId, 1, daysAgo(7 * 3));
        verifySavingsOnHoldBalance(selfSavingsId, self1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId3, external1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId2, external2HoldFunds);

        // Second repayment
        external1HoldFunds = external1HoldFunds.subtract(BigDecimal.valueOf(831.4067));
        external2HoldFunds = external2HoldFunds.subtract(BigDecimal.valueOf(415.7033333));
        repayInstallment(loanId, 2, daysAgo(7 * 2));
        verifySavingsOnHoldBalance(selfSavingsId, self1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId3, external1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId2, external2HoldFunds);

        // third repayment
        final String thirdRepaymentDate = daysAgo(7);
        final BigDecimal self1HoldFundsTemp = self1HoldFunds.subtract(BigDecimal.valueOf(741.355));
        final Long thirdRepaymentId = repayInstallment(loanId, 3, thirdRepaymentDate);
        verifySavingsOnHoldBalance(selfSavingsId, self1HoldFundsTemp);
        verifySavingsOnHoldBalance(externalSavingsId3, BigDecimal.ZERO);
        verifySavingsOnHoldBalance(externalSavingsId2, BigDecimal.ZERO);

        // undo repayment
        adjustLoanTransaction(loanId, thirdRepaymentId, thirdRepaymentDate);
        verifySavingsOnHoldBalance(selfSavingsId, self1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId3, external1HoldFunds);
        verifySavingsOnHoldBalance(externalSavingsId2, external2HoldFunds);

        // undo disbursal
        loanHelper.undoDisbursement(loanId);
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);
        verifySavingsOnHoldBalance(selfSavingsId, SELF1_GURANTEE);
        verifySavingsOnHoldBalance(externalSavingsId3, EXTERNAL1_GURANTEE);
        verifySavingsOnHoldBalance(externalSavingsId2, EXTERNAL2_GURANTEE);
    }

    @Test
    public void testGuarantor_RECOVER_GUARANTEES() {
        recoverGuaranteesScenario(SELF1_BALANCE, EXTERNAL1_BALANCE, SELF1_GURANTEE, EXTERNAL1_GURANTEE, BigDecimal.valueOf(993.104),
                (selfBalance, selfHold) -> selfBalance.subtract(selfHold),
                (externalBalance, externalHold) -> externalBalance.subtract(externalHold));
    }

    @Test
    public void testGuarantor_RECOVER_GUARANTEES_WITH_MORE_GUARANTEE() {
        recoverGuaranteesScenario(BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), BigDecimal.valueOf(6000), BigDecimal.valueOf(7000),
                BigDecimal.valueOf(3227.588), (selfBalance, selfHold) -> selfBalance.subtract(BigDecimal.valueOf(4615.385)),
                (externalBalance, externalHold) -> externalBalance.subtract(BigDecimal.valueOf(2901.8553)));
    }

    @Test
    public void testGuarantor_WRITE_OFF_LOAN() {
        final Long clientId = createVerifiedClient();
        final Long clientIdExternal = createVerifiedClient();

        final Long selfSavingsId = openSavingsAccount(clientId, SELF1_BALANCE);
        final Long externalSavingsId1 = openSavingsAccount(clientIdExternal, EXTERNAL1_BALANCE);

        final Long loanProductId = createLoanProductWithHoldFunds("40", "20", "20");
        final String loanDisbursementDate = daysAgo(21);
        final Long loanId = applyForCollateralisedLoan(clientId, loanProductId, loanDisbursementDate);
        assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        assertNotNull(
                guarantorHelper.createGuarantor(loanId, existingCustomerWithGuaranteeAmount(clientId, selfSavingsId, SELF1_GURANTEE)));
        verifySavingsOnHoldBalance(selfSavingsId, null);

        assertNotNull(guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(clientIdExternal, externalSavingsId1, EXTERNAL1_GURANTEE)));
        verifySavingsOnHoldBalance(externalSavingsId1, null);

        approveLoan(loanId, loanDisbursementDate);
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);
        verifySavingsOnHoldBalance(selfSavingsId, SELF1_GURANTEE);
        verifySavingsOnHoldBalance(externalSavingsId1, EXTERNAL1_GURANTEE);

        disburseWithNetDisbursalAmount(loanId, loanDisbursementDate);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        // First repayment
        repayInstallment(loanId, 1, daysAgo(14));
        verifySavingsOnHoldBalance(selfSavingsId, SELF1_GURANTEE);
        verifySavingsOnHoldBalance(externalSavingsId1, EXTERNAL1_GURANTEE.subtract(BigDecimal.valueOf(993.104)));

        writeOffLoan(loanId, daysAgo(0));
        verifySavingsBalanceAndOnHoldBalance(selfSavingsId, BigDecimal.ZERO, SELF1_BALANCE);
        verifySavingsBalanceAndOnHoldBalance(externalSavingsId1, BigDecimal.ZERO, EXTERNAL1_BALANCE);
    }

    private void recoverGuaranteesScenario(final BigDecimal selfBalance, final BigDecimal externalBalance, final BigDecimal selfGuarantee,
            final BigDecimal externalGuarantee, final BigDecimal firstRepaymentRelease,
            final java.util.function.BinaryOperator<BigDecimal> expectedSelfBalance,
            final java.util.function.BinaryOperator<BigDecimal> expectedExternalBalance) {
        final Long clientId = createVerifiedClient();
        final Long clientIdExternal = createVerifiedClient();

        final Long selfSavingsId = openSavingsAccount(clientId, selfBalance);
        final Long externalSavingsId1 = openSavingsAccount(clientIdExternal, externalBalance);

        final Long loanProductId = createLoanProductWithHoldFunds("40", "20", "20");
        final String loanDisbursementDate = daysAgo(21);
        final Long loanId = applyForCollateralisedLoan(clientId, loanProductId, loanDisbursementDate);
        assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        assertNotNull(guarantorHelper.createGuarantor(loanId, existingCustomerWithGuaranteeAmount(clientId, selfSavingsId, selfGuarantee)));
        verifySavingsOnHoldBalance(selfSavingsId, null);

        assertNotNull(guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(clientIdExternal, externalSavingsId1, externalGuarantee)));
        verifySavingsOnHoldBalance(externalSavingsId1, null);

        approveLoan(loanId, loanDisbursementDate);
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);
        verifySavingsOnHoldBalance(selfSavingsId, selfGuarantee);
        verifySavingsOnHoldBalance(externalSavingsId1, externalGuarantee);

        disburseWithNetDisbursalAmount(loanId, loanDisbursementDate);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        // First repayment
        final BigDecimal externalHoldAfterRepayment = externalGuarantee.subtract(firstRepaymentRelease);
        repayInstallment(loanId, 1, daysAgo(14));
        verifySavingsOnHoldBalance(selfSavingsId, selfGuarantee);
        verifySavingsOnHoldBalance(externalSavingsId1, externalHoldAfterRepayment);

        loanHelper.recoverGuarantees(loanId);
        verifySavingsBalanceAndOnHoldBalance(selfSavingsId, BigDecimal.ZERO, expectedSelfBalance.apply(selfBalance, selfGuarantee));
        verifySavingsBalanceAndOnHoldBalance(externalSavingsId1, BigDecimal.ZERO,
                expectedExternalBalance.apply(externalBalance, externalHoldAfterRepayment));
    }

    private List<String> deleteExpectingErrors(final Long loanId, final Long guarantorId, final Long guarantorFundingId) {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> {
            if (guarantorFundingId == null) {
                guarantorHelper.deleteGuarantor(loanId, guarantorId);
            } else {
                guarantorHelper.deleteGuarantor(loanId, guarantorId, guarantorFundingId);
            }
        });
        return errorGlobalisationCodes(exception);
    }

    @Test
    public void testGuarantorWithGroupSavingsAccount() {
        final Long groupId = groupHelper.createActiveGroup().getGroupId();
        assertNotNull(groupId);

        final Long clientInGroupId = createVerifiedClient();
        groupHelper.associateClient(groupId, clientInGroupId);

        final Long savingsProductId = savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().minRequiredOpeningBalance(BigDecimal.valueOf(5000.0)))
                .getResourceId();
        assertNotNull(savingsProductId);

        final Long groupSavingsId = savingsHelper.submitApplication(new PostSavingsAccountsRequest()//
                .groupId(groupId)//
                .productId(savingsProductId)//
                .submittedOnDate(SAVINGS_TRANSACTION_DATE)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)).getSavingsId();
        assertNotNull(groupSavingsId);

        savingsHelper.approveSavings(groupSavingsId, SAVINGS_TRANSACTION_DATE);
        savingsHelper.activateSavings(groupSavingsId, SAVINGS_TRANSACTION_DATE);

        assertNotNull(savingsTransactionHelper.deposit(groupSavingsId, "5000", SAVINGS_TRANSACTION_DATE).getResourceId());

        final Long loanClientId = createVerifiedClient();
        final Long selfSavingsId = openSavingsAccount(loanClientId, BigDecimal.valueOf(5000.0));
        assertNotNull(selfSavingsId);

        final Long externalClientId = createVerifiedClient();
        final Long externalSavingsId = openSavingsAccount(externalClientId, BigDecimal.valueOf(5000.0));
        assertNotNull(externalSavingsId);

        final Long loanProductId = createLoanProductWithHoldFunds("0", "0", "0");
        assertNotNull(loanProductId);

        final Long loanId = applyForCollateralisedLoan(loanClientId, loanProductId, SAVINGS_TRANSACTION_DATE);
        assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        assertNotNull(guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(loanClientId, selfSavingsId, BigDecimal.valueOf(2000))));

        // a guarantor funded by a GROUP savings account, the case this test exists for
        final Long groupSavingsGuarantorId = guarantorHelper.createGuarantor(loanId,
                existingCustomerWithGuaranteeAmount(clientInGroupId, groupSavingsId, BigDecimal.valueOf(2000)));
        assertNotNull(groupSavingsGuarantorId);

        approveLoan(loanId, SAVINGS_TRANSACTION_DATE);
        verifyLoanStatus(loanId, LoanStatus.APPROVED);

        disburseLoan(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(SAVINGS_TRANSACTION_DATE)//
                .netDisbursalAmount(BigDecimal.valueOf(10000))//
                .note("DISBURSE NOTE")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        final List<GuarantorData> guarantors = guarantorHelper.getAllGuarantors(loanId);
        assertNotNull(guarantors);
        assertFalse(guarantors.isEmpty(), "Should have at least one guarantor");

        boolean foundGuarantorWithCorrectSavingsId = false;
        for (GuarantorData guarantor : guarantors) {
            if (!groupSavingsGuarantorId.equals(guarantor.getId())) {
                continue;
            }
            final List<GuarantorFundingData> fundingDetails = guarantor.getGuarantorFundingDetails();
            assertNotNull(fundingDetails, "Guarantor funding details should not be null");
            assertFalse(fundingDetails.isEmpty(), "Guarantor funding details should not be empty");

            for (GuarantorFundingData fundingDetail : fundingDetails) {
                assertNotNull(fundingDetail.getSavingsAccount(), "Savings account in funding details should not be null");
                final Long savingsIdFromGuarantor = fundingDetail.getSavingsAccount().getId();
                assertNotNull(savingsIdFromGuarantor, "Savings account ID should not be null");
                assertNotEquals(Long.valueOf(0), savingsIdFromGuarantor, "Savings account ID should not be 0 for group savings guarantor");
                assertEquals(groupSavingsId, savingsIdFromGuarantor, "Savings account ID should match the group savings account ID");
                foundGuarantorWithCorrectSavingsId = true;
            }
        }

        assertTrue(foundGuarantorWithCorrectSavingsId, "Should have found guarantor with correct group savings account ID");
    }

    private Long createVerifiedClient() {
        final Long clientId = createClient();
        assertEquals(clientId, clientHelper.getClient(clientId).getId(), "ERROR IN CREATING THE CLIENT");
        return clientId;
    }

    private String daysAgo(final int days) {
        return dateFormatter.format(Utils.getLocalDateOfTenant().minusDays(days));
    }

    private void approveLoan(final Long loanId, final String approvalDate) {
        approveLoan(loanId, LoanRequestBuilders.approveLoan(10000.00, approvalDate));
    }

    private List<String> approveExpectingErrors(final Long loanId, final String approvalDate) {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> approveLoan(loanId, approvalDate));
        return errorGlobalisationCodes(exception);
    }

    /** Every globalisation code in a multi-error validation response, not just the first. */
    private List<String> errorGlobalisationCodes(final CallFailedRuntimeException exception) {
        final List<String> codes = new ArrayList<>();
        try {
            final Map<String, Object> body = ObjectMapperFactory.getShared().readValue(exception.getResponseBody(),
                    new TypeReference<Map<String, Object>>() {});
            final Object errors = body.get("errors");
            if (errors instanceof List<?> errorList) {
                for (Object error : errorList) {
                    if (error instanceof Map<?, ?> errorMap) {
                        final Object code = errorMap.get("userMessageGlobalisationCode");
                        if (code != null) {
                            codes.add(code.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not read the validation errors from " + exception.getResponseBody(), e);
        }
        return codes;
    }

    private void disburseWithNetDisbursalAmount(final Long loanId, final String disbursalDate) {
        disburseLoan(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(disbursalDate)//
                .netDisbursalAmount(getLoanDetails(loanId).getNetDisbursalAmount())//
                .note("DISBURSE NOTE")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
    }

    private Long repayInstallment(final Long loanId, final int installment, final String repaymentDate) {
        final BigDecimal totalDueForCurrentPeriod = getLoanDetails(loanId).getRepaymentSchedule().getPeriods().get(installment)
                .getTotalDueForPeriod();
        return makeLoanRepayment(loanId, LoanRequestBuilders.repayLoan(totalDueForCurrentPeriod.doubleValue(), repaymentDate))
                .getResourceId();
    }

    /** A savings account that has never backed a guarantee reports no on-hold funds at all, not a zero balance. */
    private void verifySavingsOnHoldBalance(final Long savingsId, final BigDecimal expectedBalance) {
        final BigDecimal onHoldAmount = savingsHelper.getSavingsDetails(savingsId).getOnHoldFunds();
        if (expectedBalance == null) {
            assertNull(onHoldAmount, "Verifying On Hold Funds");
        } else {
            assertAmountEquals(expectedBalance, onHoldAmount, "Verifying On Hold Funds");
        }
    }

    /**
     * The expected amounts carry over from the pre-Feign test, which held every running total in {@code float} and
     * accumulated it with repeated subtraction. That keeps roughly seven significant digits and drifts a little in the
     * last of them - the stated "341.0066" and "7098.1447" sit either side of the server's 341.006667 and 7098.1446.
     * Feign reports the full-precision value where REST-assured re-rounded it to {@code float}, so the comparison
     * carries an explicit tolerance: far tighter than a cent, and wider than the transcription can resolve.
     */
    private static final BigDecimal AMOUNT_TOLERANCE = new BigDecimal("0.001");

    private static void assertAmountEquals(final BigDecimal expected, final BigDecimal actual, final String what) {
        assertTrue(actual != null && expected.subtract(actual).abs().compareTo(AMOUNT_TOLERANCE) <= 0,
                () -> what + ": expected " + expected + " but was " + actual);
    }

    private void verifySavingsBalanceAndOnHoldBalance(final Long savingsId, final BigDecimal expectedOnHold,
            final BigDecimal accountBalance) {
        final SavingsAccountData savingsDetails = savingsHelper.getSavingsDetails(savingsId, "summary");
        final BigDecimal actualOnHold = savingsDetails.getOnHoldFunds();
        final BigDecimal actualBalance = savingsDetails.getSummary().getAccountBalance();
        assertAmountEquals(expectedOnHold, actualOnHold, "Verifying On Hold Funds");
        assertAmountEquals(accountBalance, actualBalance, "Verifying Account balance");
    }

    private Long openSavingsAccount(final Long clientId, final BigDecimal minimumOpeningBalance) {
        final Long savingsProductId = savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().minRequiredOpeningBalance(minimumOpeningBalance))
                .getResourceId();
        assertNotNull(savingsProductId);
        return savingsHelper.createApproveActivateSavings(clientId, savingsProductId, SAVINGS_TRANSACTION_DATE);
    }

    private GuarantorsRequest externalCustomer() {
        return new GuarantorsRequest()//
                .guarantorTypeId(3)//
                .firstname(Utils.randomStringGenerator("guarantor_FirstName_", 5))//
                .lastname(Utils.randomStringGenerator("guarantor_LastName_", 4))//
                .addressLine1("addressLine1")//
                .addressLine2("addressLine2")//
                .city("city")//
                .state("state")//
                .zip("123456")//
                .locale(LoanTestData.LOCALE);
    }

    private GuarantorsRequest existingCustomerWithoutGuaranteeAmount(final Long entityId) {
        return new GuarantorsRequest().guarantorTypeId(1).entityId(entityId).locale(LoanTestData.LOCALE);
    }

    private GuarantorsRequest existingCustomerWithGuaranteeAmount(final Long entityId, final Long savingsId, final BigDecimal amount) {
        return new GuarantorsRequest()//
                .guarantorTypeId(1)//
                .entityId(entityId)//
                .savingsId(savingsId)//
                .amount(amount)//
                .locale(LoanTestData.LOCALE);
    }

    private Long createLoanProductWithHoldFunds(final String mandatoryGuarantee, final String minimumGuaranteeFromGuarantor,
            final String minimumGuaranteeFromOwnFunds) {
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal("10000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsWeek().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withOnHoldFundDetails(mandatoryGuarantee, minimumGuaranteeFromGuarantor, minimumGuaranteeFromOwnFunds).buildRequest(null));
    }

    private Long applyForCollateralisedLoan(final Long clientId, final Long loanProductId, final String disbursementDate) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);

        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, disbursementDate, 10000.00, 4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));

        return loanHelper.applyForLoan(application).getLoanId();
    }
}
