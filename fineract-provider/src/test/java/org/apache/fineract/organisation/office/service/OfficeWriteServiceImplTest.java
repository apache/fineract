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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeCreateRequest;
import org.apache.fineract.organisation.office.data.OfficeCreateResponse;
import org.apache.fineract.organisation.office.data.OfficeUpdateRequest;
import org.apache.fineract.organisation.office.data.OfficeUpdateResponse;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.exception.RootOfficeParentCannotBeUpdated;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OfficeWriteServiceImplTest {

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;

    @Mock
    private AppUser currentUser;

    @Mock
    private Office userOffice;

    private OfficeWriteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OfficeWriteServiceImpl(context, officeRepositoryWrapper);
    }

    @Test
    void createOfficeSuccess() {
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(userOffice);
        when(userOffice.getId()).thenReturn(1L);
        when(officeRepositoryWrapper.findOfficeHierarchy(1L)).thenReturn(userOffice);
        when(userOffice.doesNotHaveAnOfficeInHierarchyWithId(1L)).thenReturn(false);
        when(userOffice.identifiedBy(1L)).thenReturn(true);

        doAnswer(invocation -> {
            Office office = invocation.getArgument(0);
            ReflectionTestUtils.setField(office, "id", 2L);
            return office;
        }).when(officeRepositoryWrapper).saveAndFlush(any(Office.class));

        OfficeCreateRequest request = OfficeCreateRequest.builder()
                .name("Test Branch")
                .parentId(1L)
                .openingDate("2023-01-01")
                .externalId("BRANCH-01")
                .build();

        OfficeCreateResponse response = service.createOffice(request);

        assertNotNull(response);
        assertEquals(2L, response.getOfficeId());
        assertEquals(2L, response.getResourceId());
        verify(officeRepositoryWrapper).save(any(Office.class));
    }

    @Test
    void createOfficeWithNonIsoFormatAndLocaleSuccess() {
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(userOffice);
        when(userOffice.getId()).thenReturn(1L);
        when(officeRepositoryWrapper.findOfficeHierarchy(1L)).thenReturn(userOffice);
        when(userOffice.doesNotHaveAnOfficeInHierarchyWithId(1L)).thenReturn(false);
        when(userOffice.identifiedBy(1L)).thenReturn(true);

        doAnswer(invocation -> {
            Office office = invocation.getArgument(0);
            ReflectionTestUtils.setField(office, "id", 3L);
            assertEquals(LocalDate.of(2007, 7, 1), office.getOpeningDate());
            return office;
        }).when(officeRepositoryWrapper).saveAndFlush(any(Office.class));

        OfficeCreateRequest request = OfficeCreateRequest.builder()
                .name("French Branch")
                .parentId(1L)
                .openingDate("01 juillet 2007")
                .dateFormat("dd MMMM yyyy")
                .locale("fr")
                .build();

        OfficeCreateResponse response = service.createOffice(request);

        assertNotNull(response);
        assertEquals(3L, response.getOfficeId());
    }

    @Test
    void createOfficeUnauthorizedThrowsNoAuthorizationException() {
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(userOffice);
        when(userOffice.getId()).thenReturn(1L);
        when(officeRepositoryWrapper.findOfficeHierarchy(1L)).thenReturn(userOffice);
        when(userOffice.doesNotHaveAnOfficeInHierarchyWithId(5L)).thenReturn(true);

        OfficeCreateRequest request = OfficeCreateRequest.builder()
                .name("Unauthorized Branch")
                .parentId(5L)
                .openingDate("2023-01-01")
                .build();

        assertThrows(NoAuthorizationException.class, () -> service.createOffice(request));
    }

    @Test
    void createOfficeDuplicateNameThrowsPlatformDataIntegrityException() {
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(userOffice);
        when(userOffice.getId()).thenReturn(1L);
        when(officeRepositoryWrapper.findOfficeHierarchy(1L)).thenReturn(userOffice);
        when(userOffice.doesNotHaveAnOfficeInHierarchyWithId(1L)).thenReturn(false);
        when(userOffice.identifiedBy(1L)).thenReturn(true);

        DataIntegrityViolationException rootCause = new DataIntegrityViolationException("name_org unique constraint");
        when(officeRepositoryWrapper.saveAndFlush(any(Office.class))).thenThrow(rootCause);

        OfficeCreateRequest request = OfficeCreateRequest.builder()
                .name("Duplicate Branch")
                .parentId(1L)
                .openingDate("2023-01-01")
                .build();

        assertThrows(PlatformDataIntegrityException.class, () -> service.createOffice(request));
    }

    @Test
    void updateOfficeSuccess() {
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(userOffice);
        when(userOffice.getId()).thenReturn(1L);
        when(officeRepositoryWrapper.findOfficeHierarchy(1L)).thenReturn(userOffice);
        when(userOffice.doesNotHaveAnOfficeInHierarchyWithId(2L)).thenReturn(false);

        Office headOffice = Office.headOffice("Head Office", LocalDate.of(2020, 1, 1), ExternalIdFactory.produce("HO"));
        ReflectionTestUtils.setField(headOffice, "id", 1L);

        Office branchOffice = Office.create(headOffice, "Old Branch", LocalDate.of(2022, 1, 1), ExternalIdFactory.produce("OLD-EXT"));
        ReflectionTestUtils.setField(branchOffice, "id", 2L);

        when(officeRepositoryWrapper.findOfficeHierarchy(2L)).thenReturn(branchOffice);

        OfficeUpdateRequest request = OfficeUpdateRequest.builder()
                .id(2L)
                .name("Renamed Branch")
                .openingDate("2022-06-01")
                .externalId("NEW-EXT")
                .build();

        OfficeUpdateResponse response = service.updateOffice(request);

        assertNotNull(response);
        assertEquals(2L, response.getOfficeId());
        assertEquals("Renamed Branch", response.getChanges().get("name"));
        assertEquals("2022-06-01", response.getChanges().get("openingDate"));
        assertEquals("NEW-EXT", response.getChanges().get("externalId"));
        verify(officeRepositoryWrapper).saveAndFlush(branchOffice);
    }

    @Test
    void updateOfficeWithNonIsoFormatAndLocaleSuccess() {
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(userOffice);
        when(userOffice.getId()).thenReturn(1L);
        when(officeRepositoryWrapper.findOfficeHierarchy(1L)).thenReturn(userOffice);
        when(userOffice.doesNotHaveAnOfficeInHierarchyWithId(2L)).thenReturn(false);

        Office headOffice = Office.headOffice("Head Office", LocalDate.of(2020, 1, 1), ExternalIdFactory.produce("HO"));
        ReflectionTestUtils.setField(headOffice, "id", 1L);

        Office branchOffice = Office.create(headOffice, "Old Branch", LocalDate.of(2007, 7, 1), ExternalIdFactory.produce("OLD-EXT"));
        ReflectionTestUtils.setField(branchOffice, "id", 2L);

        when(officeRepositoryWrapper.findOfficeHierarchy(2L)).thenReturn(branchOffice);

        OfficeUpdateRequest request = OfficeUpdateRequest.builder()
                .id(2L)
                .name("French Branch")
                .openingDate("15 août 2010")
                .dateFormat("dd MMMM yyyy")
                .locale("fr")
                .build();

        OfficeUpdateResponse response = service.updateOffice(request);

        assertNotNull(response);
        assertEquals(2L, response.getOfficeId());
        assertEquals("French Branch", response.getChanges().get("name"));
        assertEquals("15 août 2010", response.getChanges().get("openingDate"));
        assertEquals("dd MMMM yyyy", response.getChanges().get("dateFormat"));
        assertEquals("fr", response.getChanges().get("locale"));
        assertEquals(LocalDate.of(2010, 8, 15), branchOffice.getOpeningLocalDate());
        verify(officeRepositoryWrapper).saveAndFlush(branchOffice);
    }

    @Test
    void updateOfficeRootOfficeParentThrowsRootOfficeParentCannotBeUpdated() {
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(userOffice);
        when(userOffice.getId()).thenReturn(1L);

        Office headOffice = Office.headOffice("Head Office", LocalDate.of(2020, 1, 1), ExternalIdFactory.produce("HO"));
        ReflectionTestUtils.setField(headOffice, "id", 1L);

        when(officeRepositoryWrapper.findOfficeHierarchy(1L)).thenReturn(headOffice);

        OfficeUpdateRequest request = OfficeUpdateRequest.builder()
                .id(1L)
                .parentId(2L)
                .build();

        assertThrows(RootOfficeParentCannotBeUpdated.class, () -> service.updateOffice(request));
    }
}
