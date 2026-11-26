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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanTransactionWritePlatformServiceImplTest {

    private static final Long LOAN_ID = 1L;
    private static final Long REFUND_ID = 2L;
    private static final LocalDate REFUND_DATE = LocalDate.of(2026, 1, 15);

    @Mock
    private LoanTransactionValidator loanTransactionValidator;
    @Mock
    private LoanAccountDomainService loanAccountDomainService;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private LoanAssembler loanAssembler;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    @Mock
    private AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private LoanUtilService loanUtilService;
    @Mock
    private LoanLifecycleStateMachine loanLifecycleStateMachine;
    @Mock
    private org.apache.fineract.infrastructure.core.service.ExternalIdFactory externalIdFactory;
    @Mock
    private LoanAccrualsProcessingService loanAccrualsProcessingService;
    @Mock
    private LoanScheduleService loanScheduleService;
    @Mock
    private ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    @Mock
    private LoanAccountService loanAccountService;
    @Mock
    private LoanJournalEntryPoster journalEntryPoster;
    @Mock
    private LoanTransactionProcessingService loanTransactionProcessingService;
    @Mock
    private LoanBalanceService loanBalanceService;
    @Mock
    private LoanReadPlatformService loanReadPlatformService;
    @Mock
    private GLIMAccountInfoRepository glimRepository;
    @Mock
    private LoanAdjustmentService loanAdjustmentService;
    @Mock
    private GuarantorDomainService guarantorDomainService;
    @Mock
    private LoanTransactionAssembler loanTransactionAssembler;
    @Mock
    private LoanTransactionRelationRepository loanTransactionRelationRepository;
    @Mock
    private LoanTransactionService loanTransactionService;
    @Mock
    private LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator;
    @Mock
    private LoanDownPaymentHandlerService loanDownPaymentHandlerService;
    @Mock
    private LoanFeeRefundService loanFeeRefundService;

    @InjectMocks
    private LoanTransactionWritePlatformServiceImpl underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, REFUND_DATE)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void recoverFromGuarantor_success() {
        Long loanId = 1L;
        Loan loan = mock(Loan.class);
        ExternalId externalId = ExternalId.generate();
        when(loanAssembler.assembleFromInUserScope(loanId)).thenReturn(loan);
        when(loan.getExternalId()).thenReturn(externalId);

        CommandProcessingResult result = underTest.recoverFromGuarantor(loanId);

        assertNotNull(result);
        assertEquals(loanId, result.getLoanId());
        assertEquals(loanId, result.getResourceId());
        assertEquals(externalId, result.getResourceExternalId());
        verify(guarantorDomainService).transferFundsFromGuarantor(loan);
    }

    @Test
    void handleChargebackTransaction_interestRecalculationEnabled_reprocessesTransactions() {
        Loan loan = mock(Loan.class);
        LoanTransaction chargebackTransaction = mock(LoanTransaction.class);
        LocalDate date = LocalDate.of(2025, 5, 20);

        when(chargebackTransaction.getTransactionDate()).thenReturn(date);
        when(loan.isInterestBearing()).thenReturn(true);
        when(loan.isInterestRecalculationEnabled()).thenReturn(true);

        underTest.handleChargebackTransaction(loan, chargebackTransaction);

        verify(loanTransactionValidator).validateIfTransactionIsChargeback(chargebackTransaction);
        verify(loan).addLoanTransaction(chargebackTransaction);
        verify(reprocessLoanTransactionsService).reprocessTransactions(loan);
        verify(loanLifecycleStateMachine).determineAndTransition(loan, date);
    }

    @Test
    void makeGLIMLoanRepayment_parentNotFound_throwsException() {
        Long loanId = 10L;
        JsonCommand command = mock(JsonCommand.class);
        when(glimRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> underTest.makeGLIMLoanRepayment(loanId, command));
    }

    @Test
    void makeManualFeeRefund_feeRefundAlreadyExists_throwsException() {
        final Loan loan = mock(Loan.class);
        final LoanTransaction refund = targetRefund(loan, LoanTransactionType.MERCHANT_ISSUED_REFUND);
        final LoanTransaction feeRefund = LoanTransaction.feeRefund(loan, new BigDecimal("12.5"), REFUND_DATE, null);
        feeRefund.getLoanTransactionRelations()
                .add(LoanTransactionRelation.linkToTransaction(feeRefund, refund, LoanTransactionRelationTypeEnum.RELATED));
        when(loan.getLoanTransactions()).thenReturn(List.of(refund, feeRefund));

        assertManualFeeRefundFails(loan, "error.msg.loan.transaction.fee.refund.already.exists");
    }

    @Test
    void makeManualFeeRefund_refundTypeNotSupportedAndNotForced_throwsException() {
        final Loan loan = mock(Loan.class);
        final LoanTransaction refund = targetRefund(loan, LoanTransactionType.PAYOUT_REFUND);
        when(loan.getLoanTransactions()).thenReturn(List.of(refund));
        when(loan.getSupportedFeeRefundTransactionTypes()).thenReturn(List.of(LoanTransactionType.MERCHANT_ISSUED_REFUND));

        assertManualFeeRefundFails(loan, "error.msg.loan.transaction.fee.refund.not.supported");
    }

    @Test
    void makeManualFeeRefund_amountDiffersFromTheCalculatedFeeRefund_throwsException() {
        final Loan loan = mock(Loan.class);
        final LoanTransaction refund = targetRefund(loan, LoanTransactionType.MERCHANT_ISSUED_REFUND);
        refund.setFeeRefundCalculation(false);
        when(loan.getLoanTransactions()).thenReturn(List.of(refund));
        when(loan.getSupportedFeeRefundTransactionTypes()).thenReturn(List.of(LoanTransactionType.MERCHANT_ISSUED_REFUND));
        when(loan.getLoanCharges()).thenReturn(Set.of());

        assertManualFeeRefundFails(loan, "error.msg.loan.transaction.fee.refund.amount.invalid");
    }

    private LoanTransaction targetRefund(final Loan loan, final LoanTransactionType type) {
        final LoanTransaction refund = LoanTransaction.refund(loan, type, new BigDecimal("500"), null, REFUND_DATE, null);
        when(loanAssembler.assembleFromInUserScope(LOAN_ID)).thenReturn(loan);
        when(loanTransactionRepository.findByIdAndLoanId(REFUND_ID, LOAN_ID)).thenReturn(Optional.of(refund));
        return refund;
    }

    private void assertManualFeeRefundFails(final Loan loan, final String errorCode) {
        final JsonCommand command = mock(JsonCommand.class);
        lenient().when(command.bigDecimalValueOfParameterNamed("transactionAmount")).thenReturn(new BigDecimal("12.5"));

        final GeneralPlatformDomainRuleException exception = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.makeManualFeeRefund(LOAN_ID, REFUND_ID, command));

        assertEquals(errorCode, exception.getGlobalisationMessageCode());
        verify(loan, never()).addLoanTransaction(any());
    }
}
