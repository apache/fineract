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

import javax.sql.DataSource;

import org.apache.fineract.infrastructure.core.boot.JDBCDriverConfig;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for a DataSource.
 * @see DataSourceProperties about how to configure this DS
 */
@Configuration
public class DataSourceConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(DataSourceConfiguration.class);
	
	@Autowired JDBCDriverConfig config ;
	
    @Bean
    public DataSourceProperties dataSourceProperties() {
	return new DataSourceProperties(config.getDriverClassName(), config.getProtocol(), config.getSubProtocol(), config.getPort());
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