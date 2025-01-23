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
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
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
import org.apache.fineract.portfolio.loanaccount.mapper.LoanChargeMapper;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanCollateralManagementMapper;
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
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoWritePlatformServiceImpl;
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
import org.apache.fineract.portfolio.loanaccount.service.LoanCalculateRepaymentPastDueService;
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
import org.apache.fineract.portfolio.loanaccount.service.LoanOfficerService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanRefundService;
import org.apache.fineract.portfolio.loanaccount.service.LoanScheduleService;
import org.apache.fineract.portfolio.loanaccount.service.LoanStatusChangePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanStatusChangePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionRelationReadService;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.loanaccount.service.ReplayedTransactionBusinessEventService;
import org.apache.fineract.portfolio.loanaccount.service.ReplayedTransactionBusinessEventServiceImpl;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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

            BusinessEventNotifierService businessEventNotifierService) {
        return new LoanAccrualTransactionBusinessEventServiceImpl(businessEventNotifierService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanApplicationWritePlatformService.class)
    public LoanApplicationWritePlatformService loanApplicationWritePlatformService(PlatformSecurityContext context,
            FromJsonHelper fromJsonHelper, LoanApplicationTransitionValidator loanApplicationTransitionValidator,
            LoanApplicationValidator loanApplicationValidator, LoanRepositoryWrapper loanRepositoryWrapper, NoteRepository noteRepository,
            LoanAssembler loanAssembler, LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            CalendarRepository calendarRepository, CalendarInstanceRepository calendarInstanceRepository,
            SavingsAccountRepositoryWrapper savingsAccountRepository, AccountAssociationsRepository accountAssociationsRepository,
            LoanReadPlatformService loanReadPlatformService, BusinessEventNotifierService businessEventNotifierService,
            ConfigurationDomainService configurationDomainService, LoanScheduleAssembler loanScheduleAssembler,
            LoanUtilService loanUtilService, CalendarReadPlatformService calendarReadPlatformService,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService, GLIMAccountInfoRepository glimRepository,
            LoanRepository loanRepository, GSIMReadPlatformService gsimReadPlatformService,
            LoanLifecycleStateMachine defaultLoanLifecycleStateMachine, LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator, LoanScheduleService loanScheduleService) {
        return new LoanApplicationWritePlatformServiceJpaRepositoryImpl(context, loanApplicationTransitionValidator,
                loanApplicationValidator, loanRepositoryWrapper, noteRepository, loanAssembler,
                loanRepaymentScheduleTransactionProcessorFactory, calendarRepository, calendarInstanceRepository, savingsAccountRepository,
                accountAssociationsRepository, businessEventNotifierService, loanScheduleAssembler, loanUtilService,
                calendarReadPlatformService, entityDatatableChecksWritePlatformService, glimRepository, loanRepository,
                gsimReadPlatformService, defaultLoanLifecycleStateMachine, loanAccrualsProcessingService,
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
            WorkingDaysRepositoryWrapper workingDaysRepository, RateAssembler rateAssembler,
            LoanLifecycleStateMachine defaultLoanLifecycleStateMachine, ExternalIdFactory externalIdFactory,
            AccountNumberFormatRepositoryWrapper accountNumberFormatRepository, GLIMAccountInfoRepository glimRepository,
            AccountNumberGenerator accountNumberGenerator, GLIMAccountInfoWritePlatformService glimAccountInfoWritePlatformService,
            LoanCollateralAssembler loanCollateralAssembler, LoanScheduleCalculationPlatformService calculationPlatformService,
            LoanDisbursementDetailsAssembler loanDisbursementDetailsAssembler, LoanChargeMapper loanChargeMapper,
            LoanCollateralManagementMapper loanCollateralManagementMapper, LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanDisbursementService loanDisbursementService, LoanChargeService loanChargeService, LoanOfficerService loanOfficerService) {
        return new LoanAssemblerImpl(fromApiJsonHelper, loanRepository, loanProductRepository, clientRepository, groupRepository,
                fundRepository, staffRepository, codeValueRepository, loanScheduleAssembler, loanChargeAssembler, collateralAssembler,
                loanRepaymentScheduleTransactionProcessorFactory, holidayRepository, configurationDomainService, workingDaysRepository,
                rateAssembler, defaultLoanLifecycleStateMachine, externalIdFactory, accountNumberFormatRepository, glimRepository,
                accountNumberGenerator, glimAccountInfoWritePlatformService, loanCollateralAssembler, calculationPlatformService,
                loanDisbursementDetailsAssembler, loanChargeMapper, loanCollateralManagementMapper, loanAccrualsProcessingService,
                loanDisbursementService, loanChargeService, loanOfficerService);
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
    public LoanChargeAssembler loanChargeAssembler(

            FromJsonHelper fromApiJsonHelper, ChargeRepositoryWrapper chargeRepository, LoanChargeRepository loanChargeRepository,
            LoanProductRepository loanProductRepository, ExternalIdFactory externalIdFactory) {
        return new LoanChargeAssembler(fromApiJsonHelper, chargeRepository, loanChargeRepository, loanProductRepository, externalIdFactory);
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
            LoanChargeReadPlatformService loanChargeReadPlatformService, LoanLifecycleStateMachine defaultLoanLifecycleStateMachine,
            AccountAssociationsReadPlatformService accountAssociationsReadPlatformService, FromJsonHelper fromApiJsonHelper,
            ConfigurationDomainService configurationDomainService,
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            ExternalIdFactory externalIdFactory, AccountTransferDetailRepository accountTransferDetailRepository,
            LoanChargeAssembler loanChargeAssembler, ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService, NoteRepository noteRepository,
            LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService,
            LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator, LoanChargeValidator loanChargeValidator,
            LoanScheduleService loanScheduleService) {
        return new LoanChargeWritePlatformServiceImpl(loanChargeApiJsonValidator, loanAssembler, chargeRepository,
                businessEventNotifierService, loanTransactionRepository, accountTransfersWritePlatformService, loanRepositoryWrapper,
                journalEntryWritePlatformService, loanAccountDomainService, loanChargeRepository, loanWritePlatformService, loanUtilService,
                loanChargeReadPlatformService, defaultLoanLifecycleStateMachine, accountAssociationsReadPlatformService, fromApiJsonHelper,
                configurationDomainService, loanRepaymentScheduleTransactionProcessorFactory, externalIdFactory,
                accountTransferDetailRepository, loanChargeAssembler, replayedTransactionBusinessEventService,
                paymentDetailWritePlatformService, noteRepository, loanAccrualTransactionBusinessEventService,
                loanAccrualsProcessingService, loanDownPaymentTransactionValidator, loanChargeValidator, loanScheduleService);
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
            NamedParameterJdbcTemplate namedParameterJdbcTemplate, PaymentTypeReadPlatformService paymentTypeReadPlatformService,
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            FloatingRatesReadPlatformService floatingRatesReadPlatformService, LoanUtilService loanUtilService,
            ConfigurationDomainService configurationDomainService, AccountDetailsReadPlatformService accountDetailsReadPlatformService,
            ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator,
            DelinquencyReadPlatformService delinquencyReadPlatformService, LoanTransactionRepository loanTransactionRepository,
            LoanChargePaidByReadService loanChargePaidByReadService, LoanTransactionRelationReadService loanTransactionRelationReadService,
            LoanForeclosureValidator loanForeclosureValidator) {
        return new LoanReadPlatformServiceImpl(jdbcTemplate, context, loanRepositoryWrapper, applicationCurrencyRepository,
                loanProductReadPlatformService, clientReadPlatformService, groupReadPlatformService, loanDropdownReadPlatformService,
                fundReadPlatformService, chargeReadPlatformService, codeValueReadPlatformService, calendarReadPlatformService,
                staffReadPlatformService, paginationHelper, namedParameterJdbcTemplate, paymentTypeReadPlatformService,
                loanRepaymentScheduleTransactionProcessorFactory, floatingRatesReadPlatformService, loanUtilService,
                configurationDomainService, accountDetailsReadPlatformService, columnValidator, sqlGenerator,
                delinquencyReadPlatformService, loanTransactionRepository, loanChargePaidByReadService, loanTransactionRelationReadService,
                loanForeclosureValidator);
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
            CalendarReadPlatformService calendarReadPlatformService) {
        return new LoanUtilService(applicationCurrencyRepository, calendarInstanceRepository, configurationDomainService, holidayRepository,
                workingDaysRepository, loanScheduleFactory, floatingRatesReadPlatformService, calendarReadPlatformService);
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
            LoanRepaymentScheduleTransactionProcessorFactory transactionProcessingStrategy, CodeValueRepositoryWrapper codeValueRepository,
            CashierTransactionDataValidator cashierTransactionDataValidator, GLIMAccountInfoRepository glimRepository,
            LoanRepository loanRepository, RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler,
            PostDatedChecksRepository postDatedChecksRepository,
            LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository,
            LoanLifecycleStateMachine defaultLoanLifecycleStateMachine, LoanAccountLockService loanAccountLockService,
            ExternalIdFactory externalIdFactory, ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService,
            LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService, ErrorHandler errorHandler,
            LoanDownPaymentHandlerService loanDownPaymentHandlerService, AccountTransferRepository accountTransferRepository,
            LoanTransactionAssembler loanTransactionAssembler, LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanOfficerValidator loanOfficerValidator, LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator,
            LoanDisbursementService loanDisbursementService, LoanScheduleService loanScheduleService,
            LoanChargeValidator loanChargeValidator, LoanOfficerService loanOfficerService) {
        return new LoanWritePlatformServiceJpaRepositoryImpl(context, loanTransactionValidator, loanUpdateCommandFromApiJsonDeserializer,
                loanRepositoryWrapper, loanAccountDomainService, noteRepository, loanTransactionRepository,
                loanTransactionRelationRepository, loanAssembler, journalEntryWritePlatformService, calendarInstanceRepository,
                paymentDetailWritePlatformService, holidayRepository, configurationDomainService, workingDaysRepository,
                accountTransfersWritePlatformService, accountTransfersReadPlatformService, accountAssociationsReadPlatformService,
                loanReadPlatformService, fromApiJsonHelper, calendarRepository, loanScheduleHistoryWritePlatformService,
                loanApplicationValidator, accountAssociationRepository, accountTransferDetailRepository, businessEventNotifierService,
                guarantorDomainService, loanUtilService, entityDatatableChecksWritePlatformService, transactionProcessingStrategy,
                codeValueRepository, cashierTransactionDataValidator, glimRepository, loanRepository, repaymentWithPostDatedChecksAssembler,
                postDatedChecksRepository, loanRepaymentScheduleInstallmentRepository, defaultLoanLifecycleStateMachine,
                loanAccountLockService, externalIdFactory, replayedTransactionBusinessEventService,
                loanAccrualTransactionBusinessEventService, errorHandler, loanDownPaymentHandlerService, accountTransferRepository,
                loanTransactionAssembler, loanAccrualsProcessingService, loanOfficerValidator, loanDownPaymentTransactionValidator,
                loanDisbursementService, loanScheduleService, loanChargeValidator, loanOfficerService);
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
            LoanRefundService loanRefundService, LoanRefundValidator loanRefundValidator) {
        return new LoanDownPaymentHandlerServiceImpl(loanTransactionRepository, businessEventNotifierService,
                loanDownPaymentTransactionValidator, loanScheduleService, loanRefundService, loanRefundValidator);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDisbursementDetailsAssembler.class)
    public LoanDisbursementDetailsAssembler loanDisbursementDetailsAssembler(FromJsonHelper fromApiJsonHelper) {
        return new LoanDisbursementDetailsAssembler(fromApiJsonHelper);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDisbursementService.class)
    public LoanDisbursementService loanDisbursementService(LoanChargeValidator loanChargeValidator,
            LoanDisbursementValidator loanDisbursementValidator) {
        return new LoanDisbursementService(loanChargeValidator, loanDisbursementValidator);
    }

    @Bean
    @ConditionalOnMissingBean(LoanChargeService.class)
    public LoanChargeService loanChargeService(LoanChargeValidator loanChargeValidator) {
        return new LoanChargeService(loanChargeValidator);
    }

    @Bean
    @ConditionalOnMissingBean(LoanScheduleService.class)
    public LoanScheduleService loanScheduleService(LoanChargeService loanChargeService) {
        return new LoanScheduleService(loanChargeService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanOfficerService.class)
    public LoanOfficerService loanOfficerService(LoanOfficerValidator loanOfficerValidator) {
        return new LoanOfficerService(loanOfficerValidator);
    }

    @Bean
    @ConditionalOnMissingBean(LoanRefundService.class)
    public LoanRefundService loanRefundService(LoanRefundValidator loanRefundValidator) {
        return new LoanRefundService(loanRefundValidator);
    }

    @Bean
    @ConditionalOnMissingBean(InterestPauseReadPlatformService.class)
    public InterestPauseReadPlatformService interestPauseReadPlatformService(LoanTermVariationsRepository loanTermVariationsRepository) {
        return new InterestPauseReadPlatformServiceImpl(loanTermVariationsRepository);
    }

    @Bean
    @ConditionalOnMissingBean(InterestPauseWritePlatformService.class)
    public InterestPauseWritePlatformService interestPauseWritePlatformService(LoanTermVariationsRepository loanTermVariationsRepository,
            LoanRepositoryWrapper loanRepositoryWrapper, LoanAssembler loanAssembler) {
        return new InterestPauseWritePlatformServiceImpl(loanTermVariationsRepository, loanRepositoryWrapper, loanAssembler);
    }
}
