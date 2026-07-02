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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanActiveBreachResetResolver {

    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;

    public Optional<WorkingCapitalLoanBreachAction> findLatestActiveReset(final Long workingCapitalLoanId) {
        return findLatestActiveReset(workingCapitalLoanId, null);
    }

    public Optional<LocalDate> findLatestActiveResetDate(final Long workingCapitalLoanId) {
        return findLatestActiveReset(workingCapitalLoanId).map(WorkingCapitalLoanBreachAction::getStartDate);
    }

    public Optional<WorkingCapitalLoanBreachAction> findLatestActiveReset(final Long workingCapitalLoanId,
            final Long maxActionIdExclusive) {
        if (workingCapitalLoanId == null) {
            return Optional.empty();
        }
        final List<WorkingCapitalLoanBreachAction> actions = loadActions(workingCapitalLoanId, maxActionIdExclusive);
        final Optional<Long> latestActiveResetId = findLatestActiveResetId(actions);
        return latestActiveResetId
                .flatMap(activeResetId -> actions.stream().filter(action -> Objects.equals(action.getId(), activeResetId)).findFirst());
    }

    public boolean hasActiveReset(final Long workingCapitalLoanId) {
        return findLatestActiveReset(workingCapitalLoanId).isPresent();
    }

    public boolean existsActiveResetInPeriod(final Long workingCapitalLoanId, final LocalDate fromDate, final LocalDate toDate) {
        if (workingCapitalLoanId == null || fromDate == null || toDate == null) {
            return false;
        }
        final List<WorkingCapitalLoanBreachAction> actions = loadActions(workingCapitalLoanId, null);
        final Set<Long> activeResetIds = findActiveResetIds(actions);
        return actions.stream().filter(action -> WorkingCapitalLoanBreachActionType.RESET.equals(action.getAction()))
                .filter(action -> action.getId() != null && activeResetIds.contains(action.getId()))
                .anyMatch(action -> isDateWithinPeriod(action.getStartDate(), fromDate, toDate));
    }

    private List<WorkingCapitalLoanBreachAction> loadActions(final Long workingCapitalLoanId, final Long maxActionIdExclusive) {
        return breachActionRepository.findByWorkingCapitalLoanIdOrderById(workingCapitalLoanId).stream()
                .filter(action -> action.getId() != null && (maxActionIdExclusive == null || action.getId() < maxActionIdExclusive))
                .toList();
    }

    private Optional<Long> findLatestActiveResetId(final List<WorkingCapitalLoanBreachAction> actions) {
        final Deque<Long> activeResetIds = buildActiveResetStack(actions);
        return activeResetIds.isEmpty() ? Optional.empty() : Optional.of(activeResetIds.peekLast());
    }

    private Set<Long> findActiveResetIds(final List<WorkingCapitalLoanBreachAction> actions) {
        return new HashSet<>(buildActiveResetStack(actions));
    }

    private Deque<Long> buildActiveResetStack(final List<WorkingCapitalLoanBreachAction> actions) {
        final Deque<Long> activeResetIds = new ArrayDeque<>();
        for (final WorkingCapitalLoanBreachAction action : actions) {
            if (action == null || action.getId() == null) {
                continue;
            }
            if (WorkingCapitalLoanBreachActionType.RESET.equals(action.getAction())) {
                activeResetIds.addLast(action.getId());
            } else if (WorkingCapitalLoanBreachActionType.UNDO_RESET.equals(action.getAction()) && !activeResetIds.isEmpty()) {
                activeResetIds.removeLast();
            }
        }
        return activeResetIds;
    }

    private boolean isDateWithinPeriod(final LocalDate date, final LocalDate fromDate, final LocalDate toDate) {
        return date != null && !date.isBefore(fromDate) && !date.isAfter(toDate);
    }

}
