/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.transfer.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepositoryWrapper;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.staff.domain.StaffRepositoryWrapper;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.mifosplatform.portfolio.calendar.domain.CalendarType;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.domain.ClientStatus;
import org.mifosplatform.portfolio.client.exception.ClientHasBeenClosedException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepositoryWrapper;
import org.mifosplatform.portfolio.group.exception.ClientNotInGroupException;
import org.mifosplatform.portfolio.group.exception.GroupNotActiveException;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.service.LoanWritePlatformService;
import org.mifosplatform.portfolio.note.service.NoteWritePlatformService;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountRepository;
import org.mifosplatform.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.mifosplatform.portfolio.transfer.api.TransferApiConstants;
import org.mifosplatform.portfolio.transfer.data.TransfersDataValidator;
import org.mifosplatform.portfolio.transfer.exception.ClientNotAwaitingTransferApprovalException;
import org.mifosplatform.portfolio.transfer.exception.ClientNotAwaitingTransferApprovalOrOnHoldException;
import org.mifosplatform.portfolio.transfer.exception.TransferNotSupportedException;
import org.mifosplatform.portfolio.transfer.exception.TransferNotSupportedException.TRANSFER_NOT_SUPPORTED_REASON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class TransferWritePlatformServiceJpaRepositoryImpl implements TransferWritePlatformService {

    private final ClientRepositoryWrapper clientRepository;
    private final OfficeRepositoryWrapper officeRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final LoanWritePlatformService loanWritePlatformService;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final LoanRepository loanRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final TransfersDataValidator transfersDataValidator;
    private final NoteWritePlatformService noteWritePlatformService;
    private final StaffRepositoryWrapper staffRepositoryWrapper;

    @Autowired
    public TransferWritePlatformServiceJpaRepositoryImpl(final ClientRepositoryWrapper clientRepository,
            final OfficeRepositoryWrapper officeRepository, final CalendarInstanceRepository calendarInstanceRepository,
            final LoanWritePlatformService loanWritePlatformService, final GroupRepositoryWrapper groupRepository,
            final LoanRepository loanRepository, final TransfersDataValidator transfersDataValidator,
            final NoteWritePlatformService noteWritePlatformService, final StaffRepositoryWrapper staffRepositoryWrapper,
            final SavingsAccountRepository savingsAccountRepository,
            final SavingsAccountWritePlatformService savingsAccountWritePlatformService) {
        this.clientRepository = clientRepository;
        this.officeRepository = officeRepository;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.loanWritePlatformService = loanWritePlatformService;
        this.groupRepository = groupRepository;
        this.loanRepository = loanRepository;
        this.transfersDataValidator = transfersDataValidator;
        this.noteWritePlatformService = noteWritePlatformService;
        this.staffRepositoryWrapper = staffRepositoryWrapper;
        this.savingsAccountRepository = savingsAccountRepository;
        this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
    }

    @Override
    @Transactional
    public CommandProcessingResult transferClientsBetweenGroups(final Long sourceGroupId, final JsonCommand jsonCommand) {
        this.transfersDataValidator.validateForClientsTransferBetweenGroups(jsonCommand.json());

        final Group sourceGroup = this.groupRepository.findOneWithNotFoundDetection(sourceGroupId);
        final Long destinationGroupId = jsonCommand.longValueOfParameterNamed(TransferApiConstants.destinationGroupIdParamName);
        final Group destinationGroup = this.groupRepository.findOneWithNotFoundDetection(destinationGroupId);
        final Long staffId = jsonCommand.longValueOfParameterNamed(TransferApiConstants.newStaffIdParamName);
        final Boolean inheritDestinationGroupLoanOfficer = jsonCommand
                .booleanObjectValueOfParameterNamed(TransferApiConstants.inheritDestinationGroupLoanOfficer);
        Staff staff = null;
        final Office sourceOffice = sourceGroup.getOffice();
        if (staffId != null) {
            staff = this.staffRepositoryWrapper.findByOfficeHierarchyWithNotFoundDetection(staffId, sourceOffice.getHierarchy());
        }

        final List<Client> clients = assembleListOfClients(jsonCommand);

        if (sourceGroupId == destinationGroupId) { throw new TransferNotSupportedException(
                TRANSFER_NOT_SUPPORTED_REASON.SOURCE_AND_DESTINATION_GROUP_CANNOT_BE_SAME, sourceGroupId, destinationGroupId); }

        /*** Do not allow bulk client transfers across branches ***/
        if (!(sourceOffice.getId() == destinationGroup.getOffice().getId())) { throw new TransferNotSupportedException(
                TRANSFER_NOT_SUPPORTED_REASON.BULK_CLIENT_TRANSFER_ACROSS_BRANCHES, sourceGroupId, destinationGroupId); }

        for (final Client client : clients) {
            transferClientBetweenGroups(sourceGroup, client, destinationGroup, inheritDestinationGroupLoanOfficer, staff);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(sourceGroupId) //
                .build();
    }

    /****
     * Variables that would make sense <br/>
     * <ul>
     * <li>inheritDestinationGroupLoanOfficer: Default true</li>
     * <li>newStaffId: Optional field with Id of new Loan Officer to be linked
     * to this client and all his JLG loans for this group</li>
     * ***/
    @Transactional
    public void transferClientBetweenGroups(final Group sourceGroup, final Client client, final Group destinationGroup,
            final Boolean inheritDestinationGroupLoanOfficer, final Staff newLoanOfficer) {

        // next I shall validate that the client is present in this group
        if (!sourceGroup.hasClientAsMember(client)) { throw new ClientNotInGroupException(client.getId(), sourceGroup.getId()); }
        // Is client active?
        if (client.isNotActive()) { throw new ClientHasBeenClosedException(client.getId()); }

        /**
         * TODO: for now we need to ensure that only one collection sheet
         * calendar can be linked with a center or group entity <br/>
         **/
        final CalendarInstance sourceGroupCalendarInstance = this.calendarInstanceRepository
                .findByEntityIdAndEntityTypeIdAndCalendarTypeId(sourceGroup.getId(), CalendarEntityType.GROUPS.getValue(),
                        CalendarType.COLLECTION.getValue());
        // get all customer loans synced with this group calendar Instance
        final List<CalendarInstance> activeLoanCalendarInstances = this.calendarInstanceRepository
                .findCalendarInstancesForActiveLoansByGroupIdAndClientId(sourceGroup.getId(), client.getId());

        /**
         * if a calendar is present in the source group along with loans synced
         * with it, we should ensure that the destination group also has a
         * collection calendar
         **/
        if (sourceGroupCalendarInstance != null && !activeLoanCalendarInstances.isEmpty()) {
            // get the destination calendar
            final CalendarInstance destinationGroupCalendarInstance = this.calendarInstanceRepository
                    .findByEntityIdAndEntityTypeIdAndCalendarTypeId(destinationGroup.getId(), CalendarEntityType.GROUPS.getValue(),
                            CalendarType.COLLECTION.getValue());

            if (destinationGroupCalendarInstance == null) { throw new TransferNotSupportedException(
                    TRANSFER_NOT_SUPPORTED_REASON.DESTINATION_GROUP_HAS_NO_MEETING, destinationGroup.getId());

            }
            final Calendar sourceGroupCalendar = sourceGroupCalendarInstance.getCalendar();
            final Calendar destinationGroupCalendar = destinationGroupCalendarInstance.getCalendar();

            /***
             * Ensure that the recurrence pattern are same for collection
             * meeting in both the source and the destination calendar
             ***/
            if (!(CalendarUtils.isFrequencySame(sourceGroupCalendar.getRecurrence(), destinationGroupCalendar.getRecurrence()) && CalendarUtils
                    .isIntervalSame(sourceGroupCalendar.getRecurrence(), destinationGroupCalendar.getRecurrence()))) { throw new TransferNotSupportedException(
                    TRANSFER_NOT_SUPPORTED_REASON.DESTINATION_GROUP_MEETING_FREQUENCY_MISMATCH, sourceGroup.getId(),
                    destinationGroup.getId()); }

            /** map all JLG loans for this client to the destinationGroup **/
            for (final CalendarInstance calendarInstance : activeLoanCalendarInstances) {
                calendarInstance.updateCalendar(destinationGroupCalendar);
                this.calendarInstanceRepository.save(calendarInstance);
            }
            // reschedule all JLG Loans to follow new Calendar
            this.loanWritePlatformService.applyMeetingDateChanges(destinationGroupCalendar, activeLoanCalendarInstances);
        }

        /**
         * Now Change the loan officer for this client and all his active JLG
         * loans
         **/
        final Staff destinationGroupLoanOfficer = destinationGroup.getStaff();

        /** In case of a loan officer transfer, set the new loan officer value **/
        if (sourceGroup.getId().equals(destinationGroup.getId()) && newLoanOfficer != null) {
            client.updateStaff(newLoanOfficer);
        }/*** Else default to destination group Officer (If present) ***/
        else if (destinationGroupLoanOfficer != null) {
            client.updateStaff(destinationGroupLoanOfficer);
        }

        client.getGroups().add(destinationGroup);
        this.clientRepository.saveAndFlush(client);

        /**
         * Active JLG loans are now linked to the new Group and Loan officer
         **/
        final List<Loan> allClientJLGLoans = this.loanRepository.findByClientIdAndGroupId(client.getId(), sourceGroup.getId());
        for (final Loan loan : allClientJLGLoans) {
            if (loan.status().isActiveOrAwaitingApprovalOrDisbursal()) {
                loan.updateGroup(destinationGroup);
                if (inheritDestinationGroupLoanOfficer != null && inheritDestinationGroupLoanOfficer == true
                        && destinationGroupLoanOfficer != null) {
                    loan.reassignLoanOfficer(destinationGroupLoanOfficer, DateUtils.getLocalDateOfTenant());
                } else if (newLoanOfficer != null) {
                    loan.reassignLoanOfficer(newLoanOfficer, DateUtils.getLocalDateOfTenant());
                }
                this.loanRepository.saveAndFlush(loan);
            }
        }

        /**
         * change client group membership (only if source group and destination
         * group are not the same, i.e only Loan officer Transfer)
         **/
        if (!sourceGroup.getId().equals(destinationGroup.getId())) {
            client.getGroups().remove(sourceGroup);
        }

    }

    /**
     * This API is meant for transferring clients between branches mainly by
     * Organizations following an Individual lending Model <br/>
     * 
     * @param clientId
     * @param destinationOfficeId
     * @param jsonCommand
     * @return
     **/
    @Transactional
    @Override
    public CommandProcessingResult proposeAndAcceptClientTransfer(final Long clientId, final JsonCommand jsonCommand) {
        // validation
        this.transfersDataValidator.validateForProposeAndAcceptClientTransfer(jsonCommand.json());

        final Long destinationOfficeId = jsonCommand.longValueOfParameterNamed(TransferApiConstants.destinationOfficeIdParamName);
        final Office office = this.officeRepository.findOneWithNotFoundDetection(destinationOfficeId);
        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        handleClientTransferLifecycleEvent(client, office, TransferEventType.PROPOSAL, jsonCommand);
        this.clientRepository.saveAndFlush(client);
        handleClientTransferLifecycleEvent(client, client.getTransferToOffice(), TransferEventType.ACCEPTANCE, jsonCommand);
        this.clientRepository.save(client);

        return new CommandProcessingResultBuilder() //
                .withClientId(clientId) //
                .withEntityId(clientId) //
                .build();
    }

    /**
     * This API is meant for transferring clients between branches mainly by
     * Organizations following an Individual lending Model <br/>
     * If the Client is linked to any Groups, we can optionally choose to have
     * all the linkages broken and all JLG Loans are converted into Individual
     * Loans
     * 
     * @param clientId
     * @param destinationOfficeId
     * @param jsonCommand
     * @return
     **/
    @Transactional
    @Override
    public CommandProcessingResult proposeClientTransfer(final Long clientId, final JsonCommand jsonCommand) {
        // validation
        this.transfersDataValidator.validateForProposeClientTransfer(jsonCommand.json());

        final Long destinationOfficeId = jsonCommand.longValueOfParameterNamed(TransferApiConstants.destinationOfficeIdParamName);
        final Office office = this.officeRepository.findOneWithNotFoundDetection(destinationOfficeId);
        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        handleClientTransferLifecycleEvent(client, office, TransferEventType.PROPOSAL, jsonCommand);
        this.clientRepository.save(client);

        return new CommandProcessingResultBuilder() //
                .withClientId(clientId) //
                .withEntityId(clientId) //
                .build();
    }

    /**
     * This API is meant for transferring clients between branches mainly by
     * Organizations following an Individual lending Model <br/>
     * If the Client is linked to any Groups, we can optionally choose to have
     * all the linkages broken and all JLG Loans are converted into Individual
     * Loans
     * 
     * @param clientId
     * @param destinationOfficeId
     * @param jsonCommand
     * @return
     **/
    @Transactional
    @Override
    public CommandProcessingResult acceptClientTransfer(final Long clientId, final JsonCommand jsonCommand) {
        // validation
        this.transfersDataValidator.validateForAcceptClientTransfer(jsonCommand.json());
        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        validateClientAwaitingTransferAcceptance(client);

        handleClientTransferLifecycleEvent(client, client.getTransferToOffice(), TransferEventType.ACCEPTANCE, jsonCommand);
        this.clientRepository.save(client);

        return new CommandProcessingResultBuilder() //
                .withClientId(clientId) //
                .withEntityId(clientId) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult withdrawClientTransfer(final Long clientId, final JsonCommand jsonCommand) {
        // validation
        this.transfersDataValidator.validateForWithdrawClientTransfer(jsonCommand.json());
        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        validateClientAwaitingTransferAcceptanceOnHold(client);

        handleClientTransferLifecycleEvent(client, client.getOffice(), TransferEventType.WITHDRAWAL, jsonCommand);
        this.clientRepository.save(client);

        return new CommandProcessingResultBuilder() //
                .withClientId(clientId) //
                .withEntityId(clientId) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult rejectClientTransfer(final Long clientId, final JsonCommand jsonCommand) {
        // validation
        this.transfersDataValidator.validateForRejectClientTransfer(jsonCommand.json());
        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        handleClientTransferLifecycleEvent(client, client.getOffice(), TransferEventType.REJECTION, jsonCommand);
        this.clientRepository.save(client);

        return new CommandProcessingResultBuilder() //
                .withClientId(clientId) //
                .withEntityId(clientId) //
                .build();
    }

    private void handleClientTransferLifecycleEvent(final Client client, final Office destinationOffice,
            final TransferEventType transferEventType, final JsonCommand jsonCommand) {
        final Date todaysDate = DateUtils.getDateOfTenant();
        /** Get destination loan officer if exists **/
        Staff staff = null;
        Group destinationGroup = null;
        final Long staffId = jsonCommand.longValueOfParameterNamed(TransferApiConstants.newStaffIdParamName);
        final Long destinationGroupId = jsonCommand.longValueOfParameterNamed(TransferApiConstants.destinationGroupIdParamName);
        if (staffId != null) {
            staff = this.staffRepositoryWrapper.findByOfficeHierarchyWithNotFoundDetection(staffId, destinationOffice.getHierarchy());
        }
        if (transferEventType.isAcceptance() && destinationGroupId != null) {
            destinationGroup = this.groupRepository.findByOfficeWithNotFoundDetection(destinationGroupId, destinationOffice);
        }

        /*** Handle Active Loans ***/
        if (this.loanRepository.doNonClosedLoanAccountsExistForClient(client.getId())) {
            // get each individual loan for the client
            for (final Loan loan : this.loanRepository.findLoanByClientId(client.getId())) {
                /**
                 * We need to create transactions etc only for loans which are
                 * disbursed and not yet closed
                 **/
                if (loan.isDisbursed() && !loan.isClosed()) {
                    switch (transferEventType) {
                        case ACCEPTANCE:
                            this.loanWritePlatformService.acceptLoanTransfer(loan.getId(), DateUtils.getLocalDateOfTenant(),
                                    destinationOffice, staff);
                        break;
                        case PROPOSAL:
                            this.loanWritePlatformService.initiateLoanTransfer(loan.getId(), DateUtils.getLocalDateOfTenant());
                        break;
                        case REJECTION:
                            this.loanWritePlatformService.rejectLoanTransfer(loan.getId());
                        break;
                        case WITHDRAWAL:
                            this.loanWritePlatformService.withdrawLoanTransfer(loan.getId(), DateUtils.getLocalDateOfTenant());
                    }
                }
            }
        }

        /*** Handle Active Savings (Currently throw and exception) ***/
        if (this.savingsAccountRepository.doNonClosedSavingAccountsExistForClient(client.getId())) {
            // get each individual saving account for the client
            for (final SavingsAccount savingsAccount : this.savingsAccountRepository.findSavingAccountByClientId(client.getId())) {
                if (savingsAccount.isActivated() && !savingsAccount.isClosed()) {
                    switch (transferEventType) {
                        case ACCEPTANCE:
                            this.savingsAccountWritePlatformService.acceptSavingsTransfer(savingsAccount.getId(),
                                    DateUtils.getLocalDateOfTenant(), destinationOffice, staff);
                        break;
                        case PROPOSAL:
                            this.savingsAccountWritePlatformService.initiateSavingsTransfer(savingsAccount.getId(),
                                    DateUtils.getLocalDateOfTenant());
                        break;
                        case REJECTION:
                            this.savingsAccountWritePlatformService.rejectSavingsTransfer(savingsAccount.getId());
                        break;
                        case WITHDRAWAL:
                            this.savingsAccountWritePlatformService.withdrawSavingsTransfer(savingsAccount.getId(),
                                    DateUtils.getLocalDateOfTenant());
                    }
                }
            }
        }

        switch (transferEventType) {
            case ACCEPTANCE:
                client.setStatus(ClientStatus.ACTIVE.getValue());
                client.updateTransferToOffice(null);
                client.updateOffice(destinationOffice);
                client.updateOfficeJoiningDate(todaysDate);
                if (client.getGroups().size() == 1) {
                    if (destinationGroup == null) {
                        throw new TransferNotSupportedException(TRANSFER_NOT_SUPPORTED_REASON.CLIENT_DESTINATION_GROUP_NOT_SPECIFIED,
                                client.getId());
                    } else if (!destinationGroup.isActive()) { throw new GroupNotActiveException(destinationGroup.getId()); }
                    transferClientBetweenGroups(Iterables.get(client.getGroups(), 0), client, destinationGroup, true, staff);
                } else if (client.getGroups().size() == 0 && destinationGroup != null) {
                    client.getGroups().add(destinationGroup);
                    client.updateStaff(destinationGroup.getStaff());
                    if (staff != null) {
                        client.updateStaff(staff);
                    }
                }
            break;
            case PROPOSAL:
                client.setStatus(ClientStatus.TRANSFER_IN_PROGRESS.getValue());
                client.updateTransferToOffice(destinationOffice);
            break;
            case REJECTION:
                client.setStatus(ClientStatus.TRANSFER_ON_HOLD.getValue());
                client.updateTransferToOffice(null);
            break;
            case WITHDRAWAL:
                client.setStatus(ClientStatus.ACTIVE.getValue());
                client.updateTransferToOffice(null);
        }

        this.noteWritePlatformService.createAndPersistClientNote(client, jsonCommand);
    }

    private List<Client> assembleListOfClients(final JsonCommand command) {

        final List<Client> clients = new ArrayList<>();

        if (command.parameterExists(TransferApiConstants.clients)) {
            final JsonArray clientsArray = command.arrayOfParameterNamed(TransferApiConstants.clients);
            if (clientsArray != null) {
                for (int i = 0; i < clientsArray.size(); i++) {

                    final JsonObject jsonObject = clientsArray.get(i).getAsJsonObject();
                    if (jsonObject.has(TransferApiConstants.idParamName)) {
                        final Long id = jsonObject.get(TransferApiConstants.idParamName).getAsLong();
                        final Client client = this.clientRepository.findOneWithNotFoundDetection(id);
                        clients.add(client);
                    }
                }
            }
        }
        return clients;
    }

    private void validateClientAwaitingTransferAcceptance(final Client client) {
        if (!client.isTransferInProgress()) { throw new ClientNotAwaitingTransferApprovalException(client.getId()); }
    }

    /**
     * private void validateGroupAwaitingTransferAcceptance(final Group group) {
     * if (!group.isTransferInProgress()) { throw new
     * ClientNotAwaitingTransferApprovalException(group.getId()); } }
     **/

    private void validateClientAwaitingTransferAcceptanceOnHold(final Client client) {
        if (!client.isTransferInProgressOrOnHold()) { throw new ClientNotAwaitingTransferApprovalOrOnHoldException(client.getId()); }
    }

    /**
     * private void validateGroupAwaitingTransferAcceptanceOnHold(final Group
     * group) { if (!group.isTransferInProgressOrOnHold()) { throw new
     * ClientNotAwaitingTransferApprovalException(group.getId()); } }
     **/

}