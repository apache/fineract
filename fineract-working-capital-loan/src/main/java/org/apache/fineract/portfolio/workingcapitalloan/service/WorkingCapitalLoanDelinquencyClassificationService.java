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

import java.time.LocalDate;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;

public interface WorkingCapitalLoanDelinquencyClassificationService {

    void instantClassifyDelinquency(WorkingCapitalLoan loan, LocalDate businessDate);

    void classifyDelinquency(WorkingCapitalLoan loan, LocalDate businessDate);

    /**
     * The classification step at the end of a Working Capital loan command. It runs after the status transition and
     * after the loan has been saved and flushed, so {@link #classifyDelinquency(WorkingCapitalLoan, LocalDate)} sees
     * the final loan state, and it runs at most once per command. Classifies as of the business date.
     *
     * @param loan
     *            the loan the command changed
     * @param mode
     *            the classification the command's delinquency range schedule updates asked for
     */
    void classifyDelinquencyOnFinalState(WorkingCapitalLoan loan, WorkingCapitalLoanDelinquencyClassificationMode mode);

    boolean applyDelinquencyTagForRange(WorkingCapitalLoan loan, WorkingCapitalLoanDelinquencyRangeSchedule range,
            DelinquencyRange currentRange, LocalDate businessDate);

    boolean isDelinquencyDisabled(WorkingCapitalLoan loan);

    void liftDelinquencyClassification(WorkingCapitalLoan loan, LocalDate businessDate);

}
