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

import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.RecoverableCommandErrorClassifier;
import org.springframework.stereotype.Component;

/**
 * Gives the new command processing the same notion of a recoverable failure as the legacy one, so that a retry under
 * the same idempotency key is executed again in exactly the cases where it is today: an exception marked
 * {@code RecoverableError}, or one whose error codes are all listed in the {@code idempotency-recoverable-error-codes}
 * global configuration.
 */
@Component
@RequiredArgsConstructor
public class RecoverableCommandErrorClassifierAdapter implements RecoverableCommandErrorClassifier {

    private final RecoverableErrorClassifier classifier;

    @Override
    public boolean isRecoverable(Throwable error) {
        return error instanceof RuntimeException runtimeException && classifier.isRecoverable(runtimeException);
    }
}
