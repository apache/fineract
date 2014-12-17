/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.boot;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * Base Spring Configuration with what's common to all Configuration subclasses.
 *
 * Notably the EnableAutoConfiguration excludes relevant for (and often adjusted
 * when upgrading versions of) Spring Boot, the "old" (pre. Spring Boot &
 * MariaDB4j) Mifos X appContext.xml which all configurations need, and the
 * web.xml successor WebXmlConfiguration.
 *
 * Should NOT include Configuration related to embedded Tomcat, data sources,
 * and MariaDB4j (because those differ in the subclasses).
 */
@Configuration
@Import({ WebXmlConfiguration.class, WebFrontEndConfiguration.class })
@ImportResource({ "classpath*:META-INF/spring/appContext.xml" })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		FlywayAutoConfiguration.class })
public abstract class AbstractApplicationConfiguration {

}
