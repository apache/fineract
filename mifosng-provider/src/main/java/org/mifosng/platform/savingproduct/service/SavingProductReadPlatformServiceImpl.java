package org.mifosng.platform.savingproduct.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.DateTime;
import org.mifosng.platform.api.data.SavingProductData;
import org.mifosng.platform.api.data.SavingProductLookup;
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
	
	@Autowired
	public SavingProductReadPlatformServiceImpl(final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource) {
		
		this.context=context;
		jdbcTemplate=new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<SavingProductData> retrieveAllSavingProducts() {
		
		this.context.authenticatedUser();
		
		SavingProductMapper savingProductMapper=new SavingProductMapper();
		
		String sql = "select " +savingProductMapper.savingProductSchema();
		
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
					+ " where sp.id = ?";
			SavingProductData productData = this.jdbcTemplate.queryForObject(sql,
					savingProductMapper, new Object[] { savingProductId });

			return productData;
		} catch (EmptyResultDataAccessException e) {
			throw new LoanProductNotFoundException(savingProductId);
		}
	}		// TODO Auto-generated method stub

	@Override
	public SavingProductData retrieveNewSavingProductDetails() {
		
		SavingProductData productData=new SavingProductData();
		
		return productData;
	}
	
	private static final class SavingProductMapper implements RowMapper<SavingProductData> {
		public SavingProductMapper() {
			
		}
		
		public String savingProductSchema(){
			return "sp.id as id,sp.name as name, sp.description as description, sp.created_date as createdon, sp.lastmodified_date as modifiedon from portfolio_product_savings sp ";
		}

		@Override
		public SavingProductData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			
			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String description = rs.getString("description");
			
			DateTime createdOn = JdbcSupport.getDateTime(rs, "createdon");
			DateTime lastModifedOn = JdbcSupport.getDateTime(rs, "modifiedon");
			
			return new SavingProductData(createdOn, lastModifedOn, id, name,description);
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

}
