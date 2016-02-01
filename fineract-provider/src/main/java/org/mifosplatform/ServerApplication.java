/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform;

import org.mifosplatform.infrastructure.core.boot.AbstractApplicationConfiguration;
import org.mifosplatform.infrastructure.core.boot.ApplicationExitUtil;
import org.mifosplatform.infrastructure.core.boot.EmbeddedTomcatWithSSLConfiguration;
import org.mifosplatform.infrastructure.core.boot.db.DataSourceConfiguration;
import org.mifosplatform.infrastructure.core.boot.db.DataSourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * Mifos main() application which launches Mifos X in an embedded Tomcat HTTP
 * (using Spring Boot).
 *
 * The DataSource used is a to a "normal" external database (not use MariaDB4j).
 * This DataSource can be configured with parameters, see {@link DataSourceProperties}.
 *
 * You can easily launch this via Debug as Java Application in your IDE -
 * without needing command line Gradle stuff, no need to build and deploy a WAR,
 * remote attachment etc.
 *
 * It's the old/classic Mifos (non-X) Workspace 2.0 reborn for Mifos X! ;-)
 *
 * @see DataSourceProperties about how to configure the DataSource used
 * @see ServerWithMariaDB4jApplication for an alternative with an embedded DB
 */
public class ServerApplication {

	@Import({ DataSourceConfiguration.class, EmbeddedTomcatWithSSLConfiguration.class })
	private static class Configuration extends AbstractApplicationConfiguration { }

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext ctx = SpringApplication.run(Configuration.class, args);
		ApplicationExitUtil.waitForKeyPressToCleanlyExit(ctx);
	}

}
