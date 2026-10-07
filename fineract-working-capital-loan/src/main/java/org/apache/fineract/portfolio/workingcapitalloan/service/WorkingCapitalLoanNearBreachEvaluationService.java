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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;

public interface WorkingCapitalLoanNearBreachEvaluationService {

    /**
     * Re-derives the near breach of the period covering {@code effectiveDate} and returns whether its value changed.
     */
    boolean evaluateNearBreachOnCob(WorkingCapitalLoan loan, LocalDate effectiveDate);

    /**
     * The parameters the near breach of the loan is judged with: empty when the loan is not active, has no near-breach
     * configuration or its breach evaluation is disabled.
     */
    Optional<NearBreachParameters> resolveParameters(WorkingCapitalLoan loan);

    /**
     * {@link #resolveParameters} for a caller that has already established the breach evaluation of the loan is
     * enabled, so the check is not repeated.
     */
    Optional<NearBreachParameters> resolveParametersWithBreachEvaluationEnabled(WorkingCapitalLoan loan);

    /**
     * Re-derives the near breach of {@code periods} as of {@code effectiveDate}. A period with nothing to judge against
     * keeps its value.
     *
     * @return whether any period changed value
     */
    boolean rederiveNearBreach(List<WorkingCapitalLoanBreachSchedule> periods, NearBreachParameters parameters, LocalDate effectiveDate);

    record NearBreachParameters(BigDecimal threshold, Integer frequency, WorkingCapitalLoanPeriodFrequencyType frequencyType,
            Integer breachGraceDays) {
    }
}
