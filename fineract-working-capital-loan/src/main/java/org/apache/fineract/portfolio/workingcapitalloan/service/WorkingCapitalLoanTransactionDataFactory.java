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
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargePaidByData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanTransactionMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanTransactionDataFactory {

    private final WorkingCapitalLoanTransactionMapper transactionMapper;
    private final WorkingCapitalLoanChargePaidByReadService chargePaidByReadService;

    public WorkingCapitalLoanTransactionData create(final WorkingCapitalLoanTransaction transaction) {
        return create(List.of(transaction)).getFirst();
    }

    public List<WorkingCapitalLoanTransactionData> create(final List<WorkingCapitalLoanTransaction> transactions) {
        final Map<Long, List<WorkingCapitalLoanChargePaidByData>> chargePaidByByTransactionId = chargePaidByReadService
                .fetchByTransactionIdsGrouped(transactions.stream().map(WorkingCapitalLoanTransaction::getId).toList());
        return transactions.stream().map(transaction -> {
            final WorkingCapitalLoanTransactionData data = transactionMapper.toData(transaction);
            data.setChargePaidByList(chargePaidByByTransactionId.getOrDefault(transaction.getId(), List.of()));
            return data;
        }).toList();
    }
}
