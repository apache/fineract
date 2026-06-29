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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachDisableParseAndValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanBreachDisableWriteServiceImpl implements WorkingCapitalLoanBreachDisableWriteService {

    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;
    private final WorkingCapitalLoanBreachDisableParseAndValidator validator;
    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;

    @Transactional
    @Override
    public CommandProcessingResult createBreachDisableAction(final Long workingCapitalLoanId, final JsonCommand command) {
        final WorkingCapitalLoan workingCapitalLoan = loanRepository.findById(workingCapitalLoanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(workingCapitalLoanId));

        final WorkingCapitalLoanBreachAction action = validator.validateAndParse(command, workingCapitalLoan);
        action.setWorkingCapitalLoan(workingCapitalLoan);

        final WorkingCapitalLoanBreachAction saved = breachActionRepository.saveAndFlush(action);
        log.debug("Created WC loan breach {} action for loan {}", action.getAction(), workingCapitalLoanId);

        if (WorkingCapitalLoanBreachActionType.ENABLE.equals(action.getAction())) {
            breachScheduleService.reEvaluateAfterEnable(workingCapitalLoan, action.getStartDate());
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
