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
package org.apache.fineract.organisation.staff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.staff.data.StaffUpdateRequest;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.mapper.StaffCreateRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StaffWriteServiceImplTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private StaffCreateRequestMapper staffCreateRequestMapper;

    @Mock
    private Office office;

    private StaffWriteServiceImpl service;
    private Staff staff;

    @BeforeEach
    void setUp() {
        service = new StaffWriteServiceImpl(staffRepository, officeRepository, staffCreateRequestMapper);
        staff = new Staff();
        staff.setOffice(office);
        staff.setFirstname("Jane");
        staff.setLastname("Doe");
        staff.setJoiningDate(LocalDate.of(2011, 9, 20));
        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
    }

    @Test
    void updateStaffAppliesJoiningDate() {
        var request = StaffUpdateRequest.builder().id(1L).joiningDate("15 March 2021").dateFormat("dd MMMM yyyy").locale("en").build();

        var response = service.updateStaff(request);

        assertEquals(LocalDate.of(2021, 3, 15), staff.getJoiningDate());
        assertEquals("15 March 2021", response.getChanges().get(StaffUpdateRequest.Fields.joiningDate));
        verify(staffRepository).saveAndFlush(staff);
    }

    @Test
    void updateStaffIgnoresUnchangedJoiningDate() {
        var request = StaffUpdateRequest.builder().id(1L).joiningDate("20 September 2011").dateFormat("dd MMMM yyyy").locale("en").build();

        var response = service.updateStaff(request);

        assertEquals(LocalDate.of(2011, 9, 20), staff.getJoiningDate());
        assertNull(response.getChanges());
        verify(staffRepository, never()).saveAndFlush(staff);
    }
}
