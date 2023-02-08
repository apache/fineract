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

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepaymentScheduleInstallmentRepository
        extends JpaRepository<LoanRepaymentScheduleInstallment, Long>, JpaSpecificationExecutor<LoanRepaymentScheduleInstallment> {

    String loanReminder = " SELECT DISTINCT sch.loan.id  as id , sch.dueDate as dueDate , sch.installmentNumber as installmentNumber , sch.principal as principalAmount , sch.interestCharged as interestAmount ,sch.feeChargesCharged as feeChargesAmount ,sch.penaltyCharges as penaltyChargeAmount ,(COALESCE(sch.principal,0.0) + COALESCE(sch.interestCharged,0.0) + COALESCE(sch.feeChargesCharged,0.0) + COALESCE(sch.penaltyCharges,0.0)) AS totalPaidInAdvance FROM LoanRepaymentScheduleInstallment sch INNER JOIN FETCH sch.loan where sch.obligationsMet = false and sch.obligationsMetOnDate is null and sch.loan.loanStatus = 300 ORDER BY sch.loan.id , sch.dueDate , sch.installmentNumber ASC ";

    @Query("select sch from LoanRepaymentScheduleInstallment sch where sch.loan.id = :loanId and sch.dueDate >= :disbursementDate")
    List<LoanRepaymentScheduleInstallment> findPendingLoanRepaymentScheduleInstallmentForTopUp(@Param("loanId") Long loanId,
            @Param("disbursementDate") LocalDate disbursementDate);

    @Query(loanReminder)
    List<LoanRepaymentScheduleInstallment> findLoanRepaymentReminder();

}
