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
package org.apache.fineract.portfolio.paymenttype.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeResponse;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.mapper.builder.PaymentTypeBuilderTest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PaymentTypeResponseMapperTest {

    private final PaymentTypeResponseMapper paymentTypeResponseMapper = Mappers.getMapper(PaymentTypeResponseMapper.class);

    @Test
    void mapPaymentTypeDataToPaymentTypeResponse() {
        PaymentTypeData paymentTypeData = PaymentTypeBuilderTest.createPaymentTypeData(1L, "name", "description", false, 10, "codeName",
                true);
        PaymentTypeResponse result = paymentTypeResponseMapper.map(paymentTypeData);
        assertNotNull(result);
        assertEquals(paymentTypeData.getId(), result.getId());
        assertEquals(paymentTypeData.getName(), result.getName());
        assertEquals(paymentTypeData.getDescription(), result.getDescription());
        assertEquals(paymentTypeData.getIsCashPayment(), result.getIsCashPayment());
        assertEquals(paymentTypeData.getCodeName(), result.getCodeName());
        assertEquals(paymentTypeData.getPosition(), result.getPosition());
        assertEquals(paymentTypeData.getIsSystemDefined(), result.getIsSystemDefined());
    }

    @Test
    void mapPaymentTypeToPaymentTypeResponse() {
        PaymentType paymentType = new PaymentType("name", "description", false, 10L, "codeName", true);
        PaymentTypeResponse result = paymentTypeResponseMapper.map(paymentType);
        assertNotNull(result);
        assertEquals(paymentType.getId(), result.getId());
        assertEquals(paymentType.getName(), result.getName());
        assertEquals(paymentType.getDescription(), result.getDescription());
        assertEquals(paymentType.getIsCashPayment(), result.getIsCashPayment());
        assertEquals(paymentType.getCodeName(), result.getCodeName());
        assertEquals(paymentType.getPosition(), result.getPosition().longValue());
        assertEquals(paymentType.getIsSystemDefined(), result.getIsSystemDefined());
    }

    @Test
    void mapListPaymentTypeDataToListPaymentTypeResponse() {
        List<PaymentTypeData> paymentTypeData = PaymentTypeBuilderTest.createPaymentTypeData(2, 1L, "name", "description", false, 10,
                "codeName", true);
        List<PaymentTypeResponse> results = paymentTypeResponseMapper.map(paymentTypeData);
        assertNotNull(results);

        assertEquals(paymentTypeData.size(), results.size());

        PaymentTypeData expectedResult = paymentTypeData.getFirst();
        PaymentTypeResponse actualResult = results.getFirst();

        assertEquals(expectedResult.getId(), actualResult.getId());
        assertEquals(expectedResult.getName(), actualResult.getName());
        assertEquals(expectedResult.getDescription(), actualResult.getDescription());
        assertEquals(expectedResult.getIsCashPayment(), actualResult.getIsCashPayment());
        assertEquals(expectedResult.getCodeName(), actualResult.getCodeName());
        assertEquals(expectedResult.getPosition(), actualResult.getPosition());
        assertEquals(expectedResult.getIsSystemDefined(), actualResult.getIsSystemDefined());
    }
}
