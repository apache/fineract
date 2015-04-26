/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.provider;

import org.junit.Assert;
import org.junit.Test;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public class CommandHandlerProviderTest {

    public CommandHandlerProviderTest() {
        super();
    }

    @Test
    public void shouldRegisterHandler() {
        final CommandHandlerProvider provider = new CommandHandlerProvider();
        try {
            new ValidCommandHandler(provider);
        } catch (NullPointerException pex) {
            Assert.fail();
        }

        try {
            final Long testCommandId = 815L;

            final NewCommandSourceHandler registeredHandler = provider.getHandler("HUMAN", "UPDATE");

            final CommandProcessingResult result =
                    registeredHandler.processCommand(
                            JsonCommand.fromExistingCommand(testCommandId, null, null, null, null, null, null, null, null));
            Assert.assertEquals(testCommandId, result.commandId());
        } catch (UnsupportedCommandException ucex) {
            Assert.fail();
        }
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotRegisterHandlerAndThrowNullPointerException() {
        final CommandHandlerProvider provider = new CommandHandlerProvider();
        new InvalidCommandHandler(provider);
    }

    @Test(expected = UnsupportedCommandException.class)
    public void shouldThrowUnsupportedCommandException() {
        final CommandHandlerProvider provider = new CommandHandlerProvider();
        provider.getHandler("WHATEVER", "DOSOMETHING");

    }
}
