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
import org.apache.fineract.avro.loan.v1.LoanChargeDataV1;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanChargeDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.infrastructure.event.external.service.support.ByteBufferConverter;
import org.apache.fineract.portfolio.businessevent.domain.BusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.loan.charge.LoanChargeBusinessEvent;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanChargeBusinessEventSerializer implements BusinessEventSerializer {

    private final LoanChargeReadPlatformService service;
    private final LoanChargeDataMapper mapper;
    private final ByteBufferConverter byteBufferConverter;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof LoanChargeBusinessEvent;
    }

    @Override
    public <T> byte[] serialize(BusinessEvent<T> rawEvent) throws IOException {
        LoanChargeBusinessEvent event = (LoanChargeBusinessEvent) rawEvent;
        LoanChargeData data = service.retrieveLoanChargeDetails(event.get().getId(), event.get().getLoan().getId());
        LoanChargeDataV1 avroDto = mapper.map(data);
        ByteBuffer buffer = avroDto.toByteBuffer();
        return byteBufferConverter.convert(buffer);
    }
}
