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
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachActionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanBreachActionReadServiceImpl implements WorkingCapitalLoanBreachActionReadService {

    private final WorkingCapitalLoanBreachActionRepository actionRepository;
    private final WorkingCapitalLoanRepository loanRepository;

    @Transactional(readOnly = true)
    @Override
    public List<WorkingCapitalLoanBreachActionData> retrieveBreachActions(final Long workingCapitalLoanId) {
        if (!loanRepository.existsById(workingCapitalLoanId)) {
            throw new WorkingCapitalLoanNotFoundException(workingCapitalLoanId);
        }
        final List<WorkingCapitalLoanBreachAction> actions = actionRepository.findByWorkingCapitalLoanIdOrderById(workingCapitalLoanId);
        final List<WorkingCapitalLoanBreachAction> resumes = actions.stream()
                .filter(a -> WorkingCapitalLoanBreachActionType.RESUME.equals(a.getAction())).toList();
        return actions.stream().map(action -> toData(action, resumes)).toList();
    }

    private WorkingCapitalLoanBreachActionData toData(final WorkingCapitalLoanBreachAction action,
            final List<WorkingCapitalLoanBreachAction> resumes) {
        LocalDate effectiveEndDate = null;
        if (WorkingCapitalLoanBreachActionType.PAUSE.equals(action.getAction())) {
            effectiveEndDate = resumes.stream()
                    .filter(resume -> !action.getStartDate().isAfter(resume.getStartDate())
                            && !resume.getStartDate().isAfter(action.getEndDate()))
                    .map(WorkingCapitalLoanBreachAction::getStartDate).min(LocalDate::compareTo).orElse(null);
        }
        return new WorkingCapitalLoanBreachActionData(action.getId(), action.getAction(), action.getStartDate(), action.getEndDate(),
                effectiveEndDate, action.getMinimumPayment(), action.getMinimumPaymentType(), action.getFrequency(),
                action.getFrequencyType());
    }

}
