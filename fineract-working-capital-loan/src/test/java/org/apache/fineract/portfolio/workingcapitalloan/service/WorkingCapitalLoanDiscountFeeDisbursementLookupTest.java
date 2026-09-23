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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanDiscountFeeDisbursementLookupTest {

    private static final Long LOAN_ID = 1L;
    private static final Long DISBURSEMENT_ID = 10L;
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 1, 1);

    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;

    @InjectMocks
    private WorkingCapitalLoanWritePlatformServiceImpl service;

    @Test
    void resolveRelatedDisbursementTransaction_idOnly_returnsTheDisbursementFoundById() {
        final WorkingCapitalLoanTransaction disbursement = activeDisbursementFoundById();

        assertThat(service.resolveRelatedDisbursementTransaction(LOAN_ID, DISBURSEMENT_ID, null)).isSameAs(disbursement);
    }

    @Test
    void resolveRelatedDisbursementTransaction_idWithTheDisbursementDate_returnsTheDisbursementFoundById() {
        final WorkingCapitalLoanTransaction disbursement = activeDisbursementFoundById();
        when(disbursement.getTransactionDate()).thenReturn(DISBURSEMENT_DATE);

        assertThat(service.resolveRelatedDisbursementTransaction(LOAN_ID, DISBURSEMENT_ID, DISBURSEMENT_DATE)).isSameAs(disbursement);
    }

    @Test
    void resolveRelatedDisbursementTransaction_idWithAnotherDate_rejectsTheDateMismatch() {
        final WorkingCapitalLoanTransaction disbursement = activeDisbursementFoundById();
        when(disbursement.getTransactionDate()).thenReturn(DISBURSEMENT_DATE);

        assertRejected(() -> service.resolveRelatedDisbursementTransaction(LOAN_ID, DISBURSEMENT_ID, DISBURSEMENT_DATE.plusDays(7)),
                "validation.msg.wc.loan.transaction.date.must.be.equal.disbursement.date", "transactionDate");
    }

    @Test
    void resolveRelatedDisbursementTransaction_dateOnlyWithoutAnActiveDisbursementOnThatDate_rejectsAsNotFound() {
        final LocalDate dateBeforeDisbursement = DISBURSEMENT_DATE.minusDays(1);
        when(transactionRepository.findActiveByTypeAndTransactionDate(LOAN_ID, LoanTransactionType.DISBURSEMENT, dateBeforeDisbursement))
                .thenReturn(Optional.empty());

        assertRejected(() -> service.resolveRelatedDisbursementTransaction(LOAN_ID, null, dateBeforeDisbursement),
                "validation.msg.wc.loan.disbursement.transaction.not.found", "transactionDate");
    }

    @Test
    void resolveRelatedDisbursementTransaction_dateOnly_returnsTheActiveDisbursementOnThatDate() {
        final WorkingCapitalLoanTransaction disbursement = mock(WorkingCapitalLoanTransaction.class);
        when(disbursement.getTransactionDate()).thenReturn(DISBURSEMENT_DATE);
        when(transactionRepository.findActiveByTypeAndTransactionDate(LOAN_ID, LoanTransactionType.DISBURSEMENT, DISBURSEMENT_DATE))
                .thenReturn(Optional.of(disbursement));

        assertThat(service.resolveRelatedDisbursementTransaction(LOAN_ID, null, DISBURSEMENT_DATE)).isSameAs(disbursement);
    }

    @Test
    void resolveRelatedDisbursementTransaction_neitherIdNorDate_rejectsAsRequired() {
        assertRejected(() -> service.resolveRelatedDisbursementTransaction(LOAN_ID, null, null),
                "validation.msg.wc.loan.related.resource.id.required", "relatedResourceId");
        verifyNoInteractions(transactionRepository);
    }

    private WorkingCapitalLoanTransaction activeDisbursementFoundById() {
        final WorkingCapitalLoanTransaction disbursement = mock(WorkingCapitalLoanTransaction.class);
        when(disbursement.getTypeOf()).thenReturn(LoanTransactionType.DISBURSEMENT);
        when(disbursement.isReversed()).thenReturn(false);
        when(transactionRepository.findByIdAndWcLoan_Id(DISBURSEMENT_ID, LOAN_ID)).thenReturn(Optional.of(disbursement));
        return disbursement;
    }

    private static void assertRejected(final Executable call, final String expectedCode, final String expectedParameter) {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class, call);
        assertThat(exception.getErrors()).hasSize(1);
        final ApiParameterError error = exception.getErrors().getFirst();
        assertThat(error.getUserMessageGlobalisationCode()).isEqualTo(expectedCode);
        assertThat(error.getParameterName()).isEqualTo(expectedParameter);
    }
}
