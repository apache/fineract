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
package org.apache.fineract.infrastructure.event.external.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import org.apache.fineract.avro.BulkMessageItemV1;
import org.apache.fineract.avro.BulkMessagePayloadV1;
import org.apache.fineract.avro.loan.v1.UnpaidChargeDataV1;
import org.apache.fineract.avro.office.v1.OfficeDataV1;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEvent;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.json.JsonMapper;

/**
 * Decodes stored events the way {@code GET /v1/internal/externalevents} does. Avro 1.12.2 and later refuse to decode
 * classes that are not explicitly trusted ({@code Forbidden org.apache.fineract.avro...}), which this catches before
 * the integration and E2E suites do.
 */
class InternalExternalEventServiceTest {

    private final ExternalEventRepository repository = mock(ExternalEventRepository.class);
    private final InternalExternalEventService service = new InternalExternalEventService(JsonMapper.builder().build(), repository);

    @Test
    void decodesAnEventWithNestedRecords() throws IOException {
        OfficeDataV1 parent = OfficeDataV1.newBuilder().setId(1L).setName("Head Office").build();
        OfficeDataV1 office = OfficeDataV1.newBuilder().setId(2L).setName("Branch").setAllowedParents(List.of(parent)).build();
        stored(event("OfficeCreatedBusinessEvent", OfficeDataV1.class.getName(), office.toByteBuffer()));

        List<ExternalEventResponse> events = service.getAllExternalEvents(null, null, null, null);

        assertThat(events).hasSize(1);
        Map<String, Object> payLoad = events.get(0).getPayLoad();
        assertThat(payLoad).containsEntry("id", 2).containsEntry("name", "Branch");
        assertThat((List<?>) payLoad.get("allowedParents")).singleElement().asInstanceOf(MAP).containsEntry("name", "Head Office");
    }

    @Test
    void decodesABulkEventAndItsItems() throws IOException {
        UnpaidChargeDataV1 charge = new UnpaidChargeDataV1(7L, "Fee", new BigDecimal("12.34000000"));
        BulkMessageItemV1 item = new BulkMessageItemV1(11L, "LoanChargeAdjustmentPostBusinessEvent", "Loan",
                UnpaidChargeDataV1.class.getName(), charge.toByteBuffer());
        BulkMessagePayloadV1 bulk = new BulkMessagePayloadV1(List.of(item));
        stored(event("BulkBusinessEvent", BulkMessagePayloadV1.class.getName(), bulk.toByteBuffer()));

        List<ExternalEventResponse> events = service.getAllExternalEvents(null, null, null, null);

        List<?> items = (List<?>) events.get(0).getPayLoad().get(InternalExternalEventService.BULK_ITEMS_KEY);
        assertThat(items).singleElement().asInstanceOf(MAP).containsEntry("id", 11L)
                .containsEntry("type", "LoanChargeAdjustmentPostBusinessEvent").extractingByKey("payLoad").asInstanceOf(MAP)
                .containsEntry("chargeName", "Fee");
    }

    @SuppressWarnings("unchecked")
    private void stored(ExternalEvent event) {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(event));
    }

    private static ExternalEvent event(String type, String schema, ByteBuffer data) {
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        ExternalEvent event = mock(ExternalEvent.class);
        when(event.getId()).thenReturn(1L);
        when(event.getType()).thenReturn(type);
        when(event.getCategory()).thenReturn("Category");
        when(event.getSchema()).thenReturn(schema);
        when(event.getData()).thenReturn(bytes);
        return event;
    }
}
