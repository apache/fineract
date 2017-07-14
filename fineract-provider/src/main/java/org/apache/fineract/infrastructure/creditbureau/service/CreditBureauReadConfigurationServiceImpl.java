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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauConfigurationData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CreditBureauReadConfigurationServiceImpl implements CreditBureauReadConfigurationService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public CreditBureauReadConfigurationServiceImpl(final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class CbConfigMapper implements RowMapper<CreditBureauConfigurationData> {
		public String schema() {

			return "cbconfig.id as configId,cbconfig.configkey,cbconfig.value as configValue,"
					+ "cbconfig.organisation_creditbureau_id as orgCBId,cbconfig.description as description from m_creditbureau_configuration cbconfig ";

		}

		@Override
		public CreditBureauConfigurationData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final long configId = rs.getLong("configId");
			final String configkey = rs.getString("configkey");
			final String configValue = rs.getString("configValue");
			final long orgCBId = rs.getLong("orgCBId");
			final String desc = rs.getString("description");

			return CreditBureauConfigurationData.instance(configId, configkey, configValue, orgCBId, desc);

		}
	}

	@Override
	public Collection<CreditBureauConfigurationData> readConfigurationByOrganisationCreditBureauId(long id) {

		this.context.authenticatedUser();

		final CbConfigMapper rm = new CbConfigMapper();
		final String sql = "select " + rm.schema() + " where cbconfig.organisation_creditbureau_id=?";

		return this.jdbcTemplate.query(sql, rm, new Object[] { id });

	}

	@Override
	public Map<String, String> retrieveConfigMap(long id) {
		this.context.authenticatedUser();

		final CbConfigMapper rm = new CbConfigMapper();
		final String sql = "select " + rm.schema() + " where cbconfig.organisation_creditbureau_id=" + id;

		List<CreditBureauConfigurationData> config = (List<CreditBureauConfigurationData>) this.jdbcTemplate.query(sql,
				rm, new Object[] {});
		Map<String, String> configuration = new HashMap<String, String>();
		for (CreditBureauConfigurationData data : config) {
			configuration.put(data.getConfigurationKey(), data.getValue());

		}

		return configuration;

	}

}
