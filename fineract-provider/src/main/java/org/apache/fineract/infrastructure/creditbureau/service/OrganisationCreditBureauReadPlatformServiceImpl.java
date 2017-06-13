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
import org.apache.fineract.infrastructure.creditbureau.data.OrganisationCreditBureauData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class OrganisationCreditBureauReadPlatformServiceImpl implements OrganisationCreditBureauReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public OrganisationCreditBureauReadPlatformServiceImpl(final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class OrganisationCreditBureauMapper implements RowMapper<OrganisationCreditBureauData> {
		public String schema() {
			return "ocb.id as orgCbId,ocb.alias as orgCbAlias,cb.name as creditbureauName,cb.product as creditbureauProduct,cb.country as creditbureauCountry,"
					+ "concat(cb.product,' - ',cb.name,' - ',cb.country) as CreditBureauSummary,"
					+ "ocb.creditbureau_id as cbid,ocb.is_active as is_active"
					+ " from m_organisation_creditbureau ocb,m_creditbureau cb where ocb.creditbureau_id=cb.id";

		}

		@Override
		public OrganisationCreditBureauData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final long orgCbId = rs.getLong("orgCbId");
			final String orgCbAlias = rs.getString("orgCbAlias");
			final String creditbureauName = rs.getString("creditbureauName");
			final String creditbureauProduct = rs.getString("creditbureauProduct");
			final String creditbureauCountry = rs.getString("creditbureauCountry");
			final String CreditBureauSummary = rs.getString("CreditBureauSummary");
			final long cbid = rs.getLong("cbid");
			final boolean is_active = rs.getBoolean("is_active");

			return OrganisationCreditBureauData.instance(orgCbId, orgCbAlias, cbid, creditbureauName,
					creditbureauProduct, creditbureauCountry, CreditBureauSummary, is_active);

		}
	}

	@Override
	public Collection<OrganisationCreditBureauData> retrieveOrgCreditBureau() {
		this.context.authenticatedUser();

		final OrganisationCreditBureauMapper rm = new OrganisationCreditBureauMapper();
		final String sql = "select " + rm.schema() + " order by ocb.id";

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	@Override
	public OrganisationCreditBureauData retrieveOrgCreditBureauById(long orgCbId) {
		this.context.authenticatedUser();

		final OrganisationCreditBureauMapper rm = new OrganisationCreditBureauMapper();
		final String sql = "select " + rm.schema() + " and ocb.id=?";

		return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { orgCbId });
	}

}
