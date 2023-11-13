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
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanDelinquencyActionRepository
        extends JpaRepository<LoanDelinquencyAction, Long>, JpaSpecificationExecutor<LoanDelinquencyAction> {

    @Query(value = "select da from LoanDelinquencyAction da where "
            + "    ((da.action = org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.PAUSE and da.endDate is not null and :business_date >= da.startDate and :business_date <= da.endDate) or"
            + "    (da.action = org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.RESUME and da.endDate is null and :business_date >= da.startDate)) and"
            + "    da.loan.id = :loan_id  order by da.createdDate desc")
    Page<LoanDelinquencyAction> getEffectiveDelinquencyActionForLoan(@Param("loan_id") Long loan_id,
            @Param("business_date") LocalDate business_date, Pageable page);

    List<LoanDelinquencyAction> findByLoanOrderById(Loan loan);

}
