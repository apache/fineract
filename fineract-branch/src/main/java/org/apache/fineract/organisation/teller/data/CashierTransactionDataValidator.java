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

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.teller.domain.Cashier;
import org.apache.fineract.organisation.teller.domain.Teller;
import org.apache.fineract.organisation.teller.exception.CashierAlreadyAllocated;
import org.apache.fineract.organisation.teller.exception.CashierDateRangeOutOfTellerDateRangeException;
import org.apache.fineract.organisation.teller.exception.CashierInsufficientAmountException;
import org.apache.fineract.organisation.teller.serialization.TellerCommandFromApiJsonDeserializer;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CashierTransactionDataValidator {

    private final TellerManagementReadPlatformService tellerManagementReadPlatformService;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final Logger LOG = LoggerFactory.getLogger(CashierTransactionDataValidator.class);

    @Autowired
    public CashierTransactionDataValidator(final TellerManagementReadPlatformService tellerManagementReadPlatformService,
            final NamedParameterJdbcTemplate jdbcTemplate) {
        this.tellerManagementReadPlatformService = tellerManagementReadPlatformService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void validateSettleCashAndCashOutTransactions(final Long cashierId, String currencyCode, final BigDecimal transactionAmount) {
        final SearchParameters searchParameters = SearchParameters.builder().build();
        final CashierTransactionsWithSummaryData cashierTxnWithSummary = this.tellerManagementReadPlatformService
                .retrieveCashierTransactionsWithSummary(cashierId, false, null, null, currencyCode, searchParameters);
        if (MathUtil.isGreaterThan(transactionAmount, cashierTxnWithSummary.getNetCash())) {
            throw new CashierInsufficientAmountException();
        }
    }

    public void validateSettleCashAndCashOutTransactions(final Long cashierId, JsonCommand command) {
        String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("txnAmount");
        validateSettleCashAndCashOutTransactions(cashierId, currencyCode, transactionAmount);
    }

    public void validateCashierAllowedDateAndTime(final Cashier cashier, final Teller teller) {
        Long staffId = cashier.getStaff().getId();
        final LocalDate fromDate = cashier.getStartDate();
        final LocalDate endDate = cashier.getEndDate();
        final LocalDate tellerFromDate = teller.getStartDate();
        final LocalDate tellerEndDate = teller.getEndDate();
        // Validate cashier date range in range of teller date range
        if (DateUtils.isBefore(fromDate, tellerFromDate) || DateUtils.isBefore(endDate, tellerFromDate)
                || (tellerEndDate != null && (DateUtils.isAfter(fromDate, tellerEndDate) || DateUtils.isAfter(endDate, tellerEndDate)))) {
            throw new CashierDateRangeOutOfTellerDateRangeException();
        }

        // A part-day shift must end after it starts on the same day. Shifts past midnight (e.g. 22:00-02:00) were never
        // supported: the cashier on duty check only compares the time of day
        final boolean fullDay = isFullDay(cashier);
        final LocalTime startTime = fullDay ? null : startTimeOf(cashier);
        final LocalTime endTime = fullDay ? null : endTimeOf(cashier);
        if (!fullDay && (startTime == null || endTime == null || !endTime.isAfter(startTime))) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            new DataValidatorBuilder(dataValidationErrors).resource(TellerCommandFromApiJsonDeserializer.TELLER)
                    .parameter(TellerCommandFromApiJsonDeserializer.HOUR_END_TIME).value(cashier.getEndTime())
                    .failWithCodeAndMessage("must.be.after.start.time", "The end time of a part-day cashier must be after its start time.");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        // Validate the staff member has no other cashier assignment whose dates and hours overlap with this one: either
        // of them full day, or hours overlapping (end times included). An updated assignment is not compared with
        // itself
        final boolean allocated = findCashiersOfStaff(staffId, fromDate, endDate).stream()
                .filter(existing -> !Objects.equals(existing.getId(), cashier.getId()))
                .anyMatch(existing -> fullDay || isFullDay(existing) || hoursOverlap(startTime, endTime, existing));
        if (allocated) {
            throw new CashierAlreadyAllocated();
        }
    }

    public void validateOnLoanDisbursal(AppUser user, String currencyCode, BigDecimal transactionAmount) {
        if (user.getStaff() != null) {
            final Long staffId = user.getStaff().getId();
            // One "now", so the date and the time of day cannot disagree around midnight
            final OffsetDateTime tenantDateTime = DateUtils.getOffsetDateTimeOfTenant();
            final LocalDate tenantDate = tenantDateTime.toLocalDate();
            final LocalTime tenantTime = tenantDateTime.toLocalTime();

            final List<Long> onDuty = findCashiersOfStaff(staffId, tenantDate, tenantDate).stream()
                    .filter(cashier -> isOnDuty(cashier, tenantTime)).map(Cashier::getId).toList();
            final Long cashierId = DataAccessUtils.singleResult(onDuty);
            if (cashierId == null) {
                // Normal when the user is not on duty as a cashier: there is no cashier balance to check
                LOG.debug("No cashier on duty for staff {}, cash disbursal not checked against a cashier balance", staffId);
                return;
            }
            validateSettleCashAndCashOutTransactions(cashierId, currencyCode, transactionAmount);
        }
    }

    /**
     * The cashier assignments of a staff member whose dates overlap with the given ones. Only the id, full day flag and
     * times are read; the times are compared in Java with the {@link Cashier} getters, as TIME() does not exist on
     * PostgreSQL. Plain SQL rather than JPQL, so the query does not flush pending changes (of the cashier being
     * updated, or of the loan being disbursed).
     */
    private List<Cashier> findCashiersOfStaff(final Long staffId, final LocalDate fromDate, final LocalDate toDate) {
        final String sql = "SELECT c.id, c.full_day, c.start_time, c.end_time FROM m_cashiers c WHERE c.staff_id = :staffId "
                + "AND c.start_date <= :toDate AND c.end_date >= :fromDate";
        final Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("staffId", staffId);
        paramMap.put("fromDate", fromDate);
        paramMap.put("toDate", toDate);
        return jdbcTemplate.query(sql, paramMap, (rs, rowNum) -> {
            final Cashier cashier = new Cashier().setIsFullDay(rs.getBoolean("full_day")).setStartTime(rs.getString("start_time"))
                    .setEndTime(rs.getString("end_time"));
            cashier.setId(rs.getLong("id"));
            return cashier;
        });
    }

    private static boolean isFullDay(final Cashier cashier) {
        return Boolean.TRUE.equals(cashier.getIsFullDay());
    }

    /**
     * Full day, or the time within its hours (inclusive). A part-day cashier whose times cannot be read is not on duty.
     */
    private static boolean isOnDuty(final Cashier cashier, final LocalTime time) {
        if (isFullDay(cashier)) {
            return true;
        }
        final LocalTime startTime = startTimeOf(cashier);
        final LocalTime endTime = endTimeOf(cashier);
        return startTime != null && endTime != null && !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    /**
     * Whether the hours of an existing part-day assignment overlap with the given ones, end times included (a shift
     * ending at 12:00 and one starting at 12:00 overlap). Times that cannot be read count as an overlap, so a bad
     * record never lets a second assignment through; the warning logged names the record to fix.
     */
    private static boolean hoursOverlap(final LocalTime startTime, final LocalTime endTime, final Cashier existing) {
        final LocalTime existingStart = startTimeOf(existing);
        final LocalTime existingEnd = endTimeOf(existing);
        if (existingStart == null || existingEnd == null) {
            return true;
        }
        return !startTime.isAfter(existingEnd) && !existingStart.isAfter(endTime);
    }

    private static LocalTime startTimeOf(final Cashier cashier) {
        return timeOf(cashier, "start", cashier.getStartTime(), cashier::getHourFromStartTime, cashier::getMinFromStartTime);
    }

    private static LocalTime endTimeOf(final Cashier cashier) {
        return timeOf(cashier, "end", cashier.getEndTime(), cashier::getHourFromEndTime, cashier::getMinFromEndTime);
    }

    /**
     * The time of a part-day cashier, or null when it cannot be read. For a saved cashier a warning names the record to
     * fix; a new one is rejected by the caller.
     */
    private static LocalTime timeOf(final Cashier cashier, final String which, final String storedTime, final Supplier<Long> hour,
            final Supplier<Long> minute) {
        try {
            final Long hourValue = hour.get();
            final Long minuteValue = minute.get();
            if (hourValue != null && minuteValue != null) {
                return LocalTime.of(Math.toIntExact(hourValue), Math.toIntExact(minuteValue));
            }
        } catch (NumberFormatException | IndexOutOfBoundsException | DateTimeException | ArithmeticException e) {
            LOG.debug("Cannot read {} time '{}' of cashier {}", which, storedTime, cashier.getId(), e);
        }
        if (cashier.getId() != null) {
            LOG.warn("Cashier {} is part day but its {} time '{}' cannot be read", cashier.getId(), which, storedTime);
        }
        return null;
    }
}
