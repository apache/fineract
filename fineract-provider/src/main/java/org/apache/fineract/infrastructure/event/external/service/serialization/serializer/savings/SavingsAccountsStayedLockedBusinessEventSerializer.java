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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.savings;

import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.avro.savings.v1.SavingsAccountsStayedLockedDataV1;
import org.apache.fineract.cob.savings.SavingsAccountsStayedLockedBusinessEvent;
import org.apache.fineract.cob.savings.SavingsAccountsStayedLockedData;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.savings.SavingsAccountsStayedLockedDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SavingsAccountsStayedLockedBusinessEventSerializer implements BusinessEventSerializer {

    private final SavingsAccountsStayedLockedDataMapper mapper;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof SavingsAccountsStayedLockedBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        SavingsAccountsStayedLockedBusinessEvent event = (SavingsAccountsStayedLockedBusinessEvent) rawEvent;
        SavingsAccountsStayedLockedData savingsAccounts = event.get();
        return mapper.map(savingsAccounts);
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return SavingsAccountsStayedLockedDataV1.class;
    }
}
