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
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyActionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanDelinquencyActionReadServiceImpl implements WorkingCapitalLoanDelinquencyActionReadService {

    private final WorkingCapitalLoanDelinquencyActionRepository actionRepository;

    @Transactional(readOnly = true)
    @Override
    public List<WorkingCapitalLoanDelinquencyActionData> retrieveDelinquencyActions(final Long workingCapitalLoanId) {
        return actionRepository.findByWorkingCapitalLoanIdOrderById(workingCapitalLoanId).stream().map(this::toData).toList();
    }

    private WorkingCapitalLoanDelinquencyActionData toData(final WorkingCapitalLoanDelinquencyAction action) {
        return new WorkingCapitalLoanDelinquencyActionData(action.getId(), action.getAction(), action.getStartDate(), action.getEndDate());
    }

}
