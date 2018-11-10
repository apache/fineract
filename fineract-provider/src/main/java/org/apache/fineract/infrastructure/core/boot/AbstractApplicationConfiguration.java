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
package org.apache.fineract.infrastructure.core.boot;

import org.apache.fineract.notification.config.MessagingConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

/**
 * Base Spring Configuration with what's common to all Configuration subclasses.
 *
 * Notably the EnableAutoConfiguration excludes relevant for (and often adjusted
 * when upgrading versions of) Spring Boot, the "old" (pre. Spring Boot &amp;
 * MariaDB4j) fineract appContext.xml which all configurations need, and the
 * web.xml successor WebXmlConfiguration.
 *
 * Should NOT include Configuration related to embedded Tomcat, data sources,
 * and MariaDB4j (because those differ in the subclasses).
 */
@Configuration
@Import({ WebXmlConfiguration.class, WebXmlOauthConfiguration.class, WebFrontEndConfiguration.class,
	MessagingConfiguration.class, WebTwoFactorXmlConfiguration.class })
@ImportResource({ "classpath*:META-INF/spring/appContext.xml" })
@PropertySource(value="classpath:META-INF/spring/jdbc.properties")
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		FlywayAutoConfiguration.class })
public abstract class AbstractApplicationConfiguration {

}
