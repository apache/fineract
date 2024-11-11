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
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ChangeOperationTest {

    // DateUtils.getOffsetDateTimeOfTenantFromLocalDate
    private static final MockedStatic<ThreadLocalContextUtil> threadLocalContextUtil = Mockito.mockStatic(ThreadLocalContextUtil.class);

    @BeforeAll
    public static void init() {
        threadLocalContextUtil.when(ThreadLocalContextUtil::getTenant)
                .thenReturn(new FineractPlatformTenant(null, null, null, "Europe/Budapest", null));
    }

    @AfterAll
    public static void tearDown() {
        threadLocalContextUtil.close();
    }

    @Test
    public void testCompareToEqual() {
        ChangeOperation interestChange = createInterestRateChange("2023-10-17");
        ChangeOperation charge = createCharge("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        ChangeOperation transaction = createTransaction("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        Assertions.assertTrue(interestChange.compareTo(transaction) < 0);
        Assertions.assertTrue(interestChange.compareTo(charge) < 0);
        Assertions.assertTrue(charge.compareTo(transaction) == 0);
        Assertions.assertTrue(transaction.compareTo(charge) == 0);
    }

    @Test
    public void testCompareToEqualBackdatedCharge() {
        ChangeOperation interestChange = createInterestRateChange("2023-10-17");
        ChangeOperation charge = createCharge("2023-10-16", "2023-10-17", "2023-10-17T10:15:30+01:00");
        ChangeOperation transaction = createTransaction("2023-10-16", "2023-10-17", "2023-10-17T10:15:30+01:00");
        Assertions.assertTrue(interestChange.compareTo(transaction) > 0);
        Assertions.assertTrue(interestChange.compareTo(charge) > 0);
        Assertions.assertTrue(charge.compareTo(transaction) == 0);
        Assertions.assertTrue(transaction.compareTo(charge) == 0);
    }

    @Test
    public void testCompareToCreatedDateTime() {
        ChangeOperation charge = createCharge("2023-10-17", "2023-10-17", "2023-10-17T10:15:31+01:00");
        ChangeOperation transaction = createTransaction("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        Assertions.assertTrue(charge.compareTo(transaction) > 0);
        Assertions.assertTrue(transaction.compareTo(charge) < 0);
    }

    @Test
    public void testCompareToSubmittedOnDate() {
        ChangeOperation charge = createCharge("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        ChangeOperation transaction = createTransaction("2023-10-17", "2023-10-16", "2023-10-17T10:15:30+01:00");
        Assertions.assertTrue(charge.compareTo(transaction) > 0);
        Assertions.assertTrue(transaction.compareTo(charge) < 0);
    }

    @Test
    public void testComparatorEffectiveDate() {
        ChangeOperation charge = createCharge("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        ChangeOperation transaction = createTransaction("2023-10-16", "2023-10-17", "2023-10-17T10:15:30+01:00");
        Assertions.assertTrue(charge.compareTo(transaction) > 0);
        Assertions.assertTrue(transaction.compareTo(charge) < 0);
    }

    @Test
    public void testComparatorOnDifferentSubmittedDay() {
        ChangeOperation cot1 = createCharge("2023-10-17", "2023-10-17", "2023-10-17T10:15:30+01:00");
        ChangeOperation cot2 = createTransaction("2023-10-17", "2023-10-19", "2023-10-19T10:16:30+01:00");
        ChangeOperation cot3 = createCharge("2023-10-17", "2023-10-18", "2023-10-18T10:14:30+01:00");
        Collection<List<ChangeOperation>> permutations = Collections2.permutations(List.of(cot1, cot2, cot3));
        List<ChangeOperation> expected = List.of(cot1, cot3, cot2);
        for (List<ChangeOperation> permutation : permutations) {
            Assertions.assertEquals(expected, permutation.stream().sorted().toList());
        }
    }

    @Test
    public void testComparatorOnSameDayBackdatedCharge() {
        ChangeOperation cot1 = createCharge("2023-10-17", "2023-10-19", "2023-10-19T10:15:31+01:00");
        ChangeOperation cot2 = createTransaction("2023-10-17", "2023-10-19", "2023-10-19T10:15:33+01:00");
        ChangeOperation cot3 = createCharge("2023-10-17", "2023-10-19", "2023-10-19T10:15:32+01:00");
        Collection<List<ChangeOperation>> permutations = Collections2.permutations(List.of(cot1, cot2, cot3));
        List<ChangeOperation> expected = List.of(cot1, cot3, cot2);
        for (List<ChangeOperation> permutation : permutations) {
            Assertions.assertEquals(expected, permutation.stream().sorted().toList());
        }
    }

    @Test
    public void testComparatorOnSameDay() {
        ChangeOperation cot1 = createCharge("2023-10-24", "2023-10-19", "2023-10-19T10:15:31+01:00");
        ChangeOperation cot2 = createTransaction("2023-10-19", "2023-10-19", "2023-10-19T10:15:33+01:00");
        ChangeOperation cot3 = createCharge("2023-10-24", "2023-10-19", "2023-10-19T10:15:32+01:00");
        Collection<List<ChangeOperation>> permutations = Collections2.permutations(List.of(cot1, cot2, cot3));
        List<ChangeOperation> expected = List.of(cot1, cot3, cot2);
        for (List<ChangeOperation> permutation : permutations) {
            Assertions.assertEquals(expected, permutation.stream().sorted().toList());
        }
    }

    private ChangeOperation createInterestRateChange(String submittedDate) {
        LoanTermVariationsData interestRateChange = Mockito.mock(LoanTermVariationsData.class);
        Mockito.when(interestRateChange.getTermVariationApplicableFrom()).thenReturn(LocalDate.parse(submittedDate));
        return new ChangeOperation(interestRateChange);
    }

    private ChangeOperation createCharge(String effectiveDate, String submittedDate, String creationDateTime) {
        LoanCharge charge = Mockito.mock(LoanCharge.class);
        Mockito.when(charge.getDueDate()).thenReturn(LocalDate.parse(effectiveDate));
        Mockito.when(charge.getSubmittedOnDate()).thenReturn(LocalDate.parse(submittedDate));
        Mockito.when(charge.getCreatedDate()).thenReturn(Optional.of(OffsetDateTime.parse(creationDateTime)));
        return new ChangeOperation(charge);
    }

    private ChangeOperation createTransaction(String transactionDate, String submittedDate, String creationDateTime) {
        LoanTransaction transaction = Mockito.mock(LoanTransaction.class);
        Mockito.when(transaction.getSubmittedOnDate()).thenReturn(LocalDate.parse(submittedDate));
        Mockito.when(transaction.getTransactionDate()).thenReturn(LocalDate.parse(transactionDate));
        Mockito.when(transaction.getCreatedDate()).thenReturn(Optional.of(OffsetDateTime.parse(creationDateTime)));
        return new ChangeOperation(transaction);
    }

}
