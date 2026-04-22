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
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * Thrown when a Working Capital Loan transaction is not found.
 */
public class WorkingCapitalLoanTransactionNotFoundException extends AbstractPlatformResourceNotFoundException {

    public WorkingCapitalLoanTransactionNotFoundException(final Long transactionId, final Long loanId) {
        super("error.msg.wc.loan.transaction.not.found", "Working Capital Loan transaction with identifier " + transactionId
                + " does not exist for loan with identifier " + loanId + ".", transactionId, loanId);
    }

    public WorkingCapitalLoanTransactionNotFoundException(final ExternalId transactionExternalId) {
        super("error.msg.wc.loan.transaction.not.found",
                "Working Capital Loan transaction with external identifier " + transactionExternalId.getValue() + " does not exist",
                transactionExternalId.getValue());
    }
}
