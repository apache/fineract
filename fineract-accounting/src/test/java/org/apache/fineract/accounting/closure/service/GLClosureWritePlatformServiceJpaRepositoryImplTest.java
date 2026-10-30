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
package org.apache.fineract.accounting.closure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.accounting.closure.data.GLClosureCreateRequest;
import org.apache.fineract.accounting.closure.data.GLClosureCreateResponse;
import org.apache.fineract.accounting.closure.data.GLClosureDeleteRequest;
import org.apache.fineract.accounting.closure.data.GLClosureDeleteResponse;
import org.apache.fineract.accounting.closure.data.GLClosureUpdateRequest;
import org.apache.fineract.accounting.closure.data.GLClosureUpdateResponse;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.closure.exception.GLClosureDuplicateException;
import org.apache.fineract.accounting.closure.exception.GLClosureInvalidDeleteException;
import org.apache.fineract.accounting.closure.exception.GLClosureInvalidException;
import org.apache.fineract.accounting.closure.exception.GLClosureNotFoundException;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class GLClosureWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private GLClosureRepository glClosureRepository;

    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;

    @Mock
    private Office office;

    @InjectMocks
    private GLClosureWritePlatformServiceJpaRepositoryImpl underTest;

    private static final Long OFFICE_ID = 1L;
    private static final Long CLOSURE_ID = 10L;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now())));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void testCreateGLClosureSuccess() {
        final LocalDate closingDate = LocalDate.now().minusDays(1);
        final GLClosureCreateRequest request = GLClosureCreateRequest.builder().officeId(OFFICE_ID).closingDate(closingDate)
                .comments("Test closure").build();

        when(officeRepositoryWrapper.findOneWithNotFoundDetection(OFFICE_ID)).thenReturn(office);
        when(glClosureRepository.getLatestGLClosureByBranch(OFFICE_ID)).thenReturn(null);
        when(glClosureRepository.saveAndFlush(any(GLClosure.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final GLClosureCreateResponse response = underTest.createGLClosure(request);

        assertNotNull(response);
        assertEquals(OFFICE_ID, response.getOfficeId());
        verify(glClosureRepository).saveAndFlush(any(GLClosure.class));
    }

    @Test
    void testCreateGLClosureFutureDateThrowsException() {
        final LocalDate futureDate = LocalDate.now().plusDays(5);
        final GLClosureCreateRequest request = GLClosureCreateRequest.builder().officeId(OFFICE_ID).closingDate(futureDate).build();

        when(officeRepositoryWrapper.findOneWithNotFoundDetection(OFFICE_ID)).thenReturn(office);

        assertThrows(GLClosureInvalidException.class, () -> underTest.createGLClosure(request));
    }

    @Test
    void testCreateGLClosureBeforeExistingClosureThrowsException() {
        final LocalDate closingDate = LocalDate.now().minusDays(10);
        final LocalDate latestClosingDate = LocalDate.now().minusDays(5);

        final GLClosure latestClosure = new GLClosure(office, latestClosingDate, "Latest");

        final GLClosureCreateRequest request = GLClosureCreateRequest.builder().officeId(OFFICE_ID).closingDate(closingDate).build();

        when(officeRepositoryWrapper.findOneWithNotFoundDetection(OFFICE_ID)).thenReturn(office);
        when(glClosureRepository.getLatestGLClosureByBranch(OFFICE_ID)).thenReturn(latestClosure);

        assertThrows(GLClosureInvalidException.class, () -> underTest.createGLClosure(request));
    }

    @Test
    void testCreateGLClosureDuplicateThrowsException() {
        final LocalDate closingDate = LocalDate.now().minusDays(1);
        final GLClosureCreateRequest request = GLClosureCreateRequest.builder().officeId(OFFICE_ID).closingDate(closingDate).build();

        when(officeRepositoryWrapper.findOneWithNotFoundDetection(OFFICE_ID)).thenReturn(office);
        when(glClosureRepository.getLatestGLClosureByBranch(OFFICE_ID)).thenReturn(null);
        when(glClosureRepository.saveAndFlush(any(GLClosure.class)))
                .thenThrow(new DataIntegrityViolationException("office_id_closing_date unique constraint violation"));

        assertThrows(GLClosureDuplicateException.class, () -> underTest.createGLClosure(request));
    }

    @Test
    void testUpdateGLClosureSuccess() {
        final GLClosure existingClosure = new GLClosure(office, LocalDate.now().minusDays(5), "Old comments");
        when(glClosureRepository.findById(CLOSURE_ID)).thenReturn(Optional.of(existingClosure));
        when(office.getId()).thenReturn(OFFICE_ID);

        final GLClosureUpdateRequest request = GLClosureUpdateRequest.builder().id(CLOSURE_ID).comments("Updated comments").build();

        final GLClosureUpdateResponse response = underTest.updateGLClosure(request);

        assertNotNull(response);
        assertEquals("Updated comments", response.getComments());
        assertEquals(OFFICE_ID, response.getOfficeId());
        verify(glClosureRepository).saveAndFlush(existingClosure);
    }

    @Test
    void testUpdateGLClosureNotFoundThrowsException() {
        when(glClosureRepository.findById(CLOSURE_ID)).thenReturn(Optional.empty());

        final GLClosureUpdateRequest request = GLClosureUpdateRequest.builder()
                .id(CLOSURE_ID)
                .comments("Updated comments")
                .build();

        assertThrows(GLClosureNotFoundException.class, () -> underTest.updateGLClosure(request));
    }

    @Test
    void testDeleteGLClosureSuccess() {
        final LocalDate closingDate = LocalDate.now().minusDays(5);
        final GLClosure existingClosure = new GLClosure(office, closingDate, "Closure to delete");
        when(office.getId()).thenReturn(OFFICE_ID);
        when(glClosureRepository.findById(CLOSURE_ID)).thenReturn(Optional.of(existingClosure));
        when(glClosureRepository.getLatestGLClosureByBranch(OFFICE_ID)).thenReturn(existingClosure);

        final GLClosureDeleteRequest request = GLClosureDeleteRequest.builder().id(CLOSURE_ID).build();

        final GLClosureDeleteResponse response = underTest.deleteGLClosure(request);

        assertNotNull(response);
        assertEquals(OFFICE_ID, response.getOfficeId());
        verify(glClosureRepository).delete(existingClosure);
    }

    @Test
    void testDeleteGLClosureNotLatestThrowsException() {
        final LocalDate closingDate = LocalDate.now().minusDays(10);
        final LocalDate laterClosingDate = LocalDate.now().minusDays(2);
        final GLClosure existingClosure = new GLClosure(office, closingDate, "Older closure");
        final GLClosure laterClosure = new GLClosure(office, laterClosingDate, "Later closure");

        when(office.getId()).thenReturn(OFFICE_ID);
        when(glClosureRepository.findById(CLOSURE_ID)).thenReturn(Optional.of(existingClosure));
        when(glClosureRepository.getLatestGLClosureByBranch(OFFICE_ID)).thenReturn(laterClosure);

        final GLClosureDeleteRequest request = GLClosureDeleteRequest.builder().id(CLOSURE_ID).build();

        assertThrows(GLClosureInvalidDeleteException.class, () -> underTest.deleteGLClosure(request));
    }

    @Test
    void testDeleteGLClosureNotFoundThrowsException() {
        when(glClosureRepository.findById(CLOSURE_ID)).thenReturn(Optional.empty());

        final GLClosureDeleteRequest request = GLClosureDeleteRequest.builder()
                .id(CLOSURE_ID)
                .build();

        assertThrows(GLClosureNotFoundException.class, () -> underTest.deleteGLClosure(request));
    }
}
