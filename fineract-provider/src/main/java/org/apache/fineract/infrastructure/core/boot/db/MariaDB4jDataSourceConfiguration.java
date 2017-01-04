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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;

@Configuration
public class MariaDB4jDataSourceConfiguration extends DataSourceConfiguration {

	@Bean
	public MariaDB4jSetupService mariaDB4jSetUp() {
		return new MariaDB4jSetupService(mariaDB4j().getDB());
	}

	@Bean
	public MariaDB4jSpringService mariaDB4j() {
		MariaDB4jSpringService mariaDB4jSpringService = new MariaDB4jSpringService();
		mariaDB4jSpringService.setDefaultBaseDir("build/db/bin");
		mariaDB4jSpringService.setDefaultDataDir("build/db/data");
		return mariaDB4jSpringService;
	}

	@Override
    // NOT @Bean @Override dataSourceProperties() - doesn't work :(
	protected DataSourceProperties getProperties() {
		DataSourceProperties p = super.getProperties();
		String dbName = mariaDB4jSetUp().getTenantDBName();
		// Do not use p.setUrl(mariaDB4j().getConfiguration().getURL(dbName));
		// Because TenantDataSourcePortFixService needs separate
		// host/port/db/uid/pwd:
		// (DataSourceProperties getUrl() creates the correct JDBC URL from it)
		// This intentionally overrides any fineract.datasource.* settings, because
		// in this configuration, logically the mariaDB4j settings take
		// precedence:
		p.setHost("localhost");
		p.setPort(mariaDB4j().getConfiguration().getPort());
		p.setDBName(dbName);
		// TODO p.setUsername(mariaDB4j().getConfiguration().getUsername());
		// TODO p.setPassword(mariaDB4j().getConfiguration().getPassword());
		return p;
	}

}