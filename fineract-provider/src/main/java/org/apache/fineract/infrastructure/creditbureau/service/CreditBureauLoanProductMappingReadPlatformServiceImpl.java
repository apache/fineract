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
package org.apache.fineract.infrastructure.creditbureau.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauLoanProductMappingData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CreditBureauLoanProductMappingReadPlatformServiceImpl implements CreditBureauLoanProductMappingReadPlatformService {
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public CreditBureauLoanProductMappingReadPlatformServiceImpl(final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class CreditBureauLoanProductMapper implements RowMapper<CreditBureauLoanProductMappingData> {

		public String schema() {
			return "cblp.id as mappingId, cblp.organisation_creditbureau_id as orgcbId,"
					+ "orgcb.alias as alias,concat( cb.product,' - ' ,cb.name,' - ',cb.country) as creditbureau,"
					+ "cblp.loan_product_id as lpId,lp.name as loan_product_name,cblp.is_creditcheck_mandatory as crCheck,cblp.skip_creditcheck_in_failure as skipcheck,"
					+ "cblp.stale_period as staleperiod,cblp.is_active as is_active from"
					+ " m_creditbureau_loanproduct_mapping cblp, m_organisation_creditbureau orgcb,m_product_loan lp,m_creditbureau cb"
					+ " where cblp.organisation_creditbureau_id=orgcb.id and cblp.loan_product_id=lp.id and orgcb.creditbureau_id=cb.id";
		}

		@Override
		public CreditBureauLoanProductMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final Long mapping_id = rs.getLong("mappingId");
			final Long orgcbID = rs.getLong("orgcbId");
			final String alias = rs.getString("alias");
			final String credit_bureau_name = rs.getString("creditbureau");
			final String loan_product_name = rs.getString("loan_product_name");
			final Long lpId = rs.getLong("lpId");
			final boolean is_creditcheck_mandatory = rs.getBoolean("crCheck");
			final boolean skip_credit_check_in_failure = rs.getBoolean("skipcheck");
			final int stale_period = rs.getInt("staleperiod");
			final boolean is_active = rs.getBoolean("is_active");

			return CreditBureauLoanProductMappingData.instance(mapping_id, orgcbID, alias, credit_bureau_name, loan_product_name,
					lpId, is_creditcheck_mandatory, skip_credit_check_in_failure, stale_period, is_active);
		}
	}

	private static final class LoanProductMapper implements RowMapper<CreditBureauLoanProductMappingData> {

		public String schema() {
			return "lp.name as loan_product_name,lp.id as loanid from m_product_loan lp";
		}

		@Override
		public CreditBureauLoanProductMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final Long loanid = rs.getLong("loanid");

			final String loan_product_name = rs.getString("loan_product_name");

			return CreditBureauLoanProductMappingData.instance1(loan_product_name, loanid);
		}
	}

	@Override
	public Collection<CreditBureauLoanProductMappingData> readCreditBureauLoanProductMapping() {
		this.context.authenticatedUser();

		final CreditBureauLoanProductMapper rm = new CreditBureauLoanProductMapper();
		final String sql = "select " + rm.schema() + " order by cblp.id";

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	@Override
	public CreditBureauLoanProductMappingData readMappingByLoanId(long loanProductId) {
		this.context.authenticatedUser();

		final CreditBureauLoanProductMapper rm = new CreditBureauLoanProductMapper();
		final String sql = "select " + rm.schema() + " and cblp.loan_product_id=?";

		return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanProductId });
	}

	@Override
	public Collection<CreditBureauLoanProductMappingData> fetchLoanProducts() {
		this.context.authenticatedUser();

		final LoanProductMapper rm = new LoanProductMapper();
		final String sql = "select " + rm.schema() + " order by lp.id";

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

}
