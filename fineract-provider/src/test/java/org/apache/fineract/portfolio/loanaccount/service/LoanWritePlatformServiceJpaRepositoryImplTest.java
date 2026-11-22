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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private LoanAssembler loanAssembler;

    @Mock
    private LoanTransactionValidator loanTransactionValidator;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private LoanAccountDomainService loanAccountDomainService;

    @Mock
    private PaymentDetailWritePlatformService paymentDetailWritePlatformService;

    @Mock
    private LoanTransactionWritePlatformService loanTransactionWritePlatformService;

    @Mock
    private LoanClosureWritePlatformService loanClosureWritePlatformService;

    @Mock
    private LoanTransferWritePlatformService loanTransferWritePlatformService;

    @Mock
    private LoanScheduleMaintenanceWritePlatformService loanScheduleMaintenanceWritePlatformService;

    @InjectMocks
    private LoanWritePlatformServiceJpaRepositoryImpl loanWritePlatformService;

    @Mock
    private JsonCommand command;
    private static final Long LOAN_ID = 1L;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, DateUtils.parseLocalDate("2025-05-20"))));
    }

    @Test
    public void makeGLIMLoanRepayment_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeGLIMLoanRepayment(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.makeGLIMLoanRepayment(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).makeGLIMLoanRepayment(LOAN_ID, command);
    }

    @Test
    public void makeLoanRepayment_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeLoanRepayment(LoanTransactionType.REPAYMENT, LOAN_ID, command, false))
                .thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.makeLoanRepayment(LoanTransactionType.REPAYMENT, LOAN_ID, command,
                false);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).makeLoanRepayment(LoanTransactionType.REPAYMENT, LOAN_ID, command, false);
    }

    @Test
    public void makeLoanRepaymentWithChargeRefundChargeType_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeLoanRepaymentWithChargeRefundChargeType(LoanTransactionType.REPAYMENT, LOAN_ID,
                command, false, "chargeType")).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService
                .makeLoanRepaymentWithChargeRefundChargeType(LoanTransactionType.REPAYMENT, LOAN_ID, command, false, "chargeType");

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).makeLoanRepaymentWithChargeRefundChargeType(LoanTransactionType.REPAYMENT, LOAN_ID,
                command, false, "chargeType");
    }

    @Test
    public void makeInterestPaymentWaiver_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeInterestPaymentWaiver(command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.makeInterestPaymentWaiver(command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).makeInterestPaymentWaiver(command);
    }

    @Test
    public void adjustLoanTransaction_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.adjustLoanTransaction(LOAN_ID, 2L, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.adjustLoanTransaction(LOAN_ID, 2L, command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).adjustLoanTransaction(LOAN_ID, 2L, command);
    }

    @Test
    public void chargebackLoanTransaction_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.chargebackLoanTransaction(LOAN_ID, 2L, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.chargebackLoanTransaction(LOAN_ID, 2L, command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).chargebackLoanTransaction(LOAN_ID, 2L, command);
    }

    @Test
    public void waiveInterestOnLoan_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.waiveInterestOnLoan(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.waiveInterestOnLoan(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).waiveInterestOnLoan(LOAN_ID, command);
    }

    @Test
    public void recoverFromGuarantor_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.recoverFromGuarantor(LOAN_ID)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.recoverFromGuarantor(LOAN_ID);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).recoverFromGuarantor(LOAN_ID);
    }

    @Test
    public void creditBalanceRefund_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.creditBalanceRefund(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.creditBalanceRefund(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).creditBalanceRefund(LOAN_ID, command);
    }

    @Test
    public void makeLoanRefund_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeLoanRefund(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.makeLoanRefund(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).makeLoanRefund(LOAN_ID, command);
    }

    @Test
    public void makeRefund_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeRefund(LOAN_ID, LoanTransactionType.MERCHANT_ISSUED_REFUND, command))
                .thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.makeRefund(LOAN_ID, LoanTransactionType.MERCHANT_ISSUED_REFUND,
                command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).makeRefund(LOAN_ID, LoanTransactionType.MERCHANT_ISSUED_REFUND, command);
    }

    @Test
    public void makeManualInterestRefund_shouldDelegateToLoanTransactionWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransactionWritePlatformService.makeManualInterestRefund(LOAN_ID, 2L, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.makeManualInterestRefund(LOAN_ID, 2L, command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransactionWritePlatformService).makeManualInterestRefund(LOAN_ID, 2L, command);
    }

    @Test
    public void writeOff_shouldDelegateToLoanClosureWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanClosureWritePlatformService.writeOff(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.writeOff(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanClosureWritePlatformService).writeOff(LOAN_ID, command);
    }

    @Test
    public void closeLoan_shouldDelegateToLoanClosureWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanClosureWritePlatformService.closeLoan(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.closeLoan(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanClosureWritePlatformService).closeLoan(LOAN_ID, command);
    }

    @Test
    public void closeAsRescheduled_shouldDelegateToLoanClosureWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanClosureWritePlatformService.closeAsRescheduled(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.closeAsRescheduled(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanClosureWritePlatformService).closeAsRescheduled(LOAN_ID, command);
    }

    @Test
    public void undoWriteOff_shouldDelegateToLoanClosureWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanClosureWritePlatformService.undoWriteOff(LOAN_ID)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.undoWriteOff(LOAN_ID);

        assertEquals(expectedResult, actualResult);
        verify(loanClosureWritePlatformService).undoWriteOff(LOAN_ID);
    }

    @Test
    public void forecloseLoan_shouldDelegateToLoanClosureWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanClosureWritePlatformService.forecloseLoan(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.forecloseLoan(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanClosureWritePlatformService).forecloseLoan(LOAN_ID, command);
    }

    @Test
    public void markLoanAsFraud_shouldDelegateToLoanClosureWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanClosureWritePlatformService.markLoanAsFraud(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.markLoanAsFraud(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanClosureWritePlatformService).markLoanAsFraud(LOAN_ID, command);
    }

    @Test
    public void chargeOff_shouldDelegateToLoanClosureWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanClosureWritePlatformService.chargeOff(command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.chargeOff(command);

        assertEquals(expectedResult, actualResult);
        verify(loanClosureWritePlatformService).chargeOff(command);
    }

    @Test
    public void undoChargeOff_shouldDelegateToLoanClosureWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanClosureWritePlatformService.undoChargeOff(command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.undoChargeOff(command);

        assertEquals(expectedResult, actualResult);
        verify(loanClosureWritePlatformService).undoChargeOff(command);
    }

    @Test
    public void loanReassignment_shouldDelegateToLoanTransferWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransferWritePlatformService.loanReassignment(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.loanReassignment(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransferWritePlatformService).loanReassignment(LOAN_ID, command);
    }

    @Test
    public void bulkLoanReassignment_shouldDelegateToLoanTransferWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransferWritePlatformService.bulkLoanReassignment(command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.bulkLoanReassignment(command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransferWritePlatformService).bulkLoanReassignment(command);
    }

    @Test
    public void removeLoanOfficer_shouldDelegateToLoanTransferWritePlatformService() {
        CommandProcessingResult expectedResult = mock(CommandProcessingResult.class);
        when(loanTransferWritePlatformService.removeLoanOfficer(LOAN_ID, command)).thenReturn(expectedResult);

        CommandProcessingResult actualResult = loanWritePlatformService.removeLoanOfficer(LOAN_ID, command);

        assertEquals(expectedResult, actualResult);
        verify(loanTransferWritePlatformService).removeLoanOfficer(LOAN_ID, command);
    }

    @Test
    public void initiateLoanTransfer_shouldDelegateToLoanTransferWritePlatformService() {
        Loan loan = mock(Loan.class);
        LoanTransaction expectedResult = mock(LoanTransaction.class);
        when(loanTransferWritePlatformService.initiateLoanTransfer(loan, LocalDate.of(2025, 5, 20))).thenReturn(expectedResult);

        LoanTransaction actualResult = loanWritePlatformService.initiateLoanTransfer(loan, LocalDate.of(2025, 5, 20));

        assertEquals(expectedResult, actualResult);
        verify(loanTransferWritePlatformService).initiateLoanTransfer(loan, LocalDate.of(2025, 5, 20));
    }

    @Test
    public void withdrawLoanTransfer_shouldDelegateToLoanTransferWritePlatformService() {
        Loan loan = mock(Loan.class);
        LoanTransaction expectedResult = mock(LoanTransaction.class);
        when(loanTransferWritePlatformService.withdrawLoanTransfer(loan, LocalDate.of(2025, 5, 20))).thenReturn(expectedResult);

        LoanTransaction actualResult = loanWritePlatformService.withdrawLoanTransfer(loan, LocalDate.of(2025, 5, 20));

        assertEquals(expectedResult, actualResult);
        verify(loanTransferWritePlatformService).withdrawLoanTransfer(loan, LocalDate.of(2025, 5, 20));
    }

    @Test
    public void rejectLoanTransfer_shouldDelegateToLoanTransferWritePlatformService() {
        Loan loan = mock(Loan.class);

        loanWritePlatformService.rejectLoanTransfer(loan);

        verify(loanTransferWritePlatformService).rejectLoanTransfer(loan);
    }

    @Test
    public void acceptLoanTransfer_shouldDelegateToLoanTransferWritePlatformService() {
        Loan loan = mock(Loan.class);
        Office acceptedInOffice = mock(Office.class);
        Staff loanOfficer = mock(Staff.class);
        LoanTransaction expectedResult = mock(LoanTransaction.class);
        when(loanTransferWritePlatformService.acceptLoanTransfer(loan, LocalDate.of(2025, 5, 20), acceptedInOffice, loanOfficer))
                .thenReturn(expectedResult);

        LoanTransaction actualResult = loanWritePlatformService.acceptLoanTransfer(loan, LocalDate.of(2025, 5, 20), acceptedInOffice,
                loanOfficer);

        assertEquals(expectedResult, actualResult);
        verify(loanTransferWritePlatformService).acceptLoanTransfer(loan, LocalDate.of(2025, 5, 20), acceptedInOffice, loanOfficer);
    }

    @Test
    public void applyMeetingDateChanges_shouldDelegateToLoanScheduleMaintenanceWritePlatformService() {
        Calendar calendar = mock(Calendar.class);

        loanWritePlatformService.applyMeetingDateChanges(calendar, List.of(), true, LocalDate.of(2025, 5, 20), LocalDate.of(2025, 5, 20));

        verify(loanScheduleMaintenanceWritePlatformService).applyMeetingDateChanges(calendar, List.of(), true, LocalDate.of(2025, 5, 20),
                LocalDate.of(2025, 5, 20));
    }

    @Test
    public void recalculateInterest_shouldDelegateToLoanScheduleMaintenanceWritePlatformService() {
        Loan loan = mock(Loan.class);
        Loan expectedResult = mock(Loan.class);
        when(loanScheduleMaintenanceWritePlatformService.recalculateInterest(loan)).thenReturn(expectedResult);

        Loan actualResult = loanWritePlatformService.recalculateInterest(loan);

        assertEquals(expectedResult, actualResult);
        verify(loanScheduleMaintenanceWritePlatformService).recalculateInterest(loan);
    }

    @Test
    public void applyMeetingDateChangesWithMeetingDates_shouldDelegateToLoanScheduleMaintenanceWritePlatformService() {
        Calendar calendar = mock(Calendar.class);

        loanWritePlatformService.applyMeetingDateChanges(calendar, List.of());

        verify(loanScheduleMaintenanceWritePlatformService).applyMeetingDateChanges(calendar, List.of());
    }

    @Test
    public void recalculateInterestByLoanId_shouldDelegateToLoanScheduleMaintenanceWritePlatformService() {

        loanWritePlatformService.recalculateInterest(LOAN_ID);

        verify(loanScheduleMaintenanceWritePlatformService).recalculateInterest(LOAN_ID);
    }

    @Test
    public void updateOriginalSchedule_shouldDelegateToLoanScheduleMaintenanceWritePlatformService() {
        Loan loan = mock(Loan.class);

        loanWritePlatformService.updateOriginalSchedule(loan);

        verify(loanScheduleMaintenanceWritePlatformService).updateOriginalSchedule(loan);
    }
}
