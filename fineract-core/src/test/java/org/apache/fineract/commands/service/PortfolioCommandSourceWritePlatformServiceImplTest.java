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
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.MakerCheckerCheckerOnlyInitiationException;
import org.apache.fineract.commands.exception.MakerCheckerDuplicatePendingSubmissionException;
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

    private static final Integer AWAITING_APPROVAL = CommandProcessingResultType.AWAITING_APPROVAL.getValue();

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

        this.wrapper = CommandWrapper.wrap("DISBURSE", "LOAN", 1L, null);
        when(context.authenticatedUser(any(CommandWrapper.class))).thenReturn(currentUser);
        when(currentUser.getId()).thenReturn(100L);
    }

    private void stubSuccessfulExecution() {
        when(fromApiJsonHelper.parse(any())).thenReturn(mock(JsonElement.class));
        when(processAndLogCommandService.executeCommand(any(), any(), anyBoolean())).thenReturn(CommandProcessingResult.empty());
    }

    private void stubMaker(String taskPermission, boolean hasCheckerPermission) {
        when(configurationService.isMakerCheckerEnabledForTask(taskPermission)).thenReturn(true);
        when(currentUser.hasNotPermissionForAnyOf(taskPermission)).thenReturn(false);
        when(currentUser.isCheckerSuperUser()).thenReturn(false);
        when(currentUser.hasSpecificPermissionTo(taskPermission + "_CHECKER")).thenReturn(hasCheckerPermission);
    }

    private void stubPending(String actionName, String entityName, Long resourceId, Long subResourceId, boolean pending) {
        when(commandSourceRepository.existsByActionNameAndEntityNameAndResourceIdAndSubResourceIdAndCommandAsJsonAndStatus(actionName,
                entityName, resourceId, subResourceId, "{}", AWAITING_APPROVAL)).thenReturn(pending);
    }

    private void verifyNoDuplicateLookup() {
        verify(commandSourceRepository, never()).existsByActionNameAndEntityNameAndResourceIdAndSubResourceIdAndCommandAsJsonAndStatus(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    public void makerWithNoPendingSubmissionIsAllowedToSubmit() {
        stubMaker("DISBURSE_LOAN", false);
        stubPending("DISBURSE", "LOAN", 1L, null, false);
        stubSuccessfulExecution();

        underTest.logCommandSource(wrapper);

        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    public void makerWithExistingPendingSubmissionIsBlocked() {
        stubMaker("DISBURSE_LOAN", false);
        stubPending("DISBURSE", "LOAN", 1L, null, true);

        assertThrows(MakerCheckerDuplicatePendingSubmissionException.class, () -> underTest.logCommandSource(wrapper));
        verify(processAndLogCommandService, never()).executeCommand(any(), any(), anyBoolean());
    }

    @Test
    public void makerHoldingTheCheckerPermissionIsStillBlockedBecauseTheirSubmissionIsQueued() {
        stubMaker("DISBURSE_LOAN", true);
        stubPending("DISBURSE", "LOAN", 1L, null, true);

        assertThrows(MakerCheckerDuplicatePendingSubmissionException.class, () -> underTest.logCommandSource(wrapper));
    }

    @Test
    public void checkerSuperUserSkipsTheDuplicateCheck() {
        when(configurationService.isMakerCheckerEnabledForTask("DISBURSE_LOAN")).thenReturn(true);
        when(currentUser.hasNotPermissionForAnyOf("DISBURSE_LOAN")).thenReturn(false);
        when(currentUser.isCheckerSuperUser()).thenReturn(true);
        stubSuccessfulExecution();

        underTest.logCommandSource(wrapper);

        verifyNoDuplicateLookup();
        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    public void pendingEntryIsMatchedOnTheSubResourceToo() {
        CommandWrapper undoTransaction = new CommandWrapperBuilder().undoSavingsAccountTransaction(5L, 7L).build();
        stubMaker("UNDOTRANSACTION_SAVINGSACCOUNT", false);
        stubPending("UNDOTRANSACTION", "SAVINGSACCOUNT", 5L, 7L, false);
        stubSuccessfulExecution();

        underTest.logCommandSource(undoTransaction);

        verify(commandSourceRepository).existsByActionNameAndEntityNameAndResourceIdAndSubResourceIdAndCommandAsJsonAndStatus(
                "UNDOTRANSACTION", "SAVINGSACCOUNT", 5L, 7L, "{}", AWAITING_APPROVAL);
        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    public void createWithoutResourceIdSkipsTheDuplicateCheck() {
        CommandWrapper create = CommandWrapper.wrap("CREATE", "LOAN", null, null);
        stubMaker("CREATE_LOAN", false);
        stubSuccessfulExecution();

        underTest.logCommandSource(create);

        verifyNoDuplicateLookup();
    }

    @Test
    public void checkerOnlyUserCannotInitiateAndGetsAClearRefusalMessage() {
        when(configurationService.isMakerCheckerEnabledForTask("DISBURSE_LOAN")).thenReturn(true);
        when(currentUser.hasNotPermissionForAnyOf("DISBURSE_LOAN")).thenReturn(true);
        when(currentUser.isCheckerSuperUser()).thenReturn(false);
        when(currentUser.hasSpecificPermissionTo("DISBURSE_LOAN_CHECKER")).thenReturn(true);
        stubPending("DISBURSE", "LOAN", 1L, null, false);

        assertThrows(MakerCheckerCheckerOnlyInitiationException.class, () -> underTest.logCommandSource(wrapper));
    }

    @Test
    public void checkerOnlyRefusalDoesNotApplyWhenAnIdenticalSubmissionIsPending() {
        when(configurationService.isMakerCheckerEnabledForTask("DISBURSE_LOAN")).thenReturn(true);
        when(currentUser.hasNotPermissionForAnyOf("DISBURSE_LOAN")).thenReturn(true);
        when(currentUser.isCheckerSuperUser()).thenReturn(false);
        when(currentUser.hasSpecificPermissionTo("DISBURSE_LOAN_CHECKER")).thenReturn(true);
        stubPending("DISBURSE", "LOAN", 1L, null, true);
        doThrow(new NoAuthorizationException("User has no authority to: DISBURSE_LOAN")).when(currentUser)
                .validateHasPermissionTo("DISBURSE_LOAN");

        assertThrows(NoAuthorizationException.class, () -> underTest.logCommandSource(wrapper));
    }

    @Test
    public void nonMakerCheckerTaskIsUnaffectedByTheNewChecks() {
        when(configurationService.isMakerCheckerEnabledForTask("DISBURSE_LOAN")).thenReturn(false);
        when(currentUser.hasNotPermissionForAnyOf("DISBURSE_LOAN")).thenReturn(false);
        stubSuccessfulExecution();

        underTest.logCommandSource(wrapper);

        verifyNoDuplicateLookup();
        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    public void userWithNeitherPermissionGetsTheGenericAuthorizationFailure() {
        when(configurationService.isMakerCheckerEnabledForTask("DISBURSE_LOAN")).thenReturn(true);
        when(currentUser.hasNotPermissionForAnyOf("DISBURSE_LOAN")).thenReturn(true);
        when(currentUser.isCheckerSuperUser()).thenReturn(false);
        when(currentUser.hasSpecificPermissionTo("DISBURSE_LOAN_CHECKER")).thenReturn(false);
        doThrow(new NoAuthorizationException("User has no authority to: DISBURSE_LOAN")).when(currentUser)
                .validateHasPermissionTo("DISBURSE_LOAN");

        assertThrows(NoAuthorizationException.class, () -> underTest.logCommandSource(wrapper));
    }
}
