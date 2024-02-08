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
import java.time.OffsetDateTime;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
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
            final JdbcTemplate jdbcTemplate) {
        this.tellerManagementReadPlatformService = tellerManagementReadPlatformService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void validateSettleCashAndCashOutTransactions(final Long cashierId, String currencyCode, final BigDecimal transactionAmount) {
        final Integer offset = null;
        final Integer limit = null;
        final String orderBy = null;
        final String sortOrder = null;
        final LocalDate fromDate = null;
        final LocalDate toDate = null;
        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);
        final CashierTransactionsWithSummaryData cashierTxnWithSummary = this.tellerManagementReadPlatformService
                .retrieveCashierTransactionsWithSummary(cashierId, false, fromDate, toDate, currencyCode, searchParameters);
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
        // to validate cashier date range in range of teller date range
        if (DateUtils.isBefore(fromDate, tellerFromDate) || DateUtils.isBefore(endDate, tellerFromDate)
                || (tellerEndDate != null && (DateUtils.isAfter(fromDate, tellerEndDate) || DateUtils.isAfter(endDate, tellerEndDate)))) {
            throw new CashierDateRangeOutOfTellerDateRangeException();
        }
        // to validate cashier has not been assigned for same duration
        String sql = "select count(*) from m_cashiers c where c.staff_id = " + staffId + " AND " + "(('" + fromDate
                + "' BETWEEN c.start_date AND c.end_date OR '" + endDate + "' BETWEEN c.start_date AND c.end_date )"
                + " OR ( c.start_date BETWEEN '" + fromDate + "' AND '" + endDate + "' OR c.end_date BETWEEN '" + fromDate + "' AND '"
                + endDate + "'))";
        if (!cashier.getIsFullDay()) {
            String startTime = cashier.getStartTime();
            String endTime = cashier.getEndTime();
            sql = sql + " AND ( Time(c.start_time) BETWEEN TIME('" + startTime + "') and TIME('" + endTime
                    + "') or Time(c.end_time) BETWEEN TIME('" + startTime + "') and TIME('" + endTime + "')) ";
        }
        int count = this.jdbcTemplate.queryForObject(sql, Integer.class); // NOSONAR
        if (count > 0) {
            throw new CashierAlreadyAlloacated();
        }
    }

    public void validateOnLoanDisbursal(AppUser user, String currencyCode, BigDecimal transactionAmount) {
        LocalDate tenantDate = DateUtils.getLocalDateOfTenant();
        OffsetDateTime tenantDateTime = DateUtils.getOffsetDateTimeOfTenant();
        if (user.getStaff() != null) {
            String sql = "select c.id from m_cashiers c where c.staff_id = " + user.getStaff().getId() + " AND (case when c.full_day then '"
                    + tenantDate + "' BETWEEN c.start_date AND c.end_date else ('" + tenantDate
                    + "' BETWEEN c.start_date AND c.end_date and TIME('" + tenantDateTime
                    + "') BETWEEN TIME(c.start_time) AND TIME(c.end_time)) end)";
            try {
                Long cashierId = this.jdbcTemplate.queryForObject(sql, Long.class); // NOSONAR
                validateSettleCashAndCashOutTransactions(cashierId, currencyCode, transactionAmount);
            } catch (EmptyResultDataAccessException e) {
                LOG.error("Problem occurred in validateOnLoanDisbursal function", e);
            }
        }
    }
}
