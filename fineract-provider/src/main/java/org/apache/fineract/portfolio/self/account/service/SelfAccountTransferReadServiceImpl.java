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
package org.apache.fineract.portfolio.self.account.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.portfolio.self.account.data.SelfAccountTemplateData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SelfAccountTransferReadServiceImpl implements
		SelfAccountTransferReadService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public SelfAccountTransferReadServiceImpl(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<SelfAccountTemplateData> retrieveSelfAccountTemplateData(
			AppUser user) {
		SelfAccountTemplateMapper mapper = new SelfAccountTemplateMapper();
		StringBuffer sql = new StringBuffer()
				.append("select s.id as accountId, ")
				.append("s.account_no as accountNo, ")
				.append("2 as accountType, ")
				.append("c.id as clientId, ")
				.append("c.display_name as clientName, ")
				.append("o.id as officeId, ")
				.append("o.name as officeName ")
				.append("from m_appuser as u ")
				.append("inner join m_selfservice_user_client_mapping as map on u.id = map.appuser_id ")
				.append("inner join m_client as c on map.client_id = c.id ")
				.append("inner join m_office as o on c.office_id = o.id ")
				.append("inner join m_savings_account as s on s.client_id = c.id ")
				.append("where u.id = ? ")
				.append("and s.status_enum = 300 ")
				.append("union ")
				.append("select l.id as accountId, ")
				.append("l.account_no as accountNo, ")
				.append("1 as accountType, ")
				.append("c.id as clientId, ")
				.append("c.display_name as clientName, ")
				.append("o.id as officeId, ")
				.append("o.name as officeName ")
				.append("from m_appuser as u ")
				.append("inner join m_selfservice_user_client_mapping as map on u.id = map.appuser_id ")
				.append("inner join m_client as c on map.client_id = c.id ")
				.append("inner join m_office as o on c.office_id = o.id ")
				.append("inner join m_loan as l on l.client_id = c.id ")
				.append("where u.id = ? ")
				.append("and l.loan_status_id = 300 ");
		return this.jdbcTemplate.query(sql.toString(), mapper, new Object[] {
				user.getId(), user.getId() });
	}

	private final class SelfAccountTemplateMapper implements
			RowMapper<SelfAccountTemplateData> {

		@Override
		public SelfAccountTemplateData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final Long accountId = rs.getLong("accountId");
			final String accountNo = rs.getString("accountNo");
			final Integer accountType = rs.getInt("accountType");
			final Long clientId = rs.getLong("clientId");
			final String clientName = rs.getString("clientName");
			final Long officeId = rs.getLong("officeId");
			final String officeName = rs.getString("officeName");

			return new SelfAccountTemplateData(accountId, accountNo,
					accountType, clientId, clientName, officeId, officeName);
		}
	}

}
