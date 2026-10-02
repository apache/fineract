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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PortfolioCommandSourceWritePlatformServiceImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private CommandSourceRepository commandSourceRepository;
    @Mock
    private FromJsonHelper fromApiJsonHelper;
    @Mock
    private CommandProcessingService processAndLogCommandService;
    @Mock
    private SchedulerJobRunnerReadService schedulerJobRunnerReadService;
    @Mock
    private ConfigurationDomainService configurationService;
    @Mock
    private AppUser currentUser;

    private PortfolioCommandSourceWritePlatformServiceImpl underTest;
    private CommandWrapper wrapper;

    @BeforeEach
    public void setUp() {
        underTest = new PortfolioCommandSourceWritePlatformServiceImpl(context, commandSourceRepository, fromApiJsonHelper,
                processAndLogCommandService, schedulerJobRunnerReadService, configurationService, Collections.emptyList());

        wrapper = CommandWrapper.wrap("ACTIVATE", "CLIENT", 1L, null);
        when(context.authenticatedUser(any(CommandWrapper.class))).thenReturn(currentUser);
        when(currentUser.getId()).thenReturn(100L);
    }

    private void stubApprovalOf(CommandSource pendingCommand) {
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(commandSourceRepository.findById(pendingCommand.getId())).thenReturn(java.util.Optional.of(pendingCommand));
        when(pendingCommand.isAwaitingApproval()).thenReturn(true);
        when(configurationService.isSameMakerCheckerEnabled()).thenReturn(true);
    }

    private void stubSuccessfulExecution() {
        when(fromApiJsonHelper.parse(any())).thenReturn(mock(JsonElement.class));
        when(processAndLogCommandService.executeCommand(any(), any(), anyBoolean())).thenReturn(CommandProcessingResult.empty());
    }

    @Test
    public void checkerOnlyUserWithPendingSubmissionAutoApprovesIt() {
        when(currentUser.hasSpecificPermissionTo("ACTIVATE_CLIENT")).thenReturn(false);
        when(currentUser.isCheckerSuperUser()).thenReturn(false);
        when(currentUser.hasSpecificPermissionTo("ACTIVATE_CLIENT_CHECKER")).thenReturn(true);
        CommandSource pendingCommand = mock(CommandSource.class);
        when(pendingCommand.getId()).thenReturn(55L);
        when(commandSourceRepository.findPendingByActionAndEntityAndResource("ACTIVATE", "CLIENT", 1L,
                CommandProcessingResultType.AWAITING_APPROVAL.getValue())).thenReturn(List.of(pendingCommand));
        stubApprovalOf(pendingCommand);
        stubSuccessfulExecution();

        underTest.logCommandSource(wrapper);

        verify(processAndLogCommandService).executeCommand(any(), any(), eq(true));
        verify(currentUser, never()).validateHasPermissionTo("ACTIVATE_CLIENT");
    }

    @Test
    public void checkerOnlyUserWithNoPendingSubmissionGetsTheStandardAuthorizationFailure() {
        when(currentUser.hasSpecificPermissionTo("ACTIVATE_CLIENT")).thenReturn(false);
        when(currentUser.isCheckerSuperUser()).thenReturn(false);
        when(currentUser.hasSpecificPermissionTo("ACTIVATE_CLIENT_CHECKER")).thenReturn(true);
        when(commandSourceRepository.findPendingByActionAndEntityAndResource("ACTIVATE", "CLIENT", 1L,
                CommandProcessingResultType.AWAITING_APPROVAL.getValue())).thenReturn(Collections.emptyList());
        doThrow(new NoAuthorizationException("User has no authority to: ACTIVATE_CLIENT")).when(currentUser)
                .validateHasPermissionTo("ACTIVATE_CLIENT");

        assertThrows(NoAuthorizationException.class, () -> underTest.logCommandSource(wrapper));
    }

    @Test
    public void userWithBasePermissionIsUnaffectedByTheCheckerOnlyLookup() {
        when(currentUser.hasSpecificPermissionTo("ACTIVATE_CLIENT")).thenReturn(true);
        stubSuccessfulExecution();

        underTest.logCommandSource(wrapper);

        verify(commandSourceRepository, never()).findPendingByActionAndEntityAndResource(any(), any(), any(), any());
        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }
}
