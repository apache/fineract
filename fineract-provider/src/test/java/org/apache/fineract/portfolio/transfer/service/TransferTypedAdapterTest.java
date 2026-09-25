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
package org.apache.fineract.portfolio.transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientTransferDetailsRepositoryWrapper;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.data.GroupTransferClientItem;
import org.apache.fineract.portfolio.group.data.GroupTransferClientsRequest;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanOfficerService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.apache.fineract.portfolio.transfer.data.TransfersDataValidator;
import org.apache.fineract.portfolio.transfer.exception.TransferNotSupportedException;
import org.apache.fineract.portfolio.transfer.exception.TransferNotSupportedException.TransferNotSupportedReason;
import org.junit.jupiter.api.Test;

class TransferTypedAdapterTest {

    private final ClientRepositoryWrapper clientRepositoryWrapper = mock(ClientRepositoryWrapper.class);
    private final OfficeRepositoryWrapper officeRepository = mock(OfficeRepositoryWrapper.class);
    private final CalendarInstanceRepository calendarInstanceRepository = mock(CalendarInstanceRepository.class);
    private final GroupRepositoryWrapper groupRepository = mock(GroupRepositoryWrapper.class);
    private final LoanWritePlatformService loanWritePlatformService = mock(LoanWritePlatformService.class);
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService = mock(SavingsAccountWritePlatformService.class);
    private final LoanRepositoryWrapper loanRepositoryWrapper = mock(LoanRepositoryWrapper.class);
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper = mock(SavingsAccountRepositoryWrapper.class);
    private final TransfersDataValidator transfersDataValidator = mock(TransfersDataValidator.class);
    private final StaffRepositoryWrapper staffRepositoryWrapper = mock(StaffRepositoryWrapper.class);
    private final ClientTransferDetailsRepositoryWrapper clientTransferDetailsRepositoryWrapper = mock(
            ClientTransferDetailsRepositoryWrapper.class);
    private final PlatformSecurityContext context = mock(PlatformSecurityContext.class);
    private final LoanOfficerService loanOfficerService = mock(LoanOfficerService.class);
    private final TransactionBoundApplicationEventPublisher eventPublisher = mock(TransactionBoundApplicationEventPublisher.class);

    private final TransferWritePlatformServiceJpaRepositoryImpl service = spy(new TransferWritePlatformServiceJpaRepositoryImpl(
            clientRepositoryWrapper, officeRepository, calendarInstanceRepository, groupRepository, loanWritePlatformService,
            savingsAccountWritePlatformService, loanRepositoryWrapper, savingsAccountRepositoryWrapper, transfersDataValidator,
            staffRepositoryWrapper, clientTransferDetailsRepositoryWrapper, context, loanOfficerService, eventPublisher));

    @Test
    void sameSourceAndDestinationGroupIsRejected() {
        Group group = mock(Group.class);
        when(groupRepository.findOneWithNotFoundDetection(1L)).thenReturn(group);

        GroupTransferClientsRequest request = GroupTransferClientsRequest.builder().id(1L).destinationGroupId(1L)
                .clients(List.of(GroupTransferClientItem.builder().id(9L).build())).build();

        TransferNotSupportedException exception = assertThrows(TransferNotSupportedException.class,
                () -> service.transferClientsBetweenGroups(request));
        assertEquals(TransferNotSupportedReason.SOURCE_AND_DESTINATION_GROUP_CANNOT_BE_SAME.errorCode(),
                exception.getGlobalisationMessageCode());
        verify(service, never()).transferClientBetweenGroups(any(), any(), any(), any(), any());
    }

