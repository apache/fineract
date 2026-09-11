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
package org.apache.fineract.portfolio.charge.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.producttoaccountmapping.service.AccountingChargeReadService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Charge module implementation of the contract declared by the accounting module. Keeping the implementation here means
 * the accounting module depends only on the interface it owns, and the charge module supplies the details at runtime.
 */
@Service
@RequiredArgsConstructor
public class AccountingChargeReadServiceImpl implements AccountingChargeReadService {

    private final ChargeRepository chargeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ChargeData> findChargesByIds(final Set<Long> chargeIds) {
        if (chargeIds == null || chargeIds.isEmpty()) {
            return List.of();
        }
        return chargeRepository.findAllById(chargeIds).stream().map(Charge::toData).toList();
    }
}
