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
package org.apache.fineract.accounting.journalentry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.journalentry.command.JournalEntryCommand;
import org.apache.fineract.accounting.journalentry.command.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer;
import org.apache.fineract.accounting.rule.domain.AccountingRuleRepository;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.investor.domain.ExternalAssetOwnerRepository;
import org.apache.fineract.investor.service.AccountingService;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JournalEntryWritePlatformServiceImplTest {

    @Mock
    private GLClosureRepository glClosureRepository;
    @Mock
    private GLAccountRepository glAccountRepository;
    @Mock
    private JournalEntryRepository glJournalEntryRepository;
    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;
    @Mock
    private AccountingProcessorForLoanFactory accountingProcessorForLoanFactory;
    @Mock
    private AccountingProcessorForSavingsFactory accountingProcessorForSavingsFactory;
    @Mock
    private AccountingProcessorForSharesFactory accountingProcessorForSharesFactory;
    @Mock
    private AccountingProcessorHelper helper;
    @Mock
    private JournalEntryCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private AccountingRuleRepository accountingRuleRepository;
    @Mock
    private GLAccountReadPlatformService glAccountReadPlatformService;
    @Mock
    private OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    @Mock
    private FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper;
    @Mock
    private CashBasedAccountingProcessorForClientTransactions accountingProcessorForClientTransactions;
    @Mock
    private ConfigurationReadPlatformService configurationReadPlatformService;
    @Mock
    private AccountingService accountingService;
    @Mock
    private ExternalAssetOwnerRepository externalAssetOwnerRepository;

    @InjectMocks
    private JournalEntryWritePlatformServiceImpl service;

    // Regression test for: revertJournalEntry(List<JournalEntry>, String) called journalEntries.get(0)
    // unconditionally, with no empty-list guard. defineOpeningBalance() calls this overload directly
    // (not through the guarded JsonCommand entry point) for each transactionId returned by
    // findNonReversedContraTransactionIds(), using the narrower findUnReversedManualJournalEntriesByTransactionId()
    // query. When a contra transaction has no unreversed *manual* journal entries (e.g. it was system-generated),
    // that query returns empty and this threw IndexOutOfBoundsException. Fixed by guarding the access with a
    // null/empty check, mirroring the guard already present in the other caller of this pattern in the same file.
    @Test
    void revertJournalEntryWithEmptyListDoesNotThrow() {
        assertDoesNotThrow(() -> service.revertJournalEntry(Collections.emptyList(), "defining opening balance"));
    }

    @Test
    void createJournalEntryRejectsZeroAmountLine() {
        final JsonCommand command = commandWithLines(BigDecimal.ZERO, BigDecimal.TEN);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.createJournalEntry(command));

        assertThat(exception.getErrors()).extracting(ApiParameterError::getParameterName).containsExactly("credits[0].amount");
        assertThat(exception.getErrors()).extracting(ApiParameterError::getDefaultUserMessage)
                .containsExactly("The parameter `credits[0].amount` must be greater than 0.");
        verifyNoInteractions(officeRepositoryWrapper, helper);
    }

    @Test
    void defineOpeningBalanceDoesNotRejectZeroAmountLine() {
        final JsonCommand command = commandWithLines(BigDecimal.ZERO, BigDecimal.ZERO);
        final IllegalStateException passedValidation = new IllegalStateException("validation passed");
        when(financialActivityAccountRepositoryWrapper.findByFinancialActivityTypeWithNotFoundDetection(300)).thenThrow(passedValidation);

        final IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> service.defineOpeningBalance(command));

        assertThat(thrown).isSameAs(passedValidation);
    }

    private JsonCommand commandWithLines(final BigDecimal creditAmount, final BigDecimal debitAmount) {
        final SingleDebitOrCreditEntryCommand[] credits = { new SingleDebitOrCreditEntryCommand(1L, creditAmount, null, Set.of()) };
        final SingleDebitOrCreditEntryCommand[] debits = { new SingleDebitOrCreditEntryCommand(2L, debitAmount, null, Set.of()) };
        final JournalEntryCommand journalEntryCommand = new JournalEntryCommand(1L, "USD", LocalDate.of(2026, 11, 5), "comment", null, null,
                null, null, null, null, null, null, null, credits, debits, "en", "dd MMMM yyyy", null);
        final JsonCommand command = mock(JsonCommand.class);
        when(fromApiJsonDeserializer.commandFromApiJson(any())).thenReturn(journalEntryCommand);
        return command;
    }
}
