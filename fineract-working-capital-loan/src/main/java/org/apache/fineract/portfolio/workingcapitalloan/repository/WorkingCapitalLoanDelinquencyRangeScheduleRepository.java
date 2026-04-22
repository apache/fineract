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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkingCapitalLoanDelinquencyRangeScheduleRepository
        extends JpaRepository<WorkingCapitalLoanDelinquencyRangeSchedule, Long> {

    List<WorkingCapitalLoanDelinquencyRangeSchedule> findByLoanIdOrderByPeriodNumberAsc(Long loanId);

    @Query("SELECT SUM(s.delinquentAmount) FROM WorkingCapitalLoanDelinquencyRangeSchedule s WHERE s.loan.id = :loanId AND s.delinquentAmount > 0")
    BigDecimal getTotalDelinquentAmount(@Param("loanId") Long loanId);

    Optional<WorkingCapitalLoanDelinquencyRangeSchedule> findTopByLoanIdOrderByPeriodNumberDesc(Long loanId);

    List<WorkingCapitalLoanDelinquencyRangeSchedule> findByLoanIdAndToDateIsBeforeAndMinPaymentCriteriaMet(Long loanId,
            LocalDate toDateBefore, Boolean minPaymentCriteriaMet);

    Optional<WorkingCapitalLoanDelinquencyRangeSchedule> findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(Long loanId,
            LocalDate date, LocalDate date2);

    List<WorkingCapitalLoanDelinquencyRangeSchedule> findByLoanIdAndToDateLessThanEqualAndMinPaymentCriteriaMetIsNull(Long loanId,
            LocalDate businessDate);

}
