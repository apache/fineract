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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.avro.loan.v1.LoanProductDataV1;
import org.apache.fineract.infrastructure.event.business.domain.loan.product.LoanProductBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanProductDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.ExternalEventCustomDataSerializer;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LoanProductBusinessEventSerializerTest {

    @Mock
    private LoanProductReadPlatformService loanProductReadPlatformService;
    @Mock
    private LoanProductDataMapper loanProductDataMapper;

    private LoanProductBusinessEventSerializer serializer;

    @BeforeEach
    void setUp() {
        final List<ExternalEventCustomDataSerializer<LoanProductBusinessEvent>> externalEventCustomDataSerializers = List
                .of(new ExternalEventCustomDataSerializer<>() {

                    @Override
                    public ByteBuffer serialize(final LoanProductBusinessEvent event) {
                        return ByteBuffer.wrap("test_data_for_loan_product".getBytes(UTF_8));
                    }

                    @Override
                    public String key() {
                        return "test_key_1";
                    }
                }, new ExternalEventCustomDataSerializer<>() {

                    @Override
                    public ByteBuffer serialize(final LoanProductBusinessEvent event) {
                        return ByteBuffer.wrap("test_data_for_loan_product_1".getBytes(UTF_8));
                    }

                    @Override
                    public String key() {
                        return "test_key_1";
                    }
                }, new ExternalEventCustomDataSerializer<>() {

                    @Override
                    public ByteBuffer serialize(final LoanProductBusinessEvent event) {
                        return ByteBuffer.wrap("test_data_for_loan_product_2".getBytes(UTF_8));
                    }

                    @Override
                    public String key() {
                        return "test_key_2";
                    }
                });
        serializer = new LoanProductBusinessEventSerializer(loanProductReadPlatformService, loanProductDataMapper,
                externalEventCustomDataSerializers);
    }

    @Test
    void testLoanProductCustomDataSerialization() {
        final long productId = 1;

        final LoanProduct loanProduct = mock(LoanProduct.class);
        final LoanProductData loanProductData = mock(LoanProductData.class);
        final LoanProductBusinessEvent event = mock(LoanProductBusinessEvent.class);

        when(loanProductReadPlatformService.retrieveLoanProduct(productId)).thenReturn(loanProductData);
        when(loanProduct.getId()).thenReturn(productId);
        when(event.get()).thenReturn(loanProduct);

        final LoanProductDataV1 expectedAvroData = LoanProductDataV1.newBuilder().setId(productId).setCustomData(new HashMap<>()).build();
        when(loanProductDataMapper.map(any(LoanProductData.class))).thenReturn(expectedAvroData);

        final LoanProductDataV1 result = (LoanProductDataV1) serializer.toAvroDTO(event);

        assertNotNull(result);
        assertNotNull(result.getCustomData());
        final Map<String, ByteBuffer> customData = result.getCustomData();
        assertEquals("test_data_for_loan_product_1", new String(customData.get("test_key_1").array(), UTF_8));
        assertEquals("test_data_for_loan_product_2", new String(customData.get("test_key_2").array(), UTF_8));
    }

}
