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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.MakerCheckerCheckerOnlyInitiationException;
import org.apache.fineract.commands.exception.MakerCheckerDuplicatePendingSubmissionException;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PortfolioCommandSourceWritePlatformServiceImplTest {

    private static final Long LOAN_ID = 7L;
    private static final Integer AWAITING_APPROVAL = CommandProcessingResultType.AWAITING_APPROVAL.getValue();
    private static final String WC_ADJUST_BY_DELTA = "ADJUSTBYDELTA";
    private static final String WC_TRANSACTION = "WORKINGCAPITALLOANTRANSACTION";
    private static final String WC_ADJUST_BY_DELTA_PERMISSION = WC_ADJUST_BY_DELTA + "_" + WC_TRANSACTION;
    private static final String WC_TRANSACTION_EXTERNAL_ID = "WC123";
    private static final String WC_DELTA_JSON = "{\"transactionAmount\":-5,\"externalId\":\"WC123\"}";

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
    private AppUser user;

    @InjectMocks
    private PortfolioCommandSourceWritePlatformServiceImpl underTest;

    @BeforeEach
    void setUp() {
        when(context.authenticatedUser(any(CommandWrapper.class))).thenReturn(user);
        when(user.getId()).thenReturn(99L);
        when(commandSourceRepository.findFirstByActionNameAndEntityNameAndResourceIdAndSubResourceIdAndCommandAsJsonAndStatusOrderByIdDesc(
                any(), any(), any(), any(), any(), any())).thenReturn(Optional.empty());
        when(commandSourceRepository.findFirstByActionNameAndEntityNameAndLoanIdAndTransactionIdAndCommandAsJsonAndStatusOrderByIdDesc(
                any(), any(), any(), any(), any(), any())).thenReturn(Optional.empty());
    }

    @Test
    void makerSubmittingSecondDisbursementForSameLoanIsRejected() {
        givenMaker("DISBURSE_LOAN");
        givenPending("DISBURSE", "LOAN", LOAN_ID, null, "{}", new CommandSource());

        assertThrows(MakerCheckerDuplicatePendingSubmissionException.class, () -> underTest.logCommandSource(disburse()));

        verify(processAndLogCommandService, never()).executeCommand(any(), any(), anyBoolean());
    }

    @Test
    void makerSubmittingFirstDisbursementIsExecuted() {
        givenMaker("DISBURSE_LOAN");

        underTest.logCommandSource(disburse());

        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    void makerCanQueueADifferentChangeWhileOneIsPending() {
        givenMaker("DISBURSE_LOAN");
        givenPending("DISBURSE", "LOAN", LOAN_ID, null, "{}", new CommandSource());

        underTest.logCommandSource(disburse("{\"actualDisbursementDate\":\"01 January 2026\"}"));

        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    void makerHoldingTheCheckerPermissionIsStillRejectedBecauseTheirSubmissionIsQueued() {
        when(user.hasNotPermissionForAnyOf("DISBURSE_LOAN")).thenReturn(false);
        when(user.hasNotPermissionForAnyOf("CHECKER_SUPER_USER", "DISBURSE_LOAN_CHECKER")).thenReturn(false);
        when(configurationService.isMakerCheckerEnabledForTask("DISBURSE_LOAN")).thenReturn(true);
        givenPending("DISBURSE", "LOAN", LOAN_ID, null, "{}", new CommandSource());

        assertThrows(MakerCheckerDuplicatePendingSubmissionException.class, () -> underTest.logCommandSource(disburse()));

        verify(processAndLogCommandService, never()).executeCommand(any(), any(), anyBoolean());
    }

    @Test
    void checkerSuperUserSkipsTheDuplicateCheck() {
        when(user.hasNotPermissionForAnyOf("DISBURSE_LOAN")).thenReturn(false);
        when(user.hasNotPermissionForAnyOf("CHECKER_SUPER_USER", "DISBURSE_LOAN_CHECKER")).thenReturn(false);
        when(user.isCheckerSuperUser()).thenReturn(true);
        when(configurationService.isMakerCheckerEnabledForTask("DISBURSE_LOAN")).thenReturn(true);
        givenPending("DISBURSE", "LOAN", LOAN_ID, null, "{}", new CommandSource());

        underTest.logCommandSource(disburse());

        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    void makerCanQueueSeveralRepaymentsOnTheSameLoan() {
        givenMaker("REPAYMENT_LOAN");

        underTest.logCommandSource(repayment());

        verify(commandSourceRepository, never())
                .findFirstByActionNameAndEntityNameAndResourceIdAndSubResourceIdAndCommandAsJsonAndStatusOrderByIdDesc(any(), any(), any(),
                        any(), any(), any());
        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    void checkerOnlyUserWithoutPendingSubmissionCannotInitiate() {
        givenCheckerOnly("DISBURSE_LOAN");

        assertThrows(MakerCheckerCheckerOnlyInitiationException.class, () -> underTest.logCommandSource(disburse()));

        verify(processAndLogCommandService, never()).executeCommand(any(), any(), anyBoolean());
    }

    @Test
    void checkerOnlyUserWithIdenticalPendingSubmissionApprovesIt() {
        givenCheckerOnly("DISBURSE_LOAN");
        CommandSource pendingCommand = mock(CommandSource.class);
        when(pendingCommand.getId()).thenReturn(55L);
        when(pendingCommand.isAwaitingApproval()).thenReturn(true);
        when(pendingCommand.getCommandAsJson()).thenReturn("{}");
        givenPending("DISBURSE", "LOAN", LOAN_ID, null, "{}", pendingCommand);
        when(commandSourceRepository.findById(55L)).thenReturn(Optional.of(pendingCommand));
        when(context.authenticatedUser()).thenReturn(user);
        when(configurationService.isSameMakerCheckerEnabled()).thenReturn(true);

        underTest.logCommandSource(disburse());

        verify(processAndLogCommandService).executeCommand(any(), any(), eq(true));
        verify(user, never()).validateHasPermissionTo("DISBURSE_LOAN");
    }

    @Test
    void checkerOnlyUserDoesNotApproveAPendingSubmissionWithADifferentPayload() {
        givenCheckerOnly("DISBURSE_LOAN");
        givenPending("DISBURSE", "LOAN", LOAN_ID, null, "{}", new CommandSource());

        assertThrows(MakerCheckerCheckerOnlyInitiationException.class,
                () -> underTest.logCommandSource(disburse("{\"actualDisbursementDate\":\"01 January 2026\"}")));

        verify(commandSourceRepository, never()).findById(any());
        verify(processAndLogCommandService, never()).executeCommand(any(), any(), anyBoolean());
    }

    @Test
    void pendingLookupIsMatchedOnTheSubResourceToo() {
        givenCheckerOnly("UNDOTRANSACTION_SAVINGSACCOUNT");

        assertThrows(MakerCheckerCheckerOnlyInitiationException.class,
                () -> underTest.logCommandSource(new CommandWrapperBuilder().undoSavingsAccountTransaction(5L, 8L).build()));

        verify(commandSourceRepository)
                .findFirstByActionNameAndEntityNameAndResourceIdAndSubResourceIdAndCommandAsJsonAndStatusOrderByIdDesc("UNDOTRANSACTION",
                        "SAVINGSACCOUNT", 5L, 8L, "{}", AWAITING_APPROVAL);
    }

    @Test
    void makerSubmittingTheSameAdjustmentByTransactionExternalIdTwiceIsRejected() {
        givenMaker(WC_ADJUST_BY_DELTA_PERMISSION);
        givenPendingByTransaction(WC_ADJUST_BY_DELTA, WC_TRANSACTION, LOAN_ID, WC_TRANSACTION_EXTERNAL_ID, WC_DELTA_JSON,
                new CommandSource());

        assertThrows(MakerCheckerDuplicatePendingSubmissionException.class,
                () -> underTest.logCommandSource(wcAdjustByDelta(WC_TRANSACTION_EXTERNAL_ID)));

        verify(processAndLogCommandService, never()).executeCommand(any(), any(), anyBoolean());
    }

    @Test
    void makerCanQueueTheSameAdjustmentAgainstAnotherTransactionExternalId() {
        givenMaker(WC_ADJUST_BY_DELTA_PERMISSION);
        givenPendingByTransaction(WC_ADJUST_BY_DELTA, WC_TRANSACTION, LOAN_ID, WC_TRANSACTION_EXTERNAL_ID, WC_DELTA_JSON,
                new CommandSource());

        underTest.logCommandSource(wcAdjustByDelta("WC456"));

        verify(commandSourceRepository).findFirstByActionNameAndEntityNameAndLoanIdAndTransactionIdAndCommandAsJsonAndStatusOrderByIdDesc(
                WC_ADJUST_BY_DELTA, WC_TRANSACTION, LOAN_ID, "WC456", WC_DELTA_JSON, AWAITING_APPROVAL);
        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    void checkerOnlyUserApprovesTheIdenticalAdjustmentPendingByTransactionExternalId() {
        givenCheckerOnly(WC_ADJUST_BY_DELTA_PERMISSION);
        CommandSource pendingCommand = mock(CommandSource.class);
        when(pendingCommand.getId()).thenReturn(56L);
        when(pendingCommand.isAwaitingApproval()).thenReturn(true);
        when(pendingCommand.getCommandAsJson()).thenReturn(WC_DELTA_JSON);
        givenPendingByTransaction(WC_ADJUST_BY_DELTA, WC_TRANSACTION, LOAN_ID, WC_TRANSACTION_EXTERNAL_ID, WC_DELTA_JSON, pendingCommand);
        when(commandSourceRepository.findById(56L)).thenReturn(Optional.of(pendingCommand));
        when(context.authenticatedUser()).thenReturn(user);
        when(configurationService.isSameMakerCheckerEnabled()).thenReturn(true);

        underTest.logCommandSource(wcAdjustByDelta(WC_TRANSACTION_EXTERNAL_ID));

        verify(processAndLogCommandService).executeCommand(any(), any(), eq(true));
        verify(user, never()).validateHasPermissionTo(WC_ADJUST_BY_DELTA_PERMISSION);
    }

    @Test
    void taskWithoutMakerCheckerIsNotLookedUp() {
        when(user.hasNotPermissionForAnyOf("DISBURSE_LOAN")).thenReturn(false);
        when(user.hasNotPermissionForAnyOf("CHECKER_SUPER_USER", "DISBURSE_LOAN_CHECKER")).thenReturn(true);
        when(configurationService.isMakerCheckerEnabledForTask("DISBURSE_LOAN")).thenReturn(false);

        underTest.logCommandSource(disburse());

        verifyNoInteractions(commandSourceRepository);
        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    private void givenMaker(final String permission) {
        when(user.hasNotPermissionForAnyOf(permission)).thenReturn(false);
        when(user.hasNotPermissionForAnyOf("CHECKER_SUPER_USER", permission + "_CHECKER")).thenReturn(true);
        when(configurationService.isMakerCheckerEnabledForTask(permission)).thenReturn(true);
    }

    private void givenCheckerOnly(final String permission) {
        when(user.hasNotPermissionForAnyOf(permission)).thenReturn(true);
        when(user.hasNotPermissionForAnyOf("CHECKER_SUPER_USER", permission + "_CHECKER")).thenReturn(false);
        when(configurationService.isMakerCheckerEnabledForTask(permission)).thenReturn(true);
    }

    private void givenPending(final String actionName, final String entityName, final Long resourceId, final Long subResourceId,
            final String json, final CommandSource pendingCommand) {
        when(commandSourceRepository.findFirstByActionNameAndEntityNameAndResourceIdAndSubResourceIdAndCommandAsJsonAndStatusOrderByIdDesc(
                actionName, entityName, resourceId, subResourceId, json, AWAITING_APPROVAL)).thenReturn(Optional.of(pendingCommand));
    }

    private void givenPendingByTransaction(final String actionName, final String entityName, final Long loanId,
            final String transactionId, final String json, final CommandSource pendingCommand) {
        when(commandSourceRepository.findFirstByActionNameAndEntityNameAndLoanIdAndTransactionIdAndCommandAsJsonAndStatusOrderByIdDesc(
                actionName, entityName, loanId, transactionId, json, AWAITING_APPROVAL)).thenReturn(Optional.of(pendingCommand));
    }

    private static CommandWrapper wcAdjustByDelta(final String transactionExternalId) {
        return new CommandWrapperBuilder().adjustByDeltaWorkingCapitalLoanTransaction(LOAN_ID, transactionExternalId)
                .withJson(WC_DELTA_JSON).build();
    }

    private static CommandWrapper disburse() {
        return disburse("{}");
    }

    private static CommandWrapper disburse(final String json) {
        return new CommandWrapperBuilder().disburseLoanApplication(LOAN_ID).withJson(json).build();
    }

    private static CommandWrapper repayment() {
        return new CommandWrapperBuilder().loanRepaymentTransaction(LOAN_ID).withJson("{}").build();
    }
}
