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
package org.apache.fineract.portfolio.savings.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Closing a matured fixed deposit with "reinvest principal only" must roll over the original deposit, not the matured
 * balance — otherwise the depositor's interest is silently reinvested instead of paid out, and the option is
 * indistinguishable from "reinvest principal and interest".
 *
 * <p>
 * {@code handleFDAccountMaturityClosure} (the maturity-instruction path) already branches on the closure type;
 * {@code handleFDAccountClosure} (what {@code ?command=close} calls) did not.
 *
 * <p>
 * The whole balance is still withdrawn from the closing account in both cases: {@link SavingsAccount#close} rejects a
 * non-zero balance, so interest that is not reinvested leaves the account as a payout to the depositor. The maturity
 * path can withdraw the principal alone only because it deliberately does not close the account.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DepositAccountClosureReInvestAmountTest {

    private static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("10000000.000000");
    private static final BigDecimal MATURED_BALANCE = new BigDecimal("11200000.000000");
    private static final LocalDate CLOSED_ON_DATE = LocalDate.of(2026, 8, 12);

    private static final int REINVEST_PRINCIPAL_AND_INTEREST = 300;
    private static final int REINVEST_PRINCIPAL_ONLY = 400;

    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepository;
    @Mock
    private DepositAccountAssembler depositAccountAssembler;
    @Mock
    private SavingsAccountDomainService savingsAccountDomainService;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;

    @InjectMocks
    private DepositAccountDomainServiceJpa domainService;

    @Test
    void reinvestPrincipalOnlyRollsOverTheOriginalDepositAmount() {
        final FixedDepositAccount account = maturedAccount();

        domainService.handleFDAccountClosure(account, null, mock(AppUser.class), closeCommand(REINVEST_PRINCIPAL_ONLY), new HashMap<>());

        assertThat(capturedReInvestAmount(account)).isEqualByComparingTo(DEPOSIT_AMOUNT);
    }

    @Test
    void reinvestPrincipalAndInterestRollsOverTheMaturedBalance() {
        final FixedDepositAccount account = maturedAccount();

        domainService.handleFDAccountClosure(account, null, mock(AppUser.class), closeCommand(REINVEST_PRINCIPAL_AND_INTEREST),
                new HashMap<>());

        assertThat(capturedReInvestAmount(account)).isEqualByComparingTo(MATURED_BALANCE);
    }

    @Test
    void theClosingAccountAlwaysWithdrawsItsWholeBalanceSoItCanBeClosed() {
        final FixedDepositAccount account = maturedAccount();

        domainService.handleFDAccountClosure(account, null, mock(AppUser.class), closeCommand(REINVEST_PRINCIPAL_ONLY), new HashMap<>());

        final ArgumentCaptor<BigDecimal> withdrawn = ArgumentCaptor.forClass(BigDecimal.class);
        verify(savingsAccountDomainService).handleWithdrawal(eq(account), any(), eq(CLOSED_ON_DATE), withdrawn.capture(), any(), any(),
                anyBoolean());
        assertThat(withdrawn.getValue()).isEqualByComparingTo(MATURED_BALANCE);
    }

    private static BigDecimal capturedReInvestAmount(final FixedDepositAccount account) {
        final ArgumentCaptor<BigDecimal> amount = ArgumentCaptor.forClass(BigDecimal.class);
        verify(account).reInvest(amount.capture(), any());
        return amount.getValue();
    }

    private FixedDepositAccount maturedAccount() {
        final FixedDepositAccount account = mock(FixedDepositAccount.class);
        when(account.getDepositAmount()).thenReturn(DEPOSIT_AMOUNT);
        when(account.getAccountBalance()).thenReturn(MATURED_BALANCE);
        // the closure books its maturity interest to the ledger on the way out, so it needs a currency to book in
        when(account.getCurrency()).thenReturn(mock(MonetaryCurrency.class));
        when(account.findExistingTransactionIds()).thenReturn(new ArrayList<>());
        when(account.findExistingReversedTransactionIds()).thenReturn(new ArrayList<>());
        when(account.reInvest(any(), any())).thenReturn(mock(FixedDepositAccount.class));
        when(savingsAccountDomainService.handleWithdrawal(any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(mock(SavingsAccountTransaction.class));
        return account;
    }

    private JsonCommand closeCommand(final int onAccountClosureId) {
        final JsonCommand command = mock(JsonCommand.class);
        when(command.extractLocale()).thenReturn(Locale.ENGLISH);
        when(command.dateFormat()).thenReturn("dd MMMM yyyy");
        when(command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName)).thenReturn(CLOSED_ON_DATE);
        when(command.integerValueOfParameterNamed(DepositsApiConstants.onAccountClosureIdParamName)).thenReturn(onAccountClosureId);
        return command;
    }
}
