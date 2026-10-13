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
package org.apache.fineract.organisation.teller.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.teller.domain.Cashier;
import org.apache.fineract.organisation.teller.domain.Teller;
import org.apache.fineract.organisation.teller.exception.CashierAlreadyAllocated;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class CashierTransactionDataValidatorTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private CashierTransactionDataValidator validator;
    private Teller teller;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        validator = new CashierTransactionDataValidator(mock(TellerManagementReadPlatformService.class), jdbcTemplate);
        teller = new Teller().setStartDate(LocalDate.of(2026, 1, 1));
    }

    private Cashier cashier(boolean fullDay, String startTime, String endTime) {
        Staff staff = mock(Staff.class);
        when(staff.getId()).thenReturn(1L);
        return new Cashier().setStaff(staff).setStartDate(LocalDate.of(2026, 10, 1)).setEndDate(LocalDate.of(2026, 10, 31))
                .setIsFullDay(fullDay).setStartTime(startTime).setEndTime(endTime);
    }

    @SuppressWarnings("unchecked")
    private void existingCashiers(CashierTransactionDataValidator.ExistingShift... shifts) {
        when(jdbcTemplate.query(anyString(), anyMap(), any(RowMapper.class))).thenReturn(List.of(shifts));
    }

    private static CashierTransactionDataValidator.ExistingShift partDay(String startTime, String endTime) {
        return new CashierTransactionDataValidator.ExistingShift(false, startTime, endTime);
    }

    private static CashierTransactionDataValidator.ExistingShift fullDay() {
        return new CashierTransactionDataValidator.ExistingShift(true, " ", " ");
    }

    @Test
    void parseTimeReadsStoredCashierTimes() {
        assertEquals(LocalTime.of(9, 30), CashierTransactionDataValidator.parseTime("09:30"));
        assertEquals(LocalTime.of(9, 5), CashierTransactionDataValidator.parseTime("9:5"));
        assertEquals(LocalTime.of(0, 0), CashierTransactionDataValidator.parseTime("00:00"));
        assertNull(CashierTransactionDataValidator.parseTime(" "));
        assertNull(CashierTransactionDataValidator.parseTime(null));
        assertNull(CashierTransactionDataValidator.parseTime("ab:cd"));
    }

    @Test
    void isBetweenIsInclusiveAndFalseForNull() {
        assertTrue(CashierTransactionDataValidator.isBetween(LocalTime.of(9, 0), LocalTime.of(9, 0), LocalTime.of(13, 0)));
        assertTrue(CashierTransactionDataValidator.isBetween(LocalTime.of(13, 0), LocalTime.of(9, 0), LocalTime.of(13, 0)));
        assertFalse(CashierTransactionDataValidator.isBetween(LocalTime.of(13, 1), LocalTime.of(9, 0), LocalTime.of(13, 0)));
        assertFalse(CashierTransactionDataValidator.isBetween(null, LocalTime.of(9, 0), LocalTime.of(13, 0)));
    }

    @Test
    void partDayCashierQueryDoesNotUseTimeFunction() {
        existingCashiers();
        validator.validateCashierAllowedDateAndTime(cashier(false, "09:00", "13:00"), teller);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sql.capture(), anyMap(), any(RowMapper.class));
        assertFalse(sql.getValue().toUpperCase().contains("TIME("), sql.getValue());
    }

    @Test
    void partDayCashierOverlappingAnotherShiftIsRejected() {
        existingCashiers(partDay("12:00", "17:00"));
        assertThrows(CashierAlreadyAllocated.class,
                () -> validator.validateCashierAllowedDateAndTime(cashier(false, "09:00", "13:00"), teller));
    }

    @Test
    void partDayCashierInAnotherShiftIsAllowed() {
        existingCashiers(partDay("14:00", "17:00"));
        assertDoesNotThrow(() -> validator.validateCashierAllowedDateAndTime(cashier(false, "9:0", "13:0"), teller));
    }

    @Test
    void fullDayCashierOverlappingAnyAssignmentIsRejected() {
        existingCashiers(partDay("14:00", "17:00"));
        assertThrows(CashierAlreadyAllocated.class, () -> validator.validateCashierAllowedDateAndTime(cashier(true, null, null), teller));
    }

    @Test
    void cashierWithoutOverlappingAssignmentIsAllowed() {
        existingCashiers();
        assertDoesNotThrow(() -> validator.validateCashierAllowedDateAndTime(cashier(true, null, null), teller));
    }

    @Test
    void partDayCashierInsideAnotherShiftIsRejected() {
        // The case the old check missed: neither end of the existing shift falls inside the new one
        existingCashiers(partDay("08:00", "18:00"));
        assertThrows(CashierAlreadyAllocated.class,
                () -> validator.validateCashierAllowedDateAndTime(cashier(false, "10:00", "12:00"), teller));
    }

    @Test
    void partDayCashierAroundAnotherShiftIsRejected() {
        existingCashiers(partDay("10:00", "12:00"));
        assertThrows(CashierAlreadyAllocated.class,
                () -> validator.validateCashierAllowedDateAndTime(cashier(false, "08:00", "18:00"), teller));
    }

    @Test
    void shiftsSharingAnEndTimeAreRejected() {
        existingCashiers(partDay("13:00", "17:00"));
        assertThrows(CashierAlreadyAllocated.class,
                () -> validator.validateCashierAllowedDateAndTime(cashier(false, "09:00", "13:00"), teller));
    }

    @Test
    void partDayCashierDuringAFullDayAssignmentIsRejected() {
        existingCashiers(fullDay());
        assertThrows(CashierAlreadyAllocated.class,
                () -> validator.validateCashierAllowedDateAndTime(cashier(false, "09:00", "13:00"), teller));
    }

    @Test
    void existingShiftWithUnreadableTimesIsTreatedAsOverlapping() {
        existingCashiers(partDay("ab:cd", " "));
        assertThrows(CashierAlreadyAllocated.class,
                () -> validator.validateCashierAllowedDateAndTime(cashier(false, "09:00", "13:00"), teller));
    }

    @Test
    void newAssignmentChecksOnlyOverlappingDatesOfTheSameStaff() {
        existingCashiers();
        validator.validateCashierAllowedDateAndTime(cashier(false, "09:00", "13:00"), teller);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> params = ArgumentCaptor.forClass(Map.class);
        verify(jdbcTemplate).query(sql.capture(), params.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("c.start_date <= :endDate AND c.end_date >= :fromDate"), sql.getValue());
        assertFalse(sql.getValue().contains(":cashierId"), sql.getValue());
        assertEquals(1L, params.getValue().get("staffId"));
    }

    @Test
    void updatedAssignmentIsNotComparedWithItself() {
        existingCashiers();
        Cashier cashier = cashier(false, "09:00", "13:00");
        ReflectionTestUtils.setField(cashier, "id", 7L);
        validator.validateCashierAllowedDateAndTime(cashier, teller);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> params = ArgumentCaptor.forClass(Map.class);
        verify(jdbcTemplate).query(sql.capture(), params.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("c.id <> :cashierId"), sql.getValue());
        assertEquals(7L, params.getValue().get("cashierId"));
    }

    @Test
    void timesOverlapIsInclusiveAndTreatsMissingTimesAsOverlap() {
        LocalTime nine = LocalTime.of(9, 0);
        LocalTime one = LocalTime.of(13, 0);
        assertTrue(CashierTransactionDataValidator.timesOverlap(nine, one, one, LocalTime.of(17, 0)));
        assertFalse(CashierTransactionDataValidator.timesOverlap(nine, one, LocalTime.of(13, 1), LocalTime.of(17, 0)));
        assertTrue(CashierTransactionDataValidator.timesOverlap(nine, one, null, LocalTime.of(17, 0)));
    }
}
