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
package org.apache.fineract.portfolio.loanaccount.starter;

import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.holiday.domain.HolidayRepository;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.organisation.teller.data.CashierTransactionDataValidator;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.service.ChargeDropdownReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralAssembler;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.interestpauses.service.InterestPauseReadPlatformService;
import org.apache.fineract.portfolio.interestpauses.service.InterestPauseReadPlatformServiceImpl;
import org.apache.fineract.portfolio.interestpauses.service.InterestPauseWritePlatformService;
import org.apache.fineract.portfolio.interestpauses.service.InterestPauseWritePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanAccountingBridgeMapper;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanChargeMapper;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanCollateralManagementMapper;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanMapper;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanTransactionMapper;
import org.apache.fineract.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanTermVariationsRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationTransitionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDisbursementValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanForeclosureValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanOfficerValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanRefundValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanaccount.service.BulkLoansReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.BulkLoansReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.BuyDownFeePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.BuyDownFeeWritePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoWritePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.InterestRefundServiceDelegate;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccountServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualActivityProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualEventService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualTransactionBusinessEventService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualTransactionBusinessEventServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualsProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanApplicationWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanApplicationWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanArrearsAgingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanArrearsAgingServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssemblerImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.apache.fineract.portfolio.loanaccount.service.LoanBuyDownFeeAmortizationEventService;
import org.apache.fineract.portfolio.loanaccount.service.LoanBuyDownFeeAmortizationProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanBuyDownFeeAmortizationProcessingServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanCalculateRepaymentPastDueService;
import org.apache.fineract.portfolio.loanaccount.service.LoanCapitalizedIncomeAmortizationEventService;
import org.apache.fineract.portfolio.loanaccount.service.LoanCapitalizedIncomeAmortizationProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanCapitalizedIncomeAmortizationProcessingServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargePaidByReadService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeWritePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanDisbursementDetailsAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanDisbursementService;
import org.apache.fineract.portfolio.loanaccount.service.LoanDownPaymentHandlerService;
import org.apache.fineract.portfolio.loanaccount.service.LoanDownPaymentHandlerServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanJournalEntryPoster;
import org.apache.fineract.portfolio.loanaccount.service.LoanOfficerService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanRefundService;
import org.apache.fineract.portfolio.loanaccount.service.LoanScheduleService;
import org.apache.fineract.portfolio.loanaccount.service.LoanStatusChangePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanStatusChangePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionRelationReadService;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionService;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.service.ReplayedTransactionBusinessEventService;
import org.apache.fineract.portfolio.loanaccount.service.ReplayedTransactionBusinessEventServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.ReprocessLoanTransactionsService;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import org.apache.fineract.portfolio.loanaccount.service.schedule.LoanScheduleComponent;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.note.service.NoteWritePlatformService;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.rate.service.RateAssembler;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.GSIMReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class LoanAccountConfiguration {

    @Bean
    @ConditionalOnMissingBean(BulkLoansReadPlatformService.class)
    public BulkLoansReadPlatformService bulkLoansReadPlatformServicev(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            AccountDetailsReadPlatformService accountDetailsReadPlatformService) {
        return new BulkLoansReadPlatformServiceImpl(jdbcTemplate, context, accountDetailsReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(GLIMAccountInfoReadPlatformService.class)
    public GLIMAccountInfoReadPlatformService glimAccountInfoReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            AccountDetailsReadPlatformService accountDetailsReadPlatforService) {
        return new GLIMAccountInfoReadPlatformServiceImpl(jdbcTemplate, context, accountDetailsReadPlatforService);
    }

    @Bean
    @ConditionalOnMissingBean(GLIMAccountInfoWritePlatformService.class)
    public GLIMAccountInfoWritePlatformService glimAccountInfoWritePlatformService(GLIMAccountInfoRepository glimAccountRepository) {
        return new GLIMAccountInfoWritePlatformServiceImpl(glimAccountRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAccrualTransactionBusinessEventService.class)
    public LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService(
            final BusinessEventNotifierService businessEventNotifierService, final LoanTransactionRepository loanTransactionRepository) {
        return new LoanAccrualTransactionBusinessEventServiceImpl(businessEventNotifierService, loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanApplicationWritePlatformService.class)
    public LoanApplicationWritePlatformService loanApplicationWritePlatformService(PlatformSecurityContext context,
            LoanApplicationTransitionValidator loanApplicationTransitionValidator, LoanApplicationValidator loanApplicationValidator,
            LoanRepositoryWrapper loanRepositoryWrapper, NoteRepository noteRepository, LoanAssembler loanAssembler,
            CalendarRepository calendarRepository, CalendarInstanceRepository calendarInstanceRepository,
            SavingsAccountRepositoryWrapper savingsAccountRepository, AccountAssociationsRepository accountAssociationsRepository,
            BusinessEventNotifierService businessEventNotifierService, LoanScheduleAssembler loanScheduleAssembler,
            LoanUtilService loanUtilService, CalendarReadPlatformService calendarReadPlatformService,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService, GLIMAccountInfoRepository glimRepository,
            LoanRepository loanRepository, GSIMReadPlatformService gsimReadPlatformService,
            LoanLifecycleStateMachine loanLifecycleStateMachine, LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator, LoanScheduleService loanScheduleService) {
        return new LoanApplicationWritePlatformServiceJpaRepositoryImpl(context, loanApplicationTransitionValidator,
                loanApplicationValidator, loanRepositoryWrapper, noteRepository, loanAssembler, calendarRepository,
                calendarInstanceRepository, savingsAccountRepository, accountAssociationsRepository, businessEventNotifierService,
                loanScheduleAssembler, loanUtilService, calendarReadPlatformService, entityDatatableChecksWritePlatformService,
                glimRepository, loanRepository, gsimReadPlatformService, loanLifecycleStateMachine, loanAccrualsProcessingService,
                loanDownPaymentTransactionValidator, loanScheduleService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanArrearsAgingService.class)
    public LoanArrearsAgingService loanArrearsAgingService(JdbcTemplate jdbcTemplate,
            BusinessEventNotifierService businessEventNotifierService, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new LoanArrearsAgingServiceImpl(jdbcTemplate, businessEventNotifierService, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAssembler.class)
    public LoanAssembler loanAssembler(FromJsonHelper fromApiJsonHelper, LoanRepositoryWrapper loanRepository,
            LoanProductRepository loanProductRepository, ClientRepositoryWrapper clientRepository, GroupRepositoryWrapper groupRepository,
            FundRepository fundRepository, StaffRepository staffRepository, CodeValueRepositoryWrapper codeValueRepository,
            LoanScheduleAssembler loanScheduleAssembler, LoanChargeAssembler loanChargeAssembler,
            LoanCollateralAssembler collateralAssembler,
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            HolidayRepository holidayRepository, ConfigurationDomainService configurationDomainService,
            WorkingDaysRepositoryWrapper workingDaysRepository, RateAssembler rateAssembler, ExternalIdFactory externalIdFactory,
            AccountNumberFormatRepositoryWrapper accountNumberFormatRepository, GLIMAccountInfoRepository glimRepository,
            AccountNumberGenerator accountNumberGenerator, GLIMAccountInfoWritePlatformService glimAccountInfoWritePlatformService,
            LoanCollateralAssembler loanCollateralAssembler, LoanScheduleCalculationPlatformService calculationPlatformService,
            LoanDisbursementDetailsAssembler loanDisbursementDetailsAssembler, LoanChargeMapper loanChargeMapper,
            LoanCollateralManagementMapper loanCollateralManagementMapper, LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanDisbursementService loanDisbursementService, LoanChargeService loanChargeService, LoanOfficerService loanOfficerService,
            LoanScheduleComponent loanSchedule) {
        return new LoanAssemblerImpl(fromApiJsonHelper, loanRepository, loanProductRepository, clientRepository, groupRepository,
                fundRepository, staffRepository, codeValueRepository, loanScheduleAssembler, loanChargeAssembler, collateralAssembler,
                loanRepaymentScheduleTransactionProcessorFactory, holidayRepository, configurationDomainService, workingDaysRepository,
                rateAssembler, externalIdFactory, accountNumberFormatRepository, glimRepository, accountNumberGenerator,
                glimAccountInfoWritePlatformService, loanCollateralAssembler, calculationPlatformService, loanDisbursementDetailsAssembler,
                loanChargeMapper, loanCollateralManagementMapper, loanAccrualsProcessingService, loanDisbursementService, loanChargeService,
                loanOfficerService, loanSchedule);
    }

    @Bean
    @ConditionalOnMissingBean(LoanTransactionAssembler.class)
    public LoanTransactionAssembler loanTransactionAssembler(ExternalIdFactory externalIdFactory,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService) {

        return new LoanTransactionAssembler(externalIdFactory, paymentDetailWritePlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCalculateRepaymentPastDueService.class)
    public LoanCalculateRepaymentPastDueService loanCalculateRepaymentPastDueService() {
        return new LoanCalculateRepaymentPastDueService();
    }

    @Bean
    @ConditionalOnMissingBean(LoanChargeAssembler.class)
    public LoanChargeAssembler loanChargeAssembler(final FromJsonHelper fromApiJsonHelper, final ChargeRepositoryWrapper chargeRepository,
            final LoanChargeRepository loanChargeRepository, final LoanProductRepository loanProductRepository,
            final ExternalIdFactory externalIdFactory, final LoanChargeService loanChargeService) {
        return new LoanChargeAssembler(fromApiJsonHelper, chargeRepository, loanChargeRepository, loanProductRepository, externalIdFactory,
                loanChargeService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanChargeReadPlatformService.class)
    public LoanChargeReadPlatformService loanChargeReadPlatformService(JdbcTemplate jdbcTemplate,
            ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, DropdownReadPlatformService dropdownReadPlatformService,
            LoanChargeRepository loanChargeRepository) {
        return new LoanChargeReadPlatformServiceImpl(jdbcTemplate, chargeDropdownReadPlatformService, dropdownReadPlatformService,
                loanChargeRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanChargeWritePlatformService.class)
    public LoanChargeWritePlatformService loanChargeWritePlatformService(LoanChargeApiJsonValidator loanChargeApiJsonValidator,
            LoanAssembler loanAssembler, ChargeRepositoryWrapper chargeRepository,
            BusinessEventNotifierService businessEventNotifierService, LoanTransactionRepository loanTransactionRepository,
            AccountTransfersWritePlatformService accountTransfersWritePlatformService, LoanRepositoryWrapper loanRepositoryWrapper,
            JournalEntryWritePlatformService journalEntryWritePlatformService, LoanAccountDomainService loanAccountDomainService,
            LoanChargeRepository loanChargeRepository, LoanWritePlatformService loanWritePlatformService, LoanUtilService loanUtilService,
            LoanChargeReadPlatformService loanChargeReadPlatformService, LoanLifecycleStateMachine loanLifecycleStateMachine,
            AccountAssociationsReadPlatformService accountAssociationsReadPlatformService, FromJsonHelper fromApiJsonHelper,
            ConfigurationDomainService configurationDomainService,
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            ExternalIdFactory externalIdFactory, AccountTransferDetailRepository accountTransferDetailRepository,
            LoanChargeAssembler loanChargeAssembler, PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            NoteRepository noteRepository, LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService,
            LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator, LoanChargeValidator loanChargeValidator,
            LoanScheduleService loanScheduleService, ReprocessLoanTransactionsService reprocessLoanTransactionsService,
            LoanAccountService loanAccountService, LoanAdjustmentService loanAdjustmentService,
            LoanAccountingBridgeMapper loanAccountingBridgeMapper, LoanChargeService loanChargeService) {
        return new LoanChargeWritePlatformServiceImpl(loanChargeApiJsonValidator, loanAssembler, chargeRepository,
                businessEventNotifierService, loanTransactionRepository, accountTransfersWritePlatformService, loanRepositoryWrapper,
                journalEntryWritePlatformService, loanAccountDomainService, loanChargeRepository, loanWritePlatformService, loanUtilService,
                loanChargeReadPlatformService, loanLifecycleStateMachine, accountAssociationsReadPlatformService, fromApiJsonHelper,
                configurationDomainService, loanRepaymentScheduleTransactionProcessorFactory, externalIdFactory,
                accountTransferDetailRepository, loanChargeAssembler, paymentDetailWritePlatformService, noteRepository,
                loanAccrualTransactionBusinessEventService, loanAccrualsProcessingService, loanDownPaymentTransactionValidator,
                loanChargeValidator, loanScheduleService, reprocessLoanTransactionsService, loanAccountService, loanAdjustmentService,
                loanAccountingBridgeMapper, loanChargeService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanReadPlatformService.class)
    public LoanReadPlatformServiceImpl loanReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            LoanRepositoryWrapper loanRepositoryWrapper, ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            LoanProductReadPlatformService loanProductReadPlatformService, ClientReadPlatformService clientReadPlatformService,
            GroupReadPlatformService groupReadPlatformService, LoanDropdownReadPlatformService loanDropdownReadPlatformService,
            FundReadPlatformService fundReadPlatformService, ChargeReadPlatformService chargeReadPlatformService,
            CodeValueReadPlatformService codeValueReadPlatformService, CalendarReadPlatformService calendarReadPlatformService,
            StaffReadPlatformService staffReadPlatformService, PaginationHelper paginationHelper,
            PaymentTypeReadPlatformService paymentTypeReadPlatformService,
            FloatingRatesReadPlatformService floatingRatesReadPlatformService, LoanUtilService loanUtilService,
            ConfigurationDomainService configurationDomainService, AccountDetailsReadPlatformService accountDetailsReadPlatformService,
            ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator,
            DelinquencyReadPlatformService delinquencyReadPlatformService, LoanTransactionRepository loanTransactionRepository,
            LoanChargePaidByReadService loanChargePaidByReadService, LoanTransactionRelationReadService loanTransactionRelationReadService,
            LoanForeclosureValidator loanForeclosureValidator, LoanTransactionMapper loanTransactionMapper,
            LoanTransactionProcessingService loanTransactionProcessingService, LoanBalanceService loanBalanceService,
            LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository,
            LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository,
            @Lazy InterestRefundServiceDelegate interestRefundServiceDelegate) {
        return new LoanReadPlatformServiceImpl(jdbcTemplate, context, loanRepositoryWrapper, applicationCurrencyRepository,
                loanProductReadPlatformService, clientReadPlatformService, groupReadPlatformService, loanDropdownReadPlatformService,
                fundReadPlatformService, chargeReadPlatformService, codeValueReadPlatformService, calendarReadPlatformService,
                staffReadPlatformService, paginationHelper, paymentTypeReadPlatformService, floatingRatesReadPlatformService,
                loanUtilService, configurationDomainService, accountDetailsReadPlatformService, columnValidator, sqlGenerator,
                delinquencyReadPlatformService, loanTransactionRepository, loanChargePaidByReadService, loanTransactionRelationReadService,
                loanForeclosureValidator, loanTransactionMapper, loanTransactionProcessingService, loanBalanceService,
                loanCapitalizedIncomeBalanceRepository, loanBuyDownFeeBalanceRepository, interestRefundServiceDelegate);
    }

    @Bean
    @ConditionalOnMissingBean(LoanStatusChangePlatformService.class)
    public LoanStatusChangePlatformService loanStatusChangePlatformService(BusinessEventNotifierService businessEventNotifierService,
            LoanAccrualActivityProcessingService loanAccrualActivityProcessingService) {
        return new LoanStatusChangePlatformServiceImpl(businessEventNotifierService, loanAccrualActivityProcessingService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAccrualEventService.class)
    public LoanAccrualEventService loanAccrualEventService(BusinessEventNotifierService businessEventNotifierService,
            LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanAccrualActivityProcessingService loanAccrualActivityProcessingService) {
        return new LoanAccrualEventService(businessEventNotifierService, loanAccrualsProcessingService,
                loanAccrualActivityProcessingService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanUtilService.class)
    public LoanUtilService loanUtilService(ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            CalendarInstanceRepository calendarInstanceRepository, ConfigurationDomainService configurationDomainService,
            HolidayRepository holidayRepository, WorkingDaysRepositoryWrapper workingDaysRepository,
            LoanScheduleGeneratorFactory loanScheduleFactory, FloatingRatesReadPlatformService floatingRatesReadPlatformService,
            CalendarReadPlatformService calendarReadPlatformService, NoteRepository noteRepository) {
        return new LoanUtilService(applicationCurrencyRepository, calendarInstanceRepository, configurationDomainService, holidayRepository,
                workingDaysRepository, loanScheduleFactory, floatingRatesReadPlatformService, calendarReadPlatformService, noteRepository);
    }

    @Bean
    @ConditionalOnMissingBean(BuyDownFeePlatformService.class)
    public BuyDownFeePlatformService buyDownFeePlatformService(ProgressiveLoanTransactionValidator loanTransactionValidator,
            LoanAssembler loanAssembler, LoanTransactionRepository loanTransactionRepository,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService, LoanJournalEntryPoster loanJournalEntryPoster,
            NoteWritePlatformService noteWritePlatformService, ExternalIdFactory externalIdFactory,
            LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository,
            ReprocessLoanTransactionsService reprocessLoanTransactionsService, LoanBalanceService loanBalanceService,
            LoanLifecycleStateMachine loanLifecycleStateMachine, BusinessEventNotifierService businessEventNotifierService,
            LoanTransactionRelationRepository loanTransactionRelationRepository) {
        return new BuyDownFeeWritePlatformServiceImpl(loanTransactionValidator, loanAssembler, loanTransactionRepository,
                paymentDetailWritePlatformService, loanJournalEntryPoster, noteWritePlatformService, externalIdFactory,
                loanBuyDownFeeBalanceRepository, reprocessLoanTransactionsService, loanBalanceService, loanLifecycleStateMachine,
                businessEventNotifierService, loanTransactionRelationRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanWritePlatformService.class)
    public LoanWritePlatformService loanWritePlatformService(PlatformSecurityContext context,
            LoanTransactionValidator loanTransactionValidator,
            LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer, LoanRepositoryWrapper loanRepositoryWrapper,
            LoanAccountDomainService loanAccountDomainService, NoteRepository noteRepository,
            LoanTransactionRepository loanTransactionRepository, LoanTransactionRelationRepository loanTransactionRelationRepository,
            LoanAssembler loanAssembler, JournalEntryWritePlatformService journalEntryWritePlatformService,
            CalendarInstanceRepository calendarInstanceRepository, PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            HolidayRepositoryWrapper holidayRepository, ConfigurationDomainService configurationDomainService,
            WorkingDaysRepositoryWrapper workingDaysRepository, AccountTransfersWritePlatformService accountTransfersWritePlatformService,
            AccountTransfersReadPlatformService accountTransfersReadPlatformService,
            AccountAssociationsReadPlatformService accountAssociationsReadPlatformService, LoanReadPlatformService loanReadPlatformService,
            FromJsonHelper fromApiJsonHelper, CalendarRepository calendarRepository,
            LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService,
            LoanApplicationValidator loanApplicationValidator, AccountAssociationsRepository accountAssociationRepository,
            AccountTransferDetailRepository accountTransferDetailRepository, BusinessEventNotifierService businessEventNotifierService,
            GuarantorDomainService guarantorDomainService, LoanUtilService loanUtilService,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService,
            CodeValueRepositoryWrapper codeValueRepository, CashierTransactionDataValidator cashierTransactionDataValidator,
            GLIMAccountInfoRepository glimRepository, LoanRepository loanRepository,
            RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler,
            PostDatedChecksRepository postDatedChecksRepository,
            LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository,
            LoanLifecycleStateMachine loanLifecycleStateMachine, LoanAccountLockService loanAccountLockService,
            ExternalIdFactory externalIdFactory, LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService,
            ErrorHandler errorHandler, LoanDownPaymentHandlerService loanDownPaymentHandlerService,
            LoanTransactionAssembler loanTransactionAssembler, LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanOfficerValidator loanOfficerValidator, LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator,
            LoanDisbursementService loanDisbursementService, LoanScheduleService loanScheduleService,
            LoanChargeValidator loanChargeValidator, LoanOfficerService loanOfficerService,
            ReprocessLoanTransactionsService reprocessLoanTransactionsService, LoanAccountService loanAccountService,
            LoanJournalEntryPoster journalEntryPoster, LoanAdjustmentService loanAdjustmentService,
            LoanAccountingBridgeMapper loanAccountingBridgeMapper, LoanMapper loanMapper,
            LoanTransactionProcessingService loanTransactionProcessingService, final LoanBalanceService loanBalanceService,
            LoanTransactionService loanTransactionService) {
        return new LoanWritePlatformServiceJpaRepositoryImpl(context, loanTransactionValidator, loanUpdateCommandFromApiJsonDeserializer,
                loanRepositoryWrapper, loanAccountDomainService, noteRepository, loanTransactionRepository,
                loanTransactionRelationRepository, loanAssembler, journalEntryWritePlatformService, calendarInstanceRepository,
                paymentDetailWritePlatformService, holidayRepository, configurationDomainService, workingDaysRepository,
                accountTransfersWritePlatformService, accountTransfersReadPlatformService, accountAssociationsReadPlatformService,
                loanReadPlatformService, fromApiJsonHelper, calendarRepository, loanScheduleHistoryWritePlatformService,
                loanApplicationValidator, accountAssociationRepository, accountTransferDetailRepository, businessEventNotifierService,
                guarantorDomainService, loanUtilService, entityDatatableChecksWritePlatformService, codeValueRepository,
                cashierTransactionDataValidator, glimRepository, loanRepository, repaymentWithPostDatedChecksAssembler,
                postDatedChecksRepository, loanRepaymentScheduleInstallmentRepository, loanLifecycleStateMachine, loanAccountLockService,
                externalIdFactory, loanAccrualTransactionBusinessEventService, errorHandler, loanDownPaymentHandlerService,
                loanTransactionAssembler, loanAccrualsProcessingService, loanOfficerValidator, loanDownPaymentTransactionValidator,
                loanDisbursementService, loanScheduleService, loanChargeValidator, loanOfficerService, reprocessLoanTransactionsService,
                loanAccountService, journalEntryPoster, loanAdjustmentService, loanAccountingBridgeMapper, loanMapper,
                loanTransactionProcessingService, loanBalanceService, loanTransactionService);
    }

    @Bean
    @ConditionalOnMissingBean(ReplayedTransactionBusinessEventService.class)
    public ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService(
            BusinessEventNotifierService businessEventNotifierService, LoanTransactionRepository loanTransactionRepository) {
        return new ReplayedTransactionBusinessEventServiceImpl(businessEventNotifierService, loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDownPaymentHandlerService.class)
    public LoanDownPaymentHandlerService loanDownPaymentHandlerService(LoanTransactionRepository loanTransactionRepository,
            BusinessEventNotifierService businessEventNotifierService,
            LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator, LoanScheduleService loanScheduleService,
            LoanRefundService loanRefundService, LoanRefundValidator loanRefundValidator,
            ReprocessLoanTransactionsService reprocessLoanTransactionsService,
            LoanTransactionProcessingService loanTransactionProcessingService, LoanLifecycleStateMachine loanLifecycleStateMachine,
            LoanBalanceService loanBalanceService, LoanTransactionService loanTransactionService) {
        return new LoanDownPaymentHandlerServiceImpl(loanTransactionRepository, businessEventNotifierService,
                loanDownPaymentTransactionValidator, loanScheduleService, loanRefundService, loanRefundValidator,
                reprocessLoanTransactionsService, loanTransactionProcessingService, loanLifecycleStateMachine, loanBalanceService,
                loanTransactionService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDisbursementDetailsAssembler.class)
    public LoanDisbursementDetailsAssembler loanDisbursementDetailsAssembler(FromJsonHelper fromApiJsonHelper) {
        return new LoanDisbursementDetailsAssembler(fromApiJsonHelper);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDisbursementService.class)
    public LoanDisbursementService loanDisbursementService(LoanChargeValidator loanChargeValidator,
            LoanDisbursementValidator loanDisbursementValidator, ReprocessLoanTransactionsService reprocessLoanTransactionsService,
            LoanChargeService loanChargeService, LoanBalanceService loanBalanceService) {
        return new LoanDisbursementService(loanChargeValidator, loanDisbursementValidator, reprocessLoanTransactionsService,
                loanChargeService, loanBalanceService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanChargeService.class)
    public LoanChargeService loanChargeService(final LoanChargeValidator loanChargeValidator,
            final LoanTransactionProcessingService loanTransactionProcessingService,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanBalanceService loanBalanceService,
            final LoanTransactionRepository loanTransactionRepository) {
        return new LoanChargeService(loanChargeValidator, loanTransactionProcessingService, loanLifecycleStateMachine, loanBalanceService,
                loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanScheduleService.class)
    public LoanScheduleService loanScheduleService(final LoanChargeService loanChargeService,
            final ReprocessLoanTransactionsService reprocessLoanTransactionsService, final LoanMapper loanMapper,
            final LoanTransactionProcessingService loanTransactionProcessingService, LoanScheduleComponent loanSchedule,
            final LoanTransactionRepository loanTransactionRepository) {
        return new LoanScheduleService(loanChargeService, reprocessLoanTransactionsService, loanMapper, loanTransactionProcessingService,
                loanSchedule, loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanOfficerService.class)
    public LoanOfficerService loanOfficerService(LoanOfficerValidator loanOfficerValidator) {
        return new LoanOfficerService(loanOfficerValidator);
    }

    @Bean
    @ConditionalOnMissingBean(LoanRefundService.class)
    public LoanRefundService loanRefundService(final LoanRefundValidator loanRefundValidator,
            final LoanTransactionProcessingService loanTransactionProcessingService,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransactionRepository loanTransactionRepository) {
        return new LoanRefundService(loanRefundValidator, loanTransactionProcessingService, loanLifecycleStateMachine,
                loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(InterestPauseReadPlatformService.class)
    public InterestPauseReadPlatformService interestPauseReadPlatformService(LoanTermVariationsRepository loanTermVariationsRepository) {
        return new InterestPauseReadPlatformServiceImpl(loanTermVariationsRepository);
    }

    @Bean
    @ConditionalOnMissingBean(InterestPauseWritePlatformService.class)
    public InterestPauseWritePlatformService interestPauseWritePlatformService(LoanTermVariationsRepository loanTermVariationsRepository,
            LoanRepositoryWrapper loanRepositoryWrapper, LoanAssembler loanAssembler,
            ReprocessLoanTransactionsService reprocessLoanTransactionsService) {
        return new InterestPauseWritePlatformServiceImpl(loanTermVariationsRepository, loanRepositoryWrapper, loanAssembler,
                reprocessLoanTransactionsService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAccountService.class)
    public LoanAccountService loanAccountService(LoanRepositoryWrapper loanRepositoryWrapper,
            LoanTransactionRepository loanTransactionRepository) {
        return new LoanAccountServiceImpl(loanRepositoryWrapper, loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCapitalizedIncomeAmortizationEventService.class)
    public LoanCapitalizedIncomeAmortizationEventService loanCapitalizedIncomeAmortizationEventService(
            BusinessEventNotifierService businessEventNotifierService,
            LoanCapitalizedIncomeAmortizationProcessingService loanCapitalizedIncomeAmortizationProcessingService) {
        return new LoanCapitalizedIncomeAmortizationEventService(businessEventNotifierService,
                loanCapitalizedIncomeAmortizationProcessingService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCapitalizedIncomeAmortizationProcessingService.class)
    public LoanCapitalizedIncomeAmortizationProcessingService loanCapitalizedIncomeAmortizationProcessingService(
            final ConfigurationDomainService configurationDomainService, final LoanTransactionRepository loanTransactionRepository,
            final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository,
            final BusinessEventNotifierService businessEventNotifierService, final LoanJournalEntryPoster journalEntryPoster,
            final ExternalIdFactory externalIdFactory) {
        return new LoanCapitalizedIncomeAmortizationProcessingServiceImpl(configurationDomainService, loanTransactionRepository,
                loanCapitalizedIncomeBalanceRepository, businessEventNotifierService, journalEntryPoster, externalIdFactory);
    }

    @Bean
    @ConditionalOnMissingBean(LoanBuyDownFeeAmortizationProcessingService.class)
    public LoanBuyDownFeeAmortizationProcessingService loanBuyDownFeeAmortizationProcessingService(
            final LoanTransactionRepository loanTransactionRepository,
            final LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository,
            final BusinessEventNotifierService businessEventNotifierService, final LoanJournalEntryPoster journalEntryPoster,
            final ExternalIdFactory externalIdFactory) {
        return new LoanBuyDownFeeAmortizationProcessingServiceImpl(loanTransactionRepository, loanBuyDownFeeBalanceRepository,
                businessEventNotifierService, journalEntryPoster, externalIdFactory);
    }

    @Bean
    @ConditionalOnMissingBean(LoanBuyDownFeeAmortizationEventService.class)
    public LoanBuyDownFeeAmortizationEventService loanBuyDownFeeAmortizationEventService(
            BusinessEventNotifierService businessEventNotifierService,
            LoanBuyDownFeeAmortizationProcessingService loanBuyDownFeeAmortizationProcessingService) {
        return new LoanBuyDownFeeAmortizationEventService(businessEventNotifierService, loanBuyDownFeeAmortizationProcessingService);
    }
}
