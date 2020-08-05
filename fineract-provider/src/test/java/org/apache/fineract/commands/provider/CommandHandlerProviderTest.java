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
package org.apache.fineract.commands.provider;

import org.apache.fineract.commands.exception.UnsupportedCommandException;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.configuration.spring.TestsWithoutDatabaseAndNoJobsConfiguration;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ActiveProfiles("basicauth")
@ContextConfiguration(classes = TestsWithoutDatabaseAndNoJobsConfiguration.class)
@TestPropertySource(properties = {
        "spring.redis.host=localhost",
        "spring.redis.port=6379",
        "drizzle.driver-classname=org.drizzle.jdbc.DrizzleDriver",
        "drizzle.protocol=jdbc",
        "drizzle.subprotocol=mysql:thin",
        "drizzle.port=3306"
})
public class CommandHandlerProviderTest {

    @Autowired
    private CommandHandlerProvider commandHandlerProvider;

    public CommandHandlerProviderTest() {
        super();
    }

    @Test
    public void shouldRegisterHandler() {
        try {
            final Long testCommandId = 815L;

            final NewCommandSourceHandler registeredHandler = this.commandHandlerProvider.getHandler("HUMAN", "UPDATE");

            final CommandProcessingResult result =
                    registeredHandler.processCommand(
                            JsonCommand.fromExistingCommand(testCommandId, null, null, null, null, null, null, null, null,null,null));
            Assert.assertEquals(testCommandId, result.commandId());
        } catch (UnsupportedCommandException ucex) {
            Assert.fail();
        }
    }

    @Test(expected = UnsupportedCommandException.class)
    public void shouldThrowUnsupportedCommandException() {
        this.commandHandlerProvider.getHandler("WHATEVER", "DOSOMETHING");
    }
}
