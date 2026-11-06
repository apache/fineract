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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.CannotCreateTransactionException;

class StandingInstructionSkipPolicyTest {

    private final StandingInstructionSkipPolicy skipPolicy = new StandingInstructionSkipPolicy();

    /** Insufficient funds is the ordinary failure of a mandate: recorded against it, and the run carries on. */
    @Test
    void aBusinessFailureIsSkipped() {
        assertThat(skipPolicy
                .shouldSkip(new InsufficientAccountBalanceException("savingsaccount", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN), 0))
                .isTrue();
        assertThat(skipPolicy.shouldSkip(new PlatformApiDataValidationException(List.of()), 0)).isTrue();
    }

    /** A lock conflict in the one-instruction replay belongs to that instruction, not to the run. */
    @Test
    void aTransientDatabaseFailureIsSkipped() {
        assertThat(skipPolicy.shouldSkip(new CannotAcquireLockException("lock wait timeout"), 0)).isTrue();
    }

    /**
     * A transfer that finds an account changed since its snapshot (SavingsAccountRepositoryWrapper#lockInIdOrder) or
     * loses a version check must be replayed and, failing again, recorded against its instruction rather than fail the
     * step.
     */
    @Test
    void anOptimisticLockConflictIsSkipped() {
        assertThat(skipPolicy.shouldSkip(new ObjectOptimisticLockingFailureException(Object.class, 9L), 0)).isTrue();
    }

    /**
     * A page that cannot be read must fail the step: the reader cannot move past it, so skipping it would run the same
     * query on every chunk for ever.
     */
    @Test
    void aReadFailureIsNotSkipped() {
        assertThat(skipPolicy.shouldSkip(new StandingInstructionReadException(1L, 9L, new IllegalStateException("bad row")), 0)).isFalse();
    }

    /**
     * A failure that is not a business rule still belongs to the instruction that raised it: skipping and recording it
     * keeps one bad mandate from rolling back its siblings and stopping the rest of its partition every day.
     */
    @Test
    void anUnexpectedFailureOfOneInstructionIsSkipped() {
        assertThat(skipPolicy.shouldSkip(new NullPointerException(), 0)).isTrue();
        assertThat(skipPolicy.shouldSkip(new IllegalStateException("bad data"), 0)).isTrue();
        assertThat(skipPolicy.shouldSkip(new DataIntegrityViolationException("duplicate key"), 0)).isTrue();
        assertThat(skipPolicy.shouldSkip(new PlatformDataIntegrityException("error.msg.x", "x"), 0)).isTrue();
    }

    /** The database being unavailable is not the instruction's fault, and its failure could not be recorded anyway. */
    @Test
    void aDatabaseOutageIsNotSkipped() {
        assertThat(skipPolicy.shouldSkip(new DataAccessResourceFailureException("connection refused"), 0)).isFalse();
        assertThat(skipPolicy.shouldSkip(new TransientDataAccessResourceException("pool exhausted"), 0)).isFalse();
        assertThat(skipPolicy.shouldSkip(new RecoverableDataAccessException("connection reset"), 0)).isFalse();
        assertThat(skipPolicy.shouldSkip(new CannotCreateTransactionException("no connection"), 0)).isFalse();
    }

    /** Errors such as running out of memory are not the instruction's fault either. */
    @Test
    void anErrorIsNotSkipped() {
        assertThat(skipPolicy.shouldSkip(new OutOfMemoryError(), 0)).isFalse();
    }
}
