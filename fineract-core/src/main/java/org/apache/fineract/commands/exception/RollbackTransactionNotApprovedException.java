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
package org.apache.fineract.commands.exception;

import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;

/**
 * When maker-checker is configured globally and also for the current transaction. An initial save determines if there
 * are any integrity rule or data problems. If there isn't... and the transaction is from a maker... then this roll back
 * is issued and the commandSourceResult is used to write the audit entry.
 */
public class RollbackTransactionNotApprovedException extends RuntimeException {

    private final CommandProcessingResult result;

    public RollbackTransactionNotApprovedException(Long commandId, Long entityId) {
        this.result = new CommandProcessingResultBuilder().withCommandId(commandId).withEntityId(entityId).setRollbackTransaction(true)
                .build();
    }

    public CommandProcessingResult getResult() {
        return result;
    }
}
