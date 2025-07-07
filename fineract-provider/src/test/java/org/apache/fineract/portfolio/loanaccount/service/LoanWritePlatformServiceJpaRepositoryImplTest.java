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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
public class LoanWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private LoanAssembler loanAssembler;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanTransactionRepository loanTransactionRepository;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private CommandProcessingService commandProcessingService;

    @Mock
    private ExternalIdFactory externalIdFactory;

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private LoanTransactionValidator loanTransactionValidator;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private ReprocessLoanTransactionsService reprocessLoanTransactionsService;

    @Mock
    private LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository;

    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;

    @Mock
    private LoanJournalEntryPoster journalEntryPoster;

    @Mock
    private LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;

    @InjectMocks
    private LoanWritePlatformServiceJpaRepositoryImpl loanWritePlatformService;

    private Loan loan;
    private AppUser appUser;
    private JsonCommand command;
    private static final Long LOAN_ID = 1L;

    @BeforeEach
    public void setUp() {
        appUser = mock(AppUser.class);

        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, DateUtils.parseLocalDate("2025-05-20"))));

        when(context.getAuthenticatedUserIfPresent()).thenReturn(appUser);
    }

    private void setupMoneyHelper() {
        // Set up a test tenant context
        FineractPlatformTenant tenant = new FineractPlatformTenant(1L, "test", "Test Tenant", "Asia/Kolkata", null);
        ThreadLocalContextUtil.setTenant(tenant);

        // Initialize MoneyHelper with tenant configuration (HALF_EVEN = 6)
        MoneyHelper.initializeTenantRoundingMode("test", 6);
    }

    @Test
    public void chargeOff_withInactiveLoan_expectException() {
        LoanProductRelatedDetail loanProductDetail = mock(LoanProductRelatedDetail.class);

        LoanProduct loanProduct = mock(LoanProduct.class);
        when(loanProduct.getLoanProductRelatedDetail()).thenReturn(loanProductDetail);
        loan = new LoanBuilder(loanProduct).withId(LOAN_ID).build();

        when(loanAssembler.assembleFrom(anyLong())).thenReturn(loan);

        command = mock(JsonCommand.class);

        GeneralPlatformDomainRuleException exception = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> loanWritePlatformService.chargeOff(command));

        assertEquals("Loan: 1 Charge-off is not allowed. Loan Account is not Active", exception.getMessage());
    }

    @Test
    public void chargeOff_withChargedOffLoan_expectException() {
        LoanProductRelatedDetail loanProductDetail = mock(LoanProductRelatedDetail.class);

        LoanProduct loanProduct = mock(LoanProduct.class);
        when(loanProduct.getLoanProductRelatedDetail()).thenReturn(loanProductDetail);
        loan = new LoanBuilder(loanProduct).withId(LOAN_ID).withLoanStatus(LoanStatus.ACTIVE).withChargedOff(true).build();

        when(loanAssembler.assembleFrom(anyLong())).thenReturn(loan);

        command = mock(JsonCommand.class);

        GeneralPlatformDomainRuleException exception = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> loanWritePlatformService.chargeOff(command));

        assertEquals("Loan: 1 is already charged-off", exception.getMessage());
    }

    @Test
    public void chargeOff_transactionBeforeLast_expectException() {
        setupMoneyHelper();
        LoanProductRelatedDetail loanProductDetail = mock(LoanProductRelatedDetail.class);

        LoanProduct loanProduct = mock(LoanProduct.class);
        when(loanProduct.getLoanProductRelatedDetail()).thenReturn(loanProductDetail);

        LoanTransaction t1 = LoanTransaction.repayment(null, Money.of(CurrencyData.blank(), BigDecimal.valueOf(100)), null,
                DateUtils.parseLocalDate("2025-05-15"), null);

        loan = new LoanBuilder(loanProduct).withId(LOAN_ID).withLoanStatus(LoanStatus.ACTIVE).withLoanTransactions(List.of(t1)).build();

        when(loanAssembler.assembleFrom(anyLong())).thenReturn(loan);

        command = mock(JsonCommand.class);
        when(command.localDateValueOfParameterNamed("transactionDate")).thenReturn(DateUtils.parseLocalDate("2025-05-14"));

        GeneralPlatformDomainRuleException exception = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> loanWritePlatformService.chargeOff(command));

        assertEquals("Loan: 1 charge-off cannot be executed. User transaction was found after the charge-off transaction date!",
                exception.getMessage());
    }

    @Test
    public void chargeOff_cannotBeInFuture_expectException() {
        setupMoneyHelper();
        LoanProductRelatedDetail loanProductDetail = mock(LoanProductRelatedDetail.class);

        LoanProduct loanProduct = mock(LoanProduct.class);
        when(loanProduct.getLoanProductRelatedDetail()).thenReturn(loanProductDetail);

        LoanTransaction t1 = LoanTransaction.repayment(null, Money.of(CurrencyData.blank(), BigDecimal.valueOf(100)), null,
                DateUtils.parseLocalDate("2025-05-13"), null);

        loan = new LoanBuilder(loanProduct).withId(LOAN_ID).withLoanStatus(LoanStatus.ACTIVE).withLoanTransactions(List.of(t1)).build();

        when(loanAssembler.assembleFrom(anyLong())).thenReturn(loan);

        command = mock(JsonCommand.class);
        when(command.localDateValueOfParameterNamed("transactionDate")).thenReturn(DateUtils.parseLocalDate("2025-05-24"));

        GeneralPlatformDomainRuleException exception = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> loanWritePlatformService.chargeOff(command));

        assertEquals("The transaction date cannot be in the future.", exception.getMessage());
    }

    @Test
    public void chargeOff_forReversedTransaction_shouldRun() {
        LoanProductRelatedDetail loanProductDetail = mock(LoanProductRelatedDetail.class);
        when(loanProductDetail.getAnnualNominalInterestRate()).thenReturn(BigDecimal.valueOf(10));

        LoanProduct loanProduct = mock(LoanProduct.class);
        when(loanProduct.getLoanProductRelatedDetail()).thenReturn(loanProductDetail);

        LoanCharge charge = mock(LoanCharge.class);
        when(charge.getSubmittedOnDate()).thenReturn(DateUtils.parseLocalDate("2025-05-10"));

        Client client = mock(Client.class);
        when(client.getId()).thenReturn(1L);

        LoanSummary summary = LoanSummary.create(BigDecimal.TEN);
        summary.zeroFields();
        loan = new LoanBuilder(loanProduct).withId(LOAN_ID).withLoanStatus(LoanStatus.ACTIVE).withCharges(Set.of(charge))
                .withSummary(summary).withClient(client).build();

        LoanTransaction t1 = LoanTransaction.chargeOff(loan, DateUtils.parseLocalDate("2025-05-13"), ExternalId.empty());
        t1.reverse();
        LoanTransaction t2 = LoanTransaction.chargeOff(loan, DateUtils.parseLocalDate("2025-05-10"), ExternalId.empty());

        loan.addLoanTransaction(t1);
        loan.addLoanTransaction(t2);

        when(loanAssembler.assembleFrom(anyLong())).thenReturn(loan);

        command = mock(JsonCommand.class);
        when(command.localDateValueOfParameterNamed("transactionDate")).thenReturn(DateUtils.parseLocalDate("2025-05-12"));

        CommandProcessingResult result = loanWritePlatformService.chargeOff(command);

        assertEquals(1L, result.getClientId());
    }
}
