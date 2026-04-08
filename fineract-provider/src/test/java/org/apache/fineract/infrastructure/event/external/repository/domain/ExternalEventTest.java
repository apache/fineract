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
package org.apache.fineract.infrastructure.event.external.repository.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ExternalEventTest {

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testConstructorWorks() {
        // given
        LocalDate currentBusinessDate = LocalDate.of(2022, 6, 12);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, currentBusinessDate)));

        String type = "type";
        String category = "category";
        String schema = "schema";
        byte[] data = new byte[0];
        String idempotencyKey = "idempotencyKey";
        long aggregateRootId = 1L;
        // when
        ExternalEvent result = new ExternalEvent(type, category, schema, data, idempotencyKey, aggregateRootId);
        // then
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getSchema()).isEqualTo(schema);
        assertThat(result.getData()).isEqualTo(data);
        assertThat(result.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(result.getAggregateRootId()).isEqualTo(aggregateRootId);
        assertThat(result.getStatus()).isEqualTo(ExternalEventStatus.TO_BE_SENT);
        assertThat(result.getBusinessDate()).isEqualTo(currentBusinessDate);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getSentAt()).isNull();
    }
}
