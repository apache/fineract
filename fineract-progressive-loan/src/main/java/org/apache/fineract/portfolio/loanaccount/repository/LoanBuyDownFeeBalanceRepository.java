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
package org.apache.fineract.portfolio.loanaccount.repository;

import java.util.List;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface LoanBuyDownFeeBalanceRepository
        extends JpaRepository<LoanBuyDownFeeBalance, Long>, JpaSpecificationExecutor<LoanBuyDownFeeBalance> {

    List<LoanBuyDownFeeBalance> findAllByLoanId(Long loanId);

    LoanBuyDownFeeBalance findByLoanIdAndLoanTransactionId(Long loanId, Long transactionId);

    @Query("SELECT lbdfb FROM LoanBuyDownFeeBalance lbdfb, LoanTransaction lt, LoanTransactionRelation ltr WHERE lt.loan.id = lbdfb.loan.id AND ltr.fromTransaction.id =:transactionId AND ltr.toTransaction.id=lt.id AND lbdfb.loanTransaction.id = lt.id")
    LoanBuyDownFeeBalance findBalanceForAdjustment(Long transactionId);
}
