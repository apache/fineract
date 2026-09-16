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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Collections;
import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer;
import org.apache.fineract.accounting.rule.domain.AccountingRuleRepository;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
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
}
