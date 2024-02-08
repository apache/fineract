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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.HttpMethod;
import java.util.stream.Stream;
import org.apache.fineract.batch.command.internal.ActivateClientCommandStrategy;
import org.apache.fineract.batch.command.internal.AdjustChargeByChargeExternalIdCommandStrategy;
import org.apache.fineract.batch.command.internal.AdjustChargeCommandStrategy;
import org.apache.fineract.batch.command.internal.AdjustLoanTransactionByExternalIdCommandStrategy;
import org.apache.fineract.batch.command.internal.AdjustLoanTransactionCommandStrategy;
import org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy;
import org.apache.fineract.batch.command.internal.ApplySavingsCommandStrategy;
import org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy;
import org.apache.fineract.batch.command.internal.ApproveLoanRescheduleCommandStrategy;
import org.apache.fineract.batch.command.internal.CollectChargesByLoanExternalIdCommandStrategy;
import org.apache.fineract.batch.command.internal.CollectChargesCommandStrategy;
import org.apache.fineract.batch.command.internal.CreateChargeByLoanExternalIdCommandStrategy;
import org.apache.fineract.batch.command.internal.CreateChargeCommandStrategy;
import org.apache.fineract.batch.command.internal.CreateClientCommandStrategy;
import org.apache.fineract.batch.command.internal.CreateDatatableEntryCommandStrategy;
import org.apache.fineract.batch.command.internal.CreateLoanRescheduleRequestCommandStrategy;
import org.apache.fineract.batch.command.internal.CreateTransactionByLoanExternalIdCommandStrategy;
import org.apache.fineract.batch.command.internal.CreateTransactionLoanCommandStrategy;
import org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy;
import org.apache.fineract.batch.command.internal.GetChargeByChargeExternalIdCommandStrategy;
import org.apache.fineract.batch.command.internal.GetChargeByIdCommandStrategy;
import org.apache.fineract.batch.command.internal.GetDatatableEntryByAppTableIdAndDataTableIdCommandStrategy;
import org.apache.fineract.batch.command.internal.GetDatatableEntryByAppTableIdCommandStrategy;
import org.apache.fineract.batch.command.internal.GetDatatableEntryByQueryCommandStrategy;
import org.apache.fineract.batch.command.internal.GetLoanByExternalIdCommandStrategy;
import org.apache.fineract.batch.command.internal.GetLoanByIdCommandStrategy;
import org.apache.fineract.batch.command.internal.GetLoanTransactionByExternalIdCommandStrategy;
import org.apache.fineract.batch.command.internal.GetLoanTransactionByIdCommandStrategy;
import org.apache.fineract.batch.command.internal.LoanStateTransistionsByExternalIdCommandStrategy;
import org.apache.fineract.batch.command.internal.ModifyLoanApplicationCommandStrategy;
import org.apache.fineract.batch.command.internal.UnknownCommandStrategy;
import org.apache.fineract.batch.command.internal.UpdateClientCommandStrategy;
import org.apache.fineract.batch.command.internal.UpdateDatatableEntryOneToManyCommandStrategy;
import org.apache.fineract.batch.command.internal.UpdateDatatableEntryOneToOneCommandStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationContext;

/**
 * Tests {@link CommandStrategyProvider}.
 */
public class CommandStrategyProviderTest {

