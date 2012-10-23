package org.mifosng.platform.charge.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.data.LoanChargeData;
import org.mifosng.platform.charge.domain.ChargeAppliesTo;
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

@Service
public class ChargeReadPlatformServiceImpl implements ChargeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final CurrencyReadPlatformService currencyReadPlatformService;
	private final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService;
    
	@Autowired
	public ChargeReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final CurrencyReadPlatformService currencyReadPlatformService,
			final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.chargeDropdownReadPlatformService = chargeDropdownReadPlatformService;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.currencyReadPlatformService = currencyReadPlatformService;
	}

    @Override
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

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] {chargeId});
        } catch (EmptyResultDataAccessException e){
            throw new ChargeNotFoundException(chargeId);
        }
    }

    @Override
    public ChargeData retrieveNewChargeDetails() {

        this.context.authenticatedUser();

        final List<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<EnumOptionData> allowedChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService.retrieveCalculationTypes();
        final List<EnumOptionData> allowedChargeAppliesToOptions = this.chargeDropdownReadPlatformService.retrieveApplicableToTypes();
        final List<EnumOptionData> allowedChargeTimeOptions = this.chargeDropdownReadPlatformService.retrieveCollectionTimeTypes();
        		
        return ChargeData.template(currencyOptions, allowedChargeCalculationTypeOptions,
                allowedChargeAppliesToOptions, allowedChargeTimeOptions);
    }

    @Override
    public Collection<ChargeData> retrieveLoanProductCharges(Long loanProductId) {

        this.context.authenticatedUser();

        final ChargeMapper rm = new ChargeMapper();

        final String sql = "select " + rm.loanProductChargeSchema() + " where c.is_deleted=0 and plc.product_loan_id=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] {loanProductId});
    }

    @Override
    public Collection<LoanChargeData> retrieveLoanCharges(Long loanId) {
        this.context.authenticatedUser();

        final LoanChargeMapper rm = new LoanChargeMapper();

        final String sql = "select " + rm.loanChargeSchema() + " where c.is_deleted=0 and lc.loan_id=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] {loanId});
    }

    @Override
    public Collection<ChargeData> retrieveLoanApplicableCharges() {
        this.context.authenticatedUser();

        final ChargeMapper rm = new ChargeMapper();

        final String sql = "select " + rm.chargeSchema() + " where c.is_deleted=0 and c.is_active=1 and c.charge_applies_to_enum=? order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] {ChargeAppliesTo.LOAN.getValue()});
    }

    @Override
    public ChargeData retrieveLoanChargeTemplate() {
        this.context.authenticatedUser();

        final List<EnumOptionData> allowedChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService.retrieveCalculationTypes();
        final List<EnumOptionData> allowedChargeTimeOptions = this.chargeDropdownReadPlatformService.retrieveCollectionTimeTypes();
        
        return ChargeData.template(null, allowedChargeCalculationTypeOptions, null, allowedChargeTimeOptions);
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
        public ChargeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final BigDecimal amount = rs.getBigDecimal("amount");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces,
                    currencyDisplaySymbol, currencyNameCode);

            final int chargeAppliesTo = rs.getInt("chargeAppliesTo");
            final EnumOptionData chargeAppliesToType = ChargeEnumerations.chargeAppliesTo(chargeAppliesTo);

            final int chargeTime = rs.getInt("chargeTime");
            final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTime);

            final int chargeCalculation = rs.getInt("chargeCalculation");
            final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);
            final boolean active = rs.getBoolean("active");

            return new ChargeData(id, name, amount, currency, chargeTimeType, chargeAppliesToType, chargeCalculationType, active);
        }
    }

    private static final class LoanChargeMapper implements RowMapper<LoanChargeData> {

        public String loanChargeSchema(){
            return  "lc.id as id, c.id as chargeId, c.name as name, " +
            		"lc.amount as amountDue, lc.amount_paid_derived as amountPaid, lc.amount_outstanding_derived as amountOutstanding, " +
            		"lc.calculation_percentage as percentageOf, lc.calculation_on_amount as amountPercentageAppliedTo, " +
            		"lc.charge_time_enum as chargeTime, " +
                    "lc.charge_calculation_enum as chargeCalculation, " +
                    "c.currency_code as currencyCode, oc.name as currencyName, " +
                    "oc.decimal_places as currencyDecimalPlaces, oc.display_symbol as currencyDisplaySymbol, " +
                    "oc.internationalized_name_code as currencyNameCode from m_charge c " +
                    "join m_organisation_currency oc on c.currency_code = oc.code " +
                    "join m_loan_charge lc on lc.charge_id = c.id ";
        }

        @Override
        public LoanChargeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            
        	final Long id = rs.getLong("id");
        	final Long chargeId = rs.getLong("chargeId");
        	final String name = rs.getString("name");
        	final BigDecimal amount = rs.getBigDecimal("amountDue");
        	BigDecimal amountPaid = rs.getBigDecimal("amountPaid");
        	if (amountPaid == null) {
        		amountPaid = BigDecimal.ZERO;
        	}
        	final BigDecimal amountOutstanding = rs.getBigDecimal("amountOutstanding");
        	
        	final BigDecimal percentageOf = rs.getBigDecimal("percentageOf");
        	final BigDecimal amountPercentageAppliedTo = rs.getBigDecimal("amountPercentageAppliedTo");

        	final String currencyCode = rs.getString("currencyCode");
        	final String currencyName = rs.getString("currencyName");
        	final String currencyNameCode = rs.getString("currencyNameCode");
        	final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
        	final Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");

        	final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces,
                    currencyDisplaySymbol, currencyNameCode);

        	final int chargeTime = rs.getInt("chargeTime");
        	final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTime);

        	final int chargeCalculation = rs.getInt("chargeCalculation");
        	final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);

            return new LoanChargeData(id, chargeId, name, currency, amount, amountPaid, amountOutstanding, chargeTimeType, chargeCalculationType, percentageOf, amountPercentageAppliedTo);
        }
    }
}