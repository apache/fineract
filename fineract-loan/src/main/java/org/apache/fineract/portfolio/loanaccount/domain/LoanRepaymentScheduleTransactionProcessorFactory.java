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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class LoanRepaymentScheduleTransactionProcessorFactory {

    private final LoanRepaymentScheduleTransactionProcessor defaultLoanRepaymentScheduleTransactionProcessor;

    private final List<LoanRepaymentScheduleTransactionProcessor> processors;

    @Value("${fineract.loan.transactionprocessor.error-not-found-fail}")
    private Boolean errorNotFoundFail;

    public LoanRepaymentScheduleTransactionProcessor determineProcessor(final String transactionProcessingStrategy) {

        Optional<LoanRepaymentScheduleTransactionProcessor> processor = processors.stream()
                .filter(p -> p.accept(transactionProcessingStrategy)).findFirst();

        if (processor.isEmpty() && Boolean.TRUE.equals(errorNotFoundFail)) {
            throw new LoanTransactionProcessingStrategyNotFoundException(transactionProcessingStrategy);
        } else {
            return processor.orElse(defaultLoanRepaymentScheduleTransactionProcessor);
        }
    }

    public List<TransactionProcessingStrategyData> getStrategies() {
        return processors.stream().map(p -> new TransactionProcessingStrategyData(null, p.getCode(), p.getName())).toList();
    }
}
