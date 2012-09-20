package org.mifosng.platform.charge.service;

import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.data.LoanChargeData;
import org.mifosng.platform.charge.domain.ChargeAppliesTo;
import org.mifosng.platform.charge.domain.ChargeCalculationType;
import org.mifosng.platform.charge.domain.ChargeTimeType;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.mifosng.platform.exceptions.ChargeNotFoundException;
import org.mifosng.platform.infrastructure.JdbcSupport;
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

        List<EnumOptionData> allowedChargeCalculationTypeOptions = Arrays.asList(
                chargeCalculationType(ChargeCalculationType.FLAT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST)
        );

        List<EnumOptionData> allowedChargeAppliesToOptions = Arrays.asList(chargeAppliesTo(ChargeAppliesTo.LOAN));

        List<EnumOptionData> allowedChargeTimeOptions = Arrays.asList(chargeTimeType(ChargeTimeType.DISBURSEMENT));

        return ChargeData.template(currencyOptions, allowedChargeCalculationTypeOptions,
                allowedChargeAppliesToOptions, allowedChargeTimeOptions);
    }

    @Override
    public Collection<ChargeData> retrieveLoanProductCharges(Long loanProductId) {

        this.context.authenticatedUser();

        ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.loanProductChargeSchema() + " where c.is_deleted=0 and plc.product_loan_id=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] {loanProductId});
    }

    @Override
    public Collection<LoanChargeData> retrieveLoanCharges(Long loanId) {
        this.context.authenticatedUser();

        LoanChargeMapper rm = new LoanChargeMapper();

        String sql = "select " + rm.loanChargeSchema() + " where c.is_deleted=0 and lc.loan_id=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] {loanId});
    }

    @Override
    public Collection<ChargeData> retrieveLoanApplicableCharges() {
        this.context.authenticatedUser();

        ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=0 and c.is_active=1 and c.charge_applies_to_enum=? order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] {ChargeAppliesTo.LOAN.getValue()});
    }

    @Override
    public ChargeData retrieveLoanChargeTemplate() {
        this.context.authenticatedUser();

        List<EnumOptionData> allowedChargeCalculationTypeOptions = Arrays.asList(
                chargeCalculationType(ChargeCalculationType.FLAT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST)
        );

        List<EnumOptionData> allowedChargeTimeOptions = Arrays.asList(chargeTimeType(ChargeTimeType.DISBURSEMENT));

        return ChargeData.template(null, allowedChargeCalculationTypeOptions,
                null, allowedChargeTimeOptions);
    }

    private static final class ChargeMapper implements RowMapper<ChargeData> {

        public String chargeSchema(){
            return "c.id as id, c.name as name, c.amount as amount, c.currency_code as currencyCode, " +
                   "charge_applies_to_enum as chargeAppliesTo, charge_time_enum as chargeTime, " +
                   "charge_calculation_enum as chargeCalculation, is_active as active, oc.name as currencyName, " +
                   "oc.decimal_places as currencyDecimalPlaces, oc.display_symbol as currencyDisplaySymbol, " +
                   "oc.internationalized_name_code as currencyNameCode from m_charge c " +
                   "join m_organisation_currency oc on c.currency_code = oc.code";
        }

        public String loanProductChargeSchema(){
            return chargeSchema() + " join m_product_loan_charge plc on plc.charge_id = c.id";
        }



        @Override
        public ChargeData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String name = rs.getString("name");
            BigDecimal amount = rs.getBigDecimal("amount");

            String currencyCode = rs.getString("currencyCode");
            String currencyName = rs.getString("currencyName");
            String currencyNameCode = rs.getString("currencyNameCode");
            String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");

            CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces,
                    currencyDisplaySymbol, currencyNameCode);

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

    private static final class LoanChargeMapper implements RowMapper<LoanChargeData> {

        public String loanChargeSchema(){
            return "lc.id as id, c.id as chargeId, c.name as name, lc.amount as amount, c.currency_code as currencyCode, " +
                    "lc.charge_time_enum as chargeTime, " +
                    "lc.charge_calculation_enum as chargeCalculation, oc.name as currencyName, " +
                    "oc.decimal_places as currencyDecimalPlaces, oc.display_symbol as currencyDisplaySymbol, " +
                    "oc.internationalized_name_code as currencyNameCode from m_charge c " +
                    "join m_organisation_currency oc on c.currency_code = oc.code " +
                    "join m_loan_charge lc on lc.charge_id = c.id ";
        }

        @Override
        public LoanChargeData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            Long chargeId = rs.getLong("chargeId");
            String name = rs.getString("name");
            BigDecimal amount = rs.getBigDecimal("amount");

            String currencyCode = rs.getString("currencyCode");
            String currencyName = rs.getString("currencyName");
            String currencyNameCode = rs.getString("currencyNameCode");
            String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");

            CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces,
                    currencyDisplaySymbol, currencyNameCode);

            int chargeTime = rs.getInt("chargeTime");
            EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTime);

            int chargeCalculation = rs.getInt("chargeCalculation");
            EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);

            return new LoanChargeData(id, chargeId, name, currency, amount, chargeTimeType, chargeCalculationType);
        }
    }
}
