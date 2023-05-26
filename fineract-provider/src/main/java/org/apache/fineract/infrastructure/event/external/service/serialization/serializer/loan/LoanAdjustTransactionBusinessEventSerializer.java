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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.loan;

import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanTransactionDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargePaidByReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanAdjustTransactionBusinessEventSerializer implements BusinessEventSerializer {

    private final LoanReadPlatformService service;
    private final LoanTransactionDataMapper mapper;
    private final LoanChargePaidByReadPlatformService loanChargePaidByReadPlatformService;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof LoanAdjustTransactionBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        LoanAdjustTransactionBusinessEvent event = (LoanAdjustTransactionBusinessEvent) rawEvent;
        LoanTransaction transactionToAdjust = event.get().getTransactionToAdjust();
        LoanTransactionData transactionToAdjustData = service.retrieveLoanTransaction(transactionToAdjust.getLoan().getId(),
                transactionToAdjust.getId());
        transactionToAdjustData.setLoanChargePaidByList(
                loanChargePaidByReadPlatformService.getLoanChargesPaidByTransactionId(transactionToAdjust.getId()));
        LoanTransactionDataV1 transactionToAdjustAvroDto = mapper.map(transactionToAdjustData);

        LoanTransaction newTransactionDetail = event.get().getNewTransactionDetail();
        LoanTransactionDataV1 newTransactionDetailAvroDto = null;
        if (newTransactionDetail != null) {
            LoanTransactionData newTransactionDetailData = service.retrieveLoanTransaction(newTransactionDetail.getLoan().getId(),
                    newTransactionDetail.getId());
            newTransactionDetailData.setLoanChargePaidByList(
                    loanChargePaidByReadPlatformService.getLoanChargesPaidByTransactionId(newTransactionDetail.getId()));
            newTransactionDetailAvroDto = mapper.map(newTransactionDetailData);

        }
        return new LoanTransactionAdjustmentDataV1(transactionToAdjustAvroDto, newTransactionDetailAvroDto);
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return LoanTransactionAdjustmentDataV1.class;
    }
}
