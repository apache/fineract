/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.mifosplatform.infrastructure.core.boot.AbstractApplicationConfiguration;
import org.mifosplatform.infrastructure.core.boot.ApplicationExitUtil;
import org.mifosplatform.infrastructure.core.boot.EmbeddedTomcatWithSSLConfiguration;
import org.mifosplatform.infrastructure.core.boot.db.MariaDB4jDataSourceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

/**
 * Mifos main() application which launches Mifos X in an embedded Tomcat HTTP
 * server (using Spring Boot), as well as an embedded database (using
 * MariaDB4j).
 *
 * You can easily launch this via Debug as Java Application in your IDE -
 * without needing command line Gradle stuff, no need to build and deploy a WAR,
 * remote attachment etc.
 *
 * It's the old/classic Mifos (non-X) Workspace 2.0 reborn for Mifos X! ;-)
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
			URI distURI = URI.create("https://localhost:8443/mifosng-provider" +
					"/apps/community-app/dist/community-app/index.html?baseApiUrl=https://localhost:8443" +
					"&tenantIdentifier=default#/");

			// apps/community-app/app/index.html
			Resource devResource = ctx.getResource("file:" + System.getProperty("user.dir") +
					System.getProperty("file.separator") + "apps" +
					System.getProperty("file.separator") + "community-app" +
					System.getProperty("file.separator") + "app" +
					System.getProperty("file.separator") + "index.html");
			URI devURI = URI.create("https://localhost:8443/mifosng-provider" +
					"/apps/community-app/app/index.html?baseApiUrl=https://localhost:8443" +
					"&tenantIdentifier=default#/");

			if (distResource.exists()) {
				openWebBrowser(distURI);
			} else if (devResource.exists()) {
				openWebBrowser(devURI);				
			} else {
				logger.error("Cannot open Mifos X UI in browser; not found: " + distResource.toString());
			}
		}
		
		// TODO Tray Icon stuff; dig out my very own old @see https://github.com/mifos/head/tree/hudsonBuild-MIFOS-5157_Launch4j-EXE_NewDist-squash1/server-jetty/src/main/java/org/mifos/server/tray
		
		ApplicationExitUtil.waitForKeyPressToCleanlyExit(ctx);
	}

	private static void openWebBrowser(URI uri) {
		try {
			logger.info("Opening Mifos X UI in browser: " + uri.toString());
			Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			logger.error("IOException when opening Mifos X UI in browser: " + uri.toString(), e);
		}
	}

}
