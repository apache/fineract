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
package org.apache.fineract.portfolio.client.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientCharge;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ClientChargeSavingsPaymentServiceTest {

    @Test
    void rejectsInactiveSavingsOwner() {
        PlatformSecurityContext context = mock(PlatformSecurityContext.class);
        when(context.authenticatedUser()).thenReturn(mock(AppUser.class));
        SavingsAccountAssembler assembler = mock(SavingsAccountAssembler.class);
        SavingsAccount account = savings("USD", 1L);
        Client owner = mock(Client.class);
        when(owner.isNotActive()).thenReturn(true);
        when(account.getClient()).thenReturn(owner);
        when(assembler.assembleFromInUserScope(10L, false)).thenReturn(account);
        assertThrows(ClientNotActiveException.class,
                () -> new ClientChargeSavingsPaymentService(context, assembler, mock(SavingsAccountDomainService.class)).withdraw(10L,
                        charge("USD", 1L), mock(Money.class), LocalDate.of(2026, 11, 5), DateTimeFormatter.ISO_DATE));
    }

    @Test
    void withdrawsThroughSavingsDomainUsingTransferFeePolicy() {
        PlatformSecurityContext context = mock(PlatformSecurityContext.class);
        AppUser user = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(user);
        SavingsAccountAssembler assembler = mock(SavingsAccountAssembler.class);
        SavingsAccountDomainService domain = mock(SavingsAccountDomainService.class);
        SavingsAccount account = savings("USD", 1L);
        when(account.isWithdrawalFeeApplicableForTransfer()).thenReturn(true);
        when(assembler.assembleFromInUserScope(10L, false)).thenReturn(account);
        Money amount = mock(Money.class);
        when(amount.getAmount()).thenReturn(BigDecimal.TEN);
        LocalDate date = LocalDate.of(2026, 11, 5);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        SavingsAccountTransaction withdrawal = mock(SavingsAccountTransaction.class);
        when(domain.handleWithdrawal(eq(account), eq(formatter), eq(date), eq(BigDecimal.TEN), isNull(), any(), eq(false)))
                .thenReturn(withdrawal);

        assertSame(withdrawal, new ClientChargeSavingsPaymentService(context, assembler, domain).withdraw(10L, charge("USD", 1L), amount,
                date, formatter));

        verify(user).validateHasPermissionTo("WITHDRAWAL_SAVINGSACCOUNT");
        ArgumentCaptor<SavingsTransactionBooleanValues> options = ArgumentCaptor.forClass(SavingsTransactionBooleanValues.class);
        verify(domain).handleWithdrawal(eq(account), eq(formatter), eq(date), eq(BigDecimal.TEN), isNull(), options.capture(), eq(false));
        assertTrue(options.getValue().isAccountTransfer());
        assertTrue(options.getValue().isRegularTransaction());
        assertTrue(options.getValue().isApplyWithdrawFee());
    }

    @Test
    void acceptsMatchingCurrencyAndDisabledAccounting() {
        SavingsAccount savings = savings("USD", 1L);
        ClientCharge charge = charge("USD", 1L);
        assertDoesNotThrow(() -> ClientChargeSavingsPaymentService.validateFunding(savings, charge));
    }

    @Test
    void rejectsDifferentCurrency() {
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> ClientChargeSavingsPaymentService.validateFunding(savings("EUR", 1L), charge("USD", 1L)));
    }

    @Test
    void rejectsUnverifiedCrossOfficeAccounting() {
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> ClientChargeSavingsPaymentService.validateFunding(savings("USD", 2L), charge("USD", 1L)));
    }

    @Test
    void rejectsMixedAccountingConfiguration() {
        ClientCharge charge = charge("USD", 1L);
        when(charge.getCharge().getAccount()).thenReturn(mock(GLAccount.class));
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> ClientChargeSavingsPaymentService.validateFunding(savings("USD", 1L), charge));
    }

    private SavingsAccount savings(String currencyCode, Long officeId) {
        SavingsAccount savings = mock(SavingsAccount.class);
        MonetaryCurrency currency = mock(MonetaryCurrency.class);
        when(currency.getCode()).thenReturn(currencyCode);
        when(savings.getCurrency()).thenReturn(currency);
        when(savings.officeId()).thenReturn(officeId);
        return savings;
    }

    private ClientCharge charge(String currencyCode, Long officeId) {
        ClientCharge charge = mock(ClientCharge.class);
        MonetaryCurrency currency = mock(MonetaryCurrency.class);
        when(currency.getCode()).thenReturn(currencyCode);
        when(charge.getCurrency()).thenReturn(currency);
        when(charge.getOfficeId()).thenReturn(officeId);
        when(charge.getCharge()).thenReturn(mock(Charge.class));
        return charge;
    }
}
