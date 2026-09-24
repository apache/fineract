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
package org.apache.fineract.portfolio.savings.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.core.Response;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.UnsupportedCommandException;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exceptionmapper.UnsupportedCommandExceptionMapper;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.handler.ApplyAnnualFeeSavingsAccountCommandHandler;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingsAnnualFeeCommandTest {

    @Mock
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Mock
    private SavingsAccountWritePlatformService writePlatformService;
    @Mock
    private SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    @Mock
    private DefaultToApiJsonSerializer<SavingsAccountData> toApiJsonSerializer;
    @InjectMocks
    private SavingsAccountsApiResource resource;

    @ParameterizedTest
    @ValueSource(strings = { "accountId", "externalId", "gsim" })
    void rejectsAnnualFeesWithStandardErrorBeforeAnyFinancialWrite(String route) {
        var handler = new ApplyAnnualFeeSavingsAccountCommandHandler(writePlatformService);
        when(commandsSourceWritePlatformService.logCommandSource(any())).thenAnswer(invocation -> {
            CommandWrapper wrapper = invocation.getArgument(0);
            assertThat(wrapper.actionName()).isEqualTo("APPLYANNUALFEE");
            assertThat(wrapper.entityName()).isEqualTo("SAVINGSACCOUNT");
            assertThat(wrapper.getSavingsId()).isEqualTo(42L);
            return handler.processCommand(null);
        });
        if ("externalId".equals(route)) {
            when(savingsAccountReadPlatformService.retrieveAccountIdByExternalId(any())).thenReturn(42L);
        }
        UnsupportedCommandException exception = assertThrows(UnsupportedCommandException.class, () -> {
            switch (route) {
                case "externalId" -> resource.handleCommands("savings-reference", "applyAnnualFees", "{}");
                case "gsim" -> resource.handleGSIMCommands(42L, "applyAnnualFees", "{}");
                default -> resource.handleCommands(42L, "applyAnnualFees", "{}");
            }
        });
        try (Response response = new UnsupportedCommandExceptionMapper().toResponse(exception)) {
            assertThat(response.getStatus()).isEqualTo(400);
            ApiGlobalErrorResponse error = (ApiGlobalErrorResponse) response.getEntity();
            assertThat(error.getUserMessageGlobalisationCode()).isEqualTo("validation.msg.validation.errors.exist");
            assertThat(error.getErrors()).singleElement().satisfies(detail -> {
                assertThat(detail.getUserMessageGlobalisationCode()).isEqualTo("error.msg.command.unsupported");
                assertThat(detail.getDeveloperMessage()).contains("configured scheduler");
            });
        }
        // All charge payments, savings transactions and journal posting are behind this write-service boundary.
        verifyNoInteractions(writePlatformService, toApiJsonSerializer);
    }

    @ParameterizedTest
    @CsvSource({ "activate, ACTIVATE", "calculateInterest, CALCULATEINTEREST", "postInterest, POSTINTEREST", "close, CLOSE" })
    void unrelatedSavingsCommandsStillDispatch(String command, String action) {
        CommandProcessingResult result = new CommandProcessingResultBuilder().withSavingsId(42L).build();
        when(commandsSourceWritePlatformService.logCommandSource(any())).thenAnswer(invocation -> {
            CommandWrapper wrapper = invocation.getArgument(0);
            assertThat(wrapper.actionName()).isEqualTo(action);
            assertThat(wrapper.entityName()).isEqualTo("SAVINGSACCOUNT");
            return result;
        });
        when(toApiJsonSerializer.serialize(result)).thenReturn("serialized-result");
        assertThat(resource.handleCommands(42L, command, "{}")).isEqualTo("serialized-result");
    }

    @Test
    void openApiDocumentsTheRejectionForBothAccountIdentifiers() throws Exception {
        for (Class<?> identifierType : new Class<?>[] { Long.class, String.class }) {
            Operation operation = SavingsAccountsApiResource.class.getMethod("handleCommands", identifierType, String.class, String.class)
                    .getAnnotation(Operation.class);
            assertThat(operation.description()).contains("applyAnnualFees", "HTTP 400", "error.msg.command.unsupported",
                    "configured scheduler");
        }
    }
}
