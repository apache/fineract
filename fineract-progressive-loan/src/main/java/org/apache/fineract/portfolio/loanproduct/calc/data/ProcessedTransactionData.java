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
package org.apache.fineract.portfolio.loanproduct.calc.data;

import java.time.LocalDate;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

@Getter
@Builder
public final class ProcessedTransactionData {

    private final LoanTransactionType transactionType;
    private final LocalDate transactionDate;
    private final LoanReAgeParameterData reAgeParameter;

    public static ProcessedTransactionData of(LoanTransactionType transactionType, LocalDate transactionDate,
            LoanReAgeParameterData reAgeParameter) {
        return ProcessedTransactionData.builder().transactionType(transactionType).transactionDate(transactionDate)
                .reAgeParameter(reAgeParameter).build();
    }

    public static ProcessedTransactionData of(LoanTransactionType transactionType, LocalDate transactionDate) {
        return of(transactionType, transactionDate, null);
    }

    public boolean isReAge() {
        return LoanTransactionType.REAGE.equals(transactionType);
    }

    public Optional<LoanReAgeParameterData> getReAgeParameterOptional() {
        return Optional.ofNullable(reAgeParameter);
    }
}
