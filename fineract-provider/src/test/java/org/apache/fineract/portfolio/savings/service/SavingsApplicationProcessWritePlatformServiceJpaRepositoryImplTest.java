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
package org.apache.fineract.portfolio.savings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SavingsApplicationProcessWritePlatformServiceJpaRepositoryImplTest {

    @Test
    void submitGSIMApplicationSubmitsAllClientsInOrderAndReturnsLastResult() {
        SavingsApplicationProcessWritePlatformServiceJpaRepositoryImpl service = mock(
                SavingsApplicationProcessWritePlatformServiceJpaRepositoryImpl.class, CALLS_REAL_METHODS);
        CommandProcessingResult firstResult = mock(CommandProcessingResult.class);
        CommandProcessingResult secondResult = mock(CommandProcessingResult.class);
        doReturn(firstResult, secondResult).when(service).submitApplication(any(JsonCommand.class));

        FromJsonHelper fromJsonHelper = new FromJsonHelper();
        String json = """
                {
                  "clientArray": [
                    { "clientId": 101 },
                    { "clientId": 202 }
                  ]
                }
                """;
        JsonCommand command = new JsonCommand(1L, fromJsonHelper.parse(json), fromJsonHelper);

        CommandProcessingResult result = service.submitGSIMApplication(command);

        ArgumentCaptor<JsonCommand> commandCaptor = ArgumentCaptor.forClass(JsonCommand.class);
        verify(service, times(2)).submitApplication(commandCaptor.capture());
        List<JsonCommand> submittedCommands = commandCaptor.getAllValues();
        assertThat(submittedCommands).extracting(JsonCommand::getClientId).containsExactly(101L, 202L);
        assertThat(result).isSameAs(secondResult);
    }
}
