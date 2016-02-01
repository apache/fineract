package org.apache.fineract;

import org.apache.fineract.infrastructure.core.boot.AbstractApplicationConfiguration;
import org.apache.fineract.infrastructure.core.boot.ApplicationExitUtil;
import org.apache.fineract.infrastructure.core.boot.EmbeddedTomcatWithSSLConfiguration;
import org.apache.fineract.infrastructure.core.boot.db.DataSourceConfiguration;
import org.apache.fineract.infrastructure.core.boot.db.DataSourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * Fineract main() application which launches Fineract in an embedded Tomcat HTTP
 * (using Spring Boot).
 *
 * The DataSource used is a to a "normal" external database (not use MariaDB4j).
 * This DataSource can be configured with parameters, see {@link DataSourceProperties}.
 *
 * You can easily launch this via Debug as Java Application in your IDE -
 * without needing command line Gradle stuff, no need to build and deploy a WAR,
 * remote attachment etc.
 *
 * It's the old/classic Mifos (non-X) Workspace 2.0 reborn for Fineract! ;-)
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
