package org.mifosng.platform.charge.service;

import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.charge.domain.ChargeAppliesTo;
import org.mifosng.platform.charge.domain.ChargeCalculationMethod;
import org.mifosng.platform.charge.domain.ChargeTimeType;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.mifosng.platform.exceptions.ChargeNotFoundException;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mifosng.platform.charge.service.ChargeEnumerations.chargeAppliesTo;
import static org.mifosng.platform.charge.service.ChargeEnumerations.chargeCalculationType;
import static org.mifosng.platform.charge.service.ChargeEnumerations.chargeTimeType;

@Service
public class ChargeReadPlatformServiceImpl implements ChargeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final CurrencyReadPlatformService currencyReadPlatformService;

    @Autowired
    public ChargeReadPlatformServiceImpl(PlatformSecurityContext context,
                                         final CurrencyReadPlatformService currencyReadPlatformService,
                                         final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.currencyReadPlatformService = currencyReadPlatformService;
    }

    @Override
    public Collection<ChargeData> retrieveAllCharges() {
        this.context.authenticatedUser();

        ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=0 order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public ChargeData retrieveCharge(Long chargeId) {
        try {
            this.context.authenticatedUser();

            ChargeMapper rm = new ChargeMapper();

            String sql = "select " + rm.chargeSchema() + " where c.id = ? and c.is_deleted=0 ";

            ChargeData chargeData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] {chargeId});
            return chargeData;
        } catch (EmptyResultDataAccessException e){
            throw new ChargeNotFoundException(chargeId);
        }
    }

    @Override
    public ChargeData retrieveNewChargeDetails() {
        this.context.authenticatedUser();

        List<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
        CurrencyData currency = new CurrencyData("", "", 0, "", "");

        EnumOptionData allowedChargeCalculationType = chargeCalculationType(ChargeCalculationMethod.FLAT);
        List<EnumOptionData> allowedChargeCalculationMethodsOptions = Arrays.asList(
                chargeCalculationType(ChargeCalculationMethod.FLAT),
                chargeCalculationType(ChargeCalculationMethod.PERCENT_OF_AMOUNT),
                chargeCalculationType(ChargeCalculationMethod.PERCENT_OF_AMOUNT_AND_INTEREST),
                chargeCalculationType(ChargeCalculationMethod.PERCENT_OF_INTEREST)
        );

        EnumOptionData allowedChargeAppliesTo = chargeAppliesTo(ChargeAppliesTo.LOAN);
        List<EnumOptionData> allowedChargeAppliesToOptions = Arrays.asList(allowedChargeAppliesTo);

        EnumOptionData allowedChargeTime = chargeTimeType(ChargeTimeType.DISBURSEMENT);
        List<EnumOptionData> allowedChargeTimeOptions = Arrays.asList(allowedChargeTime);

        return ChargeData.template(currency, allowedChargeTime, allowedChargeAppliesTo, allowedChargeCalculationType,
                currencyOptions, allowedChargeCalculationMethodsOptions,
                allowedChargeAppliesToOptions, allowedChargeTimeOptions);
    }

    private static final class ChargeMapper implements RowMapper<ChargeData> {

        public String chargeSchema(){
            return "c.id as id, c.name as name, c.amount as amount, c.currency_code as currencyCode, " +
                   "charge_applies_to_enum as chargeAppliesTo, charge_time_enum as chargeTime, " +
                   "charge_calculation_enum as chargeCalculation, is_active as active from m_charge c ";
        }

        @Override
        public ChargeData mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String name = rs.getString("name");
            BigDecimal amount = rs.getBigDecimal("amount");
            String currencyCode = rs.getString("currencyCode");
            CurrencyData currency = new CurrencyData(currencyCode, "", 0, "", "");

            int chargeAppliesTo = rs.getInt("chargeAppliesTo");
            EnumOptionData chargeAppliesToType = ChargeEnumerations.chargeAppliesTo(chargeAppliesTo);

            int chargeTime = rs.getInt("chargeTime");
            EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTime);

            int chargeCalculation = rs.getInt("chargeCalculation");
            EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);

            boolean active = rs.getBoolean("active");

            return new ChargeData(id, name, amount, currency, chargeTimeType, chargeAppliesToType, chargeCalculationType, active);
        }
    }
}
