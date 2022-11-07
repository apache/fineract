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
package org.apache.fineract.portfolio.delinquency.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanDelinquencyTagHistoryRepository
        extends JpaRepository<LoanDelinquencyTagHistory, Long>, JpaSpecificationExecutor<LoanDelinquencyTagHistory> {

    LoanDelinquencyTagHistory findFirstByLoanOrderByAddedOnDateDesc(Loan loan);

    List<LoanDelinquencyTagHistory> findByLoanOrderByAddedOnDateDesc(Loan loan);

    Optional<LoanDelinquencyTagHistory> findByLoanAndLiftedOnDate(Loan loan, LocalDate liftedOnDate);

    Long countByDelinquencyRangeAndLiftedOnDate(DelinquencyRange delinquencyRange, LocalDate liftedOnDate);

    Long countByDelinquencyRange(DelinquencyRange delinquencyRange);

    List<LoanDelinquencyTagHistory> findByLoan(Loan loan);

}
