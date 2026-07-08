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

    public Optional<WorkingCapitalLoanBreachAction> findLatestActiveReset(final Long workingCapitalLoanId) {
        List<WorkingCapitalLoanBreachAction> filtereredList = breachActionRepository.findByLoanAndActionType(workingCapitalLoanId,
                List.of(WorkingCapitalLoanBreachActionType.RESET, WorkingCapitalLoanBreachActionType.UNDO_RESET));
        Deque<WorkingCapitalLoanBreachAction> queue = new ArrayDeque<>();
        filtereredList.forEach(action -> {
            if (action.getAction().equals(WorkingCapitalLoanBreachActionType.RESET)) {
                queue.push(action);
            } else if (action.getAction().equals(WorkingCapitalLoanBreachActionType.UNDO_RESET)) {
                if (!queue.isEmpty()) {
                    queue.pop();
                }
            }
        });
        return queue.isEmpty() ? Optional.empty() : Optional.of(queue.peek());
    }

    public boolean hasActiveReset(final Long workingCapitalLoanId) {
        return findLatestActiveReset(workingCapitalLoanId).isPresent();
    }

    public boolean existsActiveResetInPeriod(final Long workingCapitalLoanId, final LocalDate fromDate, final LocalDate toDate) {
        return findLatestActiveReset(workingCapitalLoanId).filter(a -> DateUtils.isDateInRangeInclusive(a.getStartDate(), fromDate, toDate))
                .isPresent();
    }
}
