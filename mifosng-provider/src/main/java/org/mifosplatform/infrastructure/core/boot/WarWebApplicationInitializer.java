/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.boot;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

/**
 * Required to still have a working legacy "classic" WAR.
 *
 * Configuration just adds the JNDI-based DataSource lookup to its
 * AbstractApplicationConfiguration.
 *
 * This (intentionally) only configures the original (pre-Spring Boot &
 * MariaDB4j) Mifos X Spring Beans, and does NOT include the embedded Tomcat
 * (incl. TomcatSSLConfiguration) nor the MariaDB4jSetupService or
 * MariaDB4jDataSourceConfiguration, and not even the DataSourceConfiguration
 * (as it uses "classic" JNDI) - we want the WAR to "work like before".
 *
 * @see <a
 *      href="http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-convert-an-existing-application-to-spring-boot">#howto-convert-an-existing-application-to-spring-boot</a>
 */
public class WarWebApplicationInitializer extends SpringBootServletInitializer {

	@ImportResource({ "classpath*:META-INF/spring/jndi.xml" })
	private static class Configuration extends AbstractApplicationConfiguration {
	}

	@Override
	protected SpringApplicationBuilder configure(
			SpringApplicationBuilder application) {
		// let's share Spring Boot Love, so no showBanner(false)
		return application.sources(Configuration.class);
	}

}