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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import org.apache.fineract.portfolio.loanaccount.data.LoanAmortizationAllocationData;
import org.apache.fineract.portfolio.loanaccount.domain.AmortizationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAmortizationAllocationMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;

public interface LoanAmortizationAllocationService {

    /**
     * Retrieve amortization allocation data for a specific buy down fee loan transaction
     */
    LoanAmortizationAllocationData retrieveLoanAmortizationAllocationsForBuyDownFeeTransaction(Long loanTransactionId, Long loanId);

    /**
     * Retrieve amortization allocation data for a specific capitalized income loan transaction
     */
    LoanAmortizationAllocationData retrieveLoanAmortizationAllocationsForCapitalizedIncomeTransaction(Long loanTransactionId, Long loanId);

    BigDecimal calculateAlreadyAmortizedAmount(Long loanTransactionId, Long loanId);

    LoanAmortizationAllocationMapping createAmortizationAllocationMappingWithBaseLoanTransaction(LoanTransaction loanTransaction,
            BigDecimal amount, AmortizationType amortizationType);

    void setAmortizationTransactionDataAndSaveAmortizationAllocationMapping(LoanAmortizationAllocationMapping amortizationAllocationMapping,
            LoanTransaction amortizationTransaction);
}
