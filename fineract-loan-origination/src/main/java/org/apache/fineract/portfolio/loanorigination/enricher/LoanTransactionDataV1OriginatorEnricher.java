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
package org.apache.fineract.portfolio.loanorigination.enricher;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.avro.loan.v1.OriginatorDetailsV1;
import org.apache.fineract.infrastructure.core.service.DataEnricher;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMapping;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMappingRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public class LoanTransactionDataV1OriginatorEnricher implements DataEnricher<LoanTransactionDataV1> {

    private final LoanOriginatorMappingRepository loanOriginatorMappingRepository;
    private final LoanOriginatorAvroMapper loanOriginatorAvroMapper;

    @Override
    public boolean isDataTypeSupported(final Class<LoanTransactionDataV1> dataType) {
        return dataType.isAssignableFrom(LoanTransactionDataV1.class);
    }

    @Override
    public void enrich(final LoanTransactionDataV1 data) {
        if (data == null || data.getLoanId() == null) {
            return;
        }

        final List<LoanOriginatorMapping> mappings = loanOriginatorMappingRepository.findByLoanIdWithOriginatorDetails(data.getLoanId());
        if (mappings == null || mappings.isEmpty()) {
            return;
        }

        final List<OriginatorDetailsV1> originators = new ArrayList<>();
        for (LoanOriginatorMapping mapping : mappings) {
            final LoanOriginator originator = mapping.getOriginator();
            if (originator != null) {
                final OriginatorDetailsV1 originatorDetails = loanOriginatorAvroMapper.toAvro(originator);
                if (originatorDetails != null) {
                    originators.add(originatorDetails);
                }
            }
        }

        if (!originators.isEmpty()) {
            data.setOriginators(originators);
        }
    }
}
