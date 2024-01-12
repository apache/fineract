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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.avro.BulkMessageItemV1;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DataEnricherProcessor;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.BulkBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEvent;
import org.apache.fineract.infrastructure.event.external.service.idempotency.ExternalEventIdempotencyKeyGenerator;
import org.apache.fineract.infrastructure.event.external.service.message.BulkMessageItemFactory;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializerFactory;
import org.apache.fineract.infrastructure.event.external.service.support.ByteBufferConverter;
import org.apache.fineract.investor.enricher.LoanAccountDataV1Enricher;
import org.apache.fineract.investor.enricher.LoanTransactionAdjustmentDataV1Enricher;
import org.apache.fineract.investor.enricher.LoanTransactionDataV1Enricher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
class ExternalEventServiceTest {

    public static final String DUMMY_EXTERNAL_OWNER_ID = "dummy-external-owner-id";

    public static final String DUMMY_SETTLEMENT_DATE = "2021-01-01";
    @Mock
    private ExternalEventRepository repository;
    @Mock
    private ExternalEventIdempotencyKeyGenerator idempotencyKeyGenerator;
    @Mock
    private BusinessEventSerializerFactory serializerFactory;
    @Mock
    private ByteBufferConverter byteBufferConverter;
    @Mock
    private BulkMessageItemFactory bulkMessageItemFactory;
    @Mock
    private EntityManager entityManager;
    @Mock
    private LoanAccountDataV1Enricher loanAccountDataV1Enricher;
    @Mock
    private LoanTransactionAdjustmentDataV1Enricher loanTransactionAdjustmentDataV1Enricher;
    @Mock
    private LoanTransactionDataV1Enricher loanTransactionDataV1Enricher;

    private ExternalEventService underTest;

