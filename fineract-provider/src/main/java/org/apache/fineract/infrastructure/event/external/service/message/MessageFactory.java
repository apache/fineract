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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.apache.fineract.avro.BulkMessageV1;
import org.apache.fineract.avro.MessageV1;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.external.service.message.domain.BulkMessageData;
import org.apache.fineract.infrastructure.event.external.service.message.domain.MessageCategory;
import org.apache.fineract.infrastructure.event.external.service.message.domain.MessageData;
import org.apache.fineract.infrastructure.event.external.service.message.domain.MessageDataSchema;
import org.apache.fineract.infrastructure.event.external.service.message.domain.MessageId;
import org.apache.fineract.infrastructure.event.external.service.message.domain.MessageIdempotencyKey;
import org.apache.fineract.infrastructure.event.external.service.message.domain.MessageSource;
import org.apache.fineract.infrastructure.event.external.service.message.domain.MessageType;
import org.springframework.stereotype.Component;

@Component
public class MessageFactory {

    public MessageV1 createMessage(MessageId id, MessageSource source, MessageType type, MessageCategory category,
            MessageIdempotencyKey idempotencyKey, MessageDataSchema dataSchema, MessageData data) {
        MessageV1 result = new MessageV1();
        result.setId(id.getId());
        result.setSource(source.getSource());
        result.setType(type.getType());
        result.setCategory(category.getCategory());
        result.setCreatedAt(getMessageCreatedAt());
        result.setTenantId(getTenantId());
        result.setIdempotencyKey(idempotencyKey.getIdempotencyKey());
        result.setDataschema(dataSchema.getDataSchema());
        result.setData(data.getData());
        return result;
    }

    public BulkMessageV1 createBulkMessage(MessageId id, MessageSource source, MessageType type, BulkMessageData data) {

        BulkMessageV1 result = new BulkMessageV1();
        result.setId(id.getId());
        result.setSource(source.getSource());
        result.setType(type.getType());
        result.setCreatedAt(getMessageCreatedAt());
        result.setTenantId(getTenantId());
        result.setData(data.getData());
        return result;
    }

    private String getTenantId() {
        return ThreadLocalContextUtil.getTenant().getName();
    }

    private String getMessageCreatedAt() {
        OffsetDateTime createdAt = DateUtils.getOffsetDateTimeOfTenant();
        return createdAt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
