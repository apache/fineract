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
package org.apache.fineract.infrastructure.core.boot.tests;

import org.apache.fineract.ServerWithMariaDB4jApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ServerWithMariaDB4jApplication.Configuration.class)
@WebAppConfiguration
@ActiveProfiles("basicauth")
@IntegrationTest({ "server.port=0", "management.port=0", "mariaDB4j.port=0", "mariaDB4j.dataDir=null" })
public abstract class AbstractSpringBootWithMariaDB4jIntegrationTest {

	// do NOT put any helper methods here!
	// it's much better to use composition instead of inheritance
	// so write a test util ("fixture") and use it as a field in your test

}