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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanDelinquencyActionParseAndValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanDelinquencyActionWriteServiceImpl implements WorkingCapitalLoanDelinquencyActionWriteService {

    private static final String RESET_RESOURCE_NAME_FOR_PERMISSIONS = "WC_DELINQUENCY_RESET";
    private static final String UNDO_RESET_RESOURCE_NAME_FOR_PERMISSIONS = "WC_DELINQUENCY_UNDO_RESET";

    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanDelinquencyActionRepository actionRepository;
    private final WorkingCapitalLoanDelinquencyActionParseAndValidator validator;
    private final WorkingCapitalLoanDelinquencyRangeScheduleService rangeScheduleService;
    private final PlatformSecurityContext context;
    private final WorkingCapitalLoanDelinquencyClassificationService classificationService;

    @Transactional
    @Override
    public CommandProcessingResult createDelinquencyAction(final Long workingCapitalLoanId, final JsonCommand command) {
        final WorkingCapitalLoan workingCapitalLoan = loanRepository.findById(workingCapitalLoanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(workingCapitalLoanId));

        final List<WorkingCapitalLoanDelinquencyAction> existing = actionRepository
                .findByWorkingCapitalLoanIdOrderById(workingCapitalLoanId);

        final WorkingCapitalLoanDelinquencyAction action = validator.validateAndParse(command, workingCapitalLoan, existing);

        // A disable/enable is routed through the same create command (like the breach actions) and, as with breach,
        // each is persisted as its own row: an enable adds an ENABLE row and separately closes the open disable window.
        final WorkingCapitalLoanDelinquencyAction saved = actionRepository.saveAndFlush(action);
        log.debug("Created WC loan delinquency action {} for loan {}", action.getAction(), workingCapitalLoanId);

        if (DelinquencyAction.ENABLE.equals(action.getAction())) {
            // Mirror breach action management: close the open disable at the day before the enable date so the loan
            // stops being considered disabled from the enable date on.
            final LocalDate enableDate = action.getStartDate();
            final WorkingCapitalLoanDelinquencyAction activeDisable = validator.findActiveDisable(existing);
            activeDisable.setEndDate(enableDate.minusDays(1));
            // Effect specific to delinquency (breach reprocesses its own breach schedule instead): the disabled window
            // is treated as a pause so it does not count towards delinquency, then classification is recomputed as of
            // the enable date.
            rangeScheduleService.extendPeriodsForPause(workingCapitalLoan, activeDisable.getStartDate(), enableDate);
            rangeScheduleService.evaluateExpiredPeriods(workingCapitalLoan, enableDate);
            classificationService.classifyDelinquency(workingCapitalLoan, enableDate);
        } else if (DelinquencyAction.PAUSE.equals(action.getAction())) {
            rangeScheduleService.extendPeriodsForPause(workingCapitalLoan, action.getStartDate(), action.getEndDate());
        } else if (DelinquencyAction.RESCHEDULE.equals(action.getAction())) {
            rangeScheduleService.rescheduleMinimumPayment(workingCapitalLoan);
            rangeScheduleService.reprocessDelinquencySchedule(workingCapitalLoan);
        } else if (DelinquencyAction.RESUME.equals(action.getAction())) {
            final WorkingCapitalLoanDelinquencyAction activePause = validator.findActivePauseForResume(existing,
                    DateUtils.getBusinessLocalDate());
            rangeScheduleService.resumeActivePause(workingCapitalLoan, activePause, action);
        } else if (DelinquencyAction.RESET.equals(action.getAction())) {
            context.authenticatedUser().validateHasCreatePermission(RESET_RESOURCE_NAME_FOR_PERMISSIONS);
            rangeScheduleService.resetPeriods(workingCapitalLoan, action);
        } else if (DelinquencyAction.UNDO_RESET.equals(action.getAction())) {
            context.authenticatedUser().validateHasCreatePermission(UNDO_RESET_RESOURCE_NAME_FOR_PERMISSIONS);
            List<WorkingCapitalLoanDelinquencyAction> byWorkingCapitalLoanIdOrderById = actionRepository
                    .findByWorkingCapitalLoanIdOrderById(workingCapitalLoanId);
            rangeScheduleService.undoResetPeriods(workingCapitalLoan, action, byWorkingCapitalLoanIdOrderById);
            rangeScheduleService.reprocessDelinquencySchedule(workingCapitalLoan);
        } else if (DelinquencyAction.DISABLE.equals(action.getAction())) {
            classificationService.liftDelinquencyClassification(workingCapitalLoan, action.getStartDate());
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(saved.getId()) //
                .withLoanId(workingCapitalLoanId) //
                .withOfficeId(workingCapitalLoan.getOfficeId()) //
                .withClientId(workingCapitalLoan.getClientId()) //
                .build();
    }

}
