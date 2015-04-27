/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.provider;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.configuration.spring.TestsWithoutDatabaseAndNoJobsConfiguration;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestsWithoutDatabaseAndNoJobsConfiguration.class)
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
                            JsonCommand.fromExistingCommand(testCommandId, null, null, null, null, null, null, null, null));
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
