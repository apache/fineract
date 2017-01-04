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

import javax.validation.constraints.NotNull;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * Configurable DataSource. Properties have sensible defaults, but end-users can
 * override those via the Spring Values listed below; i.e. via -D Java System
 * properties, or main() command line arguments, OS environment variables, from
 * JNDI, or application.properties (thanks Spring Boot). For example:
 * -Dfineract.datasource.port=3307.
 */
// NOT a @Component - we do not want this to picked up by component scan, only explicitly declared in DataSourceConfiguration (if that's active)
public class DataSourceProperties extends PoolProperties {

    public final static String PORT = "fineract.datasource.port";
    public final static String HOST = "fineract.datasource.host";
    public final static String DB = "fineract.datasource.db";
    public final static String UID = "fineract.datasource.username";
    public final static String PWD = "fineract.datasource.password";
    public final static String PROTOCOL = "fineract.datasource.protocol";
    public final static String SUBPROTOCOL = "fineract.datasource.subprotocol";

    @Value("${" + PORT + ":3306}")
    private volatile @NotNull int port;

    @Value("${" + HOST + ":localhost}")
    private volatile @NotNull String hostname;

    @Value("${" + DB + ":mifosplatform-tenants}")
    private volatile @NotNull String dbName;

    @Value("${" + UID + ":root}")
    private volatile @NotNull String username;

    @Value("${" + PWD + ":mysql}")
    private volatile @NotNull String password;

    @Value("${" + PROTOCOL + ":jdbc}")
    private volatile @NotNull String jdbcProtocol;

    @Value("${" + SUBPROTOCOL + ":mysql:thin}")
    private volatile @NotNull String jdbcSubprotocol;

    public DataSourceProperties(String driverClassName, String protocol, String subProtocol, Integer port) {
        super();
        setDriverClassName(driverClassName);
        this.jdbcProtocol = protocol ;
        this.jdbcSubprotocol = subProtocol ;
        this.port = port ;
        setDefaults();
    }

    /**
     * as per (some of..) INSTALL.md and
     * org.apache.fineract.infrastructure.core.service
     * .TomcatJdbcDataSourcePerTenantService
     * .createNewDataSourceFor(FineractPlatformTenant)
     */
    protected void setDefaults() {
        setInitialSize(3);
        // setMaxIdle(6); -- strange, why?
        // setMinIdle(3); -- JavaDoc says default is initialSize.. so shouldn't
        // be needed
        if (getValidationQuery() == null) setValidationQuery("SELECT 1");
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
    public void setUrl(@SuppressWarnings("unused") String url) {
	throw new UnsupportedOperationException("Use setHost/Port/DB() instead of setURL()");
    }

	@Override
	public String getUrl() {
		String url = super.getUrl();
		if (StringUtils.hasText(url)) {
			throw new IllegalStateException();
		}
		return jdbcProtocol + ":" + jdbcSubprotocol + "://" + getHost() + ":" + getPort() + "/" + getDBName();
	}

	public String getHost() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public String getDBName() {
		return dbName;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setHost(String hostname) {
		this.hostname = hostname;
	}

	public void setDBName(String dbName) {
		this.dbName = dbName;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

}