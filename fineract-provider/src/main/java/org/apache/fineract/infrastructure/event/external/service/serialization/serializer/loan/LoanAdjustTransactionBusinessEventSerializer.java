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

import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanTransactionDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.infrastructure.event.external.service.support.ByteBufferConverter;
import org.apache.fineract.portfolio.businessevent.domain.BusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanAdjustTransactionBusinessEventSerializer implements BusinessEventSerializer {

    private final LoanReadPlatformService service;
    private final LoanTransactionDataMapper mapper;
    private final ByteBufferConverter byteBufferConverter;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof LoanAdjustTransactionBusinessEvent;
    }

    @Override
    public <T> byte[] serialize(BusinessEvent<T> rawEvent) throws IOException {
        LoanAdjustTransactionBusinessEvent event = (LoanAdjustTransactionBusinessEvent) rawEvent;
        LoanTransaction transactionToAdjust = event.get().getTransactionToAdjust();
        LoanTransaction newTransactionDetail = event.get().getNewTransactionDetail();
        LoanTransactionData transactionToAdjustData = service.retrieveLoanTransaction(transactionToAdjust.getLoan().getId(),
                transactionToAdjust.getId());
        LoanTransactionData newTransactionDetailData = service.retrieveLoanTransaction(newTransactionDetail.getLoan().getId(),
                newTransactionDetail.getId());
        LoanTransactionDataV1 transactionToAdjustAvroDto = mapper.map(transactionToAdjustData);
        LoanTransactionDataV1 newTransactionDetailAvroDto = mapper.map(newTransactionDetailData);
        LoanTransactionAdjustmentDataV1 avroDto = new LoanTransactionAdjustmentDataV1(transactionToAdjustAvroDto,
                newTransactionDetailAvroDto);
        ByteBuffer buffer = avroDto.toByteBuffer();
        return byteBufferConverter.convert(buffer);
    }
}
