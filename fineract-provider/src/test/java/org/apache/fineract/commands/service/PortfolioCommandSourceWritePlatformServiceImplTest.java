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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    }

    @Test
    void makerSubmittingSecondDisbursementForSameLoanIsRejected() {
        givenMaker("DISBURSE_LOAN");
        when(commandSourceRepository.findFirstByActionNameAndEntityNameAndResourceIdAndStatusOrderByMadeOnDateDesc("DISBURSE", "LOAN",
                LOAN_ID, CommandProcessingResultType.AWAITING_APPROVAL.getValue())).thenReturn(Optional.of(new CommandSource()));

        assertThrows(MakerCheckerDuplicatePendingSubmissionException.class, () -> underTest.logCommandSource(disburse()));

        verify(processAndLogCommandService, never()).executeCommand(any(), any(), anyBoolean());
    }

    @Test
    void makerSubmittingFirstDisbursementIsExecuted() {
        givenMaker("DISBURSE_LOAN");
        when(commandSourceRepository.findFirstByActionNameAndEntityNameAndResourceIdAndStatusOrderByMadeOnDateDesc(anyString(), anyString(),
                anyLong(), any())).thenReturn(Optional.empty());

        underTest.logCommandSource(disburse());

        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    void makerCanQueueSeveralRepaymentsOnTheSameLoan() {
        givenMaker("REPAYMENT_LOAN");

        underTest.logCommandSource(repayment());

        verifyNoInteractions(commandSourceRepository);
        verify(processAndLogCommandService).executeCommand(any(), any(), eq(false));
    }

    @Test
    void checkerOnlyUserWithoutPendingSubmissionCannotInitiate() {
        givenCheckerOnly("DISBURSE_LOAN");
        when(commandSourceRepository.findFirstByActionNameAndEntityNameAndResourceIdAndStatusOrderByMadeOnDateDesc(anyString(), anyString(),
                anyLong(), any())).thenReturn(Optional.empty());

        assertThrows(MakerCheckerCheckerOnlyInitiationException.class, () -> underTest.logCommandSource(disburse()));

        verify(processAndLogCommandService, never()).executeCommand(any(), any(), anyBoolean());
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

    private static CommandWrapper disburse() {
        return new CommandWrapperBuilder().disburseLoanApplication(LOAN_ID).withJson("{}").build();
    }

    private static CommandWrapper repayment() {
        return new CommandWrapperBuilder().loanRepaymentTransaction(LOAN_ID).withJson("{}").build();
    }
}
