package org.mifosng.platform.depositproduct.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.DepositProductData;
import org.mifosng.platform.api.data.DepositProductLookup;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DepositProductReadPlatformServiceImpl implements
		DepositProductReadPlatformService {
	
	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;
	private final CurrencyReadPlatformService currencyReadPlatformService;
	
	@Autowired
	public DepositProductReadPlatformServiceImpl(final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource,final CurrencyReadPlatformService currencyReadPlatformService) {
		this.context=context;
		jdbcTemplate=new JdbcTemplate(dataSource);
		this.currencyReadPlatformService=currencyReadPlatformService;
	}

	@Override
	public Collection<DepositProductData> retrieveAllDepositProducts() {
		this.context.authenticatedUser();
		DepositProductMapper depositProductMapper= new DepositProductMapper();
		String sql="select "+depositProductMapper.depositProductSchema() + " where dp.is_deleted=0";
		return this.jdbcTemplate.query(sql,depositProductMapper, new Object[]{});
	}

	@Override
	public Collection<DepositProductLookup> retrieveAllDepositProductsForLookup() {
		this.context.authenticatedUser();
		DepositProductLookupMapper depositProductLookupMapper=new DepositProductLookupMapper();
		String sql = "select "+depositProductLookupMapper.depositProductLookupSchema();
		return this.jdbcTemplate.query(sql, depositProductLookupMapper, new Object[]{});
	}

	@Override
	public DepositProductData retrieveDepositProductData(Long productId) {
		DepositProductMapper depositProductMapper=new DepositProductMapper();
		String sql = "select "+ depositProductMapper.depositProductSchema() +" where dp.id = ? and dp.is_deleted=0";
		
		DepositProductData productData = this.jdbcTemplate.queryForObject(sql, depositProductMapper, new Object[]{productId});
		
		List<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
		return new DepositProductData(productData, currencyOptions);
	}

	@Override
	public DepositProductData retrieveNewDepositProductDetails() {
		
		List<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
		return new DepositProductData(currencyOptions);
	}
	
	private static final class DepositProductMapper implements RowMapper<DepositProductData>{
		
		public String depositProductSchema(){
			return "dp.id as id,dp.name as name, dp.description as description,dp.currency_code as currencyCode, dp.currency_digits as currencyDigits,dp.minimum_balance as minimumBalance,dp.maximum_balance as maximumBalance," +
					"dp.created_date as createdon, dp.lastmodified_date as modifiedon,dp.tenure_months as tenureMonths, dp.maturity_default_interest_rate as maturityDefaultInterestRate, " +
					"dp.maturity_min_interest_rate as maturityMinInterestRate, dp.maturity_max_interest_rate as maturityMaxInterestRate, dp.can_renew as canRenew, dp.can_pre_close as canPreClose, dp.pre_closure_interest_rate as preClosureInterestRate, " +
					"curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, curr.display_symbol as currencyDisplaySymbol " +
					"from portfolio_product_deposit dp join ref_currency curr on curr.code = dp.currency_code ";
		}

		@Override
		public DepositProductData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			
			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String description = rs.getString("description");
			
			String currencyCode = rs.getString("currencyCode");
			Integer currencyDigits = JdbcSupport.getInteger(rs,"currencyDigits");
			
			DateTime createdOn = JdbcSupport.getDateTime(rs, "createdon");
			DateTime lastModifedOn = JdbcSupport.getDateTime(rs, "modifiedon");
			
			BigDecimal minimumBalance=rs.getBigDecimal("minimumBalance");
			BigDecimal maximumBalance=rs.getBigDecimal("maximumBalance");
			
			Integer tenureMonths = JdbcSupport.getInteger(rs, "tenureMonths");
			BigDecimal maturityDefaultInterestRate=rs.getBigDecimal("maturityDefaultInterestRate");
			BigDecimal maturityMinInterestRate= rs.getBigDecimal("maturityMinInterestRate");
			BigDecimal maturityMaxInterestRate = rs.getBigDecimal("maturityMaxInterestRate");
			
			Boolean canRenew = rs.getBoolean("canRenew");
			Boolean canPreClose=rs.getBoolean("canPreClose");
			
			BigDecimal preClosureInterestRate=rs.getBigDecimal("preClosureInterestRate");
			
			return new DepositProductData(createdOn, lastModifedOn, id, name, description, currencyCode, currencyDigits, minimumBalance, maximumBalance,
					tenureMonths, maturityDefaultInterestRate,maturityMinInterestRate,maturityMaxInterestRate,canRenew,canPreClose,preClosureInterestRate);
		}
		
	}
	
	private static final class DepositProductLookupMapper implements RowMapper<DepositProductLookup> {

		public String depositProductLookupSchema() {
			return "dp.id as id, dp.name as name from portfolio_product_deposit dp";
		}

		@Override
		public DepositProductLookup mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");

			return new DepositProductLookup(id, name);
		}

	}
}