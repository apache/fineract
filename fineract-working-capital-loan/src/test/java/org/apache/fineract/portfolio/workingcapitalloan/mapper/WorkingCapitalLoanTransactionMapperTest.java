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
package org.apache.fineract.portfolio.workingcapitalloan.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanTransactionMapperTest {

    private final WorkingCapitalLoanTransactionMapper mapper = Mappers.getMapper(WorkingCapitalLoanTransactionMapper.class);

    @Mock
    private WorkingCapitalLoanTransaction transaction;

    @Mock
    private WorkingCapitalLoanTransactionAllocation allocation;

    @Test
    void toData_mapsAllFieldsIncludingAllocationPortions() {
        final LocalDate txnDate = LocalDate.of(2024, 2, 1);
        final BigDecimal amount = BigDecimal.valueOf(10000);
        when(transaction.getId()).thenReturn(1L);
        when(transaction.getTransactionType()).thenReturn(LoanTransactionType.DISBURSEMENT);
        when(transaction.getTransactionDate()).thenReturn(txnDate);
        when(transaction.getSubmittedOnDate()).thenReturn(txnDate);
        when(transaction.getTransactionAmount()).thenReturn(amount);
        when(transaction.getExternalId()).thenReturn(new ExternalId("ext-1"));
        when(transaction.isReversed()).thenReturn(false);
        when(transaction.getReversalExternalId()).thenReturn(null);
        when(transaction.getReversedOnDate()).thenReturn(null);
        when(transaction.getAllocation()).thenReturn(allocation);
        when(allocation.getPrincipalPortion()).thenReturn(amount);
        when(allocation.getFeeChargesPortion()).thenReturn(null);
        when(allocation.getPenaltyChargesPortion()).thenReturn(null);

        final WorkingCapitalLoanTransactionData data = mapper.toData(transaction);

        assertNotNull(data);
        assertEquals(1L, data.getId());
        assertNotNull(data.getType());
        assertEquals(LoanTransactionType.DISBURSEMENT.getValue().longValue(), data.getType().getId());
        assertEquals(LoanTransactionType.DISBURSEMENT.getCode(), data.getType().getCode());
        assertEquals(txnDate, data.getTransactionDate());
        assertEquals(txnDate, data.getSubmittedOnDate());
        assertEquals(amount, data.getTransactionAmount());
        assertEquals(amount, data.getPrincipalPortion());
        assertNull(data.getFeeChargesPortion());
        assertNull(data.getPenaltyChargesPortion());
        assertEquals(false, data.getReversed());
    }

    @Test
    void toData_whenAllocationNull_setsPortionsToNull() {
        when(transaction.getId()).thenReturn(2L);
        when(transaction.getTransactionType()).thenReturn(LoanTransactionType.DISBURSEMENT);
        when(transaction.getTransactionDate()).thenReturn(LocalDate.of(2024, 2, 1));
        when(transaction.getSubmittedOnDate()).thenReturn(LocalDate.of(2024, 2, 1));
        when(transaction.getTransactionAmount()).thenReturn(BigDecimal.valueOf(5000));
        when(transaction.getExternalId()).thenReturn(null);
        when(transaction.isReversed()).thenReturn(false);
        when(transaction.getReversalExternalId()).thenReturn(null);
        when(transaction.getReversedOnDate()).thenReturn(null);
        when(transaction.getAllocation()).thenReturn(null);

        final WorkingCapitalLoanTransactionData data = mapper.toData(transaction);

        assertNotNull(data);
        assertNotNull(data.getType());
        assertEquals(LoanTransactionType.DISBURSEMENT.getCode(), data.getType().getCode());
        assertNull(data.getPrincipalPortion());
        assertNull(data.getFeeChargesPortion());
        assertNull(data.getPenaltyChargesPortion());
    }
}
