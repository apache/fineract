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
package org.apache.fineract.portfolio.client.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.address.data.ClientAddressData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientAddressReadPlatformServiceImpl implements ClientAddressReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public ClientAddressReadPlatformServiceImpl(final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class ClientAddrMapper implements RowMapper<ClientAddressData> {
		public String schema() {
			return "fld.id as fieldConfigurationId,fld.entity as entity,fld.table as entitytable,fld.field as field,fld.is_enabled as is_enabled,"
					+ "fld.is_mandatory as is_mandatory,fld.validation_regex as validation_regex from m_field_configuration fld";
		}

		@Override
		public ClientAddressData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final long clientAddressId = rs.getLong("clientAddressId");
			final long client_id = rs.getLong("client_id");
			final long address_id = rs.getLong("address_id");
			final long address_type_id = rs.getLong("address_type_id");
			final boolean is_active = rs.getBoolean("is_active");

			return ClientAddressData.instance(clientAddressId, client_id, address_id, address_type_id, is_active);

		}
	}

	@Override
	public Collection<ClientAddressData> retrieveClientAddrConfiguration(final String entity) {
		this.context.authenticatedUser();

		final ClientAddrMapper rm = new ClientAddrMapper();
		final String sql = "select " + rm.schema() + " where fld.entity=?";

		return this.jdbcTemplate.query(sql, rm, new Object[] { entity });
	}

}
