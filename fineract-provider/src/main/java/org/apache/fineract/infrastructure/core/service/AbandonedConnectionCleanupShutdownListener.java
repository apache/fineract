/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;

@Service
public class AbandonedConnectionCleanupShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private final static Logger logger = LoggerFactory.getLogger(AbandonedConnectionCleanupShutdownListener.class);

    /**
     * @see JobRegisterServiceImpl#onApplicationEvent(ContextClosedEvent) doc
     *      re. why we use ContextClosedEvent instead of ContextStoppedEvent
     */
    @Override
    public void onApplicationEvent(@SuppressWarnings("unused") ContextClosedEvent event) {
        shutDowncleanUpThreadAndDeregisterJDBCDriver();
    }

    private void shutDowncleanUpThreadAndDeregisterJDBCDriver() {
        try {
            AbandonedConnectionCleanupThread.shutdown();
            logger.info("Shut-down of AbandonedConnectionCleanupThread successful");
        } catch (Throwable t) {
            logger.error("Exception occurred while shut-down of AbandonedConnectionCleanupThread", t);
        }

        // This manually deregisters JDBC driver, which prevents Tomcat 7 from
        // complaining about memory leaks
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                java.sql.DriverManager.deregisterDriver(driver);
                logger.info("JDBC driver de-registered successfully");
            } catch (Throwable t) {
                logger.error("Exception occured while deristering jdbc driver", t);
            }
        }
        try {
            Thread.sleep(2000L);
        } catch (Exception e) {}
    }
}