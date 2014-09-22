/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.boot.db;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for a DataSource.
 * @see DataSourceProperties about how to configure this DS
 */
@Configuration
public class DataSourceConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(DataSourceConfiguration.class);

    @Bean
    public DataSourceProperties dataSourceProperties() {
	return new DataSourceProperties();
    }

    @Bean
    public DataSource tenantDataSourceJndi() {
	PoolConfiguration p = getProperties();
        org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource(p);
        logger.info("Created new DataSource; url=" + p.getUrl());
        return ds;
    }

    protected DataSourceProperties getProperties() {
        return dataSourceProperties();
    }

}