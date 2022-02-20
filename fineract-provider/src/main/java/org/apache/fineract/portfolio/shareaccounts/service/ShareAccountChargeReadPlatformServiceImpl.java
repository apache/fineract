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
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeEnumerations;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountChargeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ShareAccountChargeReadPlatformServiceImpl implements ShareAccountChargeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ShareAccountChargeReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<ShareAccountChargeData> retrieveAccountCharges(Long accountId, String status) {
        final ShareAccountChargeMapper rm = new ShareAccountChargeMapper();
        final StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select ").append(rm.schema()).append(" where sc.account_id=? ");
        if (status.equalsIgnoreCase("active")) {
            sqlBuilder.append(" and sc.is_active = true ");
        } else if (status.equalsIgnoreCase("inactive")) {
            sqlBuilder.append(" and sc.is_active = false ");
        }
        sqlBuilder.append(" order by sc.charge_time_enum ASC");

        return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] { accountId });
    }

    private static final class ShareAccountChargeMapper implements RowMapper<ShareAccountChargeData> {

        private final String schema;

        ShareAccountChargeMapper() {
            StringBuilder buff = new StringBuilder().append("sc.id as id, c.id as chargeId, sc.account_id as accountId, c.name as name, ")
                    .append("sc.amount as amountDue, sc.amount_paid_derived as amountPaid, ")
                    .append("sc.amount_waived_derived as amountWaived, sc.amount_writtenoff_derived as amountWrittenOff, ")
                    .append("sc.amount_outstanding_derived as amountOutstanding, sc.calculation_percentage as percentageOf, ")
                    .append("sc.calculation_on_amount as amountPercentageAppliedTo, sc.charge_time_enum as chargeTime, ")
                    .append("sc.charge_calculation_enum as chargeCalculation, c.is_active as isActive, ")
                    .append("c.currency_code as currencyCode, oc.name as currencyName, ")
                    .append("oc.decimal_places as currencyDecimalPlaces, oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol, ")
                    .append("sc.charge_amount_or_percentage, ")
                    .append("oc.internationalized_name_code as currencyNameCode from m_charge c ")
                    .append("join m_organisation_currency oc on c.currency_code = oc.code ")
                    .append("join m_share_account_charge sc on sc.charge_id = c.id ");

            schema = buff.toString();
        }

        @Override
        public ShareAccountChargeData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long chargeId = rs.getLong("chargeId");
            final Long accountId = rs.getLong("accountId");
            final String name = rs.getString("name");
            final BigDecimal amount = rs.getBigDecimal("amountDue");
            final BigDecimal amountPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountPaid");
            final BigDecimal amountWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountWaived");
            final BigDecimal amountWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountWrittenOff");
            final BigDecimal amountOutstanding = rs.getBigDecimal("amountOutstanding");

            final BigDecimal percentageOf = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "percentageOf");
            final BigDecimal amountPercentageAppliedTo = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountPercentageAppliedTo");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final int chargeTime = rs.getInt("chargeTime");
            final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTime);

            final int chargeCalculation = rs.getInt("chargeCalculation");
            final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);
            final Boolean isActive = rs.getBoolean("isActive");
            final BigDecimal chargeamountorpercentage = rs.getBigDecimal("charge_amount_or_percentage");

            final Collection<ChargeData> chargeOptions = null;
            return new ShareAccountChargeData(id, chargeId, accountId, name, currency, amount, amountPaid, amountWaived, amountWrittenOff,
                    amountOutstanding, chargeTimeType, chargeCalculationType, percentageOf, amountPercentageAppliedTo, chargeOptions,
                    isActive, chargeamountorpercentage);
        }

        public String schema() {
            return this.schema;
        }
    }
}
