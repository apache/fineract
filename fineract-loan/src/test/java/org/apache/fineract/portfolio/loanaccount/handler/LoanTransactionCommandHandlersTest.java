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
package org.apache.fineract.portfolio.loanaccount.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.DataIntegrityErrorHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanTransactionCommandHandlersTest {

    private static final Long LOAN_ID = 1L;
    private static final Long TRANSACTION_ID = 2L;

    @Mock
    private LoanTransactionWritePlatformService loanTransactionWritePlatformService;

    @Mock
    private DataIntegrityErrorHandler dataIntegrityErrorHandler;

    @Mock
    private JsonCommand command;

    @Test
    void testCreditBalanceRefundCommandHandler() {
        CreditBalanceRefundCommandHandler handler = new CreditBalanceRefundCommandHandler(loanTransactionWritePlatformService,
                dataIntegrityErrorHandler);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.creditBalanceRefund(LOAN_ID, command)).thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).creditBalanceRefund(LOAN_ID, command);
    }

    @Test
    void testGLIMBulkRepaymentCommandHandler() {
        GLIMBulkRepaymentCommandHandler handler = new GLIMBulkRepaymentCommandHandler(loanTransactionWritePlatformService);
        when(command.entityId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeGLIMLoanRepayment(LOAN_ID, command)).thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).makeGLIMLoanRepayment(LOAN_ID, command);
    }

    @Test
    void testLoanDownPaymentCommandHandler() {
        LoanDownPaymentCommandHandler handler = new LoanDownPaymentCommandHandler(loanTransactionWritePlatformService,
                dataIntegrityErrorHandler);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeLoanRepayment(LoanTransactionType.DOWN_PAYMENT, LOAN_ID, command, false))
                .thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).makeLoanRepayment(LoanTransactionType.DOWN_PAYMENT, LOAN_ID, command, false);
    }

    @Test
    void testLoanGoodwillCreditCommandHandler() {
        LoanGoodwillCreditCommandHandler handler = new LoanGoodwillCreditCommandHandler(loanTransactionWritePlatformService,
                dataIntegrityErrorHandler);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeLoanRepayment(LoanTransactionType.GOODWILL_CREDIT, LOAN_ID, command, false))
                .thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).makeLoanRepayment(LoanTransactionType.GOODWILL_CREDIT, LOAN_ID, command, false);
    }

    @Test
    void testLoanMerchantIssuedRefundCommandHandler() {
        LoanMerchantIssuedRefundCommandHandler handler = new LoanMerchantIssuedRefundCommandHandler(loanTransactionWritePlatformService);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeRefund(LOAN_ID, LoanTransactionType.MERCHANT_ISSUED_REFUND, command))
                .thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).makeRefund(LOAN_ID, LoanTransactionType.MERCHANT_ISSUED_REFUND, command);
    }

    @Test
    void testLoanPayoutRefundCommandHandler() {
        LoanPayoutRefundCommandHandler handler = new LoanPayoutRefundCommandHandler(loanTransactionWritePlatformService);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeRefund(LOAN_ID, LoanTransactionType.PAYOUT_REFUND, command)).thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).makeRefund(LOAN_ID, LoanTransactionType.PAYOUT_REFUND, command);
    }

    @Test
    void testLoanRecoveryPaymentCommandHandler() {
        LoanRecoveryPaymentCommandHandler handler = new LoanRecoveryPaymentCommandHandler(loanTransactionWritePlatformService);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeLoanRepayment(LoanTransactionType.REPAYMENT, LOAN_ID, command, true))
                .thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).makeLoanRepayment(LoanTransactionType.REPAYMENT, LOAN_ID, command, true);
    }

    @Test
    void testLoanRefundByCashCommandHandler() {
        LoanRefundByCashCommandHandler handler = new LoanRefundByCashCommandHandler(loanTransactionWritePlatformService);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeLoanRefund(LOAN_ID, command)).thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).makeLoanRefund(LOAN_ID, command);
    }

    @Test
    void testLoanRepaymentAdjustmentCommandHandler() {
        LoanRepaymentAdjustmentCommandHandler handler = new LoanRepaymentAdjustmentCommandHandler(loanTransactionWritePlatformService);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        when(command.entityId()).thenReturn(TRANSACTION_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.adjustLoanTransaction(LOAN_ID, TRANSACTION_ID, command)).thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).adjustLoanTransaction(LOAN_ID, TRANSACTION_ID, command);
    }

    @Test
    void testLoanRepaymentChargebackCommandHandler() {
        LoanRepaymentChargebackCommandHandler handler = new LoanRepaymentChargebackCommandHandler(loanTransactionWritePlatformService);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        when(command.entityId()).thenReturn(TRANSACTION_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.chargebackLoanTransaction(LOAN_ID, TRANSACTION_ID, command)).thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).chargebackLoanTransaction(LOAN_ID, TRANSACTION_ID, command);
    }

    @Test
    void testLoanRepaymentCommandHandler() {
        LoanRepaymentCommandHandler handler = new LoanRepaymentCommandHandler(loanTransactionWritePlatformService,
                dataIntegrityErrorHandler);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeLoanRepayment(LoanTransactionType.REPAYMENT, LOAN_ID, command, false))
                .thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).makeLoanRepayment(LoanTransactionType.REPAYMENT, LOAN_ID, command, false);
    }

    @Test
    void testManualInterestRefundCommandHandler() {
        ManualInterestRefundCommandHandler handler = new ManualInterestRefundCommandHandler(loanTransactionWritePlatformService);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        when(command.entityId()).thenReturn(TRANSACTION_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeManualInterestRefund(LOAN_ID, TRANSACTION_ID, command)).thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).makeManualInterestRefund(LOAN_ID, TRANSACTION_ID, command);
    }

    @Test
    void testRecoverFromGuarantorCommandHandler() {
        RecoverFromGuarantorCommandHandler handler = new RecoverFromGuarantorCommandHandler(loanTransactionWritePlatformService);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.recoverFromGuarantor(LOAN_ID)).thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).recoverFromGuarantor(LOAN_ID);
    }

    @Test
    void testWaiveInterestPortionOnLoanCommandHandler() {
        WaiveInterestPortionOnLoanCommandHandler handler = new WaiveInterestPortionOnLoanCommandHandler(
                loanTransactionWritePlatformService);
        when(command.getLoanId()).thenReturn(LOAN_ID);
        CommandProcessingResult expected = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.waiveInterestOnLoan(LOAN_ID, command)).thenReturn(expected);

        CommandProcessingResult actual = handler.processCommand(command);

        assertEquals(expected, actual);
        verify(loanTransactionWritePlatformService).waiveInterestOnLoan(LOAN_ID, command);
    }
}
