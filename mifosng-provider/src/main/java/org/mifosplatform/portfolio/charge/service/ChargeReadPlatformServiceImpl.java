/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.service.CurrencyReadPlatformService;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.domain.ChargeAppliesTo;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ChargeReadPlatformServiceImpl implements ChargeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService;

    @Autowired
    public ChargeReadPlatformServiceImpl(final PlatformSecurityContext context,
            final CurrencyReadPlatformService currencyReadPlatformService,
            final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, final RoutingDataSource dataSource) {
        this.context = context;
        this.chargeDropdownReadPlatformService = chargeDropdownReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.currencyReadPlatformService = currencyReadPlatformService;
    }

    @Override
    @Cacheable(value = "charges", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public Collection<ChargeData> retrieveAllCharges() {
        this.context.authenticatedUser();

        final ChargeMapper rm = new ChargeMapper();

        final String sql = "select " + rm.chargeSchema() + " where c.is_deleted=0 order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public ChargeData retrieveCharge(final Long chargeId) {
        try {
            this.context.authenticatedUser();

            final ChargeMapper rm = new ChargeMapper();

            final String sql = "select " + rm.chargeSchema() + " where c.id = ? and c.is_deleted=0 ";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { chargeId });
        } catch (EmptyResultDataAccessException e) {
            throw new ChargeNotFoundException(chargeId);
        }
    }

    @Override
    public ChargeData retrieveNewChargeDetails() {

        this.context.authenticatedUser();

        final Collection<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<EnumOptionData> allowedChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService.retrieveCalculationTypes();
        final List<EnumOptionData> allowedChargeAppliesToOptions = this.chargeDropdownReadPlatformService.retrieveApplicableToTypes();
        final List<EnumOptionData> allowedChargeTimeOptions = this.chargeDropdownReadPlatformService.retrieveCollectionTimeTypes();
        final List<EnumOptionData> chargePaymentOptions = this.chargeDropdownReadPlatformService.retrivePaymentModes();

        return ChargeData.template(currencyOptions, allowedChargeCalculationTypeOptions, allowedChargeAppliesToOptions,
                allowedChargeTimeOptions, chargePaymentOptions);
    }

    @Override
    public Collection<ChargeData> retrieveLoanProductCharges(final Long loanProductId) {

        this.context.authenticatedUser();

        final ChargeMapper rm = new ChargeMapper();

        final String sql = "select " + rm.loanProductChargeSchema() + " where c.is_deleted=0 and c.is_active=1 and plc.product_loan_id=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { loanProductId });
    }

    @Override
    public Collection<ChargeData> retrieveLoanApplicableCharges(final boolean feeChargesOnly) {
        this.context.authenticatedUser();

        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema()
                + " where c.is_deleted=0 and c.is_active=1 and c.charge_applies_to_enum=? order by c.name ";
        if (feeChargesOnly) {
            sql = "select " + rm.chargeSchema()
                    + " where c.is_deleted=0 and c.is_active=1 and c.is_penalty=0 and c.charge_applies_to_enum=? order by c.name ";
        }

        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.LOAN.getValue() });
    }

    @Override
    public Collection<ChargeData> retrieveLoanApplicablePenalties() {
        this.context.authenticatedUser();

        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema()
                + " where c.is_deleted=0 and c.is_active=1 and c.is_penalty=1 and c.charge_applies_to_enum=? order by c.name ";
        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.LOAN.getValue() });
    }

    private static final class ChargeMapper implements RowMapper<ChargeData> {

        public String chargeSchema() {
            return "c.id as id, c.name as name, c.amount as amount, c.currency_code as currencyCode, "
                    + "c.charge_applies_to_enum as chargeAppliesTo, c.charge_time_enum as chargeTime, "
                    +"c.charge_payment_mode_enum as chargePaymentMode, "
                    + "c.charge_calculation_enum as chargeCalculation, c.is_penalty as penalty, c.is_active as active, oc.name as currencyName, "
                    + "oc.decimal_places as currencyDecimalPlaces,oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol, "
                    + "oc.internationalized_name_code as currencyNameCode from m_charge c "
                    + "join m_organisation_currency oc on c.currency_code = oc.code";
        }

        public String loanProductChargeSchema() {
            return chargeSchema() + " join m_product_loan_charge plc on plc.charge_id = c.id";
        }

        @Override
        public ChargeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final BigDecimal amount = rs.getBigDecimal("amount");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final int chargeAppliesTo = rs.getInt("chargeAppliesTo");
            final EnumOptionData chargeAppliesToType = ChargeEnumerations.chargeAppliesTo(chargeAppliesTo);

            final int chargeTime = rs.getInt("chargeTime");
            final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTime);

            final int chargeCalculation = rs.getInt("chargeCalculation");
            final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);

            final int paymentMode = rs.getInt("chargePaymentMode");
            final EnumOptionData chargePaymentMode = ChargeEnumerations.chargePaymentMode(paymentMode);

            
            final boolean penalty = rs.getBoolean("penalty");
            final boolean active = rs.getBoolean("active");

            return ChargeData.instance(id, name, amount, currency, chargeTimeType, chargeAppliesToType, chargeCalculationType, chargePaymentMode,
                    penalty, active);
        }
    }

}