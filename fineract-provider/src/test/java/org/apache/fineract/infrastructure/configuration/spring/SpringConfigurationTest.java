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
package org.apache.fineract.infrastructure.configuration.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * This integration test ensure that the "production" Spring XML configuration
 * files (appContext.xml & Co.) are valid. It does not need any database for
 * that.
 *
 * Note: For a simple test like this, contrary to
 * AbstractSpringBootWithMariaDB4jIntegrationTest, there is no need to use
 * Boot's SpringApplicationConfiguration here, instead the simpler classic Sring
 * core ContextConfiguration is sufficient (as long as the
 * TestsWithoutDatabaseAndNoJobsConfiguration used extends
 * AbstractApplicationConfiguration).
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ActiveProfiles("basicauth")
@ContextConfiguration(classes = TestsWithoutDatabaseAndNoJobsConfiguration.class)
public class SpringConfigurationTest {

    /**
     * This tests if "Aal izz well" (i.e. if we can start-up with the [almost]
     * "production" Spring XML configuration) by doing nothing - it doesn't have
     * to, as the SpringJUnit4ClassRunner with this @ContextConfiguration will
     * automatically fail if e.g. there is any invalid Spring XML, an invalid
     * bean definition somewhere or anything like that.
     * 
     * @see https://www.google.ch/search?q=Aal+izz+well
     */
    @Test
    public void testSpringXMLConfiguration() {}
}
