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

import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * Skips, without a limit, an instruction whose processing or execution failed, so that it is recorded against its
 * mandate and the rest of its partition still runs.
 *
 * <p>
 * Any exception is skipped, not only business failures. A failure that the skip policy rejects stops the step: the
 * siblings in its chunk are rolled back without a history row, the rest of the partition does not run, and because the
 * instruction is still due the next day, one bad mandate stops its partition at the same point every day. The tasklet
 * this replaces recorded any exception against the instruction for the same reason. Skipping is unlimited because a
 * count-based limit would fail the job on a day when many accounts happen to be short, which is the day it most needs
 * to run.
 * </p>
 *
 * <p>
 * Two kinds of failure are not skipped and fail the step:
 * <ul>
 * <li>{@link StandingInstructionReadException}: the reader cannot move past a page it could not read, so skipping it
 * would run the same query on every chunk for ever.</li>
 * <li>The database or its connection being unavailable. That is not a fault of the instruction: every instruction after
 * it would fail the same way, and its failure could not be recorded either. Failing the step shows the run as failed,
 * and the instructions stay due for the next run.</li>
 * </ul>
 * Errors that are not {@link Exception}s, such as {@link OutOfMemoryError}, are not skipped either.
 * </p>
 */
public class StandingInstructionSkipPolicy implements SkipPolicy {

    @Override
    public boolean shouldSkip(final Throwable t, final long skipCount) {
        return t instanceof Exception && !(t instanceof StandingInstructionReadException) && !isInfrastructureFailure(t);
    }

    private static boolean isInfrastructureFailure(final Throwable t) {
        return t instanceof NonTransientDataAccessResourceException || t instanceof TransientDataAccessResourceException
                || t instanceof RecoverableDataAccessException || t instanceof CannotCreateTransactionException;
    }
}
