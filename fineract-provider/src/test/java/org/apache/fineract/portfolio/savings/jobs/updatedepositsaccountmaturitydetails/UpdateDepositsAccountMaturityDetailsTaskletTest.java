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
package org.apache.fineract.portfolio.savings.jobs.updatedepositsaccountmaturitydetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.batch.infrastructure.repeat.RepeatStatus.FINISHED;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import jakarta.persistence.OptimisticLockException;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositAccountWritePlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateDepositsAccountMaturityDetailsTaskletTest {

    private static final Long FAILING_ACCOUNT_ID = 376L;
    private static final Long HEALTHY_ACCOUNT_ID = 377L;

    @Mock
    private DepositAccountReadPlatformService depositAccountReadPlatformService;
    @Mock
    private DepositAccountWritePlatformService depositAccountWritePlatformService;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private StepContribution stepContribution;
    @Mock
    private ChunkContext chunkContext;

    private UpdateDepositsAccountMaturityDetailsTasklet tasklet;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "Test Tenant", "UTC", null));
        TransactionTemplate requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        requiresNewTransactionTemplate.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
        tasklet = new UpdateDepositsAccountMaturityDetailsTasklet(depositAccountReadPlatformService, depositAccountWritePlatformService,
                requiresNewTransactionTemplate);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldMatureEachAccountInItsOwnTransactionSoOneFailureDoesNotRollBackTheOthers() throws Exception {
        TransactionStatus failingTransaction = mock(TransactionStatus.class);
        TransactionStatus healthyTransaction = mock(TransactionStatus.class);
        given(transactionManager.getTransaction(any())).willReturn(failingTransaction, healthyTransaction);
        List<DepositAccountData> dueAccounts = List.of(fixedDeposit(FAILING_ACCOUNT_ID), fixedDeposit(HEALTHY_ACCOUNT_ID));
        given(depositAccountReadPlatformService.retrieveForMaturityUpdate()).willReturn(dueAccounts);
        willThrow(new OptimisticLockException("row was updated by the interest posting job")).given(depositAccountWritePlatformService)
                .updateMaturityDetails(FAILING_ACCOUNT_ID, DepositAccountType.FIXED_DEPOSIT);

        assertThatThrownBy(() -> tasklet.execute(stepContribution, chunkContext)).isInstanceOfSatisfying(JobExecutionException.class, e -> {
            assertThat(e.getCauses()).singleElement().isInstanceOf(OptimisticLockException.class);
            assertThat(e.getMessage()).contains("000000376");
        });

        verify(depositAccountWritePlatformService).updateMaturityDetails(HEALTHY_ACCOUNT_ID, DepositAccountType.FIXED_DEPOSIT);
        ArgumentCaptor<TransactionDefinition> definitions = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(transactionManager, times(2)).getTransaction(definitions.capture());
        assertThat(definitions.getAllValues()).extracting(TransactionDefinition::getPropagationBehavior)
                .containsOnly(PROPAGATION_REQUIRES_NEW);
        verify(transactionManager).rollback(failingTransaction);
        verify(transactionManager).commit(healthyTransaction);
    }

    @Test
    void shouldReportAValidationFailureAndStillMatureTheNextAccount() throws Exception {
        TransactionStatus failingTransaction = mock(TransactionStatus.class);
        TransactionStatus healthyTransaction = mock(TransactionStatus.class);
        given(transactionManager.getTransaction(any())).willReturn(failingTransaction, healthyTransaction);
        List<DepositAccountData> dueAccounts = List.of(fixedDeposit(FAILING_ACCOUNT_ID), fixedDeposit(HEALTHY_ACCOUNT_ID));
        given(depositAccountReadPlatformService.retrieveForMaturityUpdate()).willReturn(dueAccounts);
        PlatformApiDataValidationException validationFailure = new PlatformApiDataValidationException(
                List.of(ApiParameterError.generalError("error.msg.linked.account.closed", "Linked savings account is closed")));
        willThrow(validationFailure).given(depositAccountWritePlatformService).updateMaturityDetails(FAILING_ACCOUNT_ID,
                DepositAccountType.FIXED_DEPOSIT);

        assertThatThrownBy(() -> tasklet.execute(stepContribution, chunkContext)).isInstanceOfSatisfying(JobExecutionException.class, e -> {
            assertThat(e.getCauses()).containsExactly(validationFailure);
            assertThat(e.getMessage()).contains("000000376");
        });

        verify(transactionManager).rollback(failingTransaction);
        verify(transactionManager).commit(healthyTransaction);
    }

    @Test
    void shouldFinishWhenEveryAccountMatures() throws Exception {
        TransactionStatus healthyTransaction = mock(TransactionStatus.class);
        given(transactionManager.getTransaction(any())).willReturn(healthyTransaction);
        List<DepositAccountData> dueAccounts = List.of(fixedDeposit(HEALTHY_ACCOUNT_ID));
        given(depositAccountReadPlatformService.retrieveForMaturityUpdate()).willReturn(dueAccounts);

        assertThat(tasklet.execute(stepContribution, chunkContext)).isEqualTo(FINISHED);

        verify(depositAccountWritePlatformService).updateMaturityDetails(HEALTHY_ACCOUNT_ID, DepositAccountType.FIXED_DEPOSIT);
        verify(transactionManager).commit(healthyTransaction);
    }

    private static DepositAccountData fixedDeposit(Long id) {
        DepositAccountData account = mock(DepositAccountData.class);
        given(account.getId()).willReturn(id);
        given(account.getAccountNo()).willReturn(String.format("%09d", id));
        given(account.getDepositType()).willReturn(new EnumOptionData(DepositAccountType.FIXED_DEPOSIT.getValue().longValue(),
                DepositAccountType.FIXED_DEPOSIT.getCode(), "Fixed Deposit"));
        return account;
    }
}