    @Test
    void crossBranchTransferIsRejected() {
        Group sourceGroup = mock(Group.class);
        Group destinationGroup = mock(Group.class);
        Office sourceOffice = mock(Office.class);
        Office destinationOffice = mock(Office.class);
        when(sourceOffice.getId()).thenReturn(10L);
        when(destinationOffice.getId()).thenReturn(20L);
        when(sourceGroup.getOffice()).thenReturn(sourceOffice);
        when(destinationGroup.getOffice()).thenReturn(destinationOffice);
        when(groupRepository.findOneWithNotFoundDetection(1L)).thenReturn(sourceGroup);
        when(groupRepository.findOneWithNotFoundDetection(2L)).thenReturn(destinationGroup);

        GroupTransferClientsRequest request = GroupTransferClientsRequest.builder().id(1L).destinationGroupId(2L)
                .clients(List.of(GroupTransferClientItem.builder().id(9L).build())).build();

        TransferNotSupportedException exception = assertThrows(TransferNotSupportedException.class,
                () -> service.transferClientsBetweenGroups(request));
        assertEquals(TransferNotSupportedReason.BULK_CLIENT_TRANSFER_ACROSS_BRANCHES.errorCode(), exception.getGlobalisationMessageCode());
        verify(service, never()).transferClientBetweenGroups(any(), any(), any(), any(), any());
    }

    @Test
    void happyPathTransfersEachClientAndReturnsSourceGroupId() {
        Group sourceGroup = mock(Group.class);
        Group destinationGroup = mock(Group.class);
        Office office = mock(Office.class);
        when(office.getId()).thenReturn(10L);
        when(office.getHierarchy()).thenReturn(".10.");
        when(sourceGroup.getOffice()).thenReturn(office);
        when(destinationGroup.getOffice()).thenReturn(office);
        when(groupRepository.findOneWithNotFoundDetection(1L)).thenReturn(sourceGroup);
        when(groupRepository.findOneWithNotFoundDetection(2L)).thenReturn(destinationGroup);

        Client client1 = mock(Client.class);
        Client client2 = mock(Client.class);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(100L)).thenReturn(client1);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(200L)).thenReturn(client2);

        Staff staff = mock(Staff.class);
        when(staffRepositoryWrapper.findByOfficeHierarchyWithNotFoundDetection(5L, ".10.")).thenReturn(staff);

        doNothing().when(service).transferClientBetweenGroups(any(), any(), any(), any(), any());

        GroupTransferClientsRequest request = GroupTransferClientsRequest.builder().id(1L).destinationGroupId(2L).staffId(5L)
                .inheritDestinationGroupLoanOfficer(true)
                .clients(List.of(GroupTransferClientItem.builder().id(100L).build(), GroupTransferClientItem.builder().id(200L).build()))
                .build();

        GroupCommandResponse response = service.transferClientsBetweenGroups(request);

        verify(service).transferClientBetweenGroups(sourceGroup, client1, destinationGroup, true, staff);
        verify(service).transferClientBetweenGroups(sourceGroup, client2, destinationGroup, true, staff);
        assertEquals(1L, response.getGroupId());
        assertEquals(1L, response.getResourceId());
    }

    @Test
    void nullClientsListTransfersNobody() {
        Group sourceGroup = mock(Group.class);
        Group destinationGroup = mock(Group.class);
        Office office = mock(Office.class);
        when(office.getId()).thenReturn(10L);
        when(sourceGroup.getOffice()).thenReturn(office);
        when(destinationGroup.getOffice()).thenReturn(office);
        when(groupRepository.findOneWithNotFoundDetection(1L)).thenReturn(sourceGroup);
        when(groupRepository.findOneWithNotFoundDetection(2L)).thenReturn(destinationGroup);

        GroupTransferClientsRequest request = GroupTransferClientsRequest.builder().id(1L).destinationGroupId(2L).clients(null).build();

        GroupCommandResponse response = service.transferClientsBetweenGroups(request);

        verify(service, never()).transferClientBetweenGroups(any(), any(), any(), any(), any());
        assertEquals(1L, response.getGroupId());
        assertEquals(1L, response.getResourceId());
    }
}
