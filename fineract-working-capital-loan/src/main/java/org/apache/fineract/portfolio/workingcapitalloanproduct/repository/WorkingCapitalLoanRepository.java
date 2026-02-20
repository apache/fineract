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
package org.apache.fineract.portfolio.workingcapitalloanproduct.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface WorkingCapitalLoanRepository extends JpaRepository<WorkingCapitalLoan, Long>, JpaSpecificationExecutor<WorkingCapitalLoan>,
        CrudRepository<WorkingCapitalLoan, Long> {

    @Query("select loan.id from WorkingCapitalLoan loan where loan.id BETWEEN :minAccountId and :maxAccountId and loan.loanStatus in :nonClosedLoanStatuses and :cobBusinessDate = loan.lastClosedBusinessDate")
    List<Long> findAllLoansByLastClosedBusinessDateNotNullAndMinAndMaxLoanIdAndStatuses(Long minAccountId, Long maxAccountId,
            LocalDate cobBusinessDate, Collection<LoanStatus> nonClosedLoanStatuses);

    @Query("select loan.id from WorkingCapitalLoan loan where loan.id BETWEEN :minAccountId and :maxAccountId and loan.loanStatus in :nonClosedLoanStatuses and (:cobBusinessDate = loan.lastClosedBusinessDate or loan.lastClosedBusinessDate is NULL)")
    List<Long> findAllLoansByLastClosedBusinessDateAndMinAndMaxLoanIdAndStatuses(Long minAccountId, Long maxAccountId,
            LocalDate cobBusinessDate, Collection<LoanStatus> nonClosedLoanStatuses);
}
