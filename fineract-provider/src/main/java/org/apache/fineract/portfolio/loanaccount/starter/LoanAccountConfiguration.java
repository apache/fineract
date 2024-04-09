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

import jakarta.persistence.EntityManager;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelationRepository;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingRepository;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.holiday.domain.HolidayRepository;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.organisation.teller.data.CashierTransactionDataValidator;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.service.ChargeDropdownReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.domain.AccountNumberGenerator;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralAssembler;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainServiceJpa;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetailsRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanTransactionRelationMapper;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationCommandFromApiJsonHelper;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationTransitionApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanEventApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanaccount.service.BulkLoansReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.BulkLoansReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoWritePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualTransactionBusinessEventService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualTransactionBusinessEventServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualWritePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanApplicationWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanApplicationWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanArrearsAgingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanArrearsAgingServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanCalculateRepaymentPastDueService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargePaidByReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargePaidByReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeWritePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanDownPaymentHandlerService;
import org.apache.fineract.portfolio.loanaccount.service.LoanDownPaymentHandlerServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanStatusChangePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanStatusChangePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.loanaccount.service.RecalculateInterestPoster;
import org.apache.fineract.portfolio.loanaccount.service.ReplayedTransactionBusinessEventService;
import org.apache.fineract.portfolio.loanaccount.service.ReplayedTransactionBusinessEventServiceImpl;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.rate.service.RateAssembler;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.service.GSIMReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
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
    public GLIMAccountInfoWritePlatformService glimAccountInfoWritePlatformService(PlatformSecurityContext context,
            GLIMAccountInfoRepository glimAccountRepository,

            LoanRepository loanRepository) {
        return new GLIMAccountInfoWritePlatformServiceImpl(context, glimAccountRepository, loanRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAccrualPlatformService.class)
    public LoanAccrualPlatformService loanAccrualPlatformService(LoanReadPlatformService loanReadPlatformService,
            LoanAccrualWritePlatformService loanAccrualWritePlatformService) {
        return new LoanAccrualPlatformServiceImpl(loanReadPlatformService, loanAccrualWritePlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAccrualTransactionBusinessEventService.class)
    public LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService(

            BusinessEventNotifierService businessEventNotifierService) {
        return new LoanAccrualTransactionBusinessEventServiceImpl(businessEventNotifierService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAccrualWritePlatformService.class)
    public LoanAccrualWritePlatformService loanAccrualWritePlatformService(LoanReadPlatformService loanReadPlatformService,
            LoanChargeReadPlatformService loanChargeReadPlatformService, JdbcTemplate jdbcTemplate,
            DatabaseSpecificSQLGenerator sqlGenerator, JournalEntryWritePlatformService journalEntryWritePlatformService,
            PlatformSecurityContext context, LoanRepositoryWrapper loanRepositoryWrapper, LoanRepository loanRepository,
            OfficeRepository officeRepository, BusinessEventNotifierService businessEventNotifierService,
            LoanTransactionRepository loanTransactionRepository,
            LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService,
            ConfigurationDomainService configurationDomainService, ExternalIdFactory externalIdFactory) {
        return new LoanAccrualWritePlatformServiceImpl(loanReadPlatformService, loanChargeReadPlatformService, jdbcTemplate, sqlGenerator,
                journalEntryWritePlatformService, context, loanRepositoryWrapper, loanRepository, officeRepository,
                businessEventNotifierService, loanTransactionRepository, loanAccrualTransactionBusinessEventService,
                configurationDomainService, externalIdFactory);
    }

    @Bean
    @ConditionalOnMissingBean(LoanApplicationWritePlatformService.class)
    public LoanApplicationWritePlatformService loanApplicationWritePlatformService(PlatformSecurityContext context,
            FromJsonHelper fromJsonHelper, LoanApplicationTransitionApiJsonValidator loanApplicationTransitionApiJsonValidator,
            LoanProductDataValidator loanProductCommandFromApiJsonDeserializer,
            LoanApplicationCommandFromApiJsonHelper fromApiJsonDeserializer, LoanRepositoryWrapper loanRepositoryWrapper,
            NoteRepository noteRepository, LoanScheduleCalculationPlatformService calculationPlatformService, LoanAssembler loanAssembler,
            ClientRepositoryWrapper clientRepository, LoanProductRepository loanProductRepository, LoanChargeAssembler loanChargeAssembler,
            LoanCollateralAssembler loanCollateralAssembler, AprCalculator aprCalculator, AccountNumberGenerator accountNumberGenerator,
            LoanSummaryWrapper loanSummaryWrapper, GroupRepositoryWrapper groupRepository,
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            CalendarRepository calendarRepository, CalendarInstanceRepository calendarInstanceRepository,
            SavingsAccountAssembler savingsAccountAssembler, AccountAssociationsRepository accountAssociationsRepository,
            LoanReadPlatformService loanReadPlatformService, AccountNumberFormatRepositoryWrapper accountNumberFormatRepository,
            BusinessEventNotifierService businessEventNotifierService, ConfigurationDomainService configurationDomainService,
            LoanScheduleAssembler loanScheduleAssembler, LoanUtilService loanUtilService,
            CalendarReadPlatformService calendarReadPlatformService,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService,
            GlobalConfigurationRepositoryWrapper globalConfigurationRepository,
            FineractEntityToEntityMappingRepository entityMappingRepository,
            FineractEntityRelationRepository fineractEntityRelationRepository,
            LoanProductReadPlatformService loanProductReadPlatformService,

            RateAssembler rateAssembler, GLIMAccountInfoWritePlatformService glimAccountInfoWritePlatformService,
            GLIMAccountInfoRepository glimRepository, LoanRepository loanRepository, GSIMReadPlatformService gsimReadPlatformService,
            LoanLifecycleStateMachine defaultLoanLifecycleStateMachine, LoanProductDataValidator loanProductDataValidator,
            EntityManager entityManager) {
        return new LoanApplicationWritePlatformServiceJpaRepositoryImpl(context, fromJsonHelper, loanApplicationTransitionApiJsonValidator,
                loanProductCommandFromApiJsonDeserializer, fromApiJsonDeserializer, loanRepositoryWrapper, noteRepository,
                calculationPlatformService, loanAssembler, clientRepository, loanProductRepository, loanChargeAssembler,
                loanCollateralAssembler, aprCalculator, accountNumberGenerator, loanSummaryWrapper, groupRepository,
                loanRepaymentScheduleTransactionProcessorFactory, calendarRepository, calendarInstanceRepository, savingsAccountAssembler,
                accountAssociationsRepository, loanReadPlatformService, accountNumberFormatRepository, businessEventNotifierService,
                configurationDomainService, loanScheduleAssembler, loanUtilService, calendarReadPlatformService,
                entityDatatableChecksWritePlatformService, globalConfigurationRepository, entityMappingRepository,
                fineractEntityRelationRepository, loanProductReadPlatformService, rateAssembler, glimAccountInfoWritePlatformService,
                glimRepository, loanRepository, gsimReadPlatformService, defaultLoanLifecycleStateMachine, loanProductDataValidator,
                entityManager);
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
            LoanProductRepository loanProductRepository, ClientRepositoryWrapper clientRepository, GroupRepository groupRepository,
            FundRepository fundRepository, StaffRepository staffRepository, CodeValueRepositoryWrapper codeValueRepository,
            LoanScheduleAssembler loanScheduleAssembler, LoanChargeAssembler loanChargeAssembler,
            LoanCollateralAssembler collateralAssembler, LoanSummaryWrapper loanSummaryWrapper,
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            HolidayRepository holidayRepository, ConfigurationDomainService configurationDomainService,
            WorkingDaysRepositoryWrapper workingDaysRepository, LoanUtilService loanUtilService, RateAssembler rateAssembler,
            LoanLifecycleStateMachine defaultLoanLifecycleStateMachine, ExternalIdFactory externalIdFactory) {
        return new LoanAssembler(fromApiJsonHelper, loanRepository, loanProductRepository, clientRepository, groupRepository,
                fundRepository, staffRepository, codeValueRepository, loanScheduleAssembler, loanChargeAssembler, collateralAssembler,
                loanSummaryWrapper, loanRepaymentScheduleTransactionProcessorFactory, holidayRepository, configurationDomainService,
                workingDaysRepository, loanUtilService, rateAssembler, defaultLoanLifecycleStateMachine, externalIdFactory);
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
    @ConditionalOnMissingBean(LoanChargePaidByReadPlatformService.class)
    public LoanChargePaidByReadPlatformService loanChargePaidByReadPlatformService(JdbcTemplate jdbcTemplate,
            PlatformSecurityContext context) {
        return new LoanChargePaidByReadPlatformServiceImpl(jdbcTemplate, context);
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
            LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService

    ) {
        return new LoanChargeWritePlatformServiceImpl(loanChargeApiJsonValidator, loanAssembler, chargeRepository,
                businessEventNotifierService, loanTransactionRepository, accountTransfersWritePlatformService, loanRepositoryWrapper,
                journalEntryWritePlatformService, loanAccountDomainService, loanChargeRepository, loanWritePlatformService, loanUtilService,
                loanChargeReadPlatformService, defaultLoanLifecycleStateMachine, accountAssociationsReadPlatformService, fromApiJsonHelper,
                configurationDomainService, loanRepaymentScheduleTransactionProcessorFactory, externalIdFactory,
                accountTransferDetailRepository, loanChargeAssembler, replayedTransactionBusinessEventService,
                paymentDetailWritePlatformService, noteRepository, loanAccrualTransactionBusinessEventService);
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
            LoanTransactionRelationRepository loanTransactionRelationRepository,
            LoanTransactionRelationMapper loanTransactionRelationMapper,
            LoanChargePaidByReadPlatformService loanChargePaidByReadPlatformService) {
        return new LoanReadPlatformServiceImpl(jdbcTemplate, context, loanRepositoryWrapper, applicationCurrencyRepository,
                loanProductReadPlatformService, clientReadPlatformService, groupReadPlatformService, loanDropdownReadPlatformService,
                fundReadPlatformService, chargeReadPlatformService, codeValueReadPlatformService, calendarReadPlatformService,
                staffReadPlatformService, paginationHelper, namedParameterJdbcTemplate, paymentTypeReadPlatformService,
                loanRepaymentScheduleTransactionProcessorFactory, floatingRatesReadPlatformService, loanUtilService,
                configurationDomainService, accountDetailsReadPlatformService, columnValidator, sqlGenerator,
                delinquencyReadPlatformService, loanTransactionRepository, loanTransactionRelationRepository, loanTransactionRelationMapper,
                loanChargePaidByReadPlatformService

        );
    }

    @Bean
    @ConditionalOnMissingBean(LoanStatusChangePlatformService.class)
    public LoanStatusChangePlatformService loanStatusChangePlatformService(BusinessEventNotifierService businessEventNotifierService,
            LoanAccountDomainServiceJpa loanAccountDomainService) {
        return new LoanStatusChangePlatformServiceImpl(businessEventNotifierService, loanAccountDomainService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanUtilService.class)
    public LoanUtilService loanUtilService(ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            CalendarInstanceRepository calendarInstanceRepository, ConfigurationDomainService configurationDomainService,
            HolidayRepository holidayRepository, WorkingDaysRepositoryWrapper workingDaysRepository,
            LoanScheduleGeneratorFactory loanScheduleFactory, FloatingRatesReadPlatformService floatingRatesReadPlatformService,
            FromJsonHelper fromApiJsonHelper, CalendarReadPlatformService calendarReadPlatformService) {
        return new LoanUtilService(applicationCurrencyRepository, calendarInstanceRepository, configurationDomainService, holidayRepository,
                workingDaysRepository, loanScheduleFactory, floatingRatesReadPlatformService, fromApiJsonHelper,
                calendarReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanWritePlatformService.class)
    public LoanWritePlatformService loanWritePlatformService(PlatformSecurityContext context,
            LoanEventApiJsonValidator loanEventApiJsonValidator,
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
            LoanApplicationCommandFromApiJsonHelper loanApplicationCommandFromApiJsonHelper,
            AccountAssociationsRepository accountAssociationRepository, AccountTransferDetailRepository accountTransferDetailRepository,
            BusinessEventNotifierService businessEventNotifierService, GuarantorDomainService guarantorDomainService,
            LoanUtilService loanUtilService, LoanSummaryWrapper loanSummaryWrapper,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService,
            LoanRepaymentScheduleTransactionProcessorFactory transactionProcessingStrategy, CodeValueRepositoryWrapper codeValueRepository,
            CashierTransactionDataValidator cashierTransactionDataValidator, GLIMAccountInfoRepository glimRepository,
            LoanRepository loanRepository, RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler,
            PostDatedChecksRepository postDatedChecksRepository, LoanDisbursementDetailsRepository loanDisbursementDetailsRepository,
            LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository,
            LoanLifecycleStateMachine defaultLoanLifecycleStateMachine, LoanAccountLockService loanAccountLockService,
            ExternalIdFactory externalIdFactory, ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService,
            LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService, ErrorHandler errorHandler,
            LoanDownPaymentHandlerService loanDownPaymentHandlerService, EntityManager entityManager) {
        return new LoanWritePlatformServiceJpaRepositoryImpl(context, loanEventApiJsonValidator, loanUpdateCommandFromApiJsonDeserializer,
                loanRepositoryWrapper, loanAccountDomainService, noteRepository, loanTransactionRepository,
                loanTransactionRelationRepository, loanAssembler, journalEntryWritePlatformService, calendarInstanceRepository,
                paymentDetailWritePlatformService, holidayRepository, configurationDomainService, workingDaysRepository,
                accountTransfersWritePlatformService, accountTransfersReadPlatformService, accountAssociationsReadPlatformService,
                loanReadPlatformService, fromApiJsonHelper, calendarRepository, loanScheduleHistoryWritePlatformService,
                loanApplicationCommandFromApiJsonHelper, accountAssociationRepository, accountTransferDetailRepository,
                businessEventNotifierService, guarantorDomainService, loanUtilService, loanSummaryWrapper,
                entityDatatableChecksWritePlatformService, transactionProcessingStrategy, codeValueRepository,
                cashierTransactionDataValidator, glimRepository, loanRepository, repaymentWithPostDatedChecksAssembler,
                postDatedChecksRepository, loanDisbursementDetailsRepository, loanRepaymentScheduleInstallmentRepository,
                defaultLoanLifecycleStateMachine, loanAccountLockService, externalIdFactory, replayedTransactionBusinessEventService,
                loanAccrualTransactionBusinessEventService, errorHandler, loanDownPaymentHandlerService, entityManager);
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean(RecalculateInterestPoster.class)
    public RecalculateInterestPoster recalculateInterestPoster() {
        return new RecalculateInterestPoster();
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
            BusinessEventNotifierService businessEventNotifierService) {
        return new LoanDownPaymentHandlerServiceImpl(loanTransactionRepository, businessEventNotifierService);
    }
}
