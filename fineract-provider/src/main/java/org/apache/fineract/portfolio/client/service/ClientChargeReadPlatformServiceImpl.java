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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeEnumerations;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.ClientChargeData;
import org.apache.fineract.portfolio.client.exception.ClientChargeNotFoundException;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientChargeReadPlatformServiceImpl implements ClientChargeReadPlatformService {

    private final PaginationHelper<ClientChargeData> paginationHelper = new PaginationHelper<>();
    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ClientChargeMapper clientChargeMapper;

    @Autowired
    public ClientChargeReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientChargeMapper = new ClientChargeMapper();
    }

    public static final class ClientChargeMapper implements RowMapper<ClientChargeData> {

        @Override
        public ClientChargeData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long chargeId = rs.getLong("chargeId");
            final Long clientId = rs.getLong("clientId");
            final String name = rs.getString("name");
            final BigDecimal amount = rs.getBigDecimal("amountDue");
            final BigDecimal amountPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountPaid");
            final BigDecimal amountWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountWaived");
            final BigDecimal amountWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountWrittenOff");
            final BigDecimal amountOutstanding = rs.getBigDecimal("amountOutstanding");

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

            final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "dueAsOfDate");
            final int chargeCalculation = rs.getInt("chargeCalculation");
            final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);
            final boolean penalty = rs.getBoolean("penalty");
            final Boolean isPaid = rs.getBoolean("isPaid");
            final Boolean isWaived = rs.getBoolean("waived");
            final Boolean isActive = rs.getBoolean("isActive");
            final LocalDate inactivationDate = JdbcSupport.getLocalDate(rs, "inactivationDate");

            final Collection<ChargeData> chargeOptions = null;

            return ClientChargeData.instance(id, clientId, chargeId, name, chargeTimeType, dueDate, chargeCalculationType, currency, amount,
                    amountPaid, amountWaived, amountWrittenOff, amountOutstanding, penalty, isPaid, isWaived, isActive, inactivationDate,
                    chargeOptions);

        }

        public String schema() {
            return " cc.id as id, c.id as chargeId, cc.client_id as clientId, c.name as name, cc.amount as amountDue, "
                    + "cc.amount_paid_derived as amountPaid, cc.amount_waived_derived as amountWaived, "
                    + "cc.amount_writtenoff_derived as amountWrittenOff, cc.amount_outstanding_derived as amountOutstanding, "
                    + "cc.charge_time_enum as chargeTime, cc.is_penalty as penalty, cc.charge_due_date as dueAsOfDate, "
                    + "cc.charge_calculation_enum as chargeCalculation, cc.is_paid_derived as isPaid, cc.waived as waived, "
                    + "cc.is_active as isActive, cc.inactivated_on_date as inactivationDate, "
                    + "c.currency_code as currencyCode, oc.name as currencyName, "
                    + "oc.decimal_places as currencyDecimalPlaces, oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol, "
                    + "oc.internationalized_name_code as currencyNameCode from m_charge c "
                    + "join m_organisation_currency oc on c.currency_code = oc.code join m_client_charge cc on cc.charge_id = c.id ";
        }

    }

    @Override
    public ClientChargeData retrieveClientCharge(Long clientId, Long clientChargeId) {
        try {
            this.context.authenticatedUser();

            final ClientChargeMapper rm = new ClientChargeMapper();

            final String sql = "select " + rm.schema() + " where cc.client_id=? and cc.id=? ";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { clientId, clientChargeId });
        } catch (final EmptyResultDataAccessException e) {
            throw new ClientChargeNotFoundException(clientChargeId, clientId);
        }
    }

    @Override
    public Page<ClientChargeData> retrieveClientCharges(Long clientId, String status, Boolean pendingPayment,
            SearchParameters searchParameters) {
        final ClientChargeMapper rm = new ClientChargeMapper();
        final StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ").append(rm.schema()).append(" where cc.client_id=? ");

        // filter for active charges
        if (status.equalsIgnoreCase(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ACTIVE)) {
            sqlBuilder.append(" and cc.is_active = 1 ");
        } else if (status.equalsIgnoreCase(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_INACTIVE)) {
            sqlBuilder.append(" and cc.is_active = 0 ");
        }

        // filter for paid charges
        if (pendingPayment != null && pendingPayment) {
            sqlBuilder.append(" and ( cc.is_paid_derived = 0 and cc.waived = 0) ");
        } else if (pendingPayment != null && !pendingPayment) {
            sqlBuilder.append(" and (cc.is_paid_derived = 1 or cc.waived = 1) ");
        }

        sqlBuilder.append(" order by cc.charge_time_enum ASC, cc.charge_due_date DESC, cc.is_penalty ASC ");

        // apply limit and offsets
        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), new Object[] { clientId },
                this.clientChargeMapper);
    }

}
