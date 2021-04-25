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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.teller.domain.Cashier;
import org.apache.fineract.organisation.teller.domain.Teller;
import org.apache.fineract.organisation.teller.exception.CashierAlreadyAlloacated;
import org.apache.fineract.organisation.teller.exception.CashierDateRangeOutOfTellerDateRangeException;
import org.apache.fineract.organisation.teller.exception.CashierInsufficientAmountException;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CashierTransactionDataValidator {

    private final TellerManagementReadPlatformService tellerManagementReadPlatformService;
    private final JdbcTemplate jdbcTemplate;
    private static final Logger LOG = LoggerFactory.getLogger(CashierTransactionDataValidator.class);

    @Autowired
    public CashierTransactionDataValidator(final TellerManagementReadPlatformService tellerManagementReadPlatformService,
            final RoutingDataSource dataSource) {
        this.tellerManagementReadPlatformService = tellerManagementReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void validateSettleCashAndCashOutTransactions(final Long cashierId, String currencyCode, final BigDecimal transactionAmount) {

        final Integer offset = null;
        final Integer limit = null;
        final String orderBy = null;
        final String sortOrder = null;
        final Date fromDate = null;
        final Date toDate = null;
        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);
        final CashierTransactionsWithSummaryData cashierTxnWithSummary = this.tellerManagementReadPlatformService
                .retrieveCashierTransactionsWithSummary(cashierId, false, fromDate, toDate, currencyCode, searchParameters);
        if (cashierTxnWithSummary.getNetCash().subtract(transactionAmount).compareTo(BigDecimal.ZERO) < 0) {
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
        final LocalDate fromDate = LocalDate.ofInstant(cashier.getStartDate().toInstant(), DateUtils.getDateTimeZoneOfTenant());
        final LocalDate endDate = LocalDate.ofInstant(cashier.getEndDate().toInstant(), DateUtils.getDateTimeZoneOfTenant());
        final LocalDate tellerFromDate = teller.getStartLocalDate();
        final LocalDate tellerEndDate = teller.getEndLocalDate();
        /**
         * to validate cashier date range in range of teller date range
         */
        if (fromDate.isBefore(tellerFromDate) || endDate.isBefore(tellerFromDate)
                || (tellerEndDate != null && (fromDate.isAfter(tellerEndDate) || endDate.isAfter(tellerEndDate)))) {
            throw new CashierDateRangeOutOfTellerDateRangeException();
        }
        /**
         * to validate cashier has not been assigned for same duration
         */
        StringBuilder sqlBuilder = new StringBuilder("");
        sqlBuilder.append("SELECT");
        sqlBuilder.append(" count(*) ");
        sqlBuilder.append("FROM m_cashiers c ");
        sqlBuilder.append("WHERE c.staff_id = ?");
        sqlBuilder.append("  AND (");
        sqlBuilder.append("    (");
        sqlBuilder.append("      ? BETWEEN c.start_date AND c.end_date");
        sqlBuilder.append("      OR ? BETWEEN c.start_date AND c.end_date");
        sqlBuilder.append("    )");
        sqlBuilder.append("    OR");
        sqlBuilder.append("    (");
        sqlBuilder.append("      c.start_date BETWEEN ? AND ?");
        sqlBuilder.append("      OR c.end_date BETWEEN ? AND ?");
        sqlBuilder.append("    )");
        sqlBuilder.append("  )");
        boolean nonfullDay = Boolean.FALSE.equals(cashier.isFullDay());
        String startTime = cashier.getStartTime();
        String endTime = cashier.getEndTime();
        if (nonfullDay) {
            sqlBuilder.append("  AND (");
            sqlBuilder.append("    time(c.start_time) BETWEEN time(?) AND time(?)");
            sqlBuilder.append("    OR time(c.end_time) BETWEEN time(?) AND time(?)");
            sqlBuilder.append("  )");
        }

        int count = this.jdbcTemplate.queryForObject(sqlBuilder.toString(), Integer.class, nonfullDay
                ? new Object[] { staffId, fromDate, endDate, fromDate, endDate, fromDate, endDate, startTime, endTime, startTime, endTime }
                : new Object[] { staffId, fromDate, endDate, fromDate, endDate, fromDate, endDate });
        if (count > 0) {
            throw new CashierAlreadyAlloacated();
        }
    }

    public void validateOnLoanDisbursal(AppUser user, String currencyCode, BigDecimal transactionAmount) {
        LocalDateTime localDateTime = DateUtils.getLocalDateTimeOfTenant();
        Staff staff = user.getStaff();
        if (staff != null) {
            StringBuilder sqlBuilder = new StringBuilder("");
            sqlBuilder.append("SELECT");
            sqlBuilder.append("  c.id ");
            sqlBuilder.append("FROM m_cashiers c ");
            sqlBuilder.append("WHERE c.staff_id = ?");
            sqlBuilder.append("  AND (");
            sqlBuilder.append("    CASE WHEN c.full_day THEN");
            sqlBuilder.append("      ?  BETWEEN c.start_date AND c.end_date");
            sqlBuilder.append("    ELSE (");
            sqlBuilder.append("      ?  BETWEEN c.start_date AND c.end_date");
            sqlBuilder.append("      AND time(?) BETWEEN time(c.start_time) AND time(c.end_time)");
            sqlBuilder.append("    )");
            sqlBuilder.append("    END");
            sqlBuilder.append("  )");
            try {
                Long staffID = staff.getId();
                LocalDate date = localDateTime.toLocalDate();
                Long cashierId = this.jdbcTemplate.queryForObject(sqlBuilder.toString(), Long.class, staffID, date, date,
                        ZonedDateTime.of(localDateTime, DateUtils.getDateTimeZoneOfTenant()));
                validateSettleCashAndCashOutTransactions(cashierId, currencyCode, transactionAmount);
            } catch (EmptyResultDataAccessException e) {
                LOG.error("Problem occurred in validateOnLoanDisbursal function", e);
            }
        }
    }
}
