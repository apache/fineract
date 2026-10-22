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
package org.apache.fineract.commands.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CommandWrapperBuilderTest {

    private static final Long ACCOUNT_ID = 42L;

    static Stream<Arguments> savingsAccountCommands() {
        return Stream.of(Arguments.of("updateSavingsAccount", command(b -> b.updateSavingsAccount(ACCOUNT_ID))),
                Arguments.of("deleteSavingsAccount", command(b -> b.deleteSavingsAccount(ACCOUNT_ID))),
                Arguments.of("assignSavingsOfficer", command(b -> b.assignSavingsOfficer(ACCOUNT_ID))),
                Arguments.of("unassignSavingsOfficer", command(b -> b.unassignSavingsOfficer(ACCOUNT_ID))),
                Arguments.of("updateWithHoldTax", command(b -> b.updateWithHoldTax(ACCOUNT_ID))),
                Arguments.of("updateFixedDepositAccount", command(b -> b.updateFixedDepositAccount(ACCOUNT_ID))),
                Arguments.of("deleteFixedDepositAccount", command(b -> b.deleteFixedDepositAccount(ACCOUNT_ID))),
                Arguments.of("updateRecurringDepositAccount", command(b -> b.updateRecurringDepositAccount(ACCOUNT_ID))),
                Arguments.of("deleteRecurringDepositAccount", command(b -> b.deleteRecurringDepositAccount(ACCOUNT_ID))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("savingsAccountCommands")
    void savingsAccountCommandCarriesTheSavingsAccountId(String name, CommandWrapper wrapper) {
        assertEquals(ACCOUNT_ID, wrapper.getEntityId());
        assertEquals(ACCOUNT_ID, wrapper.getSavingsId());
    }

    private static CommandWrapper command(Function<CommandWrapperBuilder, CommandWrapperBuilder> action) {
        return action.apply(new CommandWrapperBuilder()).build();
    }
}
