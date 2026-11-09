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
package org.apache.fineract.cob.exceptions;

import java.util.Optional;
import lombok.Getter;

/**
 * Names the account whose COB failed, so that the failure can be recorded on that account's lock rather than on every
 * account of its chunk. Raised by {@code AbstractCOBItemWriter} around the failure of one account; the cause is the
 * original failure.
 */
@Getter
public class COBAccountFailedException extends BusinessStepException {

    private final Long accountId;

    public COBAccountFailedException(Long accountId, Throwable cause) {
        super("COB of account (id: " + accountId + ") failed", cause);
        this.accountId = accountId;
    }

    /** The innermost {@code COBAccountFailedException} in the cause chain of {@code failure}, if any. */
    public static Optional<COBAccountFailedException> find(Throwable failure) {
        COBAccountFailedException found = null;
        for (Throwable current = failure; current != null && current != current.getCause(); current = current.getCause()) {
            if (current instanceof COBAccountFailedException accountFailure) {
                found = accountFailure;
            }
        }
        return Optional.ofNullable(found);
    }
}
