/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.boot.db;

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
		// This intentionally overrides any mifos.datasource.* settings, because
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