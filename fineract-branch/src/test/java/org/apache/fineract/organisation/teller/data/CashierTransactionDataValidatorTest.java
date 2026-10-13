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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.teller.domain.Cashier;
import org.apache.fineract.organisation.teller.domain.Teller;
import org.apache.fineract.organisation.teller.exception.CashierAlreadyAllocated;
import org.apache.fineract.organisation.teller.exception.CashierInsufficientAmountException;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class CashierTransactionDataValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private static final String CURRENCY = "USD";

    private NamedParameterJdbcTemplate jdbcTemplate;
    private TellerManagementReadPlatformService tellerManagementReadPlatformService;
    private CashierTransactionDataValidator validator;
    private Teller teller;
    private MockedStatic<DateUtils> dateUtils;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        tellerManagementReadPlatformService = mock(TellerManagementReadPlatformService.class);
        validator = new CashierTransactionDataValidator(tellerManagementReadPlatformService, jdbcTemplate);
        teller = new Teller().setStartDate(LocalDate.of(2026, 1, 1));
    }

    @AfterEach
    void tearDown() {
        if (dateUtils != null) {
            dateUtils.close();
        }
    }

    private static Staff staff() {
        Staff staff = mock(Staff.class);
        when(staff.getId()).thenReturn(1L);
        return staff;
    }

    /** The assignment being created (no id) or updated (with an id). */
    private static Cashier cashier(Long id, boolean fullDay, String startTime, String endTime) {
        Cashier cashier = new Cashier().setStaff(staff()).setStartDate(LocalDate.of(2026, 10, 1)).setEndDate(LocalDate.of(2026, 10, 31))
                .setIsFullDay(fullDay).setStartTime(startTime).setEndTime(endTime);
        cashier.setId(id);
        return cashier;
    }

    private static Cashier newPartDay(String startTime, String endTime) {
        return cashier(null, false, startTime, endTime);
    }

    /** A stored assignment, as read from m_cashiers. */
    private static Cashier stored(long id, boolean fullDay, String startTime, String endTime) {
        Cashier cashier = new Cashier().setIsFullDay(fullDay).setStartTime(startTime).setEndTime(endTime);
        cashier.setId(id);
        return cashier;
    }

    private static Cashier storedPartDay(long id, String startTime, String endTime) {
        return stored(id, false, startTime, endTime);
    }

    private static Cashier storedFullDay(long id) {
        // Full day assignments are stored with blank times
        return stored(id, true, " ", " ");
    }

    @SuppressWarnings("unchecked")
    private void cashiersOfStaff(Cashier... cashiers) {
        when(jdbcTemplate.query(anyString(), anyMap(), any(RowMapper.class))).thenReturn(List.of(cashiers));
    }

    private void validate(Cashier cashier) {
        validator.validateCashierAllowedDateAndTime(cashier, teller);
    }

    @Test
    void partDayCashierOverlappingAnotherShiftIsRejected() {
        cashiersOfStaff(storedPartDay(1, "12:00", "17:00"));
        assertThrows(CashierAlreadyAllocated.class, () -> validate(newPartDay("09:00", "13:00")));
    }

    @Test
    void partDayCashierInAnotherShiftIsAllowed() {
        cashiersOfStaff(storedPartDay(1, "14:00", "17:00"));
        assertDoesNotThrow(() -> validate(newPartDay("9:00", "13:00")));
    }

    @Test
    void fullDayCashierOverlappingAnyAssignmentIsRejected() {
        cashiersOfStaff(storedPartDay(1, "14:00", "17:00"));
        assertThrows(CashierAlreadyAllocated.class, () -> validate(cashier(null, true, " ", " ")));
    }

    @Test
    void cashierWithoutOverlappingAssignmentIsAllowed() {
        cashiersOfStaff();
        assertDoesNotThrow(() -> validate(cashier(null, true, " ", " ")));
        assertDoesNotThrow(() -> validate(newPartDay("09:00", "13:00")));
    }

    @Test
    void partDayCashierInsideAnotherShiftIsRejected() {
        // The case the old check missed: neither end of the existing shift falls inside the new one
        cashiersOfStaff(storedPartDay(1, "08:00", "18:00"));
        assertThrows(CashierAlreadyAllocated.class, () -> validate(newPartDay("10:00", "12:00")));
    }

    @Test
    void partDayCashierAroundAnotherShiftIsRejected() {
        cashiersOfStaff(storedPartDay(1, "10:00", "12:00"));
        assertThrows(CashierAlreadyAllocated.class, () -> validate(newPartDay("08:00", "18:00")));
    }

    @Test
    void shiftsSharingAnEndTimeAreRejected() {
        cashiersOfStaff(storedPartDay(1, "13:00", "17:00"));
        assertThrows(CashierAlreadyAllocated.class, () -> validate(newPartDay("09:00", "13:00")));
    }

    @Test
    void shiftStartingAMinuteAfterAnotherEndsIsAllowed() {
        cashiersOfStaff(storedPartDay(1, "13:01", "17:00"));
        assertDoesNotThrow(() -> validate(newPartDay("09:00", "13:00")));
    }

    @Test
    void partDayCashierDuringAFullDayAssignmentIsRejected() {
        cashiersOfStaff(storedFullDay(1));
        assertThrows(CashierAlreadyAllocated.class, () -> validate(newPartDay("09:00", "13:00")));
    }

    @Test
    void existingShiftWithUnreadableTimesIsTreatedAsOverlapping() {
        cashiersOfStaff(storedPartDay(1, "ab:cd", " "));
        assertThrows(CashierAlreadyAllocated.class, () -> validate(newPartDay("09:00", "13:00")));
    }

    @Test
    void updatedAssignmentIsNotComparedWithItself() {
        // The stored version of the assignment being updated is returned too, as its dates overlap
        cashiersOfStaff(storedPartDay(7, "09:00", "13:00"));
        assertDoesNotThrow(() -> validate(cashier(7L, false, "10:00", "14:00")));
    }

    @Test
    void updatedAssignmentOverlappingAnotherOneIsRejected() {
        cashiersOfStaff(storedPartDay(7, "09:00", "13:00"), storedPartDay(8, "14:00", "17:00"));
        assertThrows(CashierAlreadyAllocated.class, () -> validate(cashier(7L, false, "12:00", "15:00")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void onlyAssignmentsOfTheSameStaffAndDatesAreRead() {
        cashiersOfStaff();
        validate(newPartDay("09:00", "13:00"));

        ArgumentCaptor<Map<String, Object>> params = ArgumentCaptor.forClass(Map.class);
        verify(jdbcTemplate).query(anyString(), params.capture(), any(RowMapper.class));
        assertEquals(Map.of("staffId", 1L, "fromDate", LocalDate.of(2026, 10, 1), "toDate", LocalDate.of(2026, 10, 31)), params.getValue());
    }

    @Test
    void partDayShiftEndingBeforeItStartsIsRejected() {
        // An overnight shift (22:00-02:00) is not supported: the end must be after the start on the same day
        PlatformApiDataValidationException failure = assertThrows(PlatformApiDataValidationException.class,
                () -> validate(newPartDay("22:00", "02:00")));
        assertEquals("validation.msg.teller.hourEndTime.must.be.after.start.time",
                failure.getErrors().get(0).getUserMessageGlobalisationCode());
        verify(jdbcTemplate, never()).query(anyString(), anyMap(), any(RowMapper.class));
    }

    @Test
    void partDayShiftEndingWhenItStartsIsRejected() {
        assertThrows(PlatformApiDataValidationException.class, () -> validate(newPartDay("09:00", "09:00")));
    }

    @Test
    void partDayShiftWithUnreadableTimeIsRejected() {
        assertThrows(PlatformApiDataValidationException.class, () -> validate(newPartDay("25:00", "26:00")));
    }

    // Loan disbursal in cash: the amount is checked against the cash of the cashier on duty

    private void now(int hour, int minute) {
        dateUtils = mockStatic(DateUtils.class);
        dateUtils.when(DateUtils::getOffsetDateTimeOfTenant).thenReturn(OffsetDateTime
                .of(LocalDateTime.of(TODAY.getYear(), TODAY.getMonth(), TODAY.getDayOfMonth(), hour, minute), ZoneOffset.UTC));
    }

    private void cashInHand(long cashierId, String netCash) {
        CashierTransactionsWithSummaryData summary = mock(CashierTransactionsWithSummaryData.class);
        when(summary.getNetCash()).thenReturn(new BigDecimal(netCash));
        when(tellerManagementReadPlatformService.retrieveCashierTransactionsWithSummary(eq(cashierId), eq(false), isNull(), isNull(),
                eq(CURRENCY), any())).thenReturn(summary);
    }

    private static AppUser cashierUser() {
        AppUser user = mock(AppUser.class);
        Staff staff = staff();
        when(user.getStaff()).thenReturn(staff);
        return user;
    }

    private void disburse(String amount) {
        validator.validateOnLoanDisbursal(cashierUser(), CURRENCY, new BigDecimal(amount));
    }

    private void verifyCashNotChecked() {
        verify(tellerManagementReadPlatformService, never()).retrieveCashierTransactionsWithSummary(any(), anyBoolean(), any(), any(),
                any(), any());
    }

    @Test
    void disbursalIsCheckedAgainstTheFullDayCashierOnDuty() {
        now(20, 0);
        cashiersOfStaff(storedFullDay(5));
        cashInHand(5, "100");

        assertThrows(CashierInsufficientAmountException.class, () -> disburse("500"));
        assertDoesNotThrow(() -> disburse("100"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void disbursalIsCheckedAgainstThePartDayCashierWithinItsHours() {
        now(10, 30);
        cashiersOfStaff(storedPartDay(5, "09:00", "13:00"));
        cashInHand(5, "100");

        assertThrows(CashierInsufficientAmountException.class, () -> disburse("500"));

        // Today's assignments of the user's staff member are read
        ArgumentCaptor<Map<String, Object>> params = ArgumentCaptor.forClass(Map.class);
        verify(jdbcTemplate).query(anyString(), params.capture(), any(RowMapper.class));
        assertEquals(Map.of("staffId", 1L, "fromDate", TODAY, "toDate", TODAY), params.getValue());
    }

    @Test
    void disbursalOutsideThePartDayCashierHoursIsNotChecked() {
        now(13, 1);
        cashiersOfStaff(storedPartDay(5, "09:00", "13:00"));

        assertDoesNotThrow(() -> disburse("500"));
        verifyCashNotChecked();
    }

    @Test
    void disbursalWithNoCashierOnDutyIsNotChecked() {
        now(10, 30);
        cashiersOfStaff();

        assertDoesNotThrow(() -> disburse("500"));
        verifyCashNotChecked();
    }
}
