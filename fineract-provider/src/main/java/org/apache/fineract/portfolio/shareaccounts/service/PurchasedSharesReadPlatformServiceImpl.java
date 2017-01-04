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
package org.apache.fineract.portfolio.shareaccounts.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountTransactionData;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PurchasedSharesReadPlatformServiceImpl implements
		PurchasedSharesReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public PurchasedSharesReadPlatformServiceImpl(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource) ;
	}
	@Override
	public Collection<ShareAccountTransactionData> retrievePurchasedShares(
			Long accountId) {
		PurchasedSharesDataRowMapper mapper = new PurchasedSharesDataRowMapper() ;
		final String sql = "select " + mapper.schema() + " where saps.account_id=? and saps.is_active = 1";
		return this.jdbcTemplate.query(sql, mapper, new Object[] { accountId});
	}

	private final static class PurchasedSharesDataRowMapper implements RowMapper<ShareAccountTransactionData> {

		private final String schema ;
		
		public PurchasedSharesDataRowMapper() {
			StringBuffer buff = new StringBuffer()
			.append("saps.id, saps.account_id, saps.transaction_date, saps.total_shares, saps.unit_price, ")
			.append("saps.status_enum, saps.type_enum, saps.amount, saps.charge_amount as chargeamount, ")
			.append("saps.amount_paid as amountPaid")
			.append(" from m_share_account_transactions saps ");
			schema = buff.toString() ;
		}
		@Override
		public ShareAccountTransactionData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			final Long id = rs.getLong("id") ;
			final Long accountId = rs.getLong("account_id") ;
			final LocalDate purchasedDate = new LocalDate(rs.getDate("transaction_date")) ;
			final Long numberOfShares = JdbcSupport.getLong(rs, "total_shares") ;
			final BigDecimal purchasedPrice = rs.getBigDecimal("unit_price") ;
			final Integer status = rs.getInt("status_enum") ;
			final EnumOptionData statusEnum = SharesEnumerations.purchasedSharesEnum(status) ;
			final Integer type = rs.getInt("type_enum") ;
			final EnumOptionData typeEnum = SharesEnumerations.purchasedSharesEnum(type) ;
			final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amount") ;
			final BigDecimal chargeAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "chargeamount") ;
			final BigDecimal amountPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountPaid") ;
			
			return new ShareAccountTransactionData(id,accountId, purchasedDate, numberOfShares, purchasedPrice, statusEnum, typeEnum, amount, chargeAmount, amountPaid);
		}
		
		public String schema() {
			return this.schema ;
		}
	}
}
