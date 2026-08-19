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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.workingcapitalloan;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanTransactionDataV1;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.workingcapitalloan.WorkingCapitalLoanTransactionDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanAdjustTransactionBusinessEventSerializer implements BusinessEventSerializer {

    private final WorkingCapitalLoanTransactionDataMapper mapper;

    @Override
    public <T> boolean canSerialize(final BusinessEvent<T> event) {
        return event instanceof WorkingCapitalLoanAdjustTransactionBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(final BusinessEvent<T> rawEvent) {
        final WorkingCapitalLoanAdjustTransactionBusinessEvent event = (WorkingCapitalLoanAdjustTransactionBusinessEvent) rawEvent;
        final WorkingCapitalLoanTransactionDataV1 transactionToAdjust = mapper.map(event.get().getTransactionToAdjust());
        final WorkingCapitalLoanTransactionDataV1 newTransactionDetail = Optional.ofNullable(event.get().getNewTransactionDetail())
                .map(mapper::map).orElse(null);
        return new WorkingCapitalLoanTransactionAdjustmentDataV1(transactionToAdjust, newTransactionDetail);
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return WorkingCapitalLoanTransactionAdjustmentDataV1.class;
    }
}
