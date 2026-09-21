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
package org.apache.fineract.portfolio.group.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.data.GroupAssociateClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.data.GroupCreateRequest;
import org.apache.fineract.portfolio.group.data.GroupCreateResponse;
import org.apache.fineract.portfolio.group.data.GroupDeleteRequest;
import org.apache.fineract.portfolio.group.data.GroupUnassignStaffRequest;
import org.apache.fineract.portfolio.group.data.GroupUpdateRequest;
import org.apache.fineract.portfolio.group.data.GroupUpdateResponse;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupLevel;
import org.apache.fineract.portfolio.group.domain.GroupLevelRepository;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.GroupHasNoStaffException;
import org.apache.fineract.portfolio.group.exception.GroupMustBePendingToBeDeletedException;
import org.apache.fineract.portfolio.group.mapping.GroupDateMapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanOfficerService;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GroupingTypesWriteServiceImplTest {

    private final PlatformSecurityContext context = mock(PlatformSecurityContext.class);
    private final GroupRepositoryWrapper groupRepository = mock(GroupRepositoryWrapper.class);
    private final ClientRepositoryWrapper clientRepository = mock(ClientRepositoryWrapper.class);
    private final OfficeRepositoryWrapper officeRepository = mock(OfficeRepositoryWrapper.class);
    private final GroupLevelRepository groupLevelRepository = mock(GroupLevelRepository.class);
    private final ExternalIdFactory externalIdFactory = mock(ExternalIdFactory.class);
    private final EntityDatatableChecksWritePlatformService datatableChecks = mock(EntityDatatableChecksWritePlatformService.class);
    private final BusinessEventNotifierService events = mock(BusinessEventNotifierService.class);
    private final AccountNumberGenerator accountNumberGenerator = mock(AccountNumberGenerator.class);

    private GroupingTypesWriteServiceImpl service;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 1))));
        service = new GroupingTypesWriteServiceImpl(context, groupRepository, clientRepository, officeRepository,
                mock(StaffRepositoryWrapper.class), mock(NoteRepository.class), groupLevelRepository, mock(LoanRepositoryWrapper.class),
                mock(CodeValueRepositoryWrapper.class), mock(CalendarInstanceRepository.class), mock(ConfigurationDomainService.class),
                mock(SavingsAccountRepositoryWrapper.class), mock(AccountNumberFormatRepositoryWrapper.class), accountNumberGenerator,
                datatableChecks, events, mock(LoanOfficerService.class), externalIdFactory, new GroupDateMapper());
        when(context.authenticatedUser()).thenReturn(mock(AppUser.class));
        when(externalIdFactory.create(any()))
                .thenAnswer(inv -> inv.getArgument(0) == null ? ExternalId.empty() : new ExternalId(inv.getArgument(0)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void createPendingGroupReturnsOfficeAndGroupIds() {
        Office office = mock(Office.class);
        when(office.getId()).thenReturn(7L);
        when(office.getHierarchy()).thenReturn(".");
        when(office.getOpeningLocalDate()).thenReturn(LocalDate.of(2000, 1, 1));
        when(officeRepository.findOneWithNotFoundDetection(7L)).thenReturn(office);
        GroupLevel level = mock(GroupLevel.class);
        when(level.isGroup()).thenReturn(true);
        when(level.getLevelName()).thenReturn("Group");
        when(groupLevelRepository.findById(anyLong())).thenReturn(Optional.of(level));
        doAnswer(inv -> { // saveAndFlush is void on the wrapper; simulate the DB assigning the id
            Group g = inv.getArgument(0);
            ReflectionTestUtils.setField(g, "id", 42L); // AbstractPersistableCustom has no setId
            return null;
        }).when(groupRepository).saveAndFlush(any(Group.class));
        when(accountNumberGenerator.generateGroupAccountNumber(any(), any())).thenReturn("000000042");

        GroupCreateResponse response = service.createGroup(GroupCreateRequest.builder().name("Alpha").officeId(7L).active(false).build());

        assertEquals(7L, response.getOfficeId());
        assertEquals(42L, response.getGroupId());
        assertEquals(42L, response.getResourceId());
        verify(accountNumberGenerator).generateGroupAccountNumber(any(), any());
    }

    @Test
    void deleteRejectsNonPendingGroup() {
        Group active = mock(Group.class);
        when(active.isNotPending()).thenReturn(true);
        when(groupRepository.findOneWithNotFoundDetection(5L)).thenReturn(active);

        assertThrows(GroupMustBePendingToBeDeletedException.class, () -> service.deleteGroup(GroupDeleteRequest.builder().id(5L).build()));
    }

    @Test
    void unassignStaffRequiresCurrentStaff() {
        Group group = mock(Group.class);
        when(group.getStaff()).thenReturn(null);
        when(groupRepository.findOneWithNotFoundDetection(9L)).thenReturn(group);

        assertThrows(GroupHasNoStaffException.class,
                () -> service.unassignStaff(GroupUnassignStaffRequest.builder().id(9L).staffId(3L).build()));
    }

    @Test
    void associateClientsRecordsChanges() {
        Group group = mock(Group.class);
        when(group.officeId()).thenReturn(7L);
        when(group.getId()).thenReturn(9L);
        when(group.isGroup()).thenReturn(false);
        when(group.associateClients(any())).thenReturn(List.of("11"));
        when(groupRepository.findOneWithNotFoundDetection(9L)).thenReturn(group);
        Client client = mock(Client.class);
        when(client.isOfficeIdentifiedBy(7L)).thenReturn(true);
        when(clientRepository.findOneWithNotFoundDetection(11L)).thenReturn(client);

        GroupCommandResponse response = service
                .associateClients(GroupAssociateClientsRequest.builder().id(9L).clientMembers(List.of(11L)).build());

        assertEquals(9L, response.getGroupId());
        assertEquals(List.of("11"), response.getChanges().get("clientMembers"));
    }

    private Group existingGroup(long id) {
        Office office = mock(Office.class);
        when(office.getHierarchy()).thenReturn(".");
        when(office.getOpeningLocalDate()).thenReturn(LocalDate.of(2000, 1, 1));
        GroupLevel level = mock(GroupLevel.class);
        when(level.getId()).thenReturn(1L);
        when(level.getLevelName()).thenReturn("Group");
        when(groupLevelRepository.findById(1L)).thenReturn(Optional.of(level));
        Group group = mock(Group.class);
        when(group.getId()).thenReturn(id);
        when(group.officeId()).thenReturn(7L);
        when(group.getOffice()).thenReturn(office);
        when(group.getGroupLevel()).thenReturn(level);
        when(group.getName()).thenReturn("Old");
        when(groupRepository.findOneWithNotFoundDetection(id)).thenReturn(group);
        return group;
    }

    @Test
    void updateRecordsOnlyChangedFieldsInLegacyOrder() {
        Group group = existingGroup(9L);

        GroupUpdateResponse response = service.updateGroup(GroupUpdateRequest.builder().id(9L).name("New").externalId("ext-9")
                .activationDate("02 January 2024").dateFormat("dd MMMM yyyy").locale("en").build());

        assertEquals(9L, response.getGroupId());
        assertEquals(List.of("externalId", "name", "activationDate", "dateFormat", "locale"),
                new ArrayList<>(response.getChanges().keySet()));
        verify(group).setName("New");
        verify(group).setExternalId("ext-9");
        verify(group).setActivationDate(LocalDate.of(2024, 1, 2));
        verify(groupRepository).saveAndFlush(group);
    }

    @Test
    void updateRejectsActivationDateBeforeSubmittedOnDate() {
        existingGroup(9L);

        PlatformApiDataValidationException e = assertThrows(PlatformApiDataValidationException.class,
                () -> service.updateGroup(GroupUpdateRequest.builder().id(9L).name("Old").activationDate("01 January 2024")
                        .submittedOnDate("02 January 2024").dateFormat("dd MMMM yyyy").locale("en").build()));

        assertEquals("validation.msg.group.activationDate.is.less.than.date", e.getErrors().get(0).getUserMessageGlobalisationCode());
    }
}
