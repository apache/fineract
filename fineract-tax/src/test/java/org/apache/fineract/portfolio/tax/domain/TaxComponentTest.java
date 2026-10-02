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
package org.apache.fineract.portfolio.tax.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaxComponentTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 10);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, TODAY)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void testUpdateNameAlone() {
        TaxComponent component = TaxComponent.createTaxComponent("TC_OLD", BigDecimal.TEN, GLAccountType.ASSET, null,
                GLAccountType.LIABILITY, null, TODAY);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC_OLD")).thenReturn(true);
        when(command.stringValueOfParameterNamed(TaxApiConstants.nameParamName)).thenReturn("TC_NEW");
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(false);

        Map<String, Object> changes = component.update(command);

        assertEquals("TC_NEW", component.getName());
        assertEquals("TC_NEW", changes.get(TaxApiConstants.nameParamName));
        assertTrue(component.getTaxComponentHistories().isEmpty());
    }

    @Test
    void testUpdatePercentageCreatesHistoryAndUpdatesStartDate() {
        LocalDate initialStartDate = TODAY.minusMonths(1);
        TaxComponent component = TaxComponent.createTaxComponent("TC1", BigDecimal.TEN, GLAccountType.ASSET, null, GLAccountType.LIABILITY,
                null, initialStartDate);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC1")).thenReturn(false);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(true);
        when(command.bigDecimalValueOfParameterNamed(TaxApiConstants.percentageParamName)).thenReturn(BigDecimal.valueOf(15));
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(false);

        Map<String, Object> changes = component.update(command);

        assertEquals(BigDecimal.valueOf(15), component.getPercentage());
        assertEquals(TODAY, component.startDate());
        assertEquals(BigDecimal.valueOf(15), changes.get(TaxApiConstants.percentageParamName));
        assertEquals(TODAY, changes.get(TaxApiConstants.startDateParamName));

        assertEquals(1, component.getTaxComponentHistories().size());
        TaxComponentHistory history = component.getTaxComponentHistories().iterator().next();
        assertEquals(BigDecimal.TEN, history.getPercentage());
        assertEquals(initialStartDate, history.startDate());
        assertEquals(TODAY, history.endDate());
    }

    @Test
    void testUpdatePercentageWithProvidedStartDateCreatesHistoryWithNewStartDate() {
        LocalDate initialStartDate = TODAY.minusMonths(1);
        LocalDate futureStartDate = TODAY.plusDays(10);
        TaxComponent component = TaxComponent.createTaxComponent("TC1", BigDecimal.TEN, GLAccountType.ASSET, null, GLAccountType.LIABILITY,
                null, initialStartDate);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC1")).thenReturn(false);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(true);
        when(command.bigDecimalValueOfParameterNamed(TaxApiConstants.percentageParamName)).thenReturn(BigDecimal.valueOf(18));
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(futureStartDate);

        Map<String, Object> changes = component.update(command);

        assertEquals(BigDecimal.valueOf(18), component.getPercentage());
        assertEquals(futureStartDate, component.startDate());
        assertEquals(BigDecimal.valueOf(18), changes.get(TaxApiConstants.percentageParamName));
        assertEquals(futureStartDate, changes.get(TaxApiConstants.startDateParamName));

        assertEquals(1, component.getTaxComponentHistories().size());
        TaxComponentHistory history = component.getTaxComponentHistories().iterator().next();
        assertEquals(BigDecimal.TEN, history.getPercentage());
        assertEquals(initialStartDate, history.startDate());
        assertEquals(futureStartDate, history.endDate());
    }

    @Test
    void testUpdateStartDateAloneUpdatesDateWithoutCreatingHistory() {
        LocalDate initialStartDate = TODAY.plusDays(5);
        LocalDate newFutureStartDate = TODAY.plusDays(15);
        TaxComponent component = TaxComponent.createTaxComponent("TC1", BigDecimal.TEN, GLAccountType.ASSET, null, GLAccountType.LIABILITY,
                null, initialStartDate);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC1")).thenReturn(false);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(newFutureStartDate);

        Map<String, Object> changes = component.update(command);

        assertEquals(newFutureStartDate, component.startDate());
        assertEquals(newFutureStartDate, changes.get(TaxApiConstants.startDateParamName));
        assertTrue(component.getTaxComponentHistories().isEmpty());
    }

    @Test
    void testUpdateStartDateAloneSameDateIsNoOp() {
        LocalDate initialStartDate = TODAY.plusDays(5);
        TaxComponent component = TaxComponent.createTaxComponent("TC1", BigDecimal.TEN, GLAccountType.ASSET, null, GLAccountType.LIABILITY,
                null, initialStartDate);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC1")).thenReturn(false);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(initialStartDate);

        Map<String, Object> changes = component.update(command);

        assertEquals(initialStartDate, component.startDate());
        assertFalse(changes.containsKey(TaxApiConstants.startDateParamName));
        assertTrue(component.getTaxComponentHistories().isEmpty());
    }

    @Test
    void testUpdateGLAccountsWhenNotLocked() {
        GLAccount oldDebitAccount = mock(GLAccount.class);
        when(oldDebitAccount.getId()).thenReturn(1L);

        GLAccount oldCreditAccount = mock(GLAccount.class);
        when(oldCreditAccount.getId()).thenReturn(2L);

        TaxComponent component = TaxComponent.createTaxComponent("TC1", BigDecimal.TEN, GLAccountType.ASSET, oldDebitAccount,
                GLAccountType.LIABILITY, oldCreditAccount, TODAY);

        GLAccount newDebitAccount = mock(GLAccount.class);
        when(newDebitAccount.getId()).thenReturn(10L);

        GLAccount newCreditAccount = mock(GLAccount.class);
        when(newCreditAccount.getId()).thenReturn(20L);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC1")).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(false);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(false);

        when(command.isChangeInIntegerParameterNamed(TaxApiConstants.debitAccountTypeParamName, GLAccountType.ASSET.getValue()))
                .thenReturn(true);
        when(command.integerValueSansLocaleOfParameterNamed(TaxApiConstants.debitAccountTypeParamName))
                .thenReturn(GLAccountType.EXPENSE.getValue());

        when(command.isChangeInLongParameterNamed(TaxApiConstants.debitAccountIdParamName, 1L)).thenReturn(true);
        when(command.longValueOfParameterNamed(TaxApiConstants.debitAccountIdParamName)).thenReturn(10L);

        when(command.isChangeInIntegerParameterNamed(TaxApiConstants.creditAccountTypeParamName, GLAccountType.LIABILITY.getValue()))
                .thenReturn(true);
        when(command.integerValueSansLocaleOfParameterNamed(TaxApiConstants.creditAccountTypeParamName))
                .thenReturn(GLAccountType.INCOME.getValue());

        when(command.isChangeInLongParameterNamed(TaxApiConstants.creditAccountIdParamName, 2L)).thenReturn(true);
        when(command.longValueOfParameterNamed(TaxApiConstants.creditAccountIdParamName)).thenReturn(20L);

        Map<String, Object> changes = component.update(command, GLAccountType.EXPENSE, newDebitAccount, GLAccountType.INCOME,
                newCreditAccount);

        assertEquals(GLAccountType.EXPENSE.getValue(), component.getDebitAccountType());
        assertEquals(newDebitAccount, component.getDebitAccount());
        assertEquals(GLAccountType.INCOME.getValue(), component.getCreditAccountType());
        assertEquals(newCreditAccount, component.getCreditAccount());

        assertEquals(GLAccountType.EXPENSE.getValue(), changes.get(TaxApiConstants.debitAccountTypeParamName));
        assertEquals(10L, changes.get(TaxApiConstants.debitAccountIdParamName));
        assertEquals(GLAccountType.INCOME.getValue(), changes.get(TaxApiConstants.creditAccountTypeParamName));
        assertEquals(20L, changes.get(TaxApiConstants.creditAccountIdParamName));
    }

    @Test
    void testUpdateGLAccountsClearWhenNull() {
        GLAccount oldDebitAccount = mock(GLAccount.class);
        when(oldDebitAccount.getId()).thenReturn(1L);

        TaxComponent component = TaxComponent.createTaxComponent("TC1", BigDecimal.TEN, GLAccountType.ASSET, oldDebitAccount,
                GLAccountType.LIABILITY, null, TODAY);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC1")).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(false);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(false);

        when(command.isChangeInLongParameterNamed(TaxApiConstants.debitAccountIdParamName, 1L)).thenReturn(true);
        when(command.longValueOfParameterNamed(TaxApiConstants.debitAccountIdParamName)).thenReturn(null);
        when(command.parameterExists(TaxApiConstants.debitAccountIdParamName)).thenReturn(true);

        when(command.isChangeInIntegerParameterNamed(TaxApiConstants.debitAccountTypeParamName, GLAccountType.ASSET.getValue()))
                .thenReturn(false);
        when(command.isChangeInIntegerParameterNamed(TaxApiConstants.creditAccountTypeParamName, GLAccountType.LIABILITY.getValue()))
                .thenReturn(false);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.creditAccountIdParamName, null)).thenReturn(false);

        Map<String, Object> changes = component.update(command, null, null, null, null);

        assertNull(component.getDebitAccount());
        assertNull(component.getDebitAccountType());
        assertNull(changes.get(TaxApiConstants.debitAccountIdParamName));
        assertNull(changes.get(TaxApiConstants.debitAccountTypeParamName));
    }
}
