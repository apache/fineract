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

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
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

    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanDelinquencyActionRepository actionRepository;
    private final WorkingCapitalLoanDelinquencyActionParseAndValidator validator;
    private final WorkingCapitalLoanDelinquencyRangeScheduleService rangeScheduleService;

    @Transactional
    @Override
    public CommandProcessingResult createDelinquencyAction(final Long workingCapitalLoanId, final JsonCommand command) {
        final WorkingCapitalLoan workingCapitalLoan = loanRepository.findById(workingCapitalLoanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(workingCapitalLoanId));

        final List<WorkingCapitalLoanDelinquencyAction> existing = actionRepository
                .findByWorkingCapitalLoanIdOrderById(workingCapitalLoanId);

        final WorkingCapitalLoanDelinquencyAction action = validator.validateAndParse(command, workingCapitalLoan, existing);
        action.setWorkingCapitalLoan(workingCapitalLoan);

        final WorkingCapitalLoanDelinquencyAction saved = actionRepository.saveAndFlush(action);
        log.debug("Created WC loan delinquency action {} for loan {}", action.getAction(), workingCapitalLoanId);

        if (DelinquencyAction.PAUSE.equals(action.getAction())) {
            rangeScheduleService.extendPeriodsForPause(workingCapitalLoan, action.getStartDate(), action.getEndDate());
        } else if (DelinquencyAction.RESCHEDULE.equals(action.getAction())) {
            rangeScheduleService.rescheduleMinimumPayment(workingCapitalLoan, action);
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
