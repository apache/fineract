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
package org.apache.fineract;

import org.apache.fineract.infrastructure.core.boot.WebFrontEndConfiguration;
import org.apache.fineract.infrastructure.core.boot.WebTwoFactorXmlConfiguration;
import org.apache.fineract.infrastructure.core.boot.WebXmlConfiguration;
import org.apache.fineract.infrastructure.core.boot.WebXmlOauthConfiguration;
import org.apache.fineract.infrastructure.core.boot.db.DataSourceConfiguration;
import org.apache.fineract.notification.config.MessagingConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@EnableAutoConfiguration(exclude = { 
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		FlywayAutoConfiguration.class })
@Import({ 
		DataSourceConfiguration.class,
		WebXmlConfiguration.class, 
		WebXmlOauthConfiguration.class, 
		WebFrontEndConfiguration.class,
		MessagingConfiguration.class, 
		WebTwoFactorXmlConfiguration.class })
@ImportResource({ "classpath*:META-INF/spring/appContext.xml" })
public class ServerApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ServerApplication.class, args);
	}
}