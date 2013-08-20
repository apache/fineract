/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.transfer.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepositoryWrapper;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.mifosplatform.portfolio.calendar.domain.CalendarType;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.exception.ClientHasBeenClosedException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepositoryWrapper;
import org.mifosplatform.portfolio.group.exception.ClientNotInGroupException;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.service.LoanWritePlatformService;
import org.mifosplatform.portfolio.transfer.api.TransferApiConstants;
import org.mifosplatform.portfolio.transfer.data.TransfersDataValidator;
import org.mifosplatform.portfolio.transfer.exception.TransferNotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class TransferWritePlatformServiceJpaRepositoryImpl implements TransferWritePlatformService {

    private final ClientRepositoryWrapper clientRepository;
    private final OfficeRepositoryWrapper officeRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final LoanWritePlatformService loanWritePlatformService;
    private final LoanRepository loanRepository;
    private final TransfersDataValidator transfersDataValidator;

    @Autowired
    public TransferWritePlatformServiceJpaRepositoryImpl(final ClientRepositoryWrapper clientRepository,
            final OfficeRepositoryWrapper officeRepository, final CalendarInstanceRepository calendarInstanceRepository,
            final LoanWritePlatformService loanWritePlatformService, final GroupRepositoryWrapper groupRepository,
            final LoanRepository loanRepository, final TransfersDataValidator transfersDataValidator) {
        this.clientRepository = clientRepository;
        this.officeRepository = officeRepository;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.loanWritePlatformService = loanWritePlatformService;
        this.groupRepository = groupRepository;
        this.loanRepository = loanRepository;
        this.transfersDataValidator = transfersDataValidator;
    }

    @Override
    @Transactional
    public CommandProcessingResult transferClientsBetweenGroups(Long sourceGroupId, JsonCommand jsonCommand) {
        this.transfersDataValidator.validateForClientsTransferBetweenGroups(jsonCommand.json());

        final Group sourceGroup = this.groupRepository.findOneWithNotFoundDetection(sourceGroupId);
        final Long destinationGroupId = jsonCommand.longValueOfParameterNamed(TransferApiConstants.destinationGroupIdParamName);
        final Group destinationGroup = this.groupRepository.findOneWithNotFoundDetection(destinationGroupId);

        final List<Client> clients = this.assembleListOfClients(jsonCommand);

        for (Client client : clients) {
            transferClientBetweenGroups(sourceGroup, client, destinationGroup);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(sourceGroupId) //
                .build();
    }

    /****
     * Variables that would make sense <br/>
     * <ul>
     * <li>inheritDestinationGroupLoanOfficer: Default False</li>
     * <li>newStaffId: Optional field with Id of new Loan Officer to be linked
     * to this client and all his JLG loans for this group</li>
     * <li>transferActiveLoans:Default False, unless overridden, this client
     * cannot be transferred between groups within the same branch if he has
     * active JLG loans and across branches if he has any active loan</li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ul>
     * ***/
    @Transactional
    public void transferClientBetweenGroups(final Group sourceGroup, final Client client, final Group destinationGroup) {

        // next I shall validate that the client is present in this group
        if (!sourceGroup.hasClientAsMember(client)) { throw new ClientNotInGroupException(client.getId(), sourceGroup.getId()); }
        // Is client active?
        if (client.isNotActive()) { throw new ClientHasBeenClosedException(client.getId()); }

        /*** Code for intra-branch transfers ***/
        if (client.getOffice().getId() == destinationGroup.getOffice().getId()) {
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
             * if a calendar is present in the source group along with loans
             * synced with it, we should ensure that the destination group also
             * has a collection calendar
             **/
            if (sourceGroupCalendarInstance != null && !activeLoanCalendarInstances.isEmpty()) {
                // get the destination calendar
                final CalendarInstance destinationGroupCalendarInstance = this.calendarInstanceRepository
                        .findByEntityIdAndEntityTypeIdAndCalendarTypeId(destinationGroup.getId(), CalendarEntityType.GROUPS.getValue(),
                                CalendarType.COLLECTION.getValue());

                if (destinationGroupCalendarInstance == null) { throw new TransferNotSupportedException(client.getId(),
                        sourceGroup.getId(), destinationGroup.getId(), false);

                }
                Calendar sourceGroupCalendar = destinationGroupCalendarInstance.getCalendar();
                Calendar destinationGroupCalendar = destinationGroupCalendarInstance.getCalendar();

                /***
                 * Ensure that the recurrence pattern are same for collection
                 * meeting in both the source and the destination calendar
                 ***/
                if (!(CalendarUtils.isFrequencySame(sourceGroupCalendar.getRecurrence(), destinationGroupCalendar.getRecurrence()) && CalendarUtils
                        .isIntervalSame(sourceGroupCalendar.getRecurrence(), destinationGroupCalendar.getRecurrence()))) { throw new TransferNotSupportedException(
                        client.getId(), sourceGroup.getId(), destinationGroup.getId()); }

                /** map all JLG loans for this client to the destinationGroup **/
                for (CalendarInstance calendarInstance : activeLoanCalendarInstances) {
                    calendarInstance.updateCalendar(destinationGroupCalendar);
                }
                // reschedule all JLG Loans to follow new Calendar
                this.loanWritePlatformService.applyMeetingDateChanges(destinationGroupCalendar, activeLoanCalendarInstances);
            }

        } else {
            Office destinationOffice = destinationGroup.getOffice();
            transferClientToNewBranch(client, destinationOffice);
        }

        /**
         * Now Change the loan officer for this client and all his active JLG
         * loans
         **/
        Staff destinationGroupLoanOfficer = destinationGroup.getStaff();
        if (destinationGroupLoanOfficer != null) {
            client.updateStaff(destinationGroupLoanOfficer);
        }

        client.getGroups().add(destinationGroup);
        this.clientRepository.saveAndFlush(client);

        /**
         * Active JLG loans are now linked to the new Group and Loan officer
         **/
        List<Loan> allClientJLGLoans = loanRepository.findByClientIdAndGroupId(client.getId(), sourceGroup.getId());
        for (Loan loan : allClientJLGLoans) {
            if (loan.status().isActiveOrAwaitingApprovalOrDisbursal()) {
                loan.updateGroup(destinationGroup);
                if (destinationGroupLoanOfficer != null) {
                    loan.reassignLoanOfficer(destinationGroupLoanOfficer, DateUtils.getLocalDateOfTenant());
                }
                this.loanRepository.saveAndFlush(loan);
            }
        }

        // change client group membership
        client.getGroups().remove(sourceGroup);

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
    public CommandProcessingResult transferClientBetweenBranches(final Long clientId, final JsonCommand jsonCommand) {
        // validation
        this.transfersDataValidator.validateForClientTransferBetweenBranches(jsonCommand.json());

        final Long destinationOfficeId = jsonCommand.longValueOfParameterNamed(TransferApiConstants.destinationOfficeIdParamName);
        final Office office = this.officeRepository.findOneWithNotFoundDetection(destinationOfficeId);
        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        /*** Remove all group associations for this client ***/
        client.getGroups().clear();
        transferClientToNewBranch(client, office);
        this.clientRepository.save(client);

        return new CommandProcessingResultBuilder() //
                .withClientId(clientId) //
                .withEntityId(clientId) //
                .build();
    }

    private void transferClientToNewBranch(final Client client, final Office destinationOffice) {
        /**
         * For now, do not allow Clients with active loans to be transferred
         * between branches
         **/
        if (loanRepository.doesClientHaveActiveLoans(client.getId())) { throw new TransferNotSupportedException(); }
        // set the new office for the client
        client.updateOffice(destinationOffice);
    }

    private List<Client> assembleListOfClients(final JsonCommand command) {

        final List<Client> clients = new ArrayList<Client>();

        if (command.parameterExists(TransferApiConstants.clients)) {
            JsonArray clientsArray = command.arrayOfParameterNamed(TransferApiConstants.clients);
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

}