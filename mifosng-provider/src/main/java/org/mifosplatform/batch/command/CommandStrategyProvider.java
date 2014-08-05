/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.command;

import java.util.concurrent.ConcurrentHashMap;

import org.mifosplatform.batch.command.internal.UnknownCommandStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Provides an appropriate CommandStrategy using the 'method' and 'resourceUrl'.
 * CommandStrategy bean is created using Spring Application Context.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.command.internal.UnknownCommandStrategy
 */
@Component
public class CommandStrategyProvider {

    private final ApplicationContext applicationContext;
    private final ConcurrentHashMap<CommandContext, String> commandStrategies = new ConcurrentHashMap<>();

    /**
     * Constructs a CommandStrategyProvider with argument of ApplicationContext
     * type. It also initialize commandStrategies using init() function by
     * filling it with available CommandStrategies in
     * {@link org.mifosplatform.batch.command.internal}.
     * 
     * @param applicationContext
     */
    @Autowired
    public CommandStrategyProvider(final ApplicationContext applicationContext) {

        // calls init() function of this class.
        init();

        this.applicationContext = applicationContext;
    }

    /**
     * Returns an appropriate commandStrategy after determining it using the
     * CommandContext of the request. If no such Strategy is found then a
     * default strategy is returned back.
     * 
     * @param commandContext
     * @return CommandStrategy
     * @see org.mifosplatform.batch.command.internal.UnknownCommandStrategy
     */
    public CommandStrategy getCommandStrategy(final CommandContext commandContext) {

        if (this.commandStrategies.containsKey(commandContext)) { return (CommandStrategy) this.applicationContext
                .getBean(this.commandStrategies.get(commandContext)); }

        for (ConcurrentHashMap.Entry<CommandContext, String> entry : this.commandStrategies.entrySet()) {
            if (commandContext.matcher(entry.getKey())) { return (CommandStrategy) this.applicationContext.getBean(this.commandStrategies
                    .get(entry.getKey())); }
        }

        return new UnknownCommandStrategy();
    }

    /**
     * Contains various available command strategies in
     * {@link org.mifosplatform.batch.command.internal}. Any new command
     * Strategy will have to be added within this function in order to initiate
     * it within the constructor.
     */
    private void init() {
        this.commandStrategies.put(CommandContext.resource("clients").method("POST").build(), "createClientCommandStrategy");
        this.commandStrategies.put(CommandContext.resource("clients\\/\\d+").method("PUT").build(), "updateClientCommandStrategy");
        this.commandStrategies.put(CommandContext.resource("loans").method("POST").build(), "applyLoanCommandStrategy");
        this.commandStrategies.put(CommandContext.resource("savingsaccounts").method("POST").build(), "applySavingsCommandStrategy");
        this.commandStrategies.put(CommandContext.resource("loans\\/\\d+\\/charges").method("POST").build(), "createChargeCommandStrategy");
        this.commandStrategies
                .put(CommandContext.resource("loans\\/\\d+\\/charges").method("GET").build(), "collectChargesCommandStrategy");
        this.commandStrategies.put(CommandContext.resource("clients\\/\\d+\\?command=activate").method("POST").build(),
                "activateClientCommandStrategy");
        this.commandStrategies.put(CommandContext.resource("loans\\/\\d+\\?command=approve").method("POST").build(),
                "approveLoanCommandStrategy");
        this.commandStrategies.put(CommandContext.resource("loans\\/\\d+\\?command=disburse").method("POST").build(),
                "disburseLoanCommandStrategy");
    }

}