package org.mifosng.platform.savingproduct.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.SavingProductData;
import org.mifosng.platform.api.data.SavingProductLookup;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.mifosng.platform.exceptions.LoanProductNotFoundException;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SavingProductReadPlatformServiceImpl implements
		SavingProductReadPlatformService {
	
	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;
	private final CurrencyReadPlatformService currencyReadPlatformService;
	
	@Autowired
	public SavingProductReadPlatformServiceImpl(final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource,final CurrencyReadPlatformService currencyReadPlatformService) {
		
		this.context=context;
		jdbcTemplate=new JdbcTemplate(dataSource);
		this.currencyReadPlatformService=currencyReadPlatformService;
	}

	@Override
	public Collection<SavingProductData> retrieveAllSavingProducts() {
		
		this.context.authenticatedUser();
		
		SavingProductMapper savingProductMapper=new SavingProductMapper();
		
		String sql = "select " + savingProductMapper.savingProductSchema() + " where sp.is_deleted=0";
		
		return this.jdbcTemplate.query(sql,savingProductMapper, new Object[]{});
	
	}

	@Override
	public Collection<SavingProductLookup> retrieveAllSavingProductsForLookup() {
		
		this.context.authenticatedUser();
		
		SavingProductLookupMapper savingProductLookupMapper=new SavingProductLookupMapper();
		
		String sql="select"+savingProductLookupMapper.savingProductLookupSchema();
		
		return this.jdbcTemplate.query(sql, savingProductLookupMapper, new Object[]{});
	}

	@Override
	public SavingProductData retrieveSavingProduct(Long savingProductId) {
		try{
			SavingProductMapper savingProductMapper=new SavingProductMapper();
			String sql = "select " + savingProductMapper.savingProductSchema()
					+ " where sp.id = ? and sp.is_deleted=0";
			SavingProductData productData = this.jdbcTemplate.queryForObject(sql,
					savingProductMapper, new Object[] { savingProductId });
			
			populateProductDataWithDropdownOptions(productData);

			return productData;
		} catch (EmptyResultDataAccessException e) {
			throw new LoanProductNotFoundException(savingProductId);
		}
	}		// TODO Auto-generated method stub

	@Override
	public SavingProductData retrieveNewSavingProductDetails() {
		
		SavingProductData productData=new SavingProductData();
		
		populateProductDataWithDropdownOptions(productData);
		
		return productData;
	}
	
	private static final class SavingProductMapper implements RowMapper<SavingProductData> {
		
		public String savingProductSchema(){
			return "sp.id as id,sp.name as name, sp.description as description,sp.currency_code as currencyCode, sp.currency_digits as currencyDigits,sp.interest_rate as interestRate,sp.minimum_balance as minimumBalance,sp.maximum_balance as maximumBalance,sp.created_date as createdon, sp.lastmodified_date as modifiedon, "
				+  "curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, curr.display_symbol as currencyDisplaySymbol" 
				+  "  from portfolio_product_savings sp join ref_currency curr on curr.code = sp.currency_code";
		}

		@Override
		public SavingProductData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			
			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String description = rs.getString("description");
			
			String currencyCode = rs.getString("currencyCode");
			//String currencyName = rs.getString("currencyName");
			//String currencyNameCode = rs.getString("currencyNameCode");
			//String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
			Integer currencyDigits = JdbcSupport.getInteger(rs,"currencyDigits");
			
			//CurrencyData currencyData = new CurrencyData(currencyCode,currencyName, currencyDigits, currencyDisplaySymbol,currencyNameCode);*/
			BigDecimal interestRate = rs.getBigDecimal("interestRate");
			
			DateTime createdOn = JdbcSupport.getDateTime(rs, "createdon");
			DateTime lastModifedOn = JdbcSupport.getDateTime(rs, "modifiedon");
			
			BigDecimal minimumBalance=rs.getBigDecimal("minimumBalance");
			BigDecimal maximumBalance=rs.getBigDecimal("maximumBalance");
			
			return new SavingProductData(createdOn, lastModifedOn, id, name,description,interestRate,currencyCode,currencyDigits,minimumBalance,maximumBalance);
		}
	}
	
	private static final class SavingProductLookupMapper implements
			RowMapper<SavingProductLookup> {

		public String savingProductLookupSchema() {
			return "sp.id as id, sp.name as name from portfolio_product_savings sp";
		}

		@Override
		public SavingProductLookup mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");

			return new SavingProductLookup(id, name);
		}

	}
	private void populateProductDataWithDropdownOptions(final SavingProductData productData) {

		List<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
		productData.setCurrencyOptions(currencyOptions);
	}

}
