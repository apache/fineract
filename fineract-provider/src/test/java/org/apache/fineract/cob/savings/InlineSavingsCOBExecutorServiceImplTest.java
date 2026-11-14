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
package org.apache.fineract.cob.savings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.fineract.cob.data.COBIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.domain.AccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.SavingsAccountLockRepository;
import org.apache.fineract.cob.exceptions.AccountLockCannotBeOverruledException;
import org.apache.fineract.cob.service.InlineLoanCOBExecutionDataParser;
import org.apache.fineract.commands.configuration.RetryConfigurationAssembler;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformRequestBodyItemLimitValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.jobs.exception.JobNotFoundException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Covers what the inline Savings COB adds on top of the shared inline COB executor: the {@code savingsIds} request
 * field, the savings-specific lock owner and query, and the lock-takeover rules it now shares with Loan.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InlineSavingsCOBExecutorServiceImplTest {

    private static final LocalDate COB_DATE = LocalDate.of(2024, 6, 1);
    private static final Long SAVINGS_ID = 1L;
    private static final String JOB_NAME = "INLINE_SAVINGS_COB";

    @InjectMocks
    private InlineSavingsCOBExecutorServiceImpl testObj;
    @Mock
    private SavingsAccountLockRepository savingsAccountLockRepository;
    @Mock
    private InlineLoanCOBExecutionDataParser dataParser;
    @Mock
    private JobOperator jobOperator;
    @Mock
    private JobRegistry jobRegistry;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private TransactionTemplate requiresNewTransactionTemplate;
    @Mock
    private CustomJobParameterRepository customJobParameterRepository;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private RetrieveSavingsIdService retrieveSavingsIdService;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private RetryConfigurationAssembler retryConfigurationAssembler;
    @Mock
    private FineractProperties.FineractQueryProperties queryProperties;
    @Mock
    private FineractProperties.FineractApiProperties apiProperties;
    @Mock
    private FineractProperties.FineractBodyItemSizeLimitProperties bodyItemSizeLimitProperties;
    @Mock
    private AppUser appUser;
    @Mock
    private JsonCommand command;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, COB_DATE.plusDays(1));
        businessDates.put(BusinessDateType.COB_DATE, COB_DATE);
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        RetryConfig retryConfig = RetryConfig.<Throwable>custom().maxAttempts(1).waitDuration(Duration.ZERO).build();
        when(retryConfigurationAssembler.getRetryConfigurationForInlineCob()).thenReturn(Retry.of("inlineSavingsCobTest", retryConfig));
        when(fineractProperties.getQuery()).thenReturn(queryProperties);
        when(queryProperties.getInClauseParameterSizeLimit()).thenReturn(1000);
        when(fineractProperties.getApi()).thenReturn(apiProperties);
        when(apiProperties.getBodyItemSizeLimit()).thenReturn(bodyItemSizeLimitProperties);
        when(bodyItemSizeLimitProperties.getInlineLoanCob()).thenReturn(1000);
        when(context.getAuthenticatedUserIfPresent()).thenReturn(appUser);
        when(appUser.isBypassUser()).thenReturn(false);
        // Run the REQUIRES_NEW callbacks inline so the lock handling is exercised
        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(mock(TransactionStatus.class));
            return null;
        }).when(requiresNewTransactionTemplate).executeWithoutResult(any());

        COBIdAndLastClosedBusinessDate behind = mock(COBIdAndLastClosedBusinessDate.class);
        when(behind.getId()).thenReturn(SAVINGS_ID);
        when(behind.getLastClosedBusinessDate()).thenReturn(COB_DATE.minusDays(1));
        when(retrieveSavingsIdService.retrieveSavingsIdsBehindDateOrNull(eq(COB_DATE), anyList())).thenReturn(List.of(behind));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    // The repository inherits findById/saveAndFlush from both AccountLockRepository and JpaRepository; stub through the
    // generic contract the executor uses
    private AccountLockRepository<SavingsAccountLock> lockRepository() {
        return savingsAccountLockRepository;
    }

    @Test
    void shouldReadSavingsIdsFromTheSavingsIdsRequestField() {
        when(dataParser.parseExecution(command, InlineSavingsCOBExecutorServiceImpl.SAVINGS_IDS_PARAMETER_NAME)).thenReturn(List.of());

        testObj.executeInlineJob(command, JOB_NAME);

        verify(dataParser).parseExecution(command, "savingsIds");
        verify(dataParser, never()).parseExecution(command);
    }

    @Test
    void shouldRejectRequestOverTheInlineItemLimit() {
        when(bodyItemSizeLimitProperties.getInlineLoanCob()).thenReturn(2);
        when(dataParser.parseExecution(command, "savingsIds")).thenReturn(List.of(1L, 2L, 3L));

        assertThatThrownBy(() -> testObj.executeInlineJob(command, JOB_NAME))
                .isInstanceOf(PlatformRequestBodyItemLimitValidationException.class)
                .hasMessageContaining("Size of the savings IDs list cannot be over 2");
        verify(retrieveSavingsIdService, never()).retrieveSavingsIdsBehindDateOrNull(any(), anyList());
    }

    @Test
    void shouldNotOverruleAHardLockHeldByTheBatchSavingsCob() {
        // The batch COB is processing the account: its lock carries no error, so inline COB must not take it over
        SavingsAccountLock batchLock = new SavingsAccountLock(SAVINGS_ID, LockOwner.SAVINGS_COB_CHUNK_PROCESSING, COB_DATE);
        when(lockRepository().findById(SAVINGS_ID)).thenReturn(Optional.of(batchLock));

        assertThatThrownBy(() -> testObj.execute(List.of(SAVINGS_ID), JOB_NAME)).isInstanceOf(AccountLockCannotBeOverruledException.class);

        verify(lockRepository(), never()).saveAndFlush(any());
        assertThat(batchLock.getLockOwner()).isEqualTo(LockOwner.SAVINGS_COB_CHUNK_PROCESSING);
    }

    @Test
    void shouldTakeOverAFailedLockClearItsErrorAndRecordTheNewFailure() {
        SavingsAccountLock failedLock = new SavingsAccountLock(SAVINGS_ID, LockOwner.SAVINGS_COB_CHUNK_PROCESSING, COB_DATE.minusDays(1));
        failedLock.setError("Savings (id: 1) processing is failed", "stacktrace");
        when(lockRepository().findById(SAVINGS_ID)).thenReturn(Optional.of(failedLock));
        when(lockRepository().saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(savingsAccountLockRepository.findAllByLoanIdInAndLockOwner(anyList(), eq(LockOwner.SAVINGS_INLINE_COB_PROCESSING)))
                .thenReturn(List.of(failedLock));
        // Fail after locking: the job is not registered
        when(jobRegistry.getJob(JOB_NAME)).thenReturn(null);

        assertThatThrownBy(() -> testObj.execute(List.of(SAVINGS_ID), JOB_NAME)).isInstanceOf(JobNotFoundException.class);

        assertThat(failedLock.getLockOwner()).isEqualTo(LockOwner.SAVINGS_INLINE_COB_PROCESSING);
        // The previous error was cleared on takeover and replaced by this attempt's failure
        assertThat(failedLock.getError()).isEqualTo("Inline COB execution failed for account (id: 1), job: " + JOB_NAME);
        verify(savingsAccountLockRepository).findAllByLoanIdInAndLockOwner(List.of(SAVINGS_ID), LockOwner.SAVINGS_INLINE_COB_PROCESSING);
    }

    @Test
    void shouldCreateAnInlineLockOnTheExecutingBusinessDateWhenNoneExists() {
        when(lockRepository().findById(SAVINGS_ID)).thenReturn(Optional.empty());
        when(lockRepository().saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRegistry.getJob(JOB_NAME)).thenReturn(null);

        assertThatThrownBy(() -> testObj.execute(List.of(SAVINGS_ID), JOB_NAME)).isInstanceOf(JobNotFoundException.class);

        ArgumentCaptor<SavingsAccountLock> captor = ArgumentCaptor.forClass(SavingsAccountLock.class);
        verify(lockRepository()).saveAndFlush(captor.capture());
        SavingsAccountLock created = captor.getValue();
        assertThat(created.getLoanId()).isEqualTo(SAVINGS_ID);
        assertThat(created.getLockOwner()).isEqualTo(LockOwner.SAVINGS_INLINE_COB_PROCESSING);
        assertThat(created.getLockPlacedOnCobBusinessDate()).isEqualTo(COB_DATE);
    }
}
