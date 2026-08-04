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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargePaidByData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargePaidBy;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanChargePaidByMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargePaidByRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkingCapitalLoanChargePaidByReadService {

    private final WorkingCapitalLoanChargePaidByRepository chargePaidByRepository;
    private final WorkingCapitalLoanChargePaidByMapper chargePaidByMapper;

    public List<WorkingCapitalLoanChargePaidByData> fetchByTransactionId(final Long transactionId) {
        return chargePaidByMapper.map(chargePaidByRepository.findByTransactionIdIn(List.of(transactionId)));
    }

    public Map<Long, List<WorkingCapitalLoanChargePaidByData>> fetchByTransactionIdsGrouped(final List<Long> transactionIds) {
        if (transactionIds.isEmpty()) {
            return Map.of();
        }
        return chargePaidByRepository.findByTransactionIdIn(transactionIds).stream()
                .collect(Collectors.groupingBy((final WorkingCapitalLoanChargePaidBy paidBy) -> paidBy.getWcLoanTransaction().getId(),
                        Collectors.mapping(chargePaidByMapper::map, Collectors.toList())));
    }
}
