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
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.dao.EmptyResultDataAccessException;
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

        // Validate cashier has not been assigned for the same duration
        String sql = "SELECT COUNT(*) FROM m_cashiers c WHERE c.staff_id = :staffId AND "
                + "((:fromDate BETWEEN c.start_date AND c.end_date OR :endDate BETWEEN c.start_date AND c.end_date) "
                + "OR (c.start_date BETWEEN :fromDate AND :endDate OR c.end_date BETWEEN :fromDate AND :endDate))";

        if (!cashier.getIsFullDay()) {
            sql += " AND (TIME(c.start_time) BETWEEN TIME(:startTime) AND TIME(:endTime) "
                    + "OR TIME(c.end_time) BETWEEN TIME(:startTime) AND TIME(:endTime))";
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("staffId", staffId);
        paramMap.put("fromDate", fromDate);
        paramMap.put("endDate", endDate);

        if (!cashier.getIsFullDay()) {
            paramMap.put("startTime", cashier.getStartTime());
            paramMap.put("endTime", cashier.getEndTime());
        }

        Integer count = jdbcTemplate.queryForObject(sql, paramMap, Integer.class);
        if (count != null && count > 0) {
            throw new CashierAlreadyAllocated();
        }
    }

    public void validateOnLoanDisbursal(AppUser user, String currencyCode, BigDecimal transactionAmount) {
        LocalDate tenantDate = DateUtils.getLocalDateOfTenant();
        OffsetDateTime tenantDateTime = DateUtils.getOffsetDateTimeOfTenant();
        if (user.getStaff() != null) {
            String sql = "SELECT c.id FROM m_cashiers c WHERE c.staff_id = :staffId "
                    + "AND (CASE WHEN c.full_day THEN :tenantDate BETWEEN c.start_date AND c.end_date "
                    + "ELSE (:tenantDate BETWEEN c.start_date AND c.end_date AND "
                    + "TIME(:tenantDateTime) BETWEEN TIME(c.start_time) AND TIME(c.end_time)) END)";

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("staffId", user.getStaff().getId());
            paramMap.put("tenantDate", tenantDate);
            paramMap.put("tenantDateTime", tenantDateTime);

            try {
                Long cashierId = jdbcTemplate.queryForObject(sql, paramMap, Long.class);
                validateSettleCashAndCashOutTransactions(cashierId, currencyCode, transactionAmount);
            } catch (EmptyResultDataAccessException e) {
                LOG.error("Problem occurred in validateOnLoanDisbursal function", e);
            }
        }
    }
}
