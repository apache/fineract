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
package org.apache.fineract.infrastructure.bulkimport.populator.centers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.bulkimport.populator.GroupSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.PersonnelSheetPopulator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class CentersWorkbookPopulatorTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";

    @Test
    void officeNameWithCharactersInvalidInDefinedNameIsSanitised() throws Exception {
        List<OfficeData> offices = List.of(office(3L, "Branch - North"));
        List<StaffData> staff = List.of(staff(1L, "Officer, One", "Branch - North"));

        try (Workbook workbook = new HSSFWorkbook()) {
            CentersWorkbookPopulator populator = populator(offices, staff);

            assertDoesNotThrow(() -> populator.populate(workbook, DATE_FORMAT));

            assertEquals(List.of("Staff_Branch___North"), definedNamesStartingWith(workbook, "Staff_"));
        }
    }

    @Test
    void officesCollidingOnNameEmitOneStaffDefinedName() throws Exception {
        List<OfficeData> offices = List.of(office(1L, "Head Office"), office(2L, "head office"));
        List<StaffData> staff = List.of(staff(1L, "Officer, One", "Head Office"), staff(2L, "Officer, Two", "head office"));

        try (Workbook workbook = new HSSFWorkbook()) {
            CentersWorkbookPopulator populator = populator(offices, staff);

            assertDoesNotThrow(() -> populator.populate(workbook, DATE_FORMAT));

            assertEquals(1, definedNamesStartingWith(workbook, "Staff_").size());
        }
    }

    private static CentersWorkbookPopulator populator(List<OfficeData> offices, List<StaffData> staff) {
        return new CentersWorkbookPopulator(new OfficeSheetPopulator(offices), new PersonnelSheetPopulator(staff, offices),
                new GroupSheetPopulator(List.of(), offices));
    }

    private static OfficeData office(Long id, String name) {
        OfficeData office = mock(OfficeData.class);
        when(office.getId()).thenReturn(id);
        when(office.getName()).thenReturn(name);
        when(office.getOpeningDate()).thenReturn(LocalDate.of(2020, 1, 1));
        return office;
    }

    private static StaffData staff(Long id, String displayName, String officeName) {
        StaffData staff = mock(StaffData.class);
        when(staff.getId()).thenReturn(id);
        when(staff.getDisplayName()).thenReturn(displayName);
        when(staff.getOfficeName()).thenReturn(officeName);
        return staff;
    }

    private static List<String> definedNamesStartingWith(Workbook workbook, String prefix) {
        return workbook.getAllNames().stream().map(Name::getNameName).filter(name -> name.startsWith(prefix)).toList();
    }
}
