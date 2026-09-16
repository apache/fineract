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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupLevel;
import org.apache.fineract.portfolio.group.domain.GroupLevelRepository;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.GroupLevelNotFoundException;
import org.apache.fineract.portfolio.group.serialization.GroupingTypesDataValidator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanOfficerService;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupingTypesWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private GroupRepositoryWrapper groupRepository;
    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;
    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;
    @Mock
    private StaffRepositoryWrapper staffRepository;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private GroupLevelRepository groupLevelRepository;
    @Mock
    private GroupingTypesDataValidator fromApiJsonDeserializer;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private CodeValueRepositoryWrapper codeValueRepository;
    @Mock
    private CommandProcessingService commandProcessingService;
    @Mock
    private CalendarInstanceRepository calendarInstanceRepository;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    @Mock
    private AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    @Mock
    private AccountNumberGenerator accountNumberGenerator;
    @Mock
    private EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private LoanOfficerService loanOfficerService;
    @Mock
    private ExternalIdFactory externalIdFactory;

    @Mock
    private Group group;
    @Mock
    private Office office;
    @Mock
    private GroupLevel groupLevel;
    @Mock
    private JsonCommand command;

    @Test
    void updateGroupThrowsGroupLevelNotFoundExceptionWhenGroupLevelMissing() {
        GroupingTypesWritePlatformServiceJpaRepositoryImpl service = new GroupingTypesWritePlatformServiceJpaRepositoryImpl(context,
                groupRepository, clientRepositoryWrapper, officeRepositoryWrapper, staffRepository, noteRepository, groupLevelRepository,
                fromApiJsonDeserializer, loanRepositoryWrapper, codeValueRepository, commandProcessingService, calendarInstanceRepository,
                configurationDomainService, savingsAccountRepositoryWrapper, accountNumberFormatRepository, accountNumberGenerator,
                entityDatatableChecksWritePlatformService, businessEventNotifierService, loanOfficerService, externalIdFactory);

        Long groupId = 1L;
        Long groupLevelId = 2L;

        when(groupRepository.findOneWithNotFoundDetection(groupId)).thenReturn(group);
        when(group.officeId()).thenReturn(10L);
        when(group.getOffice()).thenReturn(office);
        when(office.getHierarchy()).thenReturn(".");
        when(group.getGroupLevel()).thenReturn(groupLevel);
        when(groupLevel.getId()).thenReturn(groupLevelId);
        when(command.localDateValueOfParameterNamed(anyString())).thenReturn(null);
        when(group.update(any(JsonCommand.class))).thenReturn(Collections.emptyMap());
        when(groupLevelRepository.findById(groupLevelId)).thenReturn(Optional.empty());

        assertThrows(GroupLevelNotFoundException.class, () -> service.updateGroup(groupId, command));
    }
}
