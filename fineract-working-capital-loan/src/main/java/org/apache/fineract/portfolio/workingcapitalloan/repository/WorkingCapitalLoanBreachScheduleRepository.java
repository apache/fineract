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
import java.util.Optional;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkingCapitalLoanBreachScheduleRepository extends JpaRepository<WorkingCapitalLoanBreachSchedule, Long> {

    List<WorkingCapitalLoanBreachSchedule> findByLoanIdOrderByPeriodNumberAsc(Long loanId);

    List<WorkingCapitalLoanBreachSchedule> findByLoanIdAndBreachIsNullAndToDateLessThanEqualOrderByPeriodNumberAsc(Long loanId,
            LocalDate businessDate);

    boolean existsByLoanId(Long loanId);

    Optional<WorkingCapitalLoanBreachSchedule> findTopByLoanIdOrderByPeriodNumberDesc(Long loanId);

    Optional<WorkingCapitalLoanBreachSchedule> findTopByLoanIdOrderByPeriodNumberAsc(Long loanId);

    Optional<WorkingCapitalLoanBreachSchedule> findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(Long loanId,
            LocalDate transactionDate, LocalDate transactionDate1);

    Optional<WorkingCapitalLoanBreachSchedule> findTopByLoanIdAndBreachTrueOrderByFromDateAsc(Long loanId);

    /**
     * The not yet breached period covering the business date. Periods are built contiguously, so at most one of them
     * can cover a given date.
     */
    @Query("""
            SELECT s FROM WorkingCapitalLoanBreachSchedule s
            WHERE s.loan.id = :loanId
              AND s.breach IS NULL
              AND s.fromDate <= :businessDate
              AND s.toDate >= :businessDate""")
    Optional<WorkingCapitalLoanBreachSchedule> findCurrentOpenPeriod(@Param("loanId") Long loanId,
            @Param("businessDate") LocalDate businessDate);

    @Query("""
            SELECT s FROM WorkingCapitalLoanBreachSchedule s
            WHERE s.loan.id = :loanId
              AND s.fromDate > :businessDate
            ORDER BY s.periodNumber ASC""")
    List<WorkingCapitalLoanBreachSchedule> findFuturePeriodsOrderByPeriodNumberAsc(@Param("loanId") Long loanId,
            @Param("businessDate") LocalDate businessDate);
}