    /**
     * Command strategy provider.
     *
     * @return the test data stream
     */
    private static Stream<Arguments> provideCommandStrategies() {
        return Stream.of(Arguments.of("clients", HttpMethod.POST, "createClientCommandStrategy", mock(CreateClientCommandStrategy.class)),
                Arguments.of("clients/123", HttpMethod.PUT, "updateClientCommandStrategy", mock(UpdateClientCommandStrategy.class)),
                Arguments.of("loans", HttpMethod.POST, "applyLoanCommandStrategy", mock(ApplyLoanCommandStrategy.class)),
                Arguments.of("loans/123", HttpMethod.GET, "getLoanByIdCommandStrategy", mock(GetLoanByIdCommandStrategy.class)),
                Arguments.of("loans/123?associations=all", HttpMethod.GET, "getLoanByIdCommandStrategy",
                        mock(GetLoanByIdCommandStrategy.class)),
                Arguments.of("loans/123?associations=all&exclude=guarantors", HttpMethod.GET, "getLoanByIdCommandStrategy",
                        mock(GetLoanByIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1", HttpMethod.GET, "getLoanByExternalIdCommandStrategy",
                        mock(GetLoanByExternalIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1?associations=all", HttpMethod.GET,
                        "getLoanByExternalIdCommandStrategy", mock(GetLoanByExternalIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1?associations=all&exclude=guarantors", HttpMethod.GET,
                        "getLoanByExternalIdCommandStrategy", mock(GetLoanByExternalIdCommandStrategy.class)),
                Arguments.of("savingsaccounts", HttpMethod.POST, "applySavingsCommandStrategy", mock(ApplySavingsCommandStrategy.class)),
                Arguments.of("loans/123/charges", HttpMethod.POST, "createChargeCommandStrategy", mock(CreateChargeCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/charges", HttpMethod.POST,
                        "createChargeByLoanExternalIdCommandStrategy", mock(CreateChargeByLoanExternalIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/charges?command=mycommand", HttpMethod.POST,
                        "createChargeByLoanExternalIdCommandStrategy", mock(CreateChargeByLoanExternalIdCommandStrategy.class)),
                Arguments.of("loans/123/charges", HttpMethod.GET, "collectChargesCommandStrategy",
                        mock(CollectChargesCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/charges", HttpMethod.GET,
                        "collectChargesByLoanExternalIdCommandStrategy", mock(CollectChargesByLoanExternalIdCommandStrategy.class)),
                Arguments.of("loans/123/charges/123", HttpMethod.GET, "getChargeByIdCommandStrategy",
                        mock(GetChargeByIdCommandStrategy.class)),
                Arguments.of(
                        "loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/charges/external-id/7dfad438-2319-48ce-8520-10a62801e9ab",
                        HttpMethod.GET, "getChargeByChargeExternalIdCommandStrategy",
                        mock(GetChargeByChargeExternalIdCommandStrategy.class)),
                Arguments.of(
                        "loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/charges/external-id/7dfad438-2319-48ce-8520-10a62801e9ab?fields=id",
                        HttpMethod.GET, "getChargeByChargeExternalIdCommandStrategy",
                        mock(GetChargeByChargeExternalIdCommandStrategy.class)),
                Arguments.of("loans/123/charges/123?command=adjustment", HttpMethod.POST, "adjustChargeCommandStrategy",
                        mock(AdjustChargeCommandStrategy.class)),
                Arguments.of(
                        "loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/charges/external-id/7dfad438-2319-48ce-8520-10a62801e9ab?command=adjustment",
                        HttpMethod.POST, "adjustChargeByChargeExternalIdCommandStrategy",
                        mock(AdjustChargeByChargeExternalIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/transactions?command=repayment", HttpMethod.POST,
                        "createTransactionByLoanExternalIdCommandStrategy", mock(CreateTransactionByLoanExternalIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/transactions?command=creditBalanceRefund",
                        HttpMethod.POST, "createTransactionByLoanExternalIdCommandStrategy",
                        mock(CreateTransactionByLoanExternalIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/transactions?command=goodwillCredit", HttpMethod.POST,
                        "createTransactionByLoanExternalIdCommandStrategy", mock(CreateTransactionByLoanExternalIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/transactions?command=merchantIssuedRefund",
                        HttpMethod.POST, "createTransactionByLoanExternalIdCommandStrategy",
                        mock(CreateTransactionByLoanExternalIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/transactions?command=payoutRefund", HttpMethod.POST,
                        "createTransactionByLoanExternalIdCommandStrategy", mock(CreateTransactionByLoanExternalIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/transactions?command=chargeRefund", HttpMethod.POST,
                        "createTransactionByLoanExternalIdCommandStrategy", mock(CreateTransactionByLoanExternalIdCommandStrategy.class)),
                Arguments.of("loans/123/transactions?command=repayment", HttpMethod.POST, "createTransactionLoanCommandStrategy",
                        mock(CreateTransactionLoanCommandStrategy.class)),
                Arguments.of("loans/123/transactions?command=creditBalanceRefund", HttpMethod.POST, "createTransactionLoanCommandStrategy",
                        mock(CreateTransactionLoanCommandStrategy.class)),
                Arguments.of("loans/123/transactions?command=goodwillCredit", HttpMethod.POST, "createTransactionLoanCommandStrategy",
                        mock(CreateTransactionLoanCommandStrategy.class)),
                Arguments.of("loans/123/transactions?command=merchantIssuedRefund", HttpMethod.POST, "createTransactionLoanCommandStrategy",
                        mock(CreateTransactionLoanCommandStrategy.class)),
                Arguments.of("loans/123/transactions?command=payoutRefund", HttpMethod.POST, "createTransactionLoanCommandStrategy",
                        mock(CreateTransactionLoanCommandStrategy.class)),
                Arguments.of("loans/123/transactions?command=chargeRefund", HttpMethod.POST, "createTransactionLoanCommandStrategy",
                        mock(CreateTransactionLoanCommandStrategy.class)),
                Arguments.of("loans/123/transactions/123", HttpMethod.POST, "adjustLoanTransactionCommandStrategy",
                        mock(AdjustLoanTransactionCommandStrategy.class)),
                Arguments.of("loans/123/transactions/123?command=chargeback", HttpMethod.POST, "adjustLoanTransactionCommandStrategy",
                        mock(AdjustLoanTransactionCommandStrategy.class)),
                Arguments.of(
                        "loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/transactions/external-id/7dfad438-2319-48ce-8520-10a62801e9ab",
                        HttpMethod.POST, "adjustLoanTransactionByExternalIdCommandStrategy",
                        mock(AdjustLoanTransactionByExternalIdCommandStrategy.class)),
                Arguments.of(
                        "loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/transactions/external-id/7dfad438-2319-48ce-8520-10a62801e9ab?command=chargeback",
                        HttpMethod.POST, "adjustLoanTransactionByExternalIdCommandStrategy",
                        mock(AdjustLoanTransactionByExternalIdCommandStrategy.class)),
                Arguments.of("clients/456?command=activate", HttpMethod.POST, "activateClientCommandStrategy",
                        mock(ActivateClientCommandStrategy.class)),
                Arguments.of("loans/123?command=approve", HttpMethod.POST, "approveLoanCommandStrategy",
                        mock(ApproveLoanCommandStrategy.class)),
                Arguments.of("loans/123?command=disburse", HttpMethod.POST, "disburseLoanCommandStrategy",
                        mock(DisburseLoanCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1?command=approve", HttpMethod.POST,
                        "loanStateTransistionsByExternalIdCommandStrategy", mock(LoanStateTransistionsByExternalIdCommandStrategy.class)),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1?command=disburse", HttpMethod.POST,
                        "loanStateTransistionsByExternalIdCommandStrategy", mock(LoanStateTransistionsByExternalIdCommandStrategy.class)),
                Arguments.of("rescheduleloans", HttpMethod.POST, "createLoanRescheduleRequestCommandStrategy",
                        mock(CreateLoanRescheduleRequestCommandStrategy.class)),
                Arguments.of("rescheduleloans/123?command=approve", HttpMethod.POST, "approveLoanRescheduleCommandStrategy",
                        mock(ApproveLoanRescheduleCommandStrategy.class)),
                Arguments.of("loans/123/transactions/123", HttpMethod.GET, "getLoanTransactionByIdCommandStrategy",
                        mock(GetLoanTransactionByIdCommandStrategy.class)),
                Arguments.of(
                        "loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/transactions/external-id/7dfad438-2319-48ce-8520-10a62801e9ab?fields=id",
                        HttpMethod.GET, "getLoanTransactionByExternalIdCommandStrategy",
                        mock(GetLoanTransactionByExternalIdCommandStrategy.class)),
                Arguments.of("datatables/test_dt_table/123", HttpMethod.GET, "getDatatableEntryByAppTableIdCommandStrategy",
                        mock(GetDatatableEntryByAppTableIdCommandStrategy.class)),
                Arguments.of("datatables/test_dt_table/123?genericResultSet=true", HttpMethod.GET,
                        "getDatatableEntryByAppTableIdCommandStrategy", mock(GetDatatableEntryByAppTableIdCommandStrategy.class)),
                Arguments.of("datatables/test_dt_table/123/1?genericResultSet=true", HttpMethod.GET,
                        "getDatatableEntryByAppTableIdAndDataTableIdCommandStrategy",
                        mock(GetDatatableEntryByAppTableIdAndDataTableIdCommandStrategy.class)),
                Arguments.of("datatables/test_dt_table/123", HttpMethod.POST, "createDatatableEntryCommandStrategy",
                        mock(CreateDatatableEntryCommandStrategy.class)),
                Arguments.of("datatables/test_dt_table/123/1", HttpMethod.PUT, "updateDatatableEntryOneToManyCommandStrategy",
                        mock(UpdateDatatableEntryOneToManyCommandStrategy.class)),
                Arguments.of("datatables/test_dt_table/123", HttpMethod.PUT, "updateDatatableEntryOneToOneCommandStrategy",
                        mock(UpdateDatatableEntryOneToOneCommandStrategy.class)),
                Arguments.of("loans/123?command=markAsFraud", HttpMethod.PUT, "modifyLoanApplicationCommandStrategy",
                        mock(ModifyLoanApplicationCommandStrategy.class)),
                Arguments.of("loans/123", HttpMethod.PUT, "modifyLoanApplicationCommandStrategy",
                        mock(ModifyLoanApplicationCommandStrategy.class)),
                Arguments.of("datatables/test_dt_table/query?columnFilter=id&valueFilter=12&resultColumns=id", HttpMethod.GET,
                        "getDatatableEntryByQueryCommandStrategy", mock(GetDatatableEntryByQueryCommandStrategy.class)),
                Arguments.of("datatables/test_dt_table/query?columnFilter=custom_id&valueFilter=10a62-d438-2319&resultColumns=id",
                        HttpMethod.GET, "getDatatableEntryByQueryCommandStrategy", mock(GetDatatableEntryByQueryCommandStrategy.class)));
    }

    /**
     * Tests {@link CommandStrategyProvider#getCommandStrategy} for success scenarios.
     *
     * @param url
     *            the resource URL
     * @param httpMethod
     *            the resource HTTP method
     * @param beanName
     *            the context bean name
     * @param commandStrategy
     *            the command strategy
     */
    @ParameterizedTest
    @MethodSource("provideCommandStrategies")
    public void testGetCommandStrategySuccess_OldUrls(final String url, final String httpMethod, final String beanName,
            final CommandStrategy commandStrategy) {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        final CommandStrategyProvider commandStrategyProvider = new CommandStrategyProvider(applicationContext);
        when(applicationContext.getBean(beanName)).thenReturn(commandStrategy);
        final CommandStrategy result = commandStrategyProvider.getCommandStrategy(CommandContext.resource(url).method(httpMethod).build());
        assertEquals(commandStrategy, result);
    }

    /**
     * Tests {@link CommandStrategyProvider#getCommandStrategy} for success scenarios.
     *
     * @param url
     *            the resource URL
     * @param httpMethod
     *            the resource HTTP method
     * @param beanName
     *            the context bean name
     * @param commandStrategy
     *            the command strategy
     */
    @ParameterizedTest
    @MethodSource("provideCommandStrategies")
    public void testGetCommandStrategySuccess_VersionedUrls(final String url, final String httpMethod, final String beanName,
            final CommandStrategy commandStrategy) {
        String versionedUrl = "v1/" + url;
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        final CommandStrategyProvider commandStrategyProvider = new CommandStrategyProvider(applicationContext);
        when(applicationContext.getBean(beanName)).thenReturn(commandStrategy);
        final CommandStrategy result = commandStrategyProvider
                .getCommandStrategy(CommandContext.resource(versionedUrl).method(httpMethod).build());
        assertEquals(commandStrategy, result);
    }

    /**
     * Command strategy provider for error scenarios.
     *
     * @return the test data stream
     */
    private static Stream<Arguments> provideCommandStrategyResourceDetailsForErrors() {
        return Stream.of(Arguments.of("loans/123?command=reject", HttpMethod.POST),
                Arguments.of("loans/glimAccount/746?command=approve", HttpMethod.POST),
                Arguments.of("datatables/test_dt_table", HttpMethod.GET), Arguments.of("datatables", HttpMethod.GET),
                Arguments.of("loans//charges/123", HttpMethod.GET), Arguments.of("loans/123/charges/", HttpMethod.GET),
                Arguments.of("loans/123/charges/123", HttpMethod.POST),
                Arguments.of(
                        "loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/charges/external-id/7dfad438-2319-48ce-8520-10a62801e9ab",
                        HttpMethod.POST),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1/transactions", HttpMethod.POST),
                Arguments.of("loans/external-id/8dfad438-2319-48ce-8520-10a62801e9a1", HttpMethod.POST),
                Arguments.of("datatables/test_dt_table/query", HttpMethod.GET));

    }

    /**
     * Tests {@link CommandStrategyProvider#getCommandStrategy} for error scenarios.
     *
     * @param url
     *            the resource URL
     * @param httpMethod
     *            the resource HTTP method
     */
    @ParameterizedTest
    @MethodSource("provideCommandStrategyResourceDetailsForErrors")
    public void testGetCommandStrategyForError(final String url, final String httpMethod) {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        final CommandStrategyProvider commandStrategyProvider = new CommandStrategyProvider(applicationContext);

        final CommandStrategy result = commandStrategyProvider.getCommandStrategy(CommandContext.resource(url).method(httpMethod).build());
        assertEquals(UnknownCommandStrategy.class, result.getClass());
    }
}
