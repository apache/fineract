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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanActiveBreachResetResolver {

    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;

    /**
     * Active resets, latest on top. Replays in recording order, not date order: action dates are the business date of
     * their own request and are not monotonic.
     */
    public Deque<WorkingCapitalLoanBreachAction> activeResets(final List<WorkingCapitalLoanBreachAction> actions) {
        final Deque<WorkingCapitalLoanBreachAction> stack = new ArrayDeque<>();
        if (actions == null) {
            return stack;
        }
        for (final WorkingCapitalLoanBreachAction action : actions) {
            if (action == null) {
                continue;
            }
            if (WorkingCapitalLoanBreachActionType.RESET.equals(action.getAction())) {
                stack.push(action);
            } else if (WorkingCapitalLoanBreachActionType.UNDO_RESET.equals(action.getAction()) && !stack.isEmpty()) {
                stack.pop();
            }
        }
        return stack;
    }

    public Optional<WorkingCapitalLoanBreachAction> findLatestActiveReset(final Long workingCapitalLoanId) {
        return Optional.ofNullable(activeResets(breachActionRepository.findByWorkingCapitalLoanIdOrderById(workingCapitalLoanId)).peek());
    }

    public boolean hasActiveReset(final Long workingCapitalLoanId) {
        return findLatestActiveReset(workingCapitalLoanId).isPresent();
    }

    public boolean existsActiveResetInPeriod(final Long workingCapitalLoanId, final LocalDate fromDate, final LocalDate toDate) {
        return activeResets(breachActionRepository.findByWorkingCapitalLoanIdOrderById(workingCapitalLoanId)).stream()
                .anyMatch(reset -> DateUtils.isDateInRangeInclusive(reset.getStartDate(), fromDate, toDate));
    }
}
