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
package org.apache.fineract.portfolio.loanaccount.serialization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanChargeValidatorTest {

    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2024, 3, 15);

    private final LoanChargeValidator underTest = new LoanChargeValidator(mock(LoanTransactionRepository.class));
    private final Loan loan = mock(Loan.class);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(new EnumMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE))));
        when(loan.getDisbursementDate()).thenReturn(DISBURSEMENT_DATE);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void waiverDateIsOptional() {
        assertDoesNotThrow(() -> underTest.validateChargeWaiverDate(loan, null));
    }

    @Test
    void waiverDateMayBeAnyDateFromDisbursementToBusinessDate() {
        assertDoesNotThrow(() -> underTest.validateChargeWaiverDate(loan, DISBURSEMENT_DATE));
        assertDoesNotThrow(() -> underTest.validateChargeWaiverDate(loan, LocalDate.of(2024, 2, 10)));
        assertDoesNotThrow(() -> underTest.validateChargeWaiverDate(loan, BUSINESS_DATE));
    }

    @Test
    void waiverDateCannotBeInTheFuture() {
        InvalidLoanStateTransitionException exception = assertThrows(InvalidLoanStateTransitionException.class,
                () -> underTest.validateChargeWaiverDate(loan, BUSINESS_DATE.plusDays(1)));
        assertEquals("error.msg.loan.charge.waiver.cannot.be.a.future.date", exception.getGlobalisationMessageCode());
    }

    @Test
    void waiverDateCannotBeBeforeDisbursement() {
        InvalidLoanStateTransitionException exception = assertThrows(InvalidLoanStateTransitionException.class,
                () -> underTest.validateChargeWaiverDate(loan, DISBURSEMENT_DATE.minusDays(1)));
        assertEquals("error.msg.loan.charge.waiver.cannot.be.before.disbursement.date", exception.getGlobalisationMessageCode());
    }
}
