/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.service.ChargeDropdownReadPlatformService;
import org.mifosplatform.portfolio.charge.service.ChargeEnumerations;
import org.mifosplatform.portfolio.client.data.ClientChargeData;
import org.mifosplatform.portfolio.client.exception.ClientChargeNotFoundException;
import org.mifosplatform.portfolio.common.service.DropdownReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientChargeReadPlatformServiceImpl implements ClientChargeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService;
    private final DropdownReadPlatformService dropdownReadPlatformService;

    @Autowired
    public ClientChargeReadPlatformServiceImpl(final PlatformSecurityContext context,
            final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, final RoutingDataSource dataSource,
            final DropdownReadPlatformService dropdownReadPlatformService) {
        this.context = context;
        this.chargeDropdownReadPlatformService = chargeDropdownReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dropdownReadPlatformService = dropdownReadPlatformService;
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
            final Boolean isActive = rs.getBoolean("isActive");
            final LocalDate inactivationDate = JdbcSupport.getLocalDate(rs, "inactivationDate");

            final Collection<ChargeData> chargeOptions = null;

            return ClientChargeData.instance(id, clientId, chargeId, name, chargeTimeType, dueDate, chargeCalculationType, currency, amount,
                    amountPaid, amountWaived, amountWrittenOff, amountOutstanding, penalty, isActive, inactivationDate, chargeOptions);

        }

        public String schema() {
            return " cc.id as id, c.id as chargeId, cc.client_id as clientId, c.name as name, cc.amount as amountDue, "
                    + "cc.amount_paid_derived as amountPaid, cc.amount_waived_derived as amountWaived, "
                    + "cc.amount_writtenoff_derived as amountWrittenOff, cc.amount_outstanding_derived as amountOutstanding, "
                    + "cc.charge_time_enum as chargeTime, cc.is_penalty as penalty, cc.charge_due_date as dueAsOfDate, "
                    + "cc.charge_calculation_enum as chargeCalculation, "
                    + "cc.is_active as isActive, cc.inactivated_on_date as inactivationDate, "
                    + "c.currency_code as currencyCode, oc.name as currencyName, "
                    + "oc.decimal_places as currencyDecimalPlaces, oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol, "
                    + "oc.internationalized_name_code as currencyNameCode from m_charge c "
                    + "join m_organisation_currency oc on c.currency_code = oc.code join m_client_charge cc on cc.charge_id = c.id ";
        }

    }

    @Override
    public Collection<ClientChargeData> retrieveClientCharges(Long clientId, String status) {
        final ClientChargeMapper rm = new ClientChargeMapper();
        final StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select ").append(rm.schema()).append(" where cc.client_id=? ");
        if (status.equalsIgnoreCase("active")) {
            sqlBuilder.append(" and cc.is_active = 1 ");
        } else if (status.equalsIgnoreCase("inactive")) {
            sqlBuilder.append(" and cc.is_active = 0 ");
        }
        sqlBuilder.append(" order by cc.charge_time_enum ASC, cc.charge_due_date ASC, cc.is_penalty ASC");

        return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] { clientId });
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
    public ChargeData retrieveClientChargeTemplate() {
        // TODO Auto-generated method stub
        return null;
    }

}
