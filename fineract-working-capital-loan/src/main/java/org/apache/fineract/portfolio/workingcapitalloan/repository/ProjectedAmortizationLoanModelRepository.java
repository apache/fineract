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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.domain.ProjectedAmortizationLoanModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectedAmortizationLoanModelRepository
        extends JpaSpecificationExecutor<ProjectedAmortizationLoanModel>, JpaRepository<ProjectedAmortizationLoanModel, Long> {

    Optional<ProjectedAmortizationLoanModel> findByLoanId(Long loanId);

    /**
     * Whether the loan's stored model was written by a version of the calculation other than the one now in force.
     *
     * <p>
     * A loan with no model at all is not stale - there is nothing to reinterpret and nothing to rebuild from - so this
     * is deliberately not the negation of "has a model at the current version".
     */
    boolean existsByLoanIdAndJsonModelVersionNot(Long loanId, String jsonModelVersion);

    /**
     * The subset of {@code loanIds} whose stored model predates the calculation now in force, so it has to be rebuilt
     * from the loan's own history before anything reads or writes it.
     *
     * <p>
     * A model written by an older version parses without complaint - fields the current shape no longer knows are
     * simply dropped - so nothing fails; the schedule is quietly missing whatever those fields carried, and the next
     * write persists it that way. Rebuilding from the recorded transactions and rate changes is what restores it. Loans
     * with no model are not returned: there is nothing stale about them.
     */
    @Query("""
            SELECT model.loan.id FROM ProjectedAmortizationLoanModel model
            WHERE model.loan.id IN :loanIds
            AND model.loan.loanStatus IN :allowedLoanStatuses
            AND model.jsonModelVersion <> :modelVersion
            """)
    List<Long> findLoanIdsRequiringModelRecalculation(@Param("loanIds") Collection<Long> loanIds,
            @Param("allowedLoanStatuses") Collection<LoanStatus> allowedLoanStatuses, @Param("modelVersion") String modelVersion);

}
