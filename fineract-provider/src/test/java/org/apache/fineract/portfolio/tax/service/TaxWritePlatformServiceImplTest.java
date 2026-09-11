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
package org.apache.fineract.portfolio.tax.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepository;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.apache.fineract.portfolio.tax.serialization.TaxValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxWritePlatformServiceImplTest {

    @Mock
    private TaxValidator validator;
    @Mock
    private TaxAssembler taxAssembler;
    @Mock
    private TaxComponentRepository taxComponentRepository;
    @Mock
    private TaxComponentRepositoryWrapper taxComponentRepositoryWrapper;
    @Mock
    private TaxGroupRepository taxGroupRepository;
    @Mock
    private TaxGroupRepositoryWrapper taxGroupRepositoryWrapper;
    @Mock
    private ChargeRepository chargeRepository;
    @Mock
    private GLAccountRepositoryWrapper glAccountRepositoryWrapper;

    @InjectMocks
    private TaxWritePlatformServiceImpl underTest;

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 10);
    private TaxComponent taxComponent;
    private static final Long COMPONENT_ID = 1L;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, TODAY)));
        taxComponent = TaxComponent.createTaxComponent("TC1", BigDecimal.TEN, GLAccountType.ASSET, null, GLAccountType.LIABILITY, null,
                TODAY);
        ReflectionTestUtils.setField(taxComponent, "id", COMPONENT_ID);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void testUpdateTaxComponentWhenNotInUseAllowsGLAccountsAndPercentage() {
        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);

        GLAccount newDebitAccount = mock(GLAccount.class);
        GLAccount newCreditAccount = mock(GLAccount.class);
        when(glAccountRepositoryWrapper.findOneWithNotFoundDetection(10L)).thenReturn(newDebitAccount);
        when(glAccountRepositoryWrapper.findOneWithNotFoundDetection(20L)).thenReturn(newCreditAccount);

        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.debitAccountTypeParamName)).thenReturn(true);
        when(command.integerValueSansLocaleOfParameterNamed(TaxApiConstants.debitAccountTypeParamName))
                .thenReturn(GLAccountType.EXPENSE.getValue());
        when(command.parameterExists(TaxApiConstants.debitAccountIdParamName)).thenReturn(true);
        when(command.longValueOfParameterNamed(TaxApiConstants.debitAccountIdParamName)).thenReturn(10L);

        when(command.parameterExists(TaxApiConstants.creditAccountTypeParamName)).thenReturn(true);
        when(command.integerValueSansLocaleOfParameterNamed(TaxApiConstants.creditAccountTypeParamName))
                .thenReturn(GLAccountType.INCOME.getValue());
        when(command.parameterExists(TaxApiConstants.creditAccountIdParamName)).thenReturn(true);
        when(command.longValueOfParameterNamed(TaxApiConstants.creditAccountIdParamName)).thenReturn(20L);

        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC1")).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(false);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(false);

        when(command.isChangeInIntegerParameterNamed(TaxApiConstants.debitAccountTypeParamName, GLAccountType.ASSET.getValue()))
                .thenReturn(true);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.debitAccountIdParamName, null)).thenReturn(true);
        when(command.isChangeInIntegerParameterNamed(TaxApiConstants.creditAccountTypeParamName, GLAccountType.LIABILITY.getValue()))
                .thenReturn(true);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.creditAccountIdParamName, null)).thenReturn(true);

        CommandProcessingResult result = underTest.updateTaxComponent(COMPONENT_ID, command);

        assertEquals(COMPONENT_ID, result.getResourceId());
        verify(taxComponentRepository).saveAndFlush(taxComponent);
        assertEquals(GLAccountType.EXPENSE.getValue(), taxComponent.getDebitAccountType());
        assertEquals(newDebitAccount, taxComponent.getDebitAccount());
        assertEquals(GLAccountType.INCOME.getValue(), taxComponent.getCreditAccountType());
        assertEquals(newCreditAccount, taxComponent.getCreditAccount());
    }

    @Test
    void testUpdateTaxComponentInUseThrowsWhenPercentageChanged() {
        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);
        when(chargeRepository.existsByTaxGroupContainingTaxComponent(COMPONENT_ID)).thenReturn(true);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(true);

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.updateTaxComponent(COMPONENT_ID, command));

        assertTrue(exception.getErrors().get(0).getUserMessageGlobalisationCode()
                .contains("only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions"));
    }

    @Test
    void testUpdateTaxComponentInUseThrowsWhenDebitAccountTypeChanged() {
        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);
        when(chargeRepository.existsByTaxGroupContainingTaxComponent(COMPONENT_ID)).thenReturn(true);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInIntegerSansLocaleParameterNamed(TaxApiConstants.debitAccountTypeParamName,
                GLAccountType.ASSET.getValue())).thenReturn(true);

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.updateTaxComponent(COMPONENT_ID, command));

        assertTrue(exception.getErrors().get(0).getUserMessageGlobalisationCode()
                .contains("only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions"));
    }

    @Test
    void testUpdateTaxComponentInUseThrowsWhenDebitAccountIdChanged() {
        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);
        when(chargeRepository.existsByTaxGroupContainingTaxComponent(COMPONENT_ID)).thenReturn(true);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.debitAccountIdParamName, null)).thenReturn(true);

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.updateTaxComponent(COMPONENT_ID, command));

        assertTrue(exception.getErrors().get(0).getUserMessageGlobalisationCode()
                .contains("only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions"));
    }

    @Test
    void testUpdateTaxComponentInUseThrowsWhenCreditAccountTypeChanged() {
        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);
        when(chargeRepository.existsByTaxGroupContainingTaxComponent(COMPONENT_ID)).thenReturn(true);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInIntegerSansLocaleParameterNamed(TaxApiConstants.creditAccountTypeParamName,
                GLAccountType.LIABILITY.getValue())).thenReturn(true);

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.updateTaxComponent(COMPONENT_ID, command));

        assertTrue(exception.getErrors().get(0).getUserMessageGlobalisationCode()
                .contains("only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions"));
    }

    @Test
    void testUpdateTaxComponentInUseThrowsWhenCreditAccountIdChanged() {
        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);
        when(chargeRepository.existsByTaxGroupContainingTaxComponent(COMPONENT_ID)).thenReturn(true);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.creditAccountIdParamName, null)).thenReturn(true);

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.updateTaxComponent(COMPONENT_ID, command));

        assertTrue(exception.getErrors().get(0).getUserMessageGlobalisationCode()
                .contains("only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions"));
    }

    @Test
    void testUpdateTaxComponentInUseAllowsNameChange() {
        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);
        when(chargeRepository.existsByTaxGroupContainingTaxComponent(COMPONENT_ID)).thenReturn(true);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(false);
        when(command.isChangeInIntegerSansLocaleParameterNamed(TaxApiConstants.debitAccountTypeParamName,
                GLAccountType.ASSET.getValue())).thenReturn(false);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.debitAccountIdParamName, null)).thenReturn(false);
        when(command.isChangeInIntegerSansLocaleParameterNamed(TaxApiConstants.creditAccountTypeParamName,
                GLAccountType.LIABILITY.getValue())).thenReturn(false);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.creditAccountIdParamName, null)).thenReturn(false);

        when(command.parameterExists(TaxApiConstants.debitAccountTypeParamName)).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.debitAccountIdParamName)).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.creditAccountTypeParamName)).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.creditAccountIdParamName)).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(false);

        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC1")).thenReturn(true);
        when(command.stringValueOfParameterNamed(TaxApiConstants.nameParamName)).thenReturn("TC1-Renamed");

        CommandProcessingResult result = underTest.updateTaxComponent(COMPONENT_ID, command);

        assertEquals(COMPONENT_ID, result.getResourceId());
        verify(taxComponentRepository).saveAndFlush(taxComponent);
        assertEquals("TC1-Renamed", taxComponent.getName());
    }

    @Test
    void testUpdateTaxComponentInUseThrowsWhenNameAndRestrictedFieldChangedTogether() {
        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);
        when(chargeRepository.existsByTaxGroupContainingTaxComponent(COMPONENT_ID)).thenReturn(true);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(true);

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.updateTaxComponent(COMPONENT_ID, command));

        assertTrue(exception.getErrors().get(0).getUserMessageGlobalisationCode()
                .contains("only.name.can.be.modified.once.tax.component.is.linked.or.used.in.transactions"));
        assertEquals("TC1", taxComponent.getName());
    }

    @Test
    void testUpdateTaxComponentInUseAllowsFutureStartDateChange() {
        LocalDate futureDate = TODAY.plusDays(10);
        LocalDate newFutureDate = TODAY.plusDays(20);

        TaxComponent futureComponent = TaxComponent.createTaxComponent("TC_Future", BigDecimal.TEN, GLAccountType.ASSET, null,
                GLAccountType.LIABILITY, null, futureDate);
        ReflectionTestUtils.setField(futureComponent, "id", COMPONENT_ID);

        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(futureComponent);
        when(chargeRepository.existsByTaxGroupContainingTaxComponent(COMPONENT_ID)).thenReturn(true);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(false);
        when(command.isChangeInIntegerSansLocaleParameterNamed(TaxApiConstants.debitAccountTypeParamName, GLAccountType.ASSET.getValue()))
                .thenReturn(false);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.debitAccountIdParamName, null)).thenReturn(false);
        when(command.isChangeInIntegerSansLocaleParameterNamed(TaxApiConstants.creditAccountTypeParamName,
                GLAccountType.LIABILITY.getValue())).thenReturn(false);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.creditAccountIdParamName, null)).thenReturn(false);

        when(command.parameterExists(TaxApiConstants.debitAccountTypeParamName)).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.debitAccountIdParamName)).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.creditAccountTypeParamName)).thenReturn(false);
        when(command.parameterExists(TaxApiConstants.creditAccountIdParamName)).thenReturn(false);

        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(newFutureDate);

        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC_Future")).thenReturn(false);

        assertDoesNotThrow(() -> underTest.updateTaxComponent(COMPONENT_ID, command));
        verify(taxComponentRepository).saveAndFlush(futureComponent);
        assertEquals(newFutureDate, futureComponent.startDate());
    }

    @Test
    void testUpdateTaxComponentInUseDoesNotLookupGLAccountsWhenLocked() {
        GLAccount debitAccount = mock(GLAccount.class);
        when(debitAccount.getId()).thenReturn(10L);
        ReflectionTestUtils.setField(taxComponent, "debitAccount", debitAccount);
        ReflectionTestUtils.setField(taxComponent, "debitAccountType", GLAccountType.ASSET.getValue());

        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);
        when(chargeRepository.existsByTaxGroupContainingTaxComponent(COMPONENT_ID)).thenReturn(true);

        JsonCommand command = mock(JsonCommand.class);
        when(command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, BigDecimal.TEN)).thenReturn(false);
        when(command.isChangeInIntegerSansLocaleParameterNamed(TaxApiConstants.debitAccountTypeParamName, GLAccountType.ASSET.getValue()))
                .thenReturn(false);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.debitAccountIdParamName, 10L)).thenReturn(false);
        when(command.isChangeInIntegerSansLocaleParameterNamed(TaxApiConstants.creditAccountTypeParamName,
                GLAccountType.LIABILITY.getValue())).thenReturn(false);
        when(command.isChangeInLongParameterNamed(TaxApiConstants.creditAccountIdParamName, null)).thenReturn(false);

        when(command.parameterExists(TaxApiConstants.debitAccountIdParamName)).thenReturn(true);
        when(command.longValueOfParameterNamed(TaxApiConstants.debitAccountIdParamName)).thenReturn(10L);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(false);

        when(command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, "TC1")).thenReturn(true);
        when(command.stringValueOfParameterNamed(TaxApiConstants.nameParamName)).thenReturn("TC1-Updated");

        assertDoesNotThrow(() -> underTest.updateTaxComponent(COMPONENT_ID, command));
        verify(glAccountRepositoryWrapper, never()).findOneWithNotFoundDetection(any());
    }
}
