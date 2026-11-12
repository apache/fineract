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
package org.apache.fineract.organisation.office.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.OfficeTransactionRepository;
import org.apache.fineract.organisation.office.exception.OfficeNotFoundException;
import org.apache.fineract.organisation.office.serialization.OfficeCommandFromApiJsonDeserializer;
import org.apache.fineract.organisation.office.serialization.OfficeTransactionCommandFromApiJsonDeserializer;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OfficeWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private OfficeCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private OfficeTransactionCommandFromApiJsonDeserializer moneyTransferCommandFromApiJsonDeserializer;
    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;
    @Mock
    private OfficeTransactionRepository officeTransactionRepository;
    @Mock
    private ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    @Mock
    private AppUser currentUser;

    @InjectMocks
    private OfficeWritePlatformServiceJpaRepositoryImpl underTest;

    @BeforeEach
    void setUp() {
        final Office headOffice = Office.headOffice("Head Office", null, ExternalId.empty());
        ReflectionTestUtils.setField(headOffice, "id", 1L);
        headOffice.generateHierarchy();
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(headOffice);
        when(officeRepositoryWrapper.findOfficeHierarchy(1L)).thenReturn(headOffice);
    }

    @Test
    void createOfficeUnderAnUnknownParentReportsTheMissingOffice() {
        when(officeRepositoryWrapper.findOneWithNotFoundDetection(99999L)).thenThrow(new OfficeNotFoundException(99999L));

        OfficeNotFoundException exception = assertThrows(OfficeNotFoundException.class,
                () -> underTest.createOffice(command("{\"name\":\"Branch\",\"parentId\":99999}")));

        assertEquals("Office with identifier 99999 does not exist", exception.getDefaultUserMessage());
        verify(officeRepositoryWrapper, never()).saveAndFlush(any(Office.class));
    }

    @Test
    void createOfficeUnderAnExistingParentOutsideTheUserHierarchyIsNotAuthorized() {
        when(officeRepositoryWrapper.findOneWithNotFoundDetection(5L)).thenReturn(mock(Office.class));

        NoAuthorizationException exception = assertThrows(NoAuthorizationException.class,
                () -> underTest.createOffice(command("{\"name\":\"Branch\",\"parentId\":5}")));

        assertEquals("User does not have sufficient privileges to act on the provided office.", exception.getMessage());
        verify(officeRepositoryWrapper, never()).saveAndFlush(any(Office.class));
    }

    private static JsonCommand command(final String json) {
        final FromJsonHelper helper = new FromJsonHelper();
        return JsonCommand.from(json, JsonParser.parseString(json), helper, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }
}
