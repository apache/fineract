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
package org.apache.fineract.organisation.teller.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.organisation.teller.data.CashierSessionData;
import org.apache.fineract.organisation.teller.data.CashierSessionSummaryData;
import org.apache.fineract.organisation.teller.domain.CashierSessionStatus;
import org.apache.fineract.organisation.teller.exception.CashierSessionNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@RequiredArgsConstructor
public class CashierSessionReadPlatformServiceImpl implements CashierSessionReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    private static final class CashierSessionMapper implements RowMapper<CashierSessionData> {

        public String schema() {
            return "cs.id as id, cs.cashier_id as cashier_id, cs.teller_id as teller_id, "
                    + "cs.user_id as user_id, cs.office_id as office_id, cs.session_date as session_date, "
                    + "cs.opened_at as opened_at, cs.closed_at as closed_at, "
                    + "cs.opening_allocation as opening_allocation, cs.total_settled as total_settled, "
                    + "cs.status as status, cs.opening_txn_id as opening_txn_id, "
                    + "cs.closing_txn_id as closing_txn_id, cs.currency_code as currency_code "
                    + "from m_cashier_sessions cs ";
        }

        @Override
        public CashierSessionData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long cashierId = rs.getLong("cashier_id");
            final Long tellerId = rs.getLong("teller_id");
            final Long userId = rs.getLong("user_id");
            final Long officeId = rs.getLong("office_id");
            final LocalDate sessionDate = JdbcSupport.getLocalDate(rs, "session_date");

            final Timestamp openedAtTs = rs.getTimestamp("opened_at");
            final LocalDateTime openedAt = openedAtTs != null ? openedAtTs.toLocalDateTime() : null;

            final Timestamp closedAtTs = rs.getTimestamp("closed_at");
            final LocalDateTime closedAt = closedAtTs != null ? closedAtTs.toLocalDateTime() : null;

            final BigDecimal openingAllocation = rs.getBigDecimal("opening_allocation");
            final BigDecimal totalSettled = rs.getBigDecimal("total_settled");
            final String statusStr = rs.getString("status");
            final CashierSessionStatus status = statusStr != null ? CashierSessionStatus.valueOf(statusStr) : null;
            final Long openingTxnId = rs.getLong("opening_txn_id");
            final Long closingTxnId = rs.getLong("closing_txn_id");
            final String currencyCode = rs.getString("currency_code");

            return new CashierSessionData(id, cashierId, tellerId, userId, officeId, sessionDate, openedAt, closedAt, openingAllocation,
                    totalSettled, status, openingTxnId == 0 ? null : openingTxnId, closingTxnId == 0 ? null : closingTxnId, currencyCode);
        }
    }

    @Override
    public Optional<CashierSessionData> findActiveSession(final Long cashierId, final Long tellerId) {
        try {
            final CashierSessionMapper mapper = new CashierSessionMapper();
            final String sql = "select " + mapper.schema()
                    + " where cs.cashier_id = ? and cs.teller_id = ? and cs.status = 'OPEN' and cs.session_date = CURRENT_DATE";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, mapper, cashierId, tellerId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CashierSessionData> findAllSessions(final Long cashierId, final Long tellerId) {
        final CashierSessionMapper mapper = new CashierSessionMapper();
        final String sql = "select " + mapper.schema()
                + " where cs.cashier_id = ? and cs.teller_id = ? order by cs.session_date desc";
        return jdbcTemplate.query(sql, mapper, cashierId, tellerId);
    }

    @Override
    public CashierSessionData findSessionById(final Long sessionId) {
        try {
            final CashierSessionMapper mapper = new CashierSessionMapper();
            final String sql = "select " + mapper.schema() + " where cs.id = ?";
            return jdbcTemplate.queryForObject(sql, mapper, sessionId);
        } catch (EmptyResultDataAccessException e) {
            throw new CashierSessionNotFoundException(sessionId);
        }
    }

    @Override
    public CashierSessionSummaryData getSessionSummary(final Long sessionId) {
        final CashierSessionData session = findSessionById(sessionId);

        final String cashInSql = "select coalesce(sum(ct.txn_amount), 0) from m_cashier_transactions ct "
                + "where ct.cashier_id = ? and ct.txn_date >= ? and ct.txn_type in (1, 101, 102)";

        final String cashOutSql = "select coalesce(sum(ct.txn_amount), 0) from m_cashier_transactions ct "
                + "where ct.cashier_id = ? and ct.txn_date >= ? and ct.txn_type in (2, 201, 202)";

        final LocalDate sessionDate = session.getSessionDate();
        final BigDecimal openingAllocation = session.getOpeningAllocation() != null ? session.getOpeningAllocation() : BigDecimal.ZERO;

        final BigDecimal totalCashIn = jdbcTemplate.queryForObject(cashInSql, BigDecimal.class, session.getCashierId(), sessionDate);
        final BigDecimal totalCashOut = jdbcTemplate.queryForObject(cashOutSql, BigDecimal.class, session.getCashierId(), sessionDate);

        final BigDecimal safeTotalCashIn = totalCashIn != null ? totalCashIn : BigDecimal.ZERO;
        final BigDecimal safeTotalCashOut = totalCashOut != null ? totalCashOut : BigDecimal.ZERO;

        final BigDecimal expectedCash = openingAllocation.add(safeTotalCashIn).subtract(safeTotalCashOut);
        final BigDecimal settledAmount = session.getTotalSettled() != null ? session.getTotalSettled() : BigDecimal.ZERO;
        final BigDecimal variance = settledAmount.subtract(expectedCash);

        return new CashierSessionSummaryData(session, openingAllocation, safeTotalCashIn, safeTotalCashOut, expectedCash, settledAmount,
                variance);
    }

    @Override
    public Optional<CashierSessionData> findActiveSessionForUser(final Long userId, final Long officeId) {
        try {
            final CashierSessionMapper mapper = new CashierSessionMapper();
            final String sql = "select " + mapper.schema()
                    + " where cs.user_id = ? and cs.office_id = ? and cs.status = 'OPEN' and cs.session_date = CURRENT_DATE";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, mapper, userId, officeId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CashierSessionData> findOpenSessionsByOffice(final Long officeId) {
        final CashierSessionMapper mapper = new CashierSessionMapper();
        final String sql = "select " + mapper.schema() + " where cs.office_id = ? and cs.status = 'OPEN'";
        return jdbcTemplate.query(sql, mapper, officeId);
    }
}
