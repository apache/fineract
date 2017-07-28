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

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.service.AccountTransferEnumerations;
import org.apache.fineract.portfolio.self.account.data.SelfAccountTemplateData;
import org.apache.fineract.portfolio.self.account.data.SelfBeneficiariesTPTData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SelfBeneficiariesTPTReadPlatformServiceImpl implements
		SelfBeneficiariesTPTReadPlatformService {

	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;
	private final BeneficiaryMapper mapper;
	private final AccountTemplateMapper accountTemplateMapper;

	@Autowired
	public SelfBeneficiariesTPTReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.mapper = new BeneficiaryMapper();
		this.accountTemplateMapper = new AccountTemplateMapper();
	}

	@Override
	public Collection<SelfBeneficiariesTPTData> retrieveAll() {
		AppUser user = this.context.authenticatedUser();
		return this.jdbcTemplate.query(this.mapper.schema(), this.mapper,
				new Object[] { user.getId(), user.getId() });
	}

	@Override
	public Collection<SelfAccountTemplateData> retrieveTPTSelfAccountTemplateData(
			AppUser user) {
		return this.jdbcTemplate.query(this.accountTemplateMapper.schema(),
				this.accountTemplateMapper,
				new Object[] { user.getId(), user.getId() });
	}

	private static final class BeneficiaryMapper implements
			RowMapper<SelfBeneficiariesTPTData> {

		private final String schemaSql;

		public BeneficiaryMapper() {
			final StringBuilder sqlBuilder = new StringBuilder(
					"(select b.id as id, ");
			sqlBuilder.append(" b.name as name, ");
			sqlBuilder.append(" o.name as officeName, ");
			sqlBuilder.append(" c.display_name as clientName, ");
			sqlBuilder.append(" b.account_type as accountType, ");
			sqlBuilder.append(" s.account_no as accountNumber, ");
			sqlBuilder.append(" b.transfer_limit as transferLimit ");
			sqlBuilder.append(" from m_selfservice_beneficiaries_tpt as b ");
			sqlBuilder
					.append(" inner join m_office as o on b.office_id = o.id ");
			sqlBuilder
					.append(" inner join m_client as c on b.client_id = c.id ");
			sqlBuilder
					.append(" inner join m_savings_account as s on b.account_id = s.id ");
			sqlBuilder.append(" where b.is_active = 1 ");
			sqlBuilder.append(" and b.account_type = 2 ");
			sqlBuilder.append(" and b.app_user_id = ?) ");
			sqlBuilder.append(" union all ");
			sqlBuilder.append(" (select b.id as id, ");
			sqlBuilder.append(" b.name as name, ");
			sqlBuilder.append(" o.name as officeName, ");
			sqlBuilder.append(" c.display_name as clientName, ");
			sqlBuilder.append(" b.account_type as accountType, ");
			sqlBuilder.append(" l.account_no as accountNumber, ");
			sqlBuilder.append(" b.transfer_limit as transferLimit ");
			sqlBuilder.append(" from m_selfservice_beneficiaries_tpt as b ");
			sqlBuilder
					.append(" inner join m_office as o on b.office_id = o.id ");
			sqlBuilder
					.append(" inner join m_client as c on b.client_id = c.id ");
			sqlBuilder
					.append(" inner join m_loan as l on b.account_id = l.id ");
			sqlBuilder.append(" where b.is_active = 1 ");
			sqlBuilder.append(" and b.account_type = 1 ");
			sqlBuilder.append(" and b.app_user_id = ?) ");

			this.schemaSql = sqlBuilder.toString();
		}

		public String schema() {
			return this.schemaSql;
		}

		@Override
		public SelfBeneficiariesTPTData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String name = rs.getString("name");
			final String officeName = rs.getString("officeName");
			final String clientName = rs.getString("clientName");
			final Integer accountTypeId = rs.getInt("accountType");
			final EnumOptionData accountType = AccountTransferEnumerations
					.accountType(PortfolioAccountType.fromInt(accountTypeId));
			final String accountNumber = rs.getString("accountNumber");
			final Long transferLimit = rs.getLong("transferLimit");

			return new SelfBeneficiariesTPTData(id, name, officeName,
					clientName, accountType, accountNumber, transferLimit);
		}
	}

	private static final class AccountTemplateMapper implements
			RowMapper<SelfAccountTemplateData> {

		private final String schemaSql;

		public AccountTemplateMapper() {
			final StringBuilder sqlBuilder = new StringBuilder(
					"(select o.name as officeName, ");
			sqlBuilder.append(" o.id as officeId, ");
			sqlBuilder.append(" c.display_name as clientName, ");
			sqlBuilder.append(" c.id as clientId, ");
			sqlBuilder.append(" b.account_type as accountType, ");
			sqlBuilder.append(" s.account_no as accountNumber, ");
			sqlBuilder.append(" s.id as accountId ");
			sqlBuilder.append(" from m_selfservice_beneficiaries_tpt as b ");
			sqlBuilder
					.append(" inner join m_office as o on b.office_id = o.id ");
			sqlBuilder
					.append(" inner join m_client as c on b.client_id = c.id ");
			sqlBuilder
					.append(" inner join m_savings_account as s on b.account_id = s.id ");
			sqlBuilder.append(" where b.is_active = 1 ");
			sqlBuilder.append(" and b.account_type = 2 ");
			sqlBuilder.append(" and b.app_user_id = ?) ");
			sqlBuilder.append(" union all ");
			sqlBuilder.append(" (select o.name as officeName, ");
			sqlBuilder.append(" o.id as officeId, ");
			sqlBuilder.append(" c.display_name as clientName, ");
			sqlBuilder.append(" c.id as clientId, ");
			sqlBuilder.append(" b.account_type as accountType, ");
			sqlBuilder.append(" l.account_no as accountNumber, ");
			sqlBuilder.append(" l.id as accountId ");
			sqlBuilder.append(" from m_selfservice_beneficiaries_tpt as b ");
			sqlBuilder
					.append(" inner join m_office as o on b.office_id = o.id ");
			sqlBuilder
					.append(" inner join m_client as c on b.client_id = c.id ");
			sqlBuilder
					.append(" inner join m_loan as l on b.account_id = l.id ");
			sqlBuilder.append(" where b.is_active = 1 ");
			sqlBuilder.append(" and b.account_type = 1 ");
			sqlBuilder.append(" and b.app_user_id = ?) ");

			this.schemaSql = sqlBuilder.toString();
		}

		public String schema() {
			return this.schemaSql;
		}

		@Override
		public SelfAccountTemplateData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final String officeName = rs.getString("officeName");
			final Long officeId = rs.getLong("officeId");
			final String clientName = rs.getString("clientName");
			final Long clientId = rs.getLong("clientId");
			final Integer accountTypeId = rs.getInt("accountType");
			final String accountNumber = rs.getString("accountNumber");
			final Long accountId = rs.getLong("accountId");

			return new SelfAccountTemplateData(accountId, accountNumber,
					accountTypeId, clientId, clientName, officeId, officeName);
		}
	}

	@Override
	public Long getTransferLimit(Long appUserId, Long accountId, Integer accountType) {
		final StringBuilder sqlBuilder = new StringBuilder("select b.transfer_limit ");
		sqlBuilder.append(" from m_selfservice_beneficiaries_tpt as b ");
		sqlBuilder.append(" where b.app_user_id = ? ");
		sqlBuilder.append(" and b.account_id = ? ");
		sqlBuilder.append(" and b.account_type = ? ");
		sqlBuilder.append(" and b.is_active = 1; ");
		
		return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), 
				new Object[]{appUserId, accountId, accountType}, Long.class);
	}
}
