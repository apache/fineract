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
package org.apache.fineract.portfolio.loanaccount.exception;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * A {@link RuntimeException} thrown when loan resources are not found.
 */
public class LoanTransactionNotFoundException extends AbstractPlatformResourceNotFoundException {

    public LoanTransactionNotFoundException(final String msg) {
        super("error.msg.loan.transaction.not.found", msg);
    }

    public LoanTransactionNotFoundException(final Long id) {
        super("error.msg.loan.transaction.id.invalid", "Transaction with identifier " + id + " does not exist", id);
    }

    public LoanTransactionNotFoundException(final Long id, final Long loanId) {
        super("error.msg.loan.transaction.id.invalid",
                "Transaction with identifier " + id + " does not exist for loan with identifier " + loanId + ".", id, loanId);
    }

    public LoanTransactionNotFoundException(Long id, EmptyResultDataAccessException e) {
        super("error.msg.loan.transaction.id.invalid", "Transaction with identifier " + id + " does not exist", id, e);
    }

    public LoanTransactionNotFoundException(ExternalId transactionExternalId) {
        super("error.msg.loan.transaction.external.id.invalid", "Transaction with external identifier "
                + ObjectUtils.defaultIfNull(transactionExternalId, ExternalId.empty()).getValue() + " does not exist",
                transactionExternalId);
    }

    public LoanTransactionNotFoundException(ExternalId transactionExternalId, EmptyResultDataAccessException e) {
        super("error.msg.loan.transaction.external.id.invalid", "Transaction with external identifier "
                + ObjectUtils.defaultIfNull(transactionExternalId, ExternalId.empty()).getValue() + " does not exist",
                transactionExternalId, e);
    }
}
