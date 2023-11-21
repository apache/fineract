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

import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.delinquency.data.LoanInstallmentDelinquencyTagData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanInstallmentDelinquencyTagRepository
        extends JpaRepository<LoanInstallmentDelinquencyTag, Long>, JpaSpecificationExecutor<LoanInstallmentDelinquencyTag> {

    Optional<LoanInstallmentDelinquencyTag> findByLoanAndInstallment(Loan loan, LoanRepaymentScheduleInstallment installment);

    @Query("select i from LoanInstallmentDelinquencyTag i where i.loan.id = :loanId")
    List<LoanInstallmentDelinquencyTag> findByLoanId(@Param("loanId") Long loanId);

    // Fetching Installment Delinquency range and outstanding amount
    @Query("select i.installment.id, i.delinquencyRange, i.outstandingAmount from LoanInstallmentDelinquencyTag i where i.loan.id = :loanId")
    List<LoanInstallmentDelinquencyTagData> findInstallmentDelinquencyTags(@Param("loanId") Long loanId);

    @Modifying(flushAutomatically = true)
    @Query("delete from LoanInstallmentDelinquencyTag i where i.loan.id = :loanId")
    void deleteAllLoanInstallmentsTags(@Param("loanId") Long loanId);

    @Modifying(flushAutomatically = true)
    @Query("delete from LoanInstallmentDelinquencyTag i where i.id IN :tagIds")
    void deleteAllLoanInstallmentsTagsByIds(@Param("tagIds") List<Long> tagIds);

}
