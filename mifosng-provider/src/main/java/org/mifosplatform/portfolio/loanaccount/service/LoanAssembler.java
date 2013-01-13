package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.staff.domain.StaffRepository;
import org.mifosplatform.organisation.staff.exception.StaffNotFoundException;
import org.mifosplatform.organisation.staff.exception.StaffRoleException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.fund.domain.FundRepository;
import org.mifosplatform.portfolio.fund.exception.FundNotFoundException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.ClientNotInGroupException;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanSchedule;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class LoanAssembler {

    private final LoanProductRepository loanProductRepository;
    private final ClientRepository clientRepository;
    private final GroupRepository groupRepository;
    private final FundRepository fundRepository;
    private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;
    private final StaffRepository staffRepository;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanAssembler(final FromJsonHelper fromApiJsonHelper, final LoanProductRepository loanProductRepository,
            final ClientRepository clientRepository, final GroupRepository groupRepository, final FundRepository fundRepository,
            final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository,
            final StaffRepository staffRepository, final LoanScheduleAssembler loanScheduleAssembler,
            final LoanChargeAssembler loanChargeAssembler) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.loanProductRepository = loanProductRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.fundRepository = fundRepository;
        this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
        this.staffRepository = staffRepository;
        this.loanScheduleAssembler = loanScheduleAssembler;
        this.loanChargeAssembler = loanChargeAssembler;
    }

    public Loan assembleFrom(final JsonCommand command) {
        final JsonElement element = command.parsedJson();

        final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long groupId = fromApiJsonHelper.extractLongNamed("groupId", element);

        return assembleApplication(element, clientId, groupId);
    }

    private Loan assembleApplication(final JsonElement element, final Long clientId, final Long groupId) {

        final String accountNo = fromApiJsonHelper.extractStringNamed("accountNo", element);
        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        final Long fundId = fromApiJsonHelper.extractLongNamed("fundId", element);
        final Long loanOfficerId = fromApiJsonHelper.extractLongNamed("loanOfficerId", element);
        final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId", element);

        final LoanProduct loanProduct = this.loanProductRepository.findOne(productId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(productId); }

        final Fund fund = findFundByIdIfProvided(fundId);
        final Staff loanOfficer = findLoanOfficerByIdIfProvided(loanOfficerId);
        final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(transactionProcessingStrategyId);

        final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element);

        final BigDecimal inArrearsTolerance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
        final LoanSchedule loanSchedule = this.loanScheduleAssembler.fromJson(element, inArrearsTolerance);

        Loan loanApplication = null;
        Client client = null;
        Group group = null;
        if (clientId != null) {
            client = this.clientRepository.findOne(clientId);
            if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

            loanApplication = Loan.newIndividualLoanApplication(accountNo, client, loanProduct, fund, loanOfficer, loanTransactionProcessingStrategy,
                    loanSchedule, loanCharges);
        }

        if (groupId != null) {
            group = this.groupRepository.findOne(groupId);
            if (group == null || group.isDeleted()) { throw new GroupNotFoundException(groupId); }

            loanApplication = Loan.newGroupLoanApplication(accountNo, group, loanProduct, fund, loanOfficer, loanTransactionProcessingStrategy,
                    loanSchedule, loanCharges);
        }

        if (client != null && group != null){
            
            if (!group.hasClientAsMember(client)) { throw new ClientNotInGroupException(clientId, groupId); }
            
            loanApplication = Loan.newIndividualLoanApplicationFromGroup(accountNo, client, group, loanProduct, fund, loanOfficer, loanTransactionProcessingStrategy,
                    loanSchedule, loanCharges);
        }

        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        final LocalDate submittedOnDate = fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);

        if (loanApplication == null) { throw new IllegalStateException("No loan application exists for either a client or group (or both)."); }

        loanApplication.loanApplicationSubmittal(loanSchedule, defaultLoanLifecycleStateMachine(), submittedOnDate, externalId);

        return loanApplication;
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
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
}