/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This integration test ensure that the "production" Spring XML configuration
 * files (appContext.xml & Co.) are valid. It does not need any database for
 * that.
 */
@RunWith(SpringJUnit4ClassRunner.class)
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
