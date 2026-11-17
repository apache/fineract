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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
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
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.data.GroupCreateRequest;
import org.apache.fineract.portfolio.group.data.GroupCreateResponse;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupingTypesWritePlatformServiceJpaRepositoryImplTest {

    @BeforeEach
    void setBusinessDate() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 1, 1))));
    }

    @AfterEach
    void resetContext() {
        ThreadLocalContextUtil.reset();
    }

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

    @Test
    void createGroupParsesUnderscoreLocaleLikeLegacyDid() {
        ExternalIdFactory externalIdFactory = new ExternalIdFactory(configurationDomainService);
        GroupingTypesWritePlatformServiceJpaRepositoryImpl service = new GroupingTypesWritePlatformServiceJpaRepositoryImpl(context,
                groupRepository, clientRepositoryWrapper, officeRepositoryWrapper, staffRepository, noteRepository, groupLevelRepository,
                fromApiJsonDeserializer, loanRepositoryWrapper, codeValueRepository, commandProcessingService, calendarInstanceRepository,
                configurationDomainService, savingsAccountRepositoryWrapper, accountNumberFormatRepository, accountNumberGenerator,
                entityDatatableChecksWritePlatformService, businessEventNotifierService, loanOfficerService, externalIdFactory);

        when(officeRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(office);
        when(office.getId()).thenReturn(1L);
        when(office.getOpeningLocalDate()).thenReturn(LocalDate.of(2000, 1, 1));
        when(groupLevelRepository.findById(any())).thenReturn(Optional.of(groupLevel));
        when(groupLevel.isCenter()).thenReturn(false);

        // "en_GB" is the legacy underscore form Fineract clients send; Locale.forLanguageTag would silently
        // resolve this to the root locale instead, and "04 March 2011" would then fail to parse.
        GroupCreateRequest request = GroupCreateRequest.builder() //
                .name("Pending Group") //
                .officeId(1L) //
                .accountNo("GRP001") //
                .active(false) //
                .activationDate("04 March 2011") //
                .locale("en_GB") //
                .dateFormat("dd MMMM yyyy") //
                .build();

        GroupCreateResponse response = assertDoesNotThrow(() -> service.createGroup(request));

        assertThat(response.getOfficeId()).isEqualTo(1L);
    }

    @Test
    void createGroupRejectsActivationWhenMakerCheckerIsEnabled() {
        ExternalIdFactory externalIdFactory = new ExternalIdFactory(configurationDomainService);
        GroupingTypesWritePlatformServiceJpaRepositoryImpl service = new GroupingTypesWritePlatformServiceJpaRepositoryImpl(context,
                groupRepository, clientRepositoryWrapper, officeRepositoryWrapper, staffRepository, noteRepository, groupLevelRepository,
                fromApiJsonDeserializer, loanRepositoryWrapper, codeValueRepository, commandProcessingService, calendarInstanceRepository,
                configurationDomainService, savingsAccountRepositoryWrapper, accountNumberFormatRepository, accountNumberGenerator,
                entityDatatableChecksWritePlatformService, businessEventNotifierService, loanOfficerService, externalIdFactory);

        when(officeRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(office);
        when(office.getId()).thenReturn(1L);
        when(office.getOpeningLocalDate()).thenReturn(LocalDate.of(2000, 1, 1));
        when(groupLevelRepository.findById(any())).thenReturn(Optional.of(groupLevel));
        when(groupLevel.isCenter()).thenReturn(false);
        when(groupLevel.isGroup()).thenReturn(true);
        when(configurationDomainService.retrieveMinAllowedClientsInGroup()).thenReturn(0);
        when(configurationDomainService.retrieveMaxAllowedClientsInGroup()).thenReturn(100);
        // Maker-checker enabled for ACTIVATE_GROUP: the legacy JsonCommand path parks the command for checker
        // approval; the typed path has nowhere to park it yet, so it must reject instead of activating anyway.
        when(commandProcessingService.validateRollbackCommand(any(), any())).thenReturn(true);

        GroupCreateRequest request = GroupCreateRequest.builder() //
                .name("Active Group") //
                .officeId(1L) //
                .accountNo("GRP002") //
                .active(true) //
                .activationDate("04 March 2011") //
                .dateFormat("dd MMMM yyyy") //
                .build();

        assertThrows(GeneralPlatformDomainRuleException.class, () -> service.createGroup(request));
    }
}
