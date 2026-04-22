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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCommandTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkingCapitalLoanTransactionReadPlatformService {

    WorkingCapitalLoanCommandTemplateData retrieveLoanTransactionTemplate(Long loanId, String command);

    /**
     * Retrieves paginated transactions of a Working Capital Loan by loan id.
     */
    Page<WorkingCapitalLoanTransactionData> retrieveTransactions(Long loanId, Pageable pageable);

    /**
     * Retrieves paginated transactions of a Working Capital Loan by loan external id.
     */
    Page<WorkingCapitalLoanTransactionData> retrieveTransactions(ExternalId loanExternalId, Pageable pageable);

    /**
     * Retrieves a single Working Capital Loan transaction by loan id and transaction id.
     */
    WorkingCapitalLoanTransactionData retrieveTransaction(Long loanId, Long transactionId);

    /**
     * Retrieves a single Working Capital Loan transaction by loan external id and transaction id.
     */
    WorkingCapitalLoanTransactionData retrieveTransaction(ExternalId loanExternalId, Long transactionId);

    /**
     * Retrieves a single Working Capital Loan transaction by loan id and transaction external id.
     */
    WorkingCapitalLoanTransactionData retrieveTransaction(Long loanId, ExternalId transactionExternalId);

    /**
     * Retrieves a single Working Capital Loan transaction by loan external id and transaction external id.
     */
    WorkingCapitalLoanTransactionData retrieveTransaction(ExternalId loanExternalId, ExternalId transactionExternalId);
}
