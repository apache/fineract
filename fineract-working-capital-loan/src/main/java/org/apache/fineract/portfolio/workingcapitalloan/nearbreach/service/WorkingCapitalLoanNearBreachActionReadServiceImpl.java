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
package org.apache.fineract.portfolio.workingcapitalloan.nearbreach.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.workingcapitalloan.loan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.loan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.nearbreach.data.WorkingCapitalLoanNearBreachActionData;
import org.apache.fineract.portfolio.workingcapitalloan.nearbreach.mapper.WorkingCapitalLoanNearBreachActionMapper;
import org.apache.fineract.portfolio.workingcapitalloan.nearbreach.repository.WorkingCapitalLoanNearBreachActionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingCapitalLoanNearBreachActionReadServiceImpl implements WorkingCapitalLoanNearBreachActionReadService {

    private final WorkingCapitalLoanNearBreachActionRepository actionRepository;
    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanNearBreachActionMapper mapper;

    @Override
    public List<WorkingCapitalLoanNearBreachActionData> retrieveNearBreachActions(final Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            throw new WorkingCapitalLoanNotFoundException(loanId);
        }
        return mapper.toDataList(actionRepository.findByWorkingCapitalLoanIdOrderByIdDesc(loanId));
    }
}
