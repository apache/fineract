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

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * Thrown when a command targets a Working Capital Loan transaction that is already reversed: undo, adjust and
 * adjust-by-delta all need a live transaction.
 * <p>
 * It is not retried: looking the same numeric id up again finds the same reversed transaction. A command addressed by
 * transaction external id throws {@link WorkingCapitalLoanTransactionAlreadyUndoneByExternalIdException} instead, which
 * is retried.
 */
public class WorkingCapitalLoanTransactionAlreadyUndoneException extends AbstractPlatformDomainRuleException {

    public static final String ERROR_CODE = "error.msg.wc.loan.transaction.already.undone";

    public WorkingCapitalLoanTransactionAlreadyUndoneException(final Long transactionId) {
        super(ERROR_CODE, "Working Capital Loan transaction with identifier " + transactionId + " is already undone.", transactionId);
    }

    protected WorkingCapitalLoanTransactionAlreadyUndoneException(final String defaultUserMessage, final Object... args) {
        super(ERROR_CODE, defaultUserMessage, args);
    }
}
