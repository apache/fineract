/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.domain.HolidayRepository;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.staff.domain.StaffRepository;
import org.mifosplatform.organisation.staff.exception.StaffNotFoundException;
import org.mifosplatform.organisation.staff.exception.StaffRoleException;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.portfolio.accountdetails.service.AccountEnumerations;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.exception.ClientNotActiveException;
import org.mifosplatform.portfolio.collateral.domain.LoanCollateral;
import org.mifosplatform.portfolio.collateral.service.CollateralAssembler;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.fund.domain.FundRepository;
import org.mifosplatform.portfolio.fund.exception.FundNotFoundException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.ClientNotInGroupException;
import org.mifosplatform.portfolio.group.exception.GroupNotActiveException;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;
import org.mifosplatform.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class LoanAssembler {

    private final LoanProductRepository loanProductRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final FundRepository fundRepository;
    private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;
    private final StaffRepository staffRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final LoanChargeAssembler loanChargeAssembler;
    private final CollateralAssembler loanCollateralAssembler;
    private final FromJsonHelper fromApiJsonHelper;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final HolidayRepository holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;

    @Autowired
    public LoanAssembler(final FromJsonHelper fromApiJsonHelper, final LoanProductRepository loanProductRepository,
            final ClientRepositoryWrapper clientRepository, final GroupRepository groupRepository, final FundRepository fundRepository,
            final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository,
            final StaffRepository staffRepository, final CodeValueRepositoryWrapper codeValueRepository,
            final LoanScheduleAssembler loanScheduleAssembler, final LoanChargeAssembler loanChargeAssembler,
            final CollateralAssembler loanCollateralAssembler, final LoanSummaryWrapper loanSummaryWrapper,
            final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            final HolidayRepository holidayRepository, final ConfigurationDomainService configurationDomainService,
            final WorkingDaysRepositoryWrapper workingDaysRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.loanProductRepository = loanProductRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.fundRepository = fundRepository;
        this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
        this.staffRepository = staffRepository;
        this.codeValueRepository = codeValueRepository;
        this.loanScheduleAssembler = loanScheduleAssembler;
        this.loanChargeAssembler = loanChargeAssembler;
        this.loanCollateralAssembler = loanCollateralAssembler;
        this.loanSummaryWrapper = loanSummaryWrapper;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
        this.holidayRepository = holidayRepository;
        this.configurationDomainService = configurationDomainService;
        this.workingDaysRepository = workingDaysRepository;
    }

    public Loan assembleFrom(final JsonCommand command, final AppUser currentUser) {
        final JsonElement element = command.parsedJson();

        final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long groupId = fromApiJsonHelper.extractLongNamed("groupId", element);

        return assembleApplication(element, clientId, groupId, currentUser);
    }

    private Loan assembleApplication(final JsonElement element, final Long clientId, final Long groupId, final AppUser currentUser) {

        final String accountNo = fromApiJsonHelper.extractStringNamed("accountNo", element);
        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        final Long fundId = fromApiJsonHelper.extractLongNamed("fundId", element);
        final Long loanOfficerId = fromApiJsonHelper.extractLongNamed("loanOfficerId", element);
        final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId", element);
        final Long loanPurposeId = fromApiJsonHelper.extractLongNamed("loanPurposeId", element);
        final Boolean syncDisbursementWithMeeting = fromApiJsonHelper.extractBooleanNamed("syncDisbursementWithMeeting", element);

        final LoanProduct loanProduct = this.loanProductRepository.findOne(productId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(productId); }

        final Fund fund = findFundByIdIfProvided(fundId);
        final Staff loanOfficer = findLoanOfficerByIdIfProvided(loanOfficerId);
        final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(transactionProcessingStrategyId);
        CodeValue loanPurpose = null;
        if (loanPurposeId != null) {
            loanPurpose = codeValueRepository.findOneWithNotFoundDetection(loanPurposeId);
        }
        final Set<LoanCollateral> collateral = this.loanCollateralAssembler.fromParsedJson(element);
        final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element);
        for (LoanCharge loanCharge : loanCharges) {
            if (!loanProduct.hasCurrencyCodeOf(loanCharge.currencyCode())) {
                final String errorMessage = "Charge and Loan must have the same currency.";
                throw new InvalidCurrencyException("loanCharge", "attach.to.loan", errorMessage);
            }
        }

        Loan loanApplication = null;
        Client client = null;
        Group group = null;

        final LoanProductRelatedDetail loanProductRelatedDetail = this.loanScheduleAssembler.assembleLoanProductRelatedDetail(element);

        final String loanTypeParameterName = "loanType";
        final String loanTypeStr = fromApiJsonHelper.extractStringNamed(loanTypeParameterName, element);
        final EnumOptionData loanType = AccountEnumerations.loanType(loanTypeStr);

        if (clientId != null) {
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            if (client.isNotActive()) {
                throw new ClientNotActiveException(clientId);
            }

            loanApplication = Loan.newIndividualLoanApplication(accountNo, client, loanType.getId().intValue(), loanProduct, fund,
                    loanOfficer, loanPurpose, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, collateral);
        }

        if (groupId != null) {
            group = this.groupRepository.findOne(groupId);
            if (group == null) { throw new GroupNotFoundException(groupId); }
            if (group.isNotActive()) {
                throw new GroupNotActiveException(groupId);
            }

            loanApplication = Loan.newGroupLoanApplication(accountNo, group, loanType.getId().intValue(), loanProduct, fund, loanOfficer,
                    loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, syncDisbursementWithMeeting);
        }

        if (client != null && group != null) {

            if (!group.hasClientAsMember(client)) { throw new ClientNotInGroupException(clientId, groupId); }

            loanApplication = Loan.newIndividualLoanApplicationFromGroup(accountNo, client, group, loanType.getId().intValue(),
                    loanProduct, fund, loanOfficer, loanTransactionProcessingStrategy, loanProductRelatedDetail, loanCharges, syncDisbursementWithMeeting);
        }

        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        final LocalDate submittedOnDate = fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);

        if (loanApplication == null) { throw new IllegalStateException("No loan application exists for either a client or group (or both)."); }
        loanApplication.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);

        final LoanApplicationTerms loanApplicationTerms = this.loanScheduleAssembler.assembleLoanTerms(element);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loanApplication.getOfficeId(), loanApplicationTerms.getExpectedDisbursementDate().toDate());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final LoanScheduleModel loanScheduleModel = this.loanScheduleAssembler.assembleLoanScheduleFrom(loanApplicationTerms, isHolidayEnabled, holidays, workingDays, element);
        loanApplication.loanApplicationSubmittal(currentUser, loanScheduleModel, loanApplicationTerms, defaultLoanLifecycleStateMachine(),
                submittedOnDate, externalId, allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay);

        return loanApplication;
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    public CodeValue findCodeValueByIdIfProvided(final Long codeValueId) {
        CodeValue codeValue = null;
        if (codeValueId != null) {
            codeValue = this.codeValueRepository.findOneWithNotFoundDetection(codeValueId);
        }
        return codeValue;
    }

    public Fund findFundByIdIfProvided(final Long fundId) {
        Fund fund = null;
        if (fundId != null) {
            fund = this.fundRepository.findOne(fundId);
            if (fund == null) { throw new FundNotFoundException(fundId); }
        }
        return fund;
    }

    public Staff findLoanOfficerByIdIfProvided(final Long loanOfficerId) {
        Staff staff = null;
        if (loanOfficerId != null) {
            staff = this.staffRepository.findOne(loanOfficerId);
            if (staff == null) {
                throw new StaffNotFoundException(loanOfficerId);
            } else if (staff.isNotLoanOfficer()) { throw new StaffRoleException(loanOfficerId, StaffRoleException.STAFF_ROLE.LOAN_OFFICER); }
        }
        return staff;
    }

    public LoanTransactionProcessingStrategy findStrategyByIdIfProvided(final Long transactionProcessingStrategyId) {
        LoanTransactionProcessingStrategy strategy = null;
        if (transactionProcessingStrategyId != null) {
            strategy = this.loanTransactionProcessingStrategyRepository.findOne(transactionProcessingStrategyId);
            if (strategy == null) { throw new LoanTransactionProcessingStrategyNotFoundException(transactionProcessingStrategyId); }
        }
        return strategy;
    }

    public void validateExpectedDisbursementForHolidayAndNonWorkingDay(final Loan loanApplication) {

        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loanApplication.getOfficeId(),
                loanApplication.getExpectedDisbursedOnLocalDate().toDate());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
        
        loanApplication.validateExpectedDisbursementForHolidayAndNonWorkingDay(workingDays, allowTransactionsOnHoliday, holidays,
                allowTransactionsOnNonWorkingDay);
    }
}