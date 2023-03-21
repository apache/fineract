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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class LoanChargeEffectiveDueDateComparatorTest {

    @Test
    public void testLoanChargeEffectiveDueDateComparator() {
        LoanCharge lc1 = new LoanCharge();
        ReflectionTestUtils.setField(lc1, "chargeTime", ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        ReflectionTestUtils.setField(lc1, "dueDate", LocalDate.of(2023, 3, 17));

        LoanCharge lc2 = new LoanCharge();
        ReflectionTestUtils.setField(lc2, "chargeTime", ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        ReflectionTestUtils.setField(lc2, "dueDate", LocalDate.of(2023, 3, 18));

        LoanCharge lc3 = new LoanCharge();
        ReflectionTestUtils.setField(lc3, "chargeTime", ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        ReflectionTestUtils.setField(lc3, "dueDate", LocalDate.of(2023, 3, 16));

        LoanCharge lc4 = new LoanCharge();
        LoanInstallmentCharge installmentCharge = new LoanInstallmentCharge();
        LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment();
        ReflectionTestUtils.setField(installment, "dueDate", LocalDate.of(2023, 3, 15));
        ReflectionTestUtils.setField(installmentCharge, "installment", installment);
        ReflectionTestUtils.setField(lc4, "chargeTime", ChargeTimeType.INSTALMENT_FEE.getValue());
        ReflectionTestUtils.setField(lc4, "loanInstallmentCharge", Set.of(installmentCharge));

        List<LoanCharge> list = new ArrayList<>();
        list.add(lc1);
        list.add(lc2);
        list.add(lc3);
        list.add(lc4);
        list.sort(LoanChargeEffectiveDueDateComparator.INSTANCE);

        assertEquals(LocalDate.of(2023, 3, 15), list.get(0).getEffectiveDueDate());
        assertEquals(LocalDate.of(2023, 3, 16), list.get(1).getEffectiveDueDate());
        assertEquals(LocalDate.of(2023, 3, 17), list.get(2).getEffectiveDueDate());
        assertEquals(LocalDate.of(2023, 3, 18), list.get(3).getEffectiveDueDate());
    }

    @Test
    public void testBothNull() {
        LoanCharge lc1 = new LoanCharge();
        ReflectionTestUtils.setField(lc1, "chargeTime", ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        ReflectionTestUtils.setField(lc1, "dueDate", null);

        LoanCharge lc2 = new LoanCharge();
        ReflectionTestUtils.setField(lc2, "chargeTime", ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        ReflectionTestUtils.setField(lc2, "dueDate", null);

        List<LoanCharge> list = new ArrayList<>();
        list.add(lc1);
        list.add(lc2);
        list.sort(LoanChargeEffectiveDueDateComparator.INSTANCE);

        assertEquals(lc1, list.get(0));
        assertEquals(lc2, list.get(1));

        list = new ArrayList<>();
        list.add(lc2);
        list.add(lc1);
        list.sort(LoanChargeEffectiveDueDateComparator.INSTANCE);

        assertEquals(lc2, list.get(0));
        assertEquals(lc1, list.get(1));
    }

    @Test
    public void testOneOfThemIsNull() {
        LoanCharge lc1 = new LoanCharge();
        ReflectionTestUtils.setField(lc1, "chargeTime", ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        ReflectionTestUtils.setField(lc1, "dueDate", null);

        LoanCharge lc2 = new LoanCharge();
        ReflectionTestUtils.setField(lc2, "chargeTime", ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        ReflectionTestUtils.setField(lc2, "dueDate", LocalDate.of(2023, 3, 17));

        LoanCharge lc3 = new LoanCharge();
        LoanInstallmentCharge installmentCharge1 = new LoanInstallmentCharge();
        LoanRepaymentScheduleInstallment installment1 = new LoanRepaymentScheduleInstallment();
        ReflectionTestUtils.setField(installment1, "dueDate", LocalDate.of(2023, 3, 15));
        ReflectionTestUtils.setField(installmentCharge1, "installment", installment1);
        ReflectionTestUtils.setField(lc3, "chargeTime", ChargeTimeType.INSTALMENT_FEE.getValue());
        ReflectionTestUtils.setField(lc3, "loanInstallmentCharge", Set.of(installmentCharge1));

        LoanCharge lc4 = new LoanCharge();
        LoanInstallmentCharge installmentCharge2 = new LoanInstallmentCharge();
        LoanRepaymentScheduleInstallment installment2 = new LoanRepaymentScheduleInstallment();
        ReflectionTestUtils.setField(installment2, "dueDate", null);
        ReflectionTestUtils.setField(installmentCharge2, "paid", Boolean.TRUE);
        ReflectionTestUtils.setField(installmentCharge2, "installment", installment2);
        ReflectionTestUtils.setField(lc4, "chargeTime", ChargeTimeType.INSTALMENT_FEE.getValue());
        ReflectionTestUtils.setField(lc4, "loanInstallmentCharge", Set.of(installmentCharge2));

        List<LoanCharge> list = new ArrayList<>();
        list.add(lc1);
        list.add(lc2);
        list.add(lc3);
        list.add(lc4);
        list.sort(LoanChargeEffectiveDueDateComparator.INSTANCE);

        assertEquals(lc3, list.get(0));
        assertEquals(lc2, list.get(1));
        assertEquals(lc1, list.get(2));
        assertEquals(lc4, list.get(3));

        // Reversed order
        list = new ArrayList<>();
        list.add(lc4);
        list.add(lc3);
        list.add(lc2);
        list.add(lc1);
        list.sort(LoanChargeEffectiveDueDateComparator.INSTANCE);

        assertEquals(lc3, list.get(0));
        assertEquals(lc2, list.get(1));
        assertEquals(lc4, list.get(2));
        assertEquals(lc1, list.get(3));

    }

}
