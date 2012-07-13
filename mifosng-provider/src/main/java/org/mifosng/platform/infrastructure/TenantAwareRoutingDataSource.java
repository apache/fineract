package org.mifosng.platform.infrastructure;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service(value="tenantAwareDataSource")
public class TenantAwareRoutingDataSource extends AbstractDataSource {

	@Autowired
	private DataSourcePerTenantService dataSourcePerTenantService;
	
	@Override
	public Connection getConnection() throws SQLException {
		return determineTargetDataSource().getConnection();
	}

	private DataSource determineTargetDataSource() {
		return dataSourcePerTenantService.retrieveTenantAwareDataSource();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return determineTargetDataSource().getConnection(username, password);
	}
}