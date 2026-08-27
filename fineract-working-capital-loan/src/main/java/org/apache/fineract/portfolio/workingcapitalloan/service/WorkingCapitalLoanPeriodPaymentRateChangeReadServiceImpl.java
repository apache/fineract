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
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanPeriodPaymentRateChangeData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodPaymentRateChange;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanPeriodPaymentRateChangeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingCapitalLoanPeriodPaymentRateChangeReadServiceImpl implements WorkingCapitalLoanPeriodPaymentRateChangeReadService {

    private final WorkingCapitalLoanPeriodPaymentRateChangeRepository repository;
    private final WorkingCapitalLoanRepository loanRepository;

    @Override
    public List<WorkingCapitalLoanPeriodPaymentRateChangeData> retrieveRateChangeHistory(final Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            throw new WorkingCapitalLoanNotFoundException(loanId);
        }
        return repository.findByWorkingCapitalLoanIdOrderByIdDesc(loanId).stream().map(e -> toData(e, loanId)).toList();
    }

    private WorkingCapitalLoanPeriodPaymentRateChangeData toData(final WorkingCapitalLoanPeriodPaymentRateChange entity,
            final Long loanId) {
        return new WorkingCapitalLoanPeriodPaymentRateChangeData(entity.getId(), loanId, entity.getEffectiveDate(),
                entity.getPreviousRate(), entity.getNewRate(), entity.isReversed(), entity.getReversedOnDate(),
                entity.getCreatedDate().orElse(null), entity.getSubmittedOnDate());
    }
}
