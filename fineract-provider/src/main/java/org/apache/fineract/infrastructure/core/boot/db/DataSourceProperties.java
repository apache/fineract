/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.boot.db;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.util.StringUtils;

public class DataSourceProperties extends PoolProperties {

	private JdbcDriverConfig jdbcConfig;

	public DataSourceProperties(JdbcDriverConfig jdbcConfig) {
		super();
		this.jdbcConfig = jdbcConfig;
		setDriverClassName(jdbcConfig.getDriverClassName());
		setDefaults();
	}

	protected void setDefaults() {
		setInitialSize(3);
		// setMaxIdle(6); -- strange, why?
		// setMinIdle(3); -- JavaDoc says default is initialSize.. so shouldn't
		// be needed
		if (getValidationQuery() == null)
			setValidationQuery("SELECT 1");
		setTestOnBorrow(true);
		setTestOnReturn(true);
		setTestWhileIdle(true);
		setTimeBetweenEvictionRunsMillis(30000);
		setTimeBetweenEvictionRunsMillis(60000);
		setLogAbandoned(true);
		setSuspectTimeout(60);

		setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport");
	}

	@Override
	public String getUrl() {
		String url = super.getUrl();
		if (StringUtils.hasText(url)) {
			throw new IllegalStateException();
		}
		return jdbcConfig.getProtocol() + ":" + jdbcConfig.getSubProtocol() + "://" + getHost() + ":" + getPort() + "/" + getDBName();
	}

	public String getHost() {
		return jdbcConfig.getHost();
	}

	public int getPort() {
		return jdbcConfig.getPort();
	}

	public String getDBName() {
		return jdbcConfig.getDbName();
	}

	@Override
	public String getUsername() {
		return this.jdbcConfig.getUsername();
	}

	@Override
	public String getPassword() {
		return this.jdbcConfig.getPassword();
	}

	@Override
	public void setUrl(String url) {
		throw new UnsupportedOperationException("Can not change url!");
	}

	@Override
	public void setUsername(String username) {
		throw new UnsupportedOperationException("Can not change username!");
	}

	@Override
	public void setPassword(String password) {
		throw new UnsupportedOperationException("Can not change password!");
	}
}