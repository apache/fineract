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
package org.apache.fineract.test.stepdef.hook;

import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.test.initializer.FineractInitializerFactory;
import org.apache.fineract.test.initializer.InitializerProperties;
import org.apache.fineract.test.support.PropertiesFactory;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

@Slf4j
@SuppressWarnings({ "HideUtilityClassConstructor" })
public class InitializingHook {

    @BeforeAll
    public static void beforeAll() throws Exception {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new ResourcePropertySource("classpath:fineract-test-application.properties"));
        InitializerProperties initializerProperties = PropertiesFactory.get(environment, InitializerProperties.class);
        if (initializerProperties.isEnabled()) {
            log.info("Setting up defaults for Fineract");
            FineractInitializerFactory.get().setupGlobalDefaults();
        } else {
            log.info("Skipping defaults for Fineract");
        }

        FineractInitializerFactory.get().setupDefaultsForSuite();
    }

    @Before
    public static void before() throws Exception {
        FineractInitializerFactory.get().setupDefaultsForScenario();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        FineractInitializerFactory.get().resetDefaultsAfterSuite();
    }
}
