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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.avro.loan.v1.OriginatorDetailsV1;
import org.apache.fineract.infrastructure.core.service.DataEnricher;
import org.apache.fineract.portfolio.loanorigination.helper.LoanOriginatorDetailsResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public class LoanTransactionAdjustmentDataV1OriginatorEnricher implements DataEnricher<LoanTransactionAdjustmentDataV1> {

    private final LoanOriginatorDetailsResolver loanOriginatorDetailsResolver;

    @Override
    public boolean isDataTypeSupported(final Class<LoanTransactionAdjustmentDataV1> dataType) {
        return dataType.isAssignableFrom(LoanTransactionAdjustmentDataV1.class);
    }

    @Override
    public void enrich(final LoanTransactionAdjustmentDataV1 data) {
        final LoanTransactionDataV1 transactionToAdjust = data.getTransactionToAdjust();
        if (transactionToAdjust == null || transactionToAdjust.getLoanId() == null) {
            return;
        }

        final List<OriginatorDetailsV1> originators = loanOriginatorDetailsResolver
                .resolveOriginatorDetails(transactionToAdjust.getLoanId());
        if (!originators.isEmpty()) {
            transactionToAdjust.setOriginators(originators);
            if (data.getNewTransactionDetail() != null) {
                data.getNewTransactionDetail().setOriginators(originators);
            }
        }
    }
}
