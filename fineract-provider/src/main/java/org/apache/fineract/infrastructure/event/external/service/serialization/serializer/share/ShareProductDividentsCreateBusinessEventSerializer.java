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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.share;

import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.avro.share.v1.ShareProductDataV1;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.share.ShareProductDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.infrastructure.event.external.service.support.ByteBufferConverter;
import org.apache.fineract.portfolio.businessevent.domain.BusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.share.ShareProductDividentsCreateBusinessEvent;
import org.apache.fineract.portfolio.products.service.ShareProductReadPlatformService;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShareProductDividentsCreateBusinessEventSerializer implements BusinessEventSerializer {

    private final ShareProductReadPlatformService service;
    private final ShareProductDataMapper mapper;
    private final ByteBufferConverter byteBufferConverter;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof ShareProductDividentsCreateBusinessEvent;
    }

    @Override
    public <T> byte[] serialize(BusinessEvent<T> rawEvent) throws IOException {
        ShareProductDividentsCreateBusinessEvent event = (ShareProductDividentsCreateBusinessEvent) rawEvent;
        ShareProductData data = (ShareProductData) service.retrieveOne(event.get(), false);
        ShareProductDataV1 avroDto = mapper.map(data);
        ByteBuffer buffer = avroDto.toByteBuffer();
        return byteBufferConverter.convert(buffer);
    }
}
