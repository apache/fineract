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
package org.apache.fineract.infrastructure.event.external.service.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.avro.BulkMessageItemV1;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.infrastructure.core.service.DataEnricherProcessor;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializerFactory;
import org.apache.fineract.infrastructure.event.external.service.support.ByteBufferConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BulkMessageItemFactory {

    private final BusinessEventSerializerFactory serializerFactory;
    private final ByteBufferConverter byteBufferConverter;
    private final DataEnricherProcessor dataEnricherProcessor;

    public BulkMessageItemV1 createBulkMessageItem(long id, BusinessEvent<?> event) throws IOException {
        BusinessEventSerializer eventSerializer = serializerFactory.create(event);
        ByteBufferSerializable avroDto = dataEnricherProcessor.enrich(eventSerializer.toAvroDTO(event));
        ByteBuffer buffer = avroDto.toByteBuffer();
        byte[] serializedContent = byteBufferConverter.convert(buffer);
        String type = event.getType();
        String category = "nocategory"; // TODO: switch this to the actual category when implemented
        String schema = eventSerializer.getSupportedSchema().getName();
        ByteBuffer data = byteBufferConverter.convert(serializedContent);
        return new BulkMessageItemV1(id, type, category, schema, data);
    }
}
