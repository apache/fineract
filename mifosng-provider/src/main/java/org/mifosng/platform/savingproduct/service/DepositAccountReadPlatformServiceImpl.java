package org.mifosng.platform.savingproduct.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.DepositAccountData;
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
public class DepositAccountReadPlatformServiceImpl implements DepositAccountReadPlatformService {
	
	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public DepositAccountReadPlatformServiceImpl(final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource) {
		this.context=context;
		jdbcTemplate=new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<DepositAccountData> retrieveAllDepositAccounts() {
		
		this.context.authenticatedUser();
		
		DepositAccountMapper mapper = new DepositAccountMapper();
		
		String sql = "select " + mapper.schema() + " where da.is_deleted=0";
		
		return this.jdbcTemplate.query(sql,mapper, new Object[]{});
	
	}

	@Override
	public DepositAccountData retrieveDepositAccount(final Long accountId) {
		try{
			DepositAccountMapper mapper = new DepositAccountMapper();
			
			String sql = "select " + mapper.schema()
					+ " where da.id = ? and da.is_deleted=0";
			
			DepositAccountData productData = this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { accountId });
			
			return productData;
		} catch (EmptyResultDataAccessException e) {
			throw new LoanProductNotFoundException(accountId);
		}
	}

	@Override
	public DepositAccountData retrieveNewDepositAccountDetails() {
		
		DepositAccountData productData = new DepositAccountData();
		
		return productData;
	}
	
	private static final class DepositAccountMapper implements RowMapper<DepositAccountData> {
		
		public String schema() {
			return "da.id as id, da.client_id as clientId, da.product_id as productId," 
				+  " da.currency_code as currencyCode, da.currency_digits as currencyDigits, " 
				+  " da.deposit_amount as depositAmount, "	
				+  " da.maturity_nominal_interest_rate as interestRate, da.tenure_months as termInMonths, da.projected_commencement_date as projectedCommencementDate," 
				+  " da.actual_commencement_date as actualCommencementDate, da.projected_maturity_date as projectedMaturityDate, da.actual_maturity_date as actualMaturityDate,"
				+  " da.projected_interest_accrued_on_maturity as projectedInterestAccrued, da.actual_interest_accrued as actualInterestAccrued, "
				+  " da.projected_total_maturity_amount as projectedMaturityAmount, da.actual_total_amount as actualMaturityAmount, "
				+  " da.can_renew as renewalAllowed, da.can_pre_close as preClosureAllowed, da.pre_closure_interest_rate as preClosureInterestRate, "
				+  " da.created_date as createdon, da.lastmodified_date as modifiedon, "
				+  " c.firstname as firstname, c.lastname as lastname, pd.name as productName,"
				+  " curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, curr.display_symbol as currencyDisplaySymbol" 
				+  " from portfolio_deposit_account da " 
				+  " join ref_currency curr on curr.code = da.currency_code " 
				+  " join portfolio_client c on c.id = da.client_id " 
				+  " join portfolio_product_deposit pd on pd.id = da.product_id";
		}

		@Override
		public DepositAccountData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			
			Long id = rs.getLong("id");
			Long clientId = rs.getLong("clientId");
			String clientName = rs.getString("firstname") + " " + rs.getString("lastname");
			Long productId = rs.getLong("productId");
			String productName = rs.getString("productName");
			
			String currencyCode = rs.getString("currencyCode");
			String currencyName = rs.getString("currencyName");
			String currencyNameCode = rs.getString("currencyNameCode");
			String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
			Integer currencyDigits = JdbcSupport.getInteger(rs,"currencyDigits");
			CurrencyData currencyData = new CurrencyData(currencyCode,currencyName, currencyDigits, currencyDisplaySymbol,currencyNameCode);
			
			BigDecimal depositAmount = rs.getBigDecimal("depositAmount");
			BigDecimal interestRate = rs.getBigDecimal("interestRate");
			
			Integer termInMonths = JdbcSupport.getInteger(rs,"termInMonths");
			
			LocalDate projectedCommencementDate = JdbcSupport.getLocalDate(rs, "projectedCommencementDate");
			LocalDate actualCommencementDate = JdbcSupport.getLocalDate(rs, "actualCommencementDate");
			LocalDate projectedMaturityDate = JdbcSupport.getLocalDate(rs, "projectedMaturityDate");
			LocalDate actualMaturityDate = JdbcSupport.getLocalDate(rs, "actualMaturityDate");
			BigDecimal projectedInterestAccrued = rs.getBigDecimal("projectedInterestAccrued");
			BigDecimal actualInterestAccrued = rs.getBigDecimal("actualInterestAccrued");
			
			BigDecimal projectedMaturityAmount = rs.getBigDecimal("projectedMaturityAmount");
			BigDecimal actualMaturityAmount = rs.getBigDecimal("actualMaturityAmount");
			
			boolean renewalAllowed = rs.getBoolean("renewalAllowed");
			boolean preClosureAllowed = rs.getBoolean("preClosureAllowed");
			
			BigDecimal preClosureInterestRate = rs.getBigDecimal("preClosureInterestRate");
			
			DateTime createdOn = JdbcSupport.getDateTime(rs, "createdon");
			DateTime lastModifedOn = JdbcSupport.getDateTime(rs, "modifiedon");
			
			return new DepositAccountData(createdOn, lastModifedOn, id, clientId, clientName, productId, productName, currencyData, depositAmount, 
					interestRate, termInMonths, projectedCommencementDate, actualCommencementDate, projectedMaturityDate, actualMaturityDate, 
					projectedInterestAccrued, actualInterestAccrued, projectedMaturityAmount, actualMaturityAmount, renewalAllowed, preClosureAllowed, preClosureInterestRate);
		}
	}
}