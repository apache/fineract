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

import com.google.common.base.Splitter;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.teller.domain.Cashier;
import org.apache.fineract.organisation.teller.domain.Teller;
import org.apache.fineract.organisation.teller.exception.CashierAlreadyAllocated;
import org.apache.fineract.organisation.teller.exception.CashierDateRangeOutOfTellerDateRangeException;
import org.apache.fineract.organisation.teller.exception.CashierInsufficientAmountException;
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

        // Validate the staff member has no other cashier assignment whose dates and hours overlap with this one. The
        // times are compared here rather than in SQL, because TIME() does not exist on PostgreSQL
        String sql = "SELECT c.full_day, c.start_time, c.end_time FROM m_cashiers c WHERE c.staff_id = :staffId "
                + "AND c.start_date <= :endDate AND c.end_date >= :fromDate";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("staffId", staffId);
        paramMap.put("fromDate", fromDate);
        paramMap.put("endDate", endDate);
        if (cashier.getId() != null) {
            // When an existing assignment is updated, it must not be compared with itself
            sql += " AND c.id <> :cashierId";
            paramMap.put("cashierId", cashier.getId());
        }

        List<ExistingShift> overlappingDates = jdbcTemplate.query(sql, paramMap,
                (rs, rowNum) -> new ExistingShift(rs.getBoolean("full_day"), rs.getString("start_time"), rs.getString("end_time")));

        final boolean fullDay = Boolean.TRUE.equals(cashier.getIsFullDay());
        final LocalTime startTime = parseTime(cashier.getStartTime());
        final LocalTime endTime = parseTime(cashier.getEndTime());
        boolean allocated = overlappingDates.stream().anyMatch(existing -> fullDay || existing.fullDay()
                || timesOverlap(startTime, endTime, parseTime(existing.startTime()), parseTime(existing.endTime())));
        if (allocated) {
            throw new CashierAlreadyAllocated();
        }
    }

    /** A cashier assignment of the same staff member whose dates overlap with the one being validated. */
    record ExistingShift(boolean fullDay, String startTime, String endTime) {
    }

    public void validateOnLoanDisbursal(AppUser user, String currencyCode, BigDecimal transactionAmount) {
        LocalDate tenantDate = DateUtils.getLocalDateOfTenant();
        LocalTime tenantTime = DateUtils.getOffsetDateTimeOfTenant().toLocalTime();
        if (user.getStaff() != null) {
            // The cashier on duty: full day, or now within its hours (compared here, as TIME() does not exist on
            // PostgreSQL)
            String sql = "SELECT c.id, c.full_day, c.start_time, c.end_time FROM m_cashiers c WHERE c.staff_id = :staffId "
                    + "AND :tenantDate BETWEEN c.start_date AND c.end_date";

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("staffId", user.getStaff().getId());
            paramMap.put("tenantDate", tenantDate);

            List<Long> onDuty = jdbcTemplate.query(sql, paramMap, (rs, rowNum) -> {
                boolean onDutyNow = rs.getBoolean("full_day")
                        || isBetween(tenantTime, parseTime(rs.getString("start_time")), parseTime(rs.getString("end_time")));
                return onDutyNow ? rs.getLong("id") : null;
            }).stream().filter(Objects::nonNull).toList();

            Long cashierId = DataAccessUtils.singleResult(onDuty);
            if (cashierId == null) {
                LOG.error("Problem occurred in validateOnLoanDisbursal function: no cashier on duty for staff {}", user.getStaff().getId());
                return;
            }
            validateSettleCashAndCashOutTransactions(cashierId, currencyCode, transactionAmount);
        }
    }

    /**
     * Parses a cashier time as stored in m_cashiers ("hour:minute", e.g. "09:30" or "9:5"); null when blank or invalid,
     * as for a full day cashier.
     */
    static LocalTime parseTime(final String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        try {
            final List<String> parts = Splitter.on(':').trimResults().splitToList(time);
            if (parts.size() < 2) {
                return null;
            }
            final int seconds = parts.size() > 2 ? Integer.parseInt(parts.get(2)) : 0;
            return LocalTime.of(Integer.parseInt(parts.get(0)), Integer.parseInt(parts.get(1)), seconds);
        } catch (NumberFormatException | DateTimeException e) {
            return null;
        }
    }

    /**
     * Whether two shifts overlap: one starts no later than the other ends, end times included (a shift ending at 12:00
     * and one starting at 12:00 overlap). A time that is missing or cannot be read counts as an overlap, so a bad
     * record never lets a second assignment through.
     */
    static boolean timesOverlap(final LocalTime start, final LocalTime end, final LocalTime otherStart, final LocalTime otherEnd) {
        if (start == null || end == null || otherStart == null || otherEnd == null) {
            return true;
        }
        return !start.isAfter(otherEnd) && !otherStart.isAfter(end);
    }

    /** Same as SQL BETWEEN: inclusive, and false when any value is null. */
    static boolean isBetween(final LocalTime time, final LocalTime from, final LocalTime to) {
        return time != null && from != null && to != null && !time.isBefore(from) && !time.isAfter(to);
    }
}
