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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
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
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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

    @Mock
    private LoanUtilService loanUtilService;

    @Mock
    private PaymentDetailWritePlatformService paymentDetailWritePlatformService;

    @Mock
    private LoanDisbursementService loanDisbursementService;

    @Mock
    private FineractProperties fineractProperties;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private LoanLifecycleStateMachine loanLifecycleStateMachine;

    @Mock
    private LoanAccrualsProcessingService loanAccrualsProcessingService;

    @Mock
    private LoanAccountDomainService loanAccountDomainService;

    @Mock
    private LoanScheduleService loanScheduleService;

    @Mock
    private LoanBalanceService loanBalanceService;

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

    @Nested
    class DisburseLoanTest {

        private Loan loan;
        private JsonCommand command;
        private MonetaryCurrency currency;

        @BeforeEach
        void setUp() {
            // Common setup for all disburse tests
            setupMoneyHelper();
            command = mock(JsonCommand.class);
            loan = mock(Loan.class);
            currency = new MonetaryCurrency("USD", 2, null);
            LoanProduct loanProduct = mock(LoanProduct.class);
            LoanProductRelatedDetail loanProductRelatedDetail = mock(LoanProductRelatedDetail.class);
            LoanSummary loanSummary = mock(LoanSummary.class);
            AppUser currentUser = mock(AppUser.class);
            LocalDate disbursementDate = LocalDate.of(2025, 5, 21);

            when(command.localDateValueOfParameterNamed("adjustRepaymentDate")).thenReturn(disbursementDate);
            when(command.localDateValueOfParameterNamed("actualDisbursementDate")).thenReturn(disbursementDate);
            when(command.bigDecimalValueOfParameterNamed(LoanApiConstants.disbursementNetDisbursalAmountParameterName))
                    .thenReturn(BigDecimal.TEN);
            when(command.extractLocale()).thenReturn(Locale.ENGLISH);
            when(command.dateFormat()).thenReturn("yyyy-MM-dd");

            when(loan.getId()).thenReturn(LOAN_ID);
            when(loan.getOfficeId()).thenReturn(1L);
            when(loan.getCurrency()).thenReturn(currency);
            when(loan.getPrincipal()).thenReturn(Money.of(currency, new BigDecimal("1000")));
            when(loan.loanProduct()).thenReturn(loanProduct);
            when(loan.getSummary()).thenReturn(loanSummary);
            when(loan.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
            List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
            installments.add(mock(LoanRepaymentScheduleInstallment.class));
            when(loan.getRepaymentScheduleInstallments()).thenReturn(installments);
            when(loan.getTotalOverpaidAsMoney()).thenReturn(Money.zero(currency));
            when(loan.fetchRepaymentScheduleInstallment(1)).thenReturn(mock(LoanRepaymentScheduleInstallment.class));
            when(loan.getStatus()).thenReturn(LoanStatus.APPROVED);
            when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);

            LoanTransaction disbursmentTransaction = mock(LoanTransaction.class);
            when(disbursmentTransaction.getTypeOf()).thenReturn(LoanTransactionType.DISBURSEMENT);

            when(loan.getLoanTransactions()).thenReturn(List.of(disbursmentTransaction));

            when(loanProduct.isDisallowExpectedDisbursements()).thenReturn(false);

            when(loan.getLoanProduct()).thenReturn(loanProduct);
            when(loanProduct.isMultiDisburseLoan()).thenReturn(false);

            when(loanAssembler.assembleFrom(LOAN_ID)).thenReturn(loan);
            when(context.getAuthenticatedUserIfPresent()).thenReturn(currentUser);
            when(externalIdFactory.createFromCommand(any(), any())).thenReturn(ExternalId.empty());
            when(loanUtilService.buildScheduleGeneratorDTO(any(), any())).thenReturn(mock(ScheduleGeneratorDTO.class));
            when(loanDisbursementService.adjustDisburseAmount(any(), any(), any())).thenReturn(Money.of(currency, new BigDecimal("1000")));
            when(fineractProperties.getLoan()).thenReturn(mock(FineractProperties.FineractLoanProperties.class));
            when(fineractProperties.getLoan().isAllowCashAndNonCashAccrual()).thenReturn(true);
            when(loan.deriveSumTotalOfChargesDueAtDisbursement()).thenReturn(new BigDecimal(1000));
            when(loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_DISBURSED, loan)).thenReturn(LoanStatus.ACTIVE);
            when(loanRepositoryWrapper.saveAndFlush(loan)).thenReturn(loan);
        }

        @Test
        public void shouldApplyAccrualInterestWhenSingleDisbursementAndUpfrontAccrual() {

            when(loan.isMultiDisburmentLoan()).thenReturn(false);
            when(loan.isUpfrontAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
            when(loan.getSummary().getTotalInterestCharged()).thenReturn(BigDecimal.TEN);

            try (MockedStatic<TemporaryConfigurationServiceContainer> temporaryConfigServiceMock = mockStatic(TemporaryConfigurationServiceContainer.class)) {

                temporaryConfigServiceMock.when(TemporaryConfigurationServiceContainer::isExternalIdAutoGenerationEnabled)
                        .thenReturn(false);

                loanWritePlatformService.disburseLoan(LOAN_ID, command, false, false);

                verify(loan, times(2)).addLoanTransaction(argThat(tx -> tx.isDisbursement() || tx.isAccrual()));
            }

        }

        @Test
        public void shouldApplyAccrualInterestWhenMultiDisbursementFirstDisbursementAndUpfrontAccrual() {

            when(loan.isMultiDisburmentLoan()).thenReturn(true);
            when(loan.getDisbursedLoanDisbursementDetails()).thenReturn(List.of(mock(LoanDisbursementDetails.class)));
            when(loan.isUpfrontAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
            when(loan.getSummary().getTotalInterestCharged()).thenReturn(BigDecimal.TEN);

            try (MockedStatic<TemporaryConfigurationServiceContainer> temporaryConfigServiceMock = mockStatic(TemporaryConfigurationServiceContainer.class)) {

                temporaryConfigServiceMock.when(TemporaryConfigurationServiceContainer::isExternalIdAutoGenerationEnabled)
                        .thenReturn(false);

                loanWritePlatformService.disburseLoan(LOAN_ID, command, false, false);

                verify(loan, times(2)).addLoanTransaction(argThat(tx -> tx.isDisbursement() || tx.isAccrual()));
            }

        }

        @Test
        public void shouldApplyAccrualInterestWhenCashAccountingIsEnabled() {

            when(loan.isMultiDisburmentLoan()).thenReturn(false);
            when(loan.isUpfrontAccrualAccountingEnabledOnLoanProduct()).thenReturn(false);
            when(loan.isCashBasedAccountingEnabledOnLoanProduct()).thenReturn(true);
            when(fineractProperties.getLoan().isAllowCashAndNonCashAccrual()).thenReturn(true);
            when(loan.getSummary().getTotalInterestCharged()).thenReturn(BigDecimal.TEN);

            try (MockedStatic<TemporaryConfigurationServiceContainer> temporaryConfigServiceMock = mockStatic(TemporaryConfigurationServiceContainer.class)) {

                temporaryConfigServiceMock.when(TemporaryConfigurationServiceContainer::isExternalIdAutoGenerationEnabled)
                        .thenReturn(false);
                loanWritePlatformService.disburseLoan(LOAN_ID, command, false, false);

                verify(loan, times(2)).addLoanTransaction(argThat(tx -> tx.isDisbursement() || tx.isAccrual()));
            }
        }

        @Test
        public void shouldNotApplyAccrualInterestWhenInterestIsZero() {

            when(loan.isMultiDisburmentLoan()).thenReturn(false);
            when(loan.isUpfrontAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);

            when(loan.getSummary().getTotalInterestCharged()).thenReturn(BigDecimal.ZERO);

            loanWritePlatformService.disburseLoan(LOAN_ID, command, false, false);

            verify(loan, times(1)).addLoanTransaction(argThat(LoanTransaction::isDisbursement));
            verify(loan, never()).addLoanTransaction(argThat(LoanTransaction::isAccrual));
        }

        @Test
        public void shouldNotApplyAccrualInterestWhenNotUpfrontOrCashAccounting() {

            when(loan.isMultiDisburmentLoan()).thenReturn(false);
            when(loan.isUpfrontAccrualAccountingEnabledOnLoanProduct()).thenReturn(false);
            when(loan.isCashBasedAccountingEnabledOnLoanProduct()).thenReturn(false);
            when(loan.isAccountingDisabledOnLoanProduct()).thenReturn(false);
            when(loan.getSummary().getTotalInterestCharged()).thenReturn(BigDecimal.TEN);

            loanWritePlatformService.disburseLoan(LOAN_ID, command, false, false);

            verify(loan, times(1)).addLoanTransaction(argThat(LoanTransaction::isDisbursement));
            verify(loan, never()).addLoanTransaction(argThat(LoanTransaction::isAccrual));
        }

        @Test
        public void shouldNotApplyAccrualInterestWhenMultiDisbursementAndNotFirstDisbursement() {

            when(loan.isMultiDisburmentLoan()).thenReturn(true);
            when(loan.getDisbursedLoanDisbursementDetails())
                    .thenReturn(List.of(mock(LoanDisbursementDetails.class), mock(LoanDisbursementDetails.class)));
            when(loan.isUpfrontAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
            when(loan.getSummary().getTotalInterestCharged()).thenReturn(BigDecimal.TEN);

            loanWritePlatformService.disburseLoan(LOAN_ID, command, false, false);

            verify(loan, times(1)).addLoanTransaction(argThat(LoanTransaction::isDisbursement));
            verify(loan, never()).addLoanTransaction(argThat(LoanTransaction::isAccrual));
        }
    }
}
