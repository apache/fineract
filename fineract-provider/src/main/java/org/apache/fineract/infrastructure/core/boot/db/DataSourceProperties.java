/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.boot.db;

import javax.validation.constraints.NotNull;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * Configurable DataSource. Properties have sensible defaults, but end-users can
 * override those via the Spring Values listed below; i.e. via -D Java System
 * properties, or main() command line arguments, OS environment variables, from
 * JNDI, or application.properties (thanks Spring Boot). For example:
 * -Dmifos.datasource.port=3307.
 */
// NOT a @Component - we do not want this to picked up by component scan, only explicitly declared in DataSourceConfiguration (if that's active)
public class DataSourceProperties extends PoolProperties {

    public final static String PORT = "mifos.datasource.port";
    public final static String HOST = "mifos.datasource.host";
    public final static String DB = "mifos.datasource.db";
    public final static String UID = "mifos.datasource.username";
    public final static String PWD = "mifos.datasource.password";
    public final static String PROTOCOL = "mifos.datasource.protocol";
    public final static String SUBPROTOCOL = "mifos.datasource.subprotocol";

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

    @Value("${" + SUBPROTOCOL + ":mysql}")
    private volatile @NotNull String jdbcSubprotocol;


    public DataSourceProperties() {
        super();

        // default to save us from re-specifying this; note that it can still be
        // overridden
        setDriverClassName(com.mysql.jdbc.Driver.class.getName());

        setMifosDefaults();
    }

    /**
     * as per (some of..) INSTALL.md and
     * org.mifosplatform.infrastructure.core.service
     * .TomcatJdbcDataSourcePerTenantService
     * .createNewDataSourceFor(MifosPlatformTenant)
     */
    protected void setMifosDefaults() {
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