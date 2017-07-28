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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.apache.fineract.infrastructure.core.boot.AbstractApplicationConfiguration;
import org.apache.fineract.infrastructure.core.boot.ApplicationExitUtil;
import org.apache.fineract.infrastructure.core.boot.EmbeddedTomcatWithSSLConfiguration;
import org.apache.fineract.infrastructure.core.boot.db.MariaDB4jDataSourceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

/**
 * Fineract main() application which launches Fineract in an embedded Tomcat HTTP
 * server (using Spring Boot), as well as an embedded database (using
 * MariaDB4j).
 *
 * You can easily launch this via Debug as Java Application in your IDE -
 * without needing command line Gradle stuff, no need to build and deploy a WAR,
 * remote attachment etc.
 *
 * It's the old/classic Mifos (non-X) Workspace 2.0 reborn for Fineract! ;-)
 *
 * @see ServerApplication for the same without the embedded MariaDB4j database
 */
public class ServerWithMariaDB4jApplication {
    private final static Logger logger = LoggerFactory.getLogger(ServerWithMariaDB4jApplication.class);

	@Import({ MariaDB4jDataSourceConfiguration.class, EmbeddedTomcatWithSSLConfiguration.class })
	public static class Configuration extends AbstractApplicationConfiguration { }

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext ctx = SpringApplication.run(Configuration.class, args);
		if (!Desktop.isDesktopSupported()) {
			logger.info("Not going to open UI homepage in local web browser, because !Desktop.isDesktopSupported()");

		} else {
			// apps/community-app/dist/community-app/index.html
			Resource distResource = ctx.getResource("file:" + System.getProperty("user.dir") +
					System.getProperty("file.separator") + "apps" +
					System.getProperty("file.separator") + "community-app" +
					System.getProperty("file.separator") + "dist" +
					System.getProperty("file.separator") + "community-app" +
					System.getProperty("file.separator") + "index.html");
			URI distURI = URI.create("https://localhost:8443/fineract-provider" +
					"/apps/community-app/index.html?baseApiUrl=https://localhost:8443" +
					"&tenantIdentifier=default#/");

			// apps/community-app/app/index.html
			Resource devResource = ctx.getResource("file:" + System.getProperty("user.dir") +
					System.getProperty("file.separator") + "apps" +
					System.getProperty("file.separator") + "community-app" +
					System.getProperty("file.separator") + "app" +
					System.getProperty("file.separator") + "index.html");
			URI devURI = URI.create("https://localhost:8443/fineract-provider" +
					"/apps/community-app/app/index.html?baseApiUrl=https://localhost:8443" +
					"&tenantIdentifier=default#/");

			if (distResource.exists()) {
				openWebBrowser(distURI);
			} else if (devResource.exists()) {
				openWebBrowser(devURI);				
			} else {
				logger.error("Cannot open Fineract UI in browser; not found: " + distResource.toString());
			}
		}
		
		// TODO Tray Icon stuff; dig out my very own old @see https://github.com/mifos/head/tree/hudsonBuild-MIFOS-5157_Launch4j-EXE_NewDist-squash1/server-jetty/src/main/java/org/mifos/server/tray
		
		ApplicationExitUtil.waitForKeyPressToCleanlyExit(ctx);
	}

	private static void openWebBrowser(URI uri) {
		try {
			logger.info("Opening Fineract UI in browser: " + uri.toString());
			Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			logger.error("IOException when opening Fineract UI in browser: " + uri.toString(), e);
		}
	}

}
