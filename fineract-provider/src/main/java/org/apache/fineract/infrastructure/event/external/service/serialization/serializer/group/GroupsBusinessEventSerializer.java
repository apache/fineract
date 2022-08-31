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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.group;

import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.avro.generic.v1.CommandProcessingResultV1;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.generic.CommandProcessingResultMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.infrastructure.event.external.service.support.ByteBufferConverter;
import org.apache.fineract.portfolio.businessevent.domain.BusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.group.GroupsBusinessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupsBusinessEventSerializer implements BusinessEventSerializer {

    private final CommandProcessingResultMapper mapper;
    private final ByteBufferConverter byteBufferConverter;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof GroupsBusinessEvent;
    }

    @Override
    public <T> byte[] serialize(BusinessEvent<T> rawEvent) throws IOException {
        GroupsBusinessEvent event = (GroupsBusinessEvent) rawEvent;
        CommandProcessingResultV1 avroDto = mapper.map(event.get());
        ByteBuffer buffer = avroDto.toByteBuffer();
        return byteBufferConverter.convert(buffer);
    }
}
