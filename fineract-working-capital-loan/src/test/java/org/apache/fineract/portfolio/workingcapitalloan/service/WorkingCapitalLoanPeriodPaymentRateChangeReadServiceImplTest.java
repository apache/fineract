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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanPeriodPaymentRateChangeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanPeriodPaymentRateChangeReadServiceImplTest {

    private static final LocalDate AS_OF = LocalDate.of(2026, 1, 15);
    private static final BigDecimal ORIGINAL_RATE = new BigDecimal("18");

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProductRelatedDetails details;

    @Test
    void retrieveEffectivePaymentRate_nullStrategy_treatedAsTpv() {
        when(loan.getLoanProductRelatedDetails()).thenReturn(details);
        when(details.getPaymentAmountCalculationStrategy()).thenReturn(null);
        when(details.getPeriodPaymentRate()).thenReturn(ORIGINAL_RATE);

        final WorkingCapitalLoanPeriodPaymentRateChangeReadServiceImpl service = new WorkingCapitalLoanPeriodPaymentRateChangeReadServiceImpl(
                mock(WorkingCapitalLoanPeriodPaymentRateChangeRepository.class), mock(WorkingCapitalLoanRepository.class));

        assertThat(service.retrieveEffectivePaymentRate(loan, AS_OF, List.of())).isEqualByComparingTo(ORIGINAL_RATE);
    }
}
