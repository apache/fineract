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

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

/**
 * Required to still have a working legacy "classic" WAR.
 *
 * Configuration just adds the JNDI-based DataSource lookup to its
 * AbstractApplicationConfiguration.
 *
 * This (intentionally) only configures the original (pre-Spring Boot &amp;
 * MariaDB4j) fineract Spring Beans, and does NOT include the embedded Tomcat
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