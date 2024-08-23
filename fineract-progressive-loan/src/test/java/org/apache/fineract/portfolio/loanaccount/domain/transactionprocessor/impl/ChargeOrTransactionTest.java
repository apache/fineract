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
package org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl;

import com.google.common.collect.Collections2;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ChargeOrTransactionTest {

    @Test
    public void testCompareToEqual() {
        ChargeOrTransaction charge = createCharge("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        ChargeOrTransaction transaction = createTransaction("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        Assertions.assertTrue(charge.compareTo(transaction) == 0);
        Assertions.assertTrue(transaction.compareTo(charge) == 0);
    }

    @Test
    public void testCompareToEqualBackdatedCharge() {
        ChargeOrTransaction charge = createCharge("2023-10-16", "2023-10-17", "2023-10-17T10:15:30+01:00");
        ChargeOrTransaction transaction = createTransaction("2023-10-16", "2023-10-17", "2023-10-17T10:15:30+01:00");
        Assertions.assertTrue(charge.compareTo(transaction) == 0);
        Assertions.assertTrue(transaction.compareTo(charge) == 0);
    }

    @Test
    public void testCompareToCreatedDateTime() {
        ChargeOrTransaction charge = createCharge("2023-10-17", "2023-10-17", "2023-10-17T10:15:31+01:00");
        ChargeOrTransaction transaction = createTransaction("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        Assertions.assertTrue(charge.compareTo(transaction) > 0);
        Assertions.assertTrue(transaction.compareTo(charge) < 0);
    }

    @Test
    public void testCompareToSubmittedOnDate() {
        ChargeOrTransaction charge = createCharge("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        ChargeOrTransaction transaction = createTransaction("2023-10-17", "2023-10-16", "2023-10-17T10:15:30+01:00");
        Assertions.assertTrue(charge.compareTo(transaction) > 0);
        Assertions.assertTrue(transaction.compareTo(charge) < 0);
    }

    @Test
    public void testComparatorEffectiveDate() {
        ChargeOrTransaction charge = createCharge("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        ChargeOrTransaction transaction = createTransaction("2023-10-16", "2023-10-17", "2023-10-17T10:15:30+01:00");
        Assertions.assertTrue(charge.compareTo(transaction) > 0);
        Assertions.assertTrue(transaction.compareTo(charge) < 0);
    }

    @Test
    public void testComparatorOnDifferentSubmittedDay() {
        ChargeOrTransaction cot1 = createCharge("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        ChargeOrTransaction cot2 = createTransaction("2023-10-17", "2023-10-19", "2023-10-19T10:16:30+01:00");
        ChargeOrTransaction cot3 = createCharge("2023-10-17", "2023-10-18", "2023-10-18T10:14:30+01:00");
        Collection<List<ChargeOrTransaction>> permutations = Collections2.permutations(List.of(cot1, cot2, cot3));
        List<ChargeOrTransaction> expected = List.of(cot1, cot3, cot2);
        for (List<ChargeOrTransaction> permutation : permutations) {
            Assertions.assertEquals(expected, permutation.stream().sorted().toList());
        }
    }

    @Test
    public void testComparatorOnSameDayBackdatedCharge() {
        ChargeOrTransaction cot1 = createCharge("2023-10-17", "2023-10-19", "2023-10-19T10:15:31+01:00");
        ChargeOrTransaction cot2 = createTransaction("2023-10-17", "2023-10-19", "2023-10-19T10:15:33+01:00");
        ChargeOrTransaction cot3 = createCharge("2023-10-17", "2023-10-19", "2023-10-19T10:15:32+01:00");
        Collection<List<ChargeOrTransaction>> permutations = Collections2.permutations(List.of(cot1, cot2, cot3));
        List<ChargeOrTransaction> expected = List.of(cot1, cot3, cot2);
        for (List<ChargeOrTransaction> permutation : permutations) {
            Assertions.assertEquals(expected, permutation.stream().sorted().toList());
        }
    }

    @Test
    public void testComparatorOnSameDay() {
        ChargeOrTransaction cot1 = createCharge("2023-10-24", "2023-10-19", "2023-10-19T10:15:31+01:00");
        ChargeOrTransaction cot2 = createTransaction("2023-10-19", "2023-10-19", "2023-10-19T10:15:33+01:00");
        ChargeOrTransaction cot3 = createCharge("2023-10-24", "2023-10-19", "2023-10-19T10:15:32+01:00");
        Collection<List<ChargeOrTransaction>> permutations = Collections2.permutations(List.of(cot1, cot2, cot3));
        List<ChargeOrTransaction> expected = List.of(cot1, cot3, cot2);
        for (List<ChargeOrTransaction> permutation : permutations) {
            Assertions.assertEquals(expected, permutation.stream().sorted().toList());
        }
    }

    private ChargeOrTransaction createCharge(String effectiveDate, String submittedDate, String creationDateTime) {
        LoanCharge charge = Mockito.mock(LoanCharge.class);
        Mockito.when(charge.getDueDate()).thenReturn(LocalDate.parse(effectiveDate));
        Mockito.when(charge.getSubmittedOnDate()).thenReturn(LocalDate.parse(submittedDate));
        Mockito.when(charge.getCreatedDate()).thenReturn(Optional.of(OffsetDateTime.parse(creationDateTime)));
        return new ChargeOrTransaction(charge);
    }

    private ChargeOrTransaction createTransaction(String transactionDate, String submittedDate, String creationDateTime) {
        LoanTransaction transaction = Mockito.mock(LoanTransaction.class);
        Mockito.when(transaction.getSubmittedOnDate()).thenReturn(LocalDate.parse(submittedDate));
        Mockito.when(transaction.getTransactionDate()).thenReturn(LocalDate.parse(transactionDate));
        Mockito.when(transaction.getCreatedDateTime()).thenReturn(OffsetDateTime.parse(creationDateTime));
        return new ChargeOrTransaction(transaction);
    }

}
