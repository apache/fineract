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
package org.apache.fineract.portfolio.workingcapitalloan.repository;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkingCapitalLoanBreachActionRepository extends JpaRepository<WorkingCapitalLoanBreachAction, Long> {

    List<WorkingCapitalLoanBreachAction> findByWorkingCapitalLoanIdOrderById(Long workingCapitalLoanId);

    List<WorkingCapitalLoanBreachAction> findByWorkingCapitalLoanIdAndActionOrderByIdDesc(Long workingCapitalLoanId,
            WorkingCapitalLoanBreachActionType action);

    @Query("""
            SELECT CASE WHEN COUNT(action) > 0 THEN TRUE ELSE FALSE END FROM WorkingCapitalLoanBreachAction action
            WHERE action.action = org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType.DISABLE
            AND action.workingCapitalLoan.id = :loanId AND action.startDate <= :date AND (action.endDate IS NULL OR action.endDate >= :date)
            """)
    boolean isBreachDisabledAsOf(@Param("loanId") Long loanId, @Param("date") LocalDate date);

    @Query("""
            SELECT ba FROM WorkingCapitalLoanBreachAction ba
            WHERE ba.workingCapitalLoan.id= :workingCapitalLoanId
            AND ba.action IN :actionTypes
            ORDER BY ba.startDate, ba.id
            """)
    List<WorkingCapitalLoanBreachAction> findByLoanAndActionType(@Param("workingCapitalLoanId") Long workingCapitalLoanId,
            @Param("actionTypes") List<WorkingCapitalLoanBreachActionType> actionTypes);
}
