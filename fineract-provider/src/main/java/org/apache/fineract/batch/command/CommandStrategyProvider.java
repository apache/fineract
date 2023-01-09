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

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;

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
     * Regex pattern for specifying any number of query params or not specific any query param
     */
    private static final String OPTIONAL_QUERY_PARAM_REGEX = "(\\?(\\w+(?:\\=[\\w,]+|&)+)+)?";

    /**
     * Regex pattern for specifying query params
     */
    private static final String MANDATORY_QUERY_PARAM_REGEX = "(\\?(\\w+(?:\\=[\\w,]+|&)+)+)";

    /**
     * Regex pattern for specifying any query param that has key = 'command' or not specific anything.
     */
    private static final String OPTIONAL_COMMAND_PARAM_REGEX = "(\\?command=[\\w]+)?";

    /**
     * Regex pattern for specifying a mandatory query param that has key = 'command'.
     */
    private static final String MANDATORY_COMMAND_PARAM_REGEX = "\\?command=[\\w]+";

    /**
     * Regex pattern for specifying a UUID param.
     */
    private static final String UUID_PARAM_REGEX = "[\\w\\d-]+";

    /**
     * Regex pattern for specifying a param that's should be a number.
     */
    private static final String NUMBER_REGEX = "\\d+";

    /**
     * Regex pattern for specifying a param that contains case in-sensitive alphanumeric characters with underscores.
     */
    private static final String ALPHANUMBERIC_WITH_UNDERSCORE_REGEX = "[a-zA-Z0-9_]*";

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
        commandStrategies.put(CommandContext.resource("clients").method(POST).build(), "createClientCommandStrategy");
        commandStrategies.put(CommandContext.resource("clients\\/" + NUMBER_REGEX).method(PUT).build(), "updateClientCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans").method(POST).build(), "applyLoanCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/" + NUMBER_REGEX + OPTIONAL_QUERY_PARAM_REGEX).method(GET).build(),
                "getLoanByIdCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("loans\\/external-id\\/" + UUID_PARAM_REGEX + OPTIONAL_QUERY_PARAM_REGEX).method(GET).build(),
                "getLoanByExternalIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("savingsaccounts").method(POST).build(), "applySavingsCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/" + NUMBER_REGEX + "\\/charges").method(POST).build(),
                "createChargeCommandStrategy");
        commandStrategies
                .put(CommandContext.resource("loans\\/external-id\\/" + UUID_PARAM_REGEX + "\\/charges" + OPTIONAL_COMMAND_PARAM_REGEX + "")
                        .method(POST).build(), "createChargeByLoanExternalIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/" + NUMBER_REGEX + "\\/charges").method(GET).build(),
                "collectChargesCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/external-id\\/" + UUID_PARAM_REGEX + "\\/charges").method(GET).build(),
                "collectChargesByLoanExternalIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/" + NUMBER_REGEX + "\\/charges\\/" + NUMBER_REGEX).method(GET).build(),
                "getChargeByIdCommandStrategy");
        commandStrategies.put(CommandContext.resource(
                "loans\\/external-id\\/" + UUID_PARAM_REGEX + "\\/charges\\/external-id\\/" + UUID_PARAM_REGEX + OPTIONAL_QUERY_PARAM_REGEX)
                .method(GET).build(), "getChargeByChargeExternalIdCommandStrategy");
        commandStrategies.put(CommandContext
                .resource("loans\\/" + NUMBER_REGEX + "\\/charges\\/" + NUMBER_REGEX + MANDATORY_COMMAND_PARAM_REGEX).method(POST).build(),
                "adjustChargeCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/external-id\\/" + UUID_PARAM_REGEX + "\\/charges\\/external-id\\/"
                + UUID_PARAM_REGEX + MANDATORY_COMMAND_PARAM_REGEX).method(POST).build(), "adjustChargeByChargeExternalIdCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("loans\\/" + NUMBER_REGEX + "\\/transactions" + MANDATORY_COMMAND_PARAM_REGEX).method(POST).build(),
                "createTransactionLoanCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("loans\\/external-id\\/" + UUID_PARAM_REGEX + "\\/transactions" + MANDATORY_COMMAND_PARAM_REGEX)
                        .method(POST).build(),
                "createTransactionByLoanExternalIdCommandStrategy");
        commandStrategies
                .put(CommandContext.resource("loans\\/" + NUMBER_REGEX + "\\/transactions\\/" + NUMBER_REGEX + OPTIONAL_COMMAND_PARAM_REGEX)
                        .method(POST).build(), "adjustTransactionCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/external-id\\/" + UUID_PARAM_REGEX + "\\/transactions\\/external-id\\/"
                + UUID_PARAM_REGEX + OPTIONAL_COMMAND_PARAM_REGEX).method(POST).build(), "adjustTransactionByExternalIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("clients\\/" + NUMBER_REGEX + "\\?command=activate").method(POST).build(),
                "activateClientCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/" + NUMBER_REGEX + "\\?command=approve").method(POST).build(),
                "approveLoanCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/" + NUMBER_REGEX + "\\?command=disburse").method(POST).build(),
                "disburseLoanCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("loans\\/external-id\\/" + UUID_PARAM_REGEX + MANDATORY_COMMAND_PARAM_REGEX).method(POST).build(),
                "loanStateTransistionsByExternalIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("rescheduleloans").method(POST).build(),
                "createLoanRescheduleRequestCommandStrategy");
        commandStrategies.put(CommandContext.resource("rescheduleloans\\/" + NUMBER_REGEX + "\\?command=approve").method(POST).build(),
                "approveLoanRescheduleCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/" + NUMBER_REGEX + "\\/transactions\\/" + NUMBER_REGEX).method(GET).build(),
                "getTransactionByIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/external-id\\/" + UUID_PARAM_REGEX + "\\/transactions\\/external-id\\/"
                + UUID_PARAM_REGEX + OPTIONAL_QUERY_PARAM_REGEX).method(GET).build(), "getTransactionByExternalIdCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("datatables\\/" + ALPHANUMBERIC_WITH_UNDERSCORE_REGEX + "\\/" + NUMBER_REGEX).method(POST).build(),
                "createDatatableEntryCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("datatables\\/" + ALPHANUMBERIC_WITH_UNDERSCORE_REGEX + "\\/" + NUMBER_REGEX + "\\/" + NUMBER_REGEX)
                        .method(PUT).build(),
                "updateDatatableEntryOneToManyCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("datatables\\/" + ALPHANUMBERIC_WITH_UNDERSCORE_REGEX + "\\/" + NUMBER_REGEX).method(PUT).build(),
                "updateDatatableEntryOneToOneCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("datatables\\/" + ALPHANUMBERIC_WITH_UNDERSCORE_REGEX + "\\/" + NUMBER_REGEX + "\\/" + NUMBER_REGEX)
                        .method(PUT).build(),
                "updateDatatableEntryOneToManyCommandStrategy");
        commandStrategies.put(CommandContext
                .resource("datatables\\/" + ALPHANUMBERIC_WITH_UNDERSCORE_REGEX + "\\/" + NUMBER_REGEX + OPTIONAL_QUERY_PARAM_REGEX)
                .method(GET).build(), "getDatatableEntryByAppTableIdCommandStrategy");
        commandStrategies.put(CommandContext.resource("loans\\/" + NUMBER_REGEX + OPTIONAL_COMMAND_PARAM_REGEX).method(PUT).build(),
                "modifyLoanApplicationCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("loans\\/external-id\\/" + UUID_PARAM_REGEX + OPTIONAL_COMMAND_PARAM_REGEX).method(PUT).build(),
                "modifyLoanApplicationByExternalIdCommandStrategy");
        commandStrategies.put(
                CommandContext.resource("datatables\\/" + ALPHANUMBERIC_WITH_UNDERSCORE_REGEX + "\\/query" + MANDATORY_QUERY_PARAM_REGEX)
                        .method(GET).build(),
                "getDatatableEntryByQueryCommandStrategy");
    }

}
