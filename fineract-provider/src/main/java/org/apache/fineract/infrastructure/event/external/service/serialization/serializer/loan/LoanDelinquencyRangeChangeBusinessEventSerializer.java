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
import org.apache.fineract.avro.loan.v1.DelinquencyRangeDataV1;
import org.apache.fineract.avro.loan.v1.LoanAccountDelinquencyRangeDataV1;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDelinquencyRangeChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanDelinquencyRangeDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.AbstractBusinessEventSerializer;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class LoanDelinquencyRangeChangeBusinessEventSerializer extends AbstractBusinessEventSerializer {

    private final LoanReadPlatformService service;
    private final LoanDelinquencyRangeDataMapper mapper;

    @Override
    protected <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        LoanDelinquencyRangeChangeBusinessEvent event = (LoanDelinquencyRangeChangeBusinessEvent) rawEvent;
        LoanAccountData data = service.retrieveOne(event.get().getId());
        Long id = data.getId();
        String accountNumber = data.getAccountNo();
        String externalId = data.getExternalId();
        DelinquencyRangeDataV1 delinquencyRange = mapper.map(data.getDelinquencyRange());
        return new LoanAccountDelinquencyRangeDataV1(id, accountNumber, externalId, delinquencyRange);
    }

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof LoanDelinquencyRangeChangeBusinessEvent;
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return LoanAccountDelinquencyRangeDataV1.class;
    }
}
