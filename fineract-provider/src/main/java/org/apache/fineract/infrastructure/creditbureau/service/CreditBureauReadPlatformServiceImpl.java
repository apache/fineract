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
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CreditBureauReadPlatformServiceImpl implements CreditBureauReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public CreditBureauReadPlatformServiceImpl(final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class CBMapper implements RowMapper<CreditBureauData> {
		public String schema() {
			return "cb.id as creditBureauID,cb.name as creditBureauName,cb.product as creditBureauProduct,"
					+ "cb.country as country,concat(cb.product,' - ',cb.name,' - ',cb.country) as cbSummary,cb.implementationKey as implementationKey from m_creditbureau cb";
		}

		@Override
		public CreditBureauData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final long id = rs.getLong("creditBureauID");
			final String name = rs.getString("creditBureauName");
			final String product = rs.getString("creditBureauProduct");
			final String country = rs.getString("country");
			final String cbSummary = rs.getString("cbSummary");
			final long implementationKey = rs.getLong("implementationKey");

			return CreditBureauData.instance(id, name, product, country, cbSummary, implementationKey);

		}
	}

	@Override
	public Collection<CreditBureauData> retrieveCreditBureau() {
		this.context.authenticatedUser();

		final CBMapper rm = new CBMapper();
		final String sql = "select " + rm.schema() + " order by id";

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

}
