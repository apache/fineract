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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.commands.provider.CommandHandlerProvider;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SynchronousCommandProcessingServiceTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer;
    @Mock
    private ToApiJsonSerializer<CommandProcessingResult> toApiResultJsonSerializer;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private CommandHandlerProvider commandHandlerProvider;
    @Mock
    private IdempotencyKeyResolver idempotencyKeyResolver;
    @Mock
    private IdempotencyKeyGenerator idempotencyKeyGenerator;
    @Mock
    private CommandSourceService commandSourceService;

    @InjectMocks
    private SynchronousCommandProcessingService underTest;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void testExecuteCommandSuccess() {

        CommandWrapper commandWrapper = Mockito.mock(CommandWrapper.class);
        when(commandWrapper.isDatatableResource()).thenReturn(false);
        when(commandWrapper.isNoteResource()).thenReturn(false);
        when(commandWrapper.isSurveyResource()).thenReturn(false);
        when(commandWrapper.isLoanDisburseDetailResource()).thenReturn(false);
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);

        NewCommandSourceHandler newCommandSourceHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        when(newCommandSourceHandler.processCommand(jsonCommand)).thenReturn(commandProcessingResult);
        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(newCommandSourceHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSourceService.findCommandSource(commandWrapper, idk)).thenReturn(null).thenReturn(commandSource);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        CommandProcessingResult actualCommandProcessingResult = underTest.executeCommand(commandWrapper, jsonCommand, false);

        verify(commandSourceService).saveInitial(commandWrapper, jsonCommand, appUser, idk);
        verify(commandSource).setStatus(CommandProcessingResultType.PROCESSED.getValue());
        verify(commandSourceService).saveResult(commandSource);

        Assertions.assertEquals(commandProcessingResult, actualCommandProcessingResult);
    }

    @Test
    public void testExecuteCommandFails() {
        CommandWrapper commandWrapper = Mockito.mock(CommandWrapper.class);
        when(commandWrapper.isDatatableResource()).thenReturn(false);
        when(commandWrapper.isNoteResource()).thenReturn(false);
        when(commandWrapper.isSurveyResource()).thenReturn(false);
        when(commandWrapper.isLoanDisburseDetailResource()).thenReturn(false);
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);

        NewCommandSourceHandler newCommandSourceHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        RuntimeException runtimeException = new RuntimeException("foo");
        when(newCommandSourceHandler.processCommand(jsonCommand)).thenThrow(runtimeException);
        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(newCommandSourceHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        when(commandSourceService.findCommandSource(commandWrapper, idk)).thenReturn(null);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);
        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);

        CommandSource initialCommandSource = Mockito.mock(CommandSource.class);

        when(commandSourceService.findCommandSource(commandWrapper, idk)).thenReturn(initialCommandSource);

        Assertions.assertThrows(RuntimeException.class, () -> {
            underTest.executeCommand(commandWrapper, jsonCommand, false);
        });

        verify(commandSourceService).saveInitial(commandWrapper, jsonCommand, appUser, idk);
        verify(commandSourceService).generateErrorException(runtimeException);
    }
}
