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
package org.apache.fineract.portfolio.workingcapitalloan.exception;

import org.apache.fineract.infrastructure.core.domain.ExternalId;

/**
 * Thrown when a command addressed by transaction external id finds that external id on a reversed transaction.
 * <p>
 * A concurrent adjust that moved the external id to its replacement can cause this, so the command is worth running
 * again: it is listed in {@code fineract.retry.instances.executeCommand.retryExceptions}, and every attempt resolves
 * the external id again. When it is still on a reversed transaction after the last attempt (a full reversal, an undo,
 * or an adjust that did not pass the external id on), the caller gets the same error as for a numeric id.
 */
public class WorkingCapitalLoanTransactionAlreadyUndoneByExternalIdException extends WorkingCapitalLoanTransactionAlreadyUndoneException {

    public WorkingCapitalLoanTransactionAlreadyUndoneByExternalIdException(final Long transactionId, final ExternalId externalId) {
        super("Working Capital Loan transaction with identifier " + transactionId + " and external identifier " + externalId.getValue()
                + " is already undone.", transactionId, externalId.getValue());
    }
}
