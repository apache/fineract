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
package org.apache.fineract.portfolio.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.calendar.data.CalendarCreateRequest;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarHistoryRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.exception.CalendarEntityTypeNotSupportedException;
import org.apache.fineract.portfolio.calendar.serialization.CalendarCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private CalendarRepository calendarRepository;
    @Mock
    private CalendarHistoryRepository calendarHistoryRepository;
    @Mock
    private CalendarCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private CalendarInstanceRepository calendarInstanceRepository;
    @Mock
    private LoanWritePlatformService loanWritePlatformService;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private GroupRepositoryWrapper groupRepository;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private ClientRepositoryWrapper clientRepository;

    private CalendarWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new CalendarWritePlatformServiceJpaRepositoryImpl(calendarRepository, calendarHistoryRepository, fromApiJsonDeserializer,
                calendarInstanceRepository, loanWritePlatformService, configurationDomainService, groupRepository, loanRepositoryWrapper,
                clientRepository);
    }

    @Test
    void unsupportedEntityTypeIsRejected() {
        CalendarCreateRequest request = CalendarCreateRequest.builder().entityType(CalendarEntityType.SAVINGS).entityId(1L).title("t")
                .startDate("01 January 2024").dateFormat("dd MMMM yyyy").locale("en").typeId(CalendarType.COLLECTION.getValue()).build();

        assertThatThrownBy(() -> service.createCalendar(request)).isInstanceOf(CalendarEntityTypeNotSupportedException.class);
    }

    @Test
    void repeatingWithoutFrequencyIsRejected() {
        Group group = mockGroup(false, LocalDate.of(2020, 1, 1));
        when(groupRepository.findOneWithNotFoundDetection(anyLong())).thenReturn(group);

        CalendarCreateRequest request = CalendarCreateRequest.builder().entityType(CalendarEntityType.GROUPS).entityId(1L).title("t")
                .startDate("01 January 2024").dateFormat("dd MMMM yyyy").locale("en").typeId(CalendarType.COLLECTION.getValue())
                .repeating(true).build();

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.createCalendar(request));
        assertThat(exception.getErrors()).anySatisfy(error -> assertThat(error.getUserMessageGlobalisationCode())
                .isEqualTo("org.apache.fineract.portfolio.calendar.frequency.required-when-repeating"));
    }

    @Test
    void duplicateCollectionCalendarIsRejected() {
        Group group = mockGroup(false, LocalDate.of(2020, 1, 1));
        when(groupRepository.findOneWithNotFoundDetection(anyLong())).thenReturn(group);
        when(calendarInstanceRepository.findByEntityIdAndEntityTypeIdAndCalendarTypeId(anyLong(), anyInt(), anyInt()))
                .thenReturn(new CalendarInstance(null, 1L, CalendarEntityType.GROUPS.getValue()));

        CalendarCreateRequest request = CalendarCreateRequest.builder().entityType(CalendarEntityType.GROUPS).entityId(1L).title("t")
                .startDate("01 January 2024").dateFormat("dd MMMM yyyy").locale("en").typeId(CalendarType.COLLECTION.getValue())
                .repeating(true).frequency(2).interval(1).build();

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.createCalendar(request));
        assertThat(exception.getErrors()).anySatisfy(error -> assertThat(error.getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.calendar.multiple.collection.calendar.not.supported"));
    }

    @Test
    void nonRepeatingNonCollectionCalendarIsCreated() {
        Group group = mockGroup(false, LocalDate.of(2020, 1, 1));
        when(groupRepository.findOneWithNotFoundDetection(anyLong())).thenReturn(group);
        when(calendarInstanceRepository.findByEntityIdAndEntityTypeIdAndCalendarTypeId(anyLong(), anyInt(), anyInt())).thenReturn(null);

        CalendarCreateRequest request = CalendarCreateRequest.builder().entityType(CalendarEntityType.GROUPS).entityId(1L).title("t")
                .startDate("01 January 2024").dateFormat("dd MMMM yyyy").locale("en").typeId(2).build();

        assertThat(service.createCalendar(request)).isNotNull();
    }

    private Group mockGroup(final boolean isCenter, final LocalDate activationDate) {
        Group group = mock(Group.class);
        lenient().when(group.isCenter()).thenReturn(isCenter);
        lenient().when(group.getActivationDate()).thenReturn(activationDate);
        lenient().when(group.getParent()).thenReturn(null);
        lenient().when(group.getId()).thenReturn(1L);
        return group;
    }
}
