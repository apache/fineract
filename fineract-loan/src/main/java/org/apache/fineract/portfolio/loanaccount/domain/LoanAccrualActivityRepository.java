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
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LoanAccrualActivityRepository extends Repository<Loan, Long> {

    @Query("select loan.id from Loan loan left join LoanTransaction lt on lt.loan = loan and lt.typeOf = 32 and lt.reversed = false and lt.dateOf = :currentDate inner join LoanRepaymentScheduleInstallment rs on rs.loan = loan and rs.isDownPayment = false and rs.additional = false and rs.dueDate = :currentDate where loan.loanRepaymentScheduleDetail.enableAccrualActivityPosting = true and loan.loanStatus = 300 and lt.id is null ")
    Set<Long> fetchLoanIdsForAccrualActivityPosting(@Param("currentDate") LocalDate currentDate);

}
