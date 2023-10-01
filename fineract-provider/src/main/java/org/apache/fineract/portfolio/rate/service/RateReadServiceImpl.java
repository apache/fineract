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

package org.apache.fineract.portfolio.rate.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.rate.data.RateData;
import org.apache.fineract.portfolio.rate.domain.RateAppliesTo;
import org.apache.fineract.portfolio.rate.exception.RateNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * Bowpi GT Created by Jose on 19/07/2017.
 */
@RequiredArgsConstructor
public class RateReadServiceImpl implements RateReadService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Override
    public Collection<RateData> retrieveAllRates() {
        this.context.authenticatedUser();
        final RateMapper rm = new RateMapper();
        final String sql = "select " + rm.rateSchema();
        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public RateData retrieveOne(Long rateId) {
        try {
            this.context.authenticatedUser();
            final RateMapper rm = new RateMapper();
            final String sql = "select " + rm.rateSchema() + " where r.id = ?";
            final RateData selectedRate = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { rateId }); // NOSONAR
            return selectedRate;

        } catch (final EmptyResultDataAccessException e) {
            throw new RateNotFoundException(rateId, e);
        }
    }

    @Override
    public RateData retrieveByName(String name) {
        try {
            this.context.authenticatedUser();
            final RateMapper rm = new RateMapper();
            final String sql = "select " + rm.rateSchema() + " where r.name = ?";
            final RateData selectedRate = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { name }); // NOSONAR
            return selectedRate;

        } catch (final EmptyResultDataAccessException e) {
            throw new RateNotFoundException(name, e);
        }
    }

    @Override
    public Collection<RateData> retrieveLoanApplicableRates() {
        this.context.authenticatedUser();
        final RateMapper rm = new RateMapper();
        final String sql = "select " + rm.rateSchema() + " where r.active = ? and product_apply=?";
        return this.jdbcTemplate.query(sql, rm, new Object[] { true, RateAppliesTo.LOAN.getValue() }); // NOSONAR
    }

    @Override
    public List<RateData> retrieveLoanRates(Long loanId) {
        final RateMapper rm = new RateMapper();
        final String sql = "select " + rm.loanRateSchema() + " where lr.loan_id = ?";
        return this.jdbcTemplate.query(sql, rm, new Object[] { loanId }); // NOSONAR
    }

    @Override
    public List<RateData> retrieveProductLoanRates(Long loanId) {
        final RateMapper rm = new RateMapper();
        final String sql = "select " + rm.productLoanRateSchema() + " where lr.product_loan_id = ?";
        return this.jdbcTemplate.query(sql, rm, new Object[] { loanId }); // NOSONAR
    }

    private static final class RateMapper implements RowMapper<RateData> {

        public String rateSchema() {
            return " r.id as id, r.name as name, r.percentage as percentage, "
                    + "r.product_apply as productApply, r.active as active from m_rate r ";
        }

        public String loanRateSchema() {
            return rateSchema() + " join m_loan_rate lr on lr.rate_id = r.id";
        }

        public String productLoanRateSchema() {
            return rateSchema() + " join m_product_loan_rate lr on lr.rate_id = r.id";
        }

        RateMapper() {}

        @Override
        public RateData mapRow(ResultSet resultSet, int i) throws SQLException {
            final Long id = resultSet.getLong("id");
            final String name = resultSet.getString("name");
            final BigDecimal percentage = resultSet.getBigDecimal("percentage");
            final Integer productApply = resultSet.getInt("productApply");
            final EnumOptionData productAppliesTo = RateEnumerations.rateAppliesTo(productApply);
            final boolean active = resultSet.getBoolean("active");
            return RateData.instance(id, name, percentage, productAppliesTo, active);
        }

    }
}
