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

/**
 * The delinquency classification a Working Capital loan command owes once it has changed the loan status and saved the
 * loan. The delinquency range schedule updates return it instead of classifying themselves, so classification runs
 * once, at the end of the command, on the final loan state.
 */
public enum WorkingCapitalLoanDelinquencyClassificationMode {

    /** Nothing changed that needs classifying. */
    NONE,
    /**
     * Classify when {@code ENABLE_INSTANT_DELINQUENCY_CALCULATION} is on, or when the command leaves the loan closed or
     * overpaid: the COB does not process such a loan, so it would not catch up on the classification.
     */
    INSTANT,
    /** Classify regardless of the instant delinquency calculation setting. */
    ALWAYS;

    /**
     * The stronger of the two modes, for a command that updates the schedule more than once (an undo and its
     * replacement): {@link #ALWAYS} wins over {@link #INSTANT}, which wins over {@link #NONE}.
     */
    public WorkingCapitalLoanDelinquencyClassificationMode combine(final WorkingCapitalLoanDelinquencyClassificationMode other) {
        return other == null || compareTo(other) >= 0 ? this : other;
    }
}
