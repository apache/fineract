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
package org.apache.fineract.portfolio.floatingrates.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateData;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.data.InterestRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.exception.FloatingRateNotFoundException;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class FloatingRatesReadPlatformServiceImpl implements
		FloatingRatesReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public FloatingRatesReadPlatformServiceImpl(
			final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<FloatingRateData> retrieveAll() {
		FloatingRateRowMapper rateMapper = new FloatingRateRowMapper(false);
		final String sql = "select " + rateMapper.schema();
		return this.jdbcTemplate.query(sql, rateMapper);
	}

	@Override
	public List<FloatingRateData> retrieveAllActive() {
		FloatingRateRowMapper rateMapper = new FloatingRateRowMapper(false);
		final String sql = "select " + rateMapper.schema()
				+ " where rate.is_active = 1 ";
		return this.jdbcTemplate.query(sql, rateMapper);
	}

	@Override
	public List<FloatingRateData> retrieveLookupActive() {
		FloatingRateLookupMapper rateMapper = new FloatingRateLookupMapper();
		final String sql = "select " + rateMapper.schema()
				+ " where rate.is_active = 1 ";
		return this.jdbcTemplate.query(sql, rateMapper);
	}

	@Override
	public FloatingRateData retrieveOne(final Long floatingRateId) {
		try {
			FloatingRateRowMapper rateMapper = new FloatingRateRowMapper(true);
			final String sql = "select " + rateMapper.schema()
					+ " where rate.id = ?";
			return this.jdbcTemplate.queryForObject(sql, rateMapper,
					new Object[] { floatingRateId });
		} catch (final EmptyResultDataAccessException e) {
			throw new FloatingRateNotFoundException(floatingRateId);
		}
	}

	@Override
	public List<InterestRatePeriodData> retrieveInterestRatePeriods(
			final Long productId) {
		try {
			FloatingInterestRatePeriodRowMapper mapper = new FloatingInterestRatePeriodRowMapper();
			return this.jdbcTemplate.query(mapper.schema(), mapper,
					new Object[] { productId });
		} catch (final EmptyResultDataAccessException e) {
			throw new FloatingRateNotFoundException("error.msg.floatingrate.not.found.for.product");
		}
	}

	@Override
	public FloatingRateData retrieveBaseLendingRate() {
		try {
			FloatingRateRowMapper rateMapper = new FloatingRateRowMapper(true);
			final String sql = "select "
					+ rateMapper.schema()
					+ " where rate.is_base_lending_rate = 1 and rate.is_active = 1";
			return this.jdbcTemplate.queryForObject(sql, rateMapper);
		} catch (final EmptyResultDataAccessException e) {
			throw new FloatingRateNotFoundException(
					"error.msg.floatingrate.base.lending.rate.not.found");
		}
	}

	private final class FloatingRateRowMapper implements
			RowMapper<FloatingRateData> {

		private final boolean addRatePeriods;

		private final StringBuilder sqlQuery = new StringBuilder()
				.append("rate.id as id, ")
				.append("rate.name as name, ")
				.append("rate.is_base_lending_rate as isBaseLendingRate, ")
				.append("rate.is_active as isActive, ")
				.append("crappu.username as createdBy, ")
				.append("rate.created_date as createdOn, ")
				.append("moappu.username as modifiedBy, ")
				.append("rate.lastmodified_date as modifiedOn ")
				.append("FROM m_floating_rates as rate ")
				.append("LEFT JOIN m_appuser as crappu on rate.createdby_id = crappu.id ")
				.append("LEFT JOIN m_appuser as moappu on rate.lastmodifiedby_id = moappu.id ");

		public FloatingRateRowMapper(final boolean addRatePeriods) {
			this.addRatePeriods = addRatePeriods;
		}

		@Override
		public FloatingRateData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final Long id = rs.getLong("id");
			final String name = rs.getString("name");
			final boolean isBaseLendingRate = rs
					.getBoolean("isBaseLendingRate");
			final boolean isActive = rs.getBoolean("isActive");
			final String createdBy = rs.getString("createdBy");
			final LocalDate createdOn = JdbcSupport.getLocalDate(rs, "createdOn");
			final String modifiedBy = rs.getString("modifiedBy");
			final LocalDate modifiedOn = JdbcSupport.getLocalDate(rs, "modifiedOn");
			List<FloatingRatePeriodData> ratePeriods = null;
			if (addRatePeriods) {
				FloatingRatePeriodRowMapper ratePeriodMapper = new FloatingRatePeriodRowMapper();
				final String sql = "select "
						+ ratePeriodMapper.schema()
						+ " where period.is_active = 1 and period.floating_rates_id = ? "
						+ " order by period.from_date desc ";
				ratePeriods = jdbcTemplate.query(sql, ratePeriodMapper,
						new Object[] { id });
			}
			return new FloatingRateData(id, name, isBaseLendingRate, isActive,
					createdBy, createdOn, modifiedBy, modifiedOn, ratePeriods,
					null);
		}

		public String schema() {
			return sqlQuery.toString();
		}
	}

	private final class FloatingRatePeriodRowMapper implements
			RowMapper<FloatingRatePeriodData> {

		private final StringBuilder sqlQuery = new StringBuilder()
				.append("period.id as id, ")
				.append("period.from_date as fromDate, ")
				.append("period.interest_rate as interestRate, ")
				.append("period.is_differential_to_base_lending_rate as isDifferentialToBaseLendingRate, ")
				.append("period.is_active as isActive, ")
				.append("crappu.username as createdBy, ")
				.append("period.created_date as createdOn, ")
				.append("moappu.username as modifiedBy, ")
				.append("period.lastmodified_date as modifiedOn ")
				.append("FROM m_floating_rates_periods as period ")
				.append("LEFT JOIN m_appuser as crappu on period.createdby_id = crappu.id ")
				.append("LEFT JOIN m_appuser as moappu on period.lastmodifiedby_id = moappu.id ");

		@Override
		public FloatingRatePeriodData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final Long id = rs.getLong("id");
			final LocalDate fromDate = JdbcSupport.getLocalDate(rs, "fromDate");
			final BigDecimal interestRate = rs.getBigDecimal("interestRate");
			final boolean isDifferentialToBaseLendingRate = rs
					.getBoolean("isDifferentialToBaseLendingRate");
			final boolean isActive = rs.getBoolean("isActive");
			final String createdBy = rs.getString("createdBy");
			final LocalDate createdOn = JdbcSupport.getLocalDate(rs, "createdOn");
			final String modifiedBy = rs.getString("modifiedBy");
			final LocalDate modifiedOn = JdbcSupport.getLocalDate(rs, "modifiedOn");
			return new FloatingRatePeriodData(id, fromDate, interestRate,
					isDifferentialToBaseLendingRate, isActive, createdBy,
					createdOn, modifiedBy, modifiedOn);
		}

		public String schema() {
			return sqlQuery.toString();
		}
	}

	private final class FloatingRateLookupMapper implements
			RowMapper<FloatingRateData> {

		private final StringBuilder sqlQuery = new StringBuilder()
				.append("rate.id as id, ").append("rate.name as name, ")
				.append("rate.is_base_lending_rate as isBaseLendingRate ")
				.append("FROM m_floating_rates as rate ");

		@Override
		public FloatingRateData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final Long id = rs.getLong("id");
			final String name = rs.getString("name");
			final boolean isBaseLendingRate = rs
					.getBoolean("isBaseLendingRate");
			return new FloatingRateData(id, name, isBaseLendingRate, true,
					null, null, null, null, null, null);
		}

		public String schema() {
			return sqlQuery.toString();
		}
	}

	private final class FloatingInterestRatePeriodRowMapper implements
			RowMapper<InterestRatePeriodData> {

		private final StringBuilder sqlQuery = new StringBuilder()
				.append("select ")
				.append("    linkedrateperiods.from_date as linkedrateperiods_from_date, ")
				.append("    linkedrateperiods.interest_rate as linkedrateperiods_interest_rate, ")
				.append("    linkedrateperiods.is_differential_to_base_lending_rate as linkedrateperiods_is_differential_to_base_lending_rate, ")
				.append("    baserate.from_date as baserate_from_date, ")
				.append("    baserate.interest_rate as baserate_interest_rate ")
				.append(" from m_product_loan as lp ")
                                .append(" join m_product_loan_floating_rates as plfr on lp.id = plfr.loan_product_id ")
                                .append(" join  m_floating_rates as linkedrate on linkedrate.id = plfr.floating_rates_id ")
				.append("left join m_floating_rates_periods as linkedrateperiods on (linkedrate.id = linkedrateperiods.floating_rates_id and linkedrateperiods.is_active = 1) ")
				.append("left join ( ")
				.append("    select blr.name, ")
				.append("    blr.is_base_lending_rate, ")
				.append("    blr.is_active, ")
				.append("    blrperiods.from_date, ")
				.append("    blrperiods.interest_rate ")
				.append("    from m_floating_rates as blr ")
				.append("    left join m_floating_rates_periods as blrperiods on (blr.id = blrperiods.floating_rates_id and blrperiods.is_active = 1) ")
				.append("    where blr.is_base_lending_rate = 1 and blr.is_active = 1 ")
				.append(") as baserate on (linkedrateperiods.is_differential_to_base_lending_rate = 1 and linkedrate.is_base_lending_rate = 0) ")
				.append("where (baserate.from_date is null ")
				.append("    or baserate.from_date = (select MAX(b.from_date) ")
				.append("        from (select blr.name, ")
				.append("            blr.is_base_lending_rate, ")
				.append("            blr.is_active, ")
				.append("            blrperiods.from_date, ")
				.append("            blrperiods.interest_rate ")
				.append("            from m_floating_rates as blr ")
				.append("            left join m_floating_rates_periods as blrperiods on (blr.id = blrperiods.floating_rates_id and blrperiods.is_active = 1) ")
				.append("            where blr.is_base_lending_rate = 1 and blr.is_active = 1 ")
				.append("        ) as b ")
				.append("        where b.from_date <= linkedrateperiods.from_date)) ")
				.append("and lp.id = ? ")
				.append("order by linkedratePeriods_from_date desc ");

		@Override
		public InterestRatePeriodData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final Date fromDate = rs.getDate("linkedrateperiods_from_date");
			final BigDecimal interestRate = rs
					.getBigDecimal("linkedrateperiods_interest_rate");
			final boolean isDifferentialToBLR = rs
					.getBoolean("linkedrateperiods_is_differential_to_base_lending_rate");
			final Date blrFromDate = rs.getDate("baserate_from_date");
			final BigDecimal blrInterestRate = rs
					.getBigDecimal("baserate_interest_rate");

			return new InterestRatePeriodData(fromDate, interestRate,
					isDifferentialToBLR, blrFromDate, blrInterestRate);
		}

		public String schema() {
			return sqlQuery.toString();
		}
	}

}
