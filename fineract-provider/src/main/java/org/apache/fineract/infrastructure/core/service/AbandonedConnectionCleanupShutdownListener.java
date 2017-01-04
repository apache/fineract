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
package org.apache.fineract.infrastructure.core.service;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import org.apache.fineract.infrastructure.jobs.service.JobRegisterServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

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
        /*try {
        	
            AbandonedConnectionCleanupThread.shutdown(); tomcat memoroy leak with mysql connector. With Drizzle not required
            logger.info("Shut-down of AbandonedConnectionCleanupThread successful"); 
        } catch (Throwable t) {
            logger.error("Exception occurred while shut-down of AbandonedConnectionCleanupThread", t);
        }*/

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