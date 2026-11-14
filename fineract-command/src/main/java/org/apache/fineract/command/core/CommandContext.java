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
package org.apache.fineract.command.core;

import static org.apache.fineract.command.core.CommandState.UNDER_PROCESSING;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * What the dispatcher and the hooks share while one command is processed.
 *
 * <p>
 * {@code command}, {@code response}, {@code error} and {@code state} are data: the dispatcher fills them in as the
 * command progresses and hooks read them (a hook may also replace the response it returns). {@code skipExecution} and
 * {@code skipHooks} go the other way: they are instructions a hook gives, and the dispatcher and the hook manager act
 * on them.
 *
 * <p>
 * A context belongs to a single dispatch of a single command and is not reused.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public final class CommandContext<REQ, RES> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Command<REQ> command;

    private RES response;

    private Throwable error;

    @Builder.Default
    private CommandState state = UNDER_PROCESSING;

    /**
     * Set by a before hook that has answered the command itself, e.g. by replaying a stored result. The dispatcher then
     * does not run the handler and returns {@code response} as it is. The after hooks are not run either: nothing was
     * executed, so there is nothing to audit or announce. The dispatcher does not touch {@code state} on this path; the
     * hook that skips execution is responsible for whatever it leaves there.
     *
     * <p>
     * Hooks that record an execution (auditing, webhooks) check this flag, because a before hook that skips execution
     * may still throw, and the error hooks then run for a command that was never executed.
     */
    private boolean skipExecution;

    /**
     * Set by a hook to stop the remaining hooks of the current phase (before, after or error) from running. It applies
     * to that phase only: the hook manager clears it when the phase ends, also when a hook throws, so it never carries
     * over into the next phase.
     */
    private boolean skipHooks;
}
