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
package org.apache.fineract.investor.enricher;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.infrastructure.core.service.DataEnricher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanTransactionAdjustmentDataV1Enricher implements DataEnricher<LoanTransactionAdjustmentDataV1> {

    private final LoanTransactionDataV1Enricher loanTransactionDataV1Enricher;

    @Override
    public boolean isDataTypeSupported(Class<LoanTransactionAdjustmentDataV1> dataType) {
        return dataType.isAssignableFrom(LoanTransactionAdjustmentDataV1.class);
    }

    @Override
    public void enrich(LoanTransactionAdjustmentDataV1 data) {
        if (data.getTransactionToAdjust() != null) {
            loanTransactionDataV1Enricher.enrich(data.getTransactionToAdjust());
        }
        if (data.getNewTransactionDetail() != null) {
            loanTransactionDataV1Enricher.enrich(data.getNewTransactionDetail());
        }
    }
}
