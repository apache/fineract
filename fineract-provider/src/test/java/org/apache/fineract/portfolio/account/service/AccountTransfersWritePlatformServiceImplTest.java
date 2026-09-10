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
package org.apache.fineract.portfolio.account.service;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferAmountParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferDateParamName;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransfersDataValidator;
import org.apache.fineract.portfolio.account.domain.AccountTransferAssembler;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountTransfersWritePlatformServiceImplTest {

    private static final Long FROM_SAVINGS_ID = 11L;
    private static final Long TO_SAVINGS_ID = 12L;
    private static final String FROM_OFFICE_HIERARCHY = ".1.2.";
    private static final String TO_OFFICE_HIERARCHY = ".1.3.";

    @Mock
    private AccountTransfersDataValidator accountTransfersDataValidator;
    @Mock
    private AccountTransferAssembler accountTransferAssembler;
    @Mock
    private AccountTransferRepository accountTransferRepository;
    @Mock
    private SavingsAccountAssembler savingsAccountAssembler;
    @Mock
    private SavingsAccountDomainService savingsAccountDomainService;
    @Mock
    private LoanAssembler loanAccountAssembler;
    @Mock
    private LoanAccountDomainService loanAccountDomainService;
    @Mock
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    @Mock
    private AccountTransferDetailRepository accountTransferDetailRepository;
    @Mock
    private LoanReadPlatformService loanReadPlatformService;
    @Mock
    private GSIMRepositoy gsimRepository;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private LoanAdjustmentService loanAdjustmentService;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    @Mock
    private PlatformSecurityContext context;

    @InjectMocks
    private AccountTransfersWritePlatformServiceImpl service;

    @Test
    void createSavingsToSavingsTransferShouldValidateBothAccountOfficeHierarchies() {
        JsonCommand command = prepareSavingsToSavingsTransfer(FROM_OFFICE_HIERARCHY, TO_OFFICE_HIERARCHY);

        service.create(command);

        verify(context).validateAccessRights(FROM_OFFICE_HIERARCHY);
        verify(context).validateAccessRights(TO_OFFICE_HIERARCHY);
    }

    @Test
    void createSavingsToSavingsTransferAllowsParentOfficeToAccessChildOfficeResource() {
        JsonCommand command = prepareSavingsToSavingsTransfer(".1.", ".1.2.3.");

        service.create(command);

        verify(context).validateAccessRights(".1.");
        verify(context).validateAccessRights(".1.2.3.");
    }

    @Test
    void createSavingsToSavingsTransferRejectsChildOfficeAccessToParentOfficeResource() {
        JsonCommand command = prepareSavingsToSavingsTransfer(".1.2.", ".1.");
        doThrow(new NoAuthorizationException("The user doesn't have enough permissions to access the resource.")).when(context)
                .validateAccessRights(".1.");

        assertThrows(NoAuthorizationException.class, () -> service.create(command));
    }

    @Test
    void createSavingsToSavingsTransferAllowsAccountsInTheSameOffice() {
        JsonCommand command = prepareSavingsToSavingsTransfer(".1.2.", ".1.2.");

        service.create(command);

        verify(context, times(2)).validateAccessRights(".1.2.");
    }

    @Test
    void createSavingsToSavingsTransferValidatesMultiLevelOfficeHierarchy() {
        JsonCommand command = prepareSavingsToSavingsTransfer(".1.2.3.", ".1.2.3.4.");

        service.create(command);

        verify(context).validateAccessRights(".1.2.3.");
        verify(context).validateAccessRights(".1.2.3.4.");
    }

    private JsonCommand prepareSavingsToSavingsTransfer(final String fromOfficeHierarchy, final String toOfficeHierarchy) {
        JsonCommand command = savingsToSavingsCommand();
        SavingsAccount fromSavingsAccount = savingsAccount(fromOfficeHierarchy);
        SavingsAccount toSavingsAccount = savingsAccount(toOfficeHierarchy);
        SavingsAccountTransaction withdrawal = org.mockito.Mockito.mock(SavingsAccountTransaction.class);
        SavingsAccountTransaction deposit = org.mockito.Mockito.mock(SavingsAccountTransaction.class);
        AccountTransferDetails accountTransferDetails = org.mockito.Mockito.mock(AccountTransferDetails.class);

        when(savingsAccountAssembler.assembleFrom(FROM_SAVINGS_ID, false)).thenReturn(fromSavingsAccount);
        when(savingsAccountAssembler.assembleFrom(TO_SAVINGS_ID, false)).thenReturn(toSavingsAccount);
        when(savingsAccountDomainService.handleWithdrawal(eq(fromSavingsAccount), any(), any(), any(), any(),
                any(SavingsTransactionBooleanValues.class), eq(false))).thenReturn(withdrawal);
        when(savingsAccountDomainService.handleDeposit(eq(toSavingsAccount), any(), any(), any(), any(), eq(true), eq(true), eq(false)))
                .thenReturn(deposit);
        when(accountTransferAssembler.assembleSavingsToSavingsTransfer(command, fromSavingsAccount, toSavingsAccount, withdrawal, deposit))
                .thenReturn(accountTransferDetails);
        when(accountTransferDetails.getId()).thenReturn(99L);
        return command;
    }

    private JsonCommand savingsToSavingsCommand() {
        JsonCommand command = org.mockito.Mockito.mock(JsonCommand.class);
        when(command.localDateValueOfParameterNamed(transferDateParamName)).thenReturn(LocalDate.of(2025, 1, 1));
        when(command.bigDecimalValueOfParameterNamed(transferAmountParamName)).thenReturn(BigDecimal.TEN);
        when(command.extractLocale()).thenReturn(Locale.ENGLISH);
        when(command.dateFormat()).thenReturn("dd MMMM yyyy");
        when(command.integerValueSansLocaleOfParameterNamed(fromAccountTypeParamName)).thenReturn(PortfolioAccountType.SAVINGS.getValue());
        when(command.integerValueSansLocaleOfParameterNamed(toAccountTypeParamName)).thenReturn(PortfolioAccountType.SAVINGS.getValue());
        when(command.longValueOfParameterNamed(fromAccountIdParamName)).thenReturn(FROM_SAVINGS_ID);
        when(command.longValueOfParameterNamed(toAccountIdParamName)).thenReturn(TO_SAVINGS_ID);
        return command;
    }

    private SavingsAccount savingsAccount(final String officeHierarchy) {
        SavingsAccount savingsAccount = org.mockito.Mockito.mock(SavingsAccount.class);
        Office office = org.mockito.Mockito.mock(Office.class);
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, 0);
        when(office.getHierarchy()).thenReturn(officeHierarchy);
        when(savingsAccount.office()).thenReturn(office);
        when(savingsAccount.getCurrency()).thenReturn(currency);
        when(savingsAccount.isWithdrawalFeeApplicableForTransfer()).thenReturn(false);
        return savingsAccount;
    }
}