    @BeforeEach
    public void setUp() {
        lenient().when(loanAccountDataV1Enricher.isDataTypeSupported(Mockito.eq(LoanAccountDataV1.class))).thenReturn(true);
        lenient().when(loanTransactionDataV1Enricher.isDataTypeSupported(Mockito.eq(LoanTransactionDataV1.class))).thenReturn(true);
        lenient().when(loanTransactionAdjustmentDataV1Enricher.isDataTypeSupported(Mockito.eq(LoanTransactionAdjustmentDataV1.class)))
                .thenReturn(true);
        DataEnricherProcessor dataEnricherProcessor = new DataEnricherProcessor(
                Optional.of(List.of(loanAccountDataV1Enricher, loanTransactionAdjustmentDataV1Enricher, loanTransactionDataV1Enricher)));
        underTest = new ExternalEventService(repository, idempotencyKeyGenerator, serializerFactory, byteBufferConverter,
                bulkMessageItemFactory, dataEnricherProcessor);
        underTest.setEntityManager(entityManager);
        FineractPlatformTenant tenant = new FineractPlatformTenant(1L, "default", "Default Tenant", "Europe/Budapest", null);
        ThreadLocalContextUtil.setTenant(tenant);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testPostEventShouldFailWhenNullEventIsGiven() {
        // given
        // when & then
        assertThatThrownBy(() -> underTest.postEvent(null)).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testPostEventShouldFailWhenEventSerializationFails() throws IOException {
        // given
        BusinessEvent event = mock(BusinessEvent.class);
        BusinessEventSerializer eventSerializer = mock(BusinessEventSerializer.class);

        given(idempotencyKeyGenerator.generate(event)).willReturn("");
        given(serializerFactory.create(event)).willReturn(eventSerializer);
        given(eventSerializer.getSupportedSchema()).will(invocation -> LoanAccountDataV1.class);
        ByteBufferSerializable byteBuffer = mock(LoanAccountDataV1.class);
        given(eventSerializer.toAvroDTO(event)).willReturn(byteBuffer);
        given(byteBuffer.toByteBuffer()).willThrow(new IOException(""));
        // when & then
        assertThatThrownBy(() -> underTest.postEvent(event)).isExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    public void testPostEventShouldWorkWithRegularEvent() {
        // given
        ArgumentCaptor<ExternalEvent> externalEventArgumentCaptor = ArgumentCaptor.forClass(ExternalEvent.class);

        String eventSchema = "org.apache.fineract.avro.loan.v1.LoanAccountDataV1";
        String eventType = "TestType";
        String idempotencyKey = "key";
        BusinessEvent event = mock(BusinessEvent.class);
        BusinessEventSerializer eventSerializer = mock(BusinessEventSerializer.class);
        byte[] data = new byte[0];

        given(event.getType()).willReturn(eventType);
        given(idempotencyKeyGenerator.generate(event)).willReturn(idempotencyKey);
        given(serializerFactory.create(event)).willReturn(eventSerializer);
        LoanAccountDataV1 loanAccountData = new LoanAccountDataV1();
        given(eventSerializer.getSupportedSchema()).will(invocation -> LoanAccountDataV1.class);
        given(eventSerializer.toAvroDTO(event)).willReturn(loanAccountData);
        given(byteBufferConverter.convert(any(ByteBuffer.class))).willReturn(data);
        // when
        underTest.postEvent(event);
        // then
        verify(repository).save(externalEventArgumentCaptor.capture());
        verify(loanAccountDataV1Enricher).isDataTypeSupported(LoanAccountDataV1.class);
        verify(loanAccountDataV1Enricher).enrich(loanAccountData);
        ExternalEvent externalEvent = externalEventArgumentCaptor.getValue();
        assertThat(externalEvent.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(externalEvent.getData()).isEqualTo(data);
        assertThat(externalEvent.getType()).isEqualTo(eventType);
        assertThat(externalEvent.getSchema()).isEqualTo(eventSchema);
    }

    @Test
    public void testPostEventShouldWorkWithBulkEvent() throws IOException {
        // given
        ArgumentCaptor<ExternalEvent> externalEventArgumentCaptor = ArgumentCaptor.forClass(ExternalEvent.class);
        String eventType = "BulkBusinessEvent";
        String schema = "org.apache.fineract.avro.BulkMessagePayloadV1";

        String idempotencyKey = "key";
        BusinessEvent event = mock(BusinessEvent.class);
        BulkMessageItemV1 messageItem = new BulkMessageItemV1(1, "", "", "", ByteBuffer.wrap(new byte[0]));
        BulkBusinessEvent bulkEvent = new BulkBusinessEvent(List.of(event));
        byte[] data = new byte[0];

        given(bulkMessageItemFactory.createBulkMessageItem(1, event)).willReturn(messageItem);
        given(idempotencyKeyGenerator.generate(bulkEvent)).willReturn(idempotencyKey);
        given(byteBufferConverter.convert(any(ByteBuffer.class))).willReturn(data);
        // when
        underTest.postEvent(bulkEvent);
        // then
        verify(repository).save(externalEventArgumentCaptor.capture());
        ExternalEvent externalEvent = externalEventArgumentCaptor.getValue();
        assertThat(externalEvent.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(externalEvent.getData()).isEqualTo(data);
        assertThat(externalEvent.getType()).isEqualTo(eventType);
        assertThat(externalEvent.getSchema()).isEqualTo(schema);
    }

    @Test
    public void testPostEventShouldSaveEventCategory() {
        // given
        ArgumentCaptor<ExternalEvent> externalEventArgumentCaptor = ArgumentCaptor.forClass(ExternalEvent.class);
        String eventSchema = "org.apache.fineract.avro.loan.v1.LoanAccountDataV1";
        String eventType = "TestType";
        String eventCategory = "TestCategory";
        String idempotencyKey = "key";
        BusinessEvent event = mock(BusinessEvent.class);
        BusinessEventSerializer eventSerializer = mock(BusinessEventSerializer.class);

        given(event.getType()).willReturn(eventType);
        given(event.getCategory()).willReturn(eventCategory);
        given(idempotencyKeyGenerator.generate(event)).willReturn(idempotencyKey);
        given(serializerFactory.create(event)).willReturn(eventSerializer);
        given(eventSerializer.getSupportedSchema()).will(invocation -> LoanAccountDataV1.class);
        given(eventSerializer.toAvroDTO(event)).willReturn(new LoanAccountDataV1());
        // when
        underTest.postEvent(event);
        // then
        verify(repository).save(externalEventArgumentCaptor.capture());
        ExternalEvent externalEvent = externalEventArgumentCaptor.getValue();
        assertThat(externalEvent.getCategory()).isEqualTo(eventCategory);

    }

    @Test
    public void testEventShouldSaveDatesInMilliSecondFormat() {
        // given
        ArgumentCaptor<ExternalEvent> externalEventArgumentCaptor = ArgumentCaptor.forClass(ExternalEvent.class);
        String eventSchema = "org.apache.fineract.avro.loan.v1.LoanAccountDataV1";
        String eventType = "TestType";
        String eventCategory = "TestCategory";
        String idempotencyKey = "key";
        BusinessEvent event = mock(BusinessEvent.class);
        BusinessEventSerializer eventSerializer = mock(BusinessEventSerializer.class);

        given(event.getType()).willReturn(eventType);
        given(event.getCategory()).willReturn(eventCategory);
        given(idempotencyKeyGenerator.generate(event)).willReturn(idempotencyKey);
        given(serializerFactory.create(event)).willReturn(eventSerializer);
        given(eventSerializer.getSupportedSchema()).will(invocation -> LoanAccountDataV1.class);
        given(eventSerializer.toAvroDTO(event)).willReturn(new LoanAccountDataV1());
        // when
        underTest.postEvent(event);
        // then
        verify(repository).save(externalEventArgumentCaptor.capture());
        ExternalEvent externalEvent = externalEventArgumentCaptor.getValue();
        assertThat(externalEvent.getCreatedAt().isSupported(ChronoUnit.MILLIS)).isTrue();
    }

    @Test
    public void testPostEventShouldWorkWithTransactionEvent() {
        // given
        ArgumentCaptor<ExternalEvent> externalEventArgumentCaptor = ArgumentCaptor.forClass(ExternalEvent.class);

        String eventSchema = "org.apache.fineract.avro.loan.v1.LoanTransactionDataV1";
        String eventType = "TestType";
        String idempotencyKey = "key";
        BusinessEvent event = mock(BusinessEvent.class);
        BusinessEventSerializer eventSerializer = mock(BusinessEventSerializer.class);
        byte[] data = new byte[0];

        given(event.getType()).willReturn(eventType);
        given(idempotencyKeyGenerator.generate(event)).willReturn(idempotencyKey);
        given(serializerFactory.create(event)).willReturn(eventSerializer);
        LoanTransactionDataV1 loanTransactionData = new LoanTransactionDataV1();
        given(eventSerializer.getSupportedSchema()).will(invocation -> LoanTransactionDataV1.class);
        given(eventSerializer.toAvroDTO(event)).willReturn(loanTransactionData);
        given(byteBufferConverter.convert(any(ByteBuffer.class))).willReturn(data);
        // when
        underTest.postEvent(event);
        // then
        verify(repository).save(externalEventArgumentCaptor.capture());
        verify(loanTransactionDataV1Enricher).isDataTypeSupported(LoanTransactionDataV1.class);
        verify(loanTransactionDataV1Enricher).enrich(loanTransactionData);
        ExternalEvent externalEvent = externalEventArgumentCaptor.getValue();
        assertThat(externalEvent.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(externalEvent.getData()).isEqualTo(data);
        assertThat(externalEvent.getType()).isEqualTo(eventType);
        assertThat(externalEvent.getSchema()).isEqualTo(eventSchema);
    }

    @Test
    public void testPostEventShouldWorkWithTransactionAdjustEvent() {
        // given
        ArgumentCaptor<ExternalEvent> externalEventArgumentCaptor = ArgumentCaptor.forClass(ExternalEvent.class);

        String eventSchema = "org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1";
        String eventType = "TestType";
        String idempotencyKey = "key";
        BusinessEvent event = mock(BusinessEvent.class);
        BusinessEventSerializer eventSerializer = mock(BusinessEventSerializer.class);
        byte[] data = new byte[0];

        given(event.getType()).willReturn(eventType);
        given(idempotencyKeyGenerator.generate(event)).willReturn(idempotencyKey);
        given(serializerFactory.create(event)).willReturn(eventSerializer);
        LoanTransactionAdjustmentDataV1 loanTransactionAdjustmentData = new LoanTransactionAdjustmentDataV1();
        given(eventSerializer.getSupportedSchema()).will(invocation -> LoanTransactionAdjustmentDataV1.class);
        given(eventSerializer.toAvroDTO(event)).willReturn(loanTransactionAdjustmentData);
        given(byteBufferConverter.convert(any(ByteBuffer.class))).willReturn(data);
        // when
        underTest.postEvent(event);
        // then
        verify(repository).save(externalEventArgumentCaptor.capture());
        verify(loanTransactionAdjustmentDataV1Enricher).isDataTypeSupported(LoanTransactionAdjustmentDataV1.class);
        verify(loanTransactionAdjustmentDataV1Enricher).enrich(loanTransactionAdjustmentData);
        ExternalEvent externalEvent = externalEventArgumentCaptor.getValue();
        assertThat(externalEvent.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(externalEvent.getData()).isEqualTo(data);
        assertThat(externalEvent.getType()).isEqualTo(eventType);
        assertThat(externalEvent.getSchema()).isEqualTo(eventSchema);
    }
}
