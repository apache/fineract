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
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
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

    /**
     * The order every replay of the breach actions has to follow: the timeline they describe, not the order they
     * happened to be recorded in. A pause carries the date it was asked for rather than the date it was recorded on, so
     * one recorded later can still sit earlier on the timeline. The creation instant orders two actions sharing a start
     * date, and the identifier is the last tie-break for actions that are not persisted yet or were created within the
     * same instant.
     */
    private static final Comparator<WorkingCapitalLoanBreachAction> CHRONOLOGICAL = Comparator
            .comparing(WorkingCapitalLoanBreachAction::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(action -> action.getCreatedDate().orElse(null), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(WorkingCapitalLoanBreachAction::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;

    /**
     * Active resets, latest on top: an undo cancels the reset above it and the top of the stack is the one that settled
     * the schedule last. Resets and undos carry the business date, so {@link #CHRONOLOGICAL} is their recording order;
     * replaying them through it keeps every replay on the one ordering rule.
     */
    public Deque<WorkingCapitalLoanBreachAction> activeResets(final List<WorkingCapitalLoanBreachAction> actions) {
        final Deque<WorkingCapitalLoanBreachAction> stack = new ArrayDeque<>();
        if (actions == null) {
            return stack;
        }
        for (final WorkingCapitalLoanBreachAction action : chronological(actions)) {
            if (WorkingCapitalLoanBreachActionType.RESET.equals(action.getAction())) {
                stack.push(action);
            } else if (WorkingCapitalLoanBreachActionType.UNDO_RESET.equals(action.getAction()) && !stack.isEmpty()) {
                stack.pop();
            }
        }
        return stack;
    }

    /**
     * The recorded actions on their own timeline, earliest first, with the nulls a caller may hold dropped. Every
     * replay of the actions goes through this order, not only the reset and undo pair this resolver owns.
     */
    public static List<WorkingCapitalLoanBreachAction> chronological(final List<WorkingCapitalLoanBreachAction> actions) {
        if (actions == null) {
            return List.of();
        }
        return actions.stream().filter(Objects::nonNull).sorted(CHRONOLOGICAL).toList();
    }

    public Deque<WorkingCapitalLoanBreachAction> activeResets(final Long workingCapitalLoanId) {
        return activeResets(breachActionRepository.findByWorkingCapitalLoanIdOrderById(workingCapitalLoanId));
    }

    public Optional<WorkingCapitalLoanBreachAction> findLatestActiveReset(final Long workingCapitalLoanId) {
        return Optional.ofNullable(activeResets(workingCapitalLoanId).peek());
    }

    public boolean hasActiveReset(final Long workingCapitalLoanId) {
        return findLatestActiveReset(workingCapitalLoanId).isPresent();
    }

    public boolean existsActiveResetInPeriod(final Long workingCapitalLoanId, final LocalDate fromDate, final LocalDate toDate) {
        return activeResets(workingCapitalLoanId).stream()
                .anyMatch(reset -> DateUtils.isDateInRangeInclusive(reset.getStartDate(), fromDate, toDate));
    }
}
