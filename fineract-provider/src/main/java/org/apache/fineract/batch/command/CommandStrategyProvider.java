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
package org.apache.fineract.batch.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.fineract.batch.command.internal.UnknownCommandStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Provides an appropriate CommandStrategy using the 'method' and 'resourceUrl'. CommandStrategy bean is created using
 * Spring Application Context.
 *
 * @author Rishabh Shukla
 *
 * @see org.apache.fineract.batch.command.internal.UnknownCommandStrategy
 */
@Component
public class CommandStrategyProvider {

    private final ApplicationContext applicationContext;
    private static final Map<CommandContext, String> commandStrategies = new ConcurrentHashMap<>();

    /**
     * Constructs a CommandStrategyProvider with argument of ApplicationContext type. It also initializes
     * commandStrategies using init() function by filling it with available CommandStrategies in
     * {@link org.apache.fineract.batch.command.internal}.
     *
     * @param applicationContext
     */
    public CommandStrategyProvider(final ApplicationContext applicationContext) {

        // calls init() function of this class.
        init();

        this.applicationContext = applicationContext;
    }

    /**
     * Returns an appropriate commandStrategy after determining it using the CommandContext of the request. If no such
     * Strategy is found then a default strategy is returned.
     *
     * @param commandContext
     * @return CommandStrategy
     * @see org.apache.fineract.batch.command.internal.UnknownCommandStrategy
     */
    public CommandStrategy getCommandStrategy(final CommandContext commandContext) {

        if (commandStrategies.containsKey(commandContext)) {
            return (CommandStrategy) this.applicationContext.getBean(commandStrategies.get(commandContext));
        }

        for (Map.Entry<CommandContext, String> entry : commandStrategies.entrySet()) {
            if (commandContext.matcher(entry.getKey())) {
                return (CommandStrategy) applicationContext.getBean(commandStrategies.get(entry.getKey()));
            }
        }

        return new UnknownCommandStrategy();
    }

    /**
     * Contains various available command strategies in {@link org.apache.fineract.batch.command.internal}. Any new
     * command Strategy will have to be added within this function in order to initiate it within the constructor.
     */
    private static void init() {
        commandStrategies.put(CommandContext.resource("clients").method("POST").build(), "createClientCommandStrategy");
        commandStrategies.put(CommandContext.resource("clients\\/\\d+").method("PUT").build(), "updateClientCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans").method("POST").build(), "applyLoanCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/\\d+").method("GET").build(), "getLoanByIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans/\\d+(\\?(\\w+(?:\\=[\\w,]+|&)+)+)").method("GET").build(),
                "getLoanByIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("savingsaccounts").method("POST").build(), "applySavingsCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/\\d+\\/charges").method("POST").build(), "createChargeCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/\\d+\\/charges").method("GET").build(), "collectChargesCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/\\d+\\/charges\\/\\d+").method("GET").build(),
                "getChargeByIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/\\d+\\/transactions\\?(\\w+(\\=[\\w]+))").method("POST").build(),
                "createTransactionLoanCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/\\d+\\/transactions\\/\\d+(\\?command=[\\w]+)?").method("POST").build(),
                "adjustTransactionCommandStrategy");
        commandStrategies.put(CommandContext.resource("clients\\/\\d+\\?command=activate").method("POST").build(),
                "activateClientCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/\\d+\\?command=approve").method("POST").build(),
                "approveLoanCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/\\d+\\?command=disburse").method("POST").build(),
                "disburseLoanCommandStrategy");
        commandStrategies.put(CommandContext.resource("rescheduleloans").method("POST").build(),
                "createLoanRescheduleRequestCommandStrategy");
        commandStrategies.put(CommandContext.resource("rescheduleloans\\/\\d+\\?command=approve").method("POST").build(),
                "approveLoanRescheduleCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/\\d+\\/transactions\\/\\d+").method("GET").build(),
                "getTransactionByIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("datatables\\/[a-zA-Z0-9_]*\\/\\d+").method("GET").build(),
                "getDatatableEntryByAppTableIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("datatables\\/[a-zA-Z0-9_]*\\/\\d+").method("POST").build(),
                "createDatatableEntryCommandStrategy");
        commandStrategies.put(CommandContext.resource("datatables\\/[a-zA-Z0-9_]*\\/\\d+\\/\\d+").method("PUT").build(),
                "updateDatatableEntryOneToManyCommandStrategy");
        commandStrategies.put(CommandContext.resource("datatables\\/[a-zA-Z0-9_]*\\/\\d+").method("PUT").build(),
                "updateDatatableEntryOneToOneCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("datatables\\/[a-zA-Z0-9_]*\\/\\d+(\\?(\\w+(?:\\=[\\w,]+|&)+)+)").method("GET").build(),
                "getDatatableEntryByAppTableIdCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("datatables\\/[a-zA-Z0-9_]*\\/query(\\?(\\w+(?:\\=[\\w,]+|&)+)+)").method("GET").build(),
                "getDatatableEntryByQueryCommandStrategy");
    }

}
