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
package org.apache.fineract.infrastructure.businessdate.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class BusinessDateUpdateRequestMapperTest {

    private final BusinessDateMapper businessDateMapper = Mappers.getMapper(BusinessDateMapper.class);

    @Test
    void testMapping() {
        var now = LocalDate.now(ZoneId.systemDefault());
        var businessDate = BusinessDate.instance(BusinessDateType.BUSINESS_DATE, now);
        var businessDateResponse = businessDateMapper.map(businessDate);
        assertEquals(businessDate.getDate(), businessDateResponse.getDate());
        assertEquals(businessDate.getType().getDescription(), businessDateResponse.getDescription());
        assertEquals(businessDate.getType(), businessDateResponse.getType());
    }

    @Test
    void testMappingList() {
        var now = LocalDate.now(ZoneId.systemDefault());
        var businessDate = BusinessDate.instance(BusinessDateType.BUSINESS_DATE, now);
        var businessDateData = businessDateMapper.map(Collections.singletonList(businessDate));
        assertEquals(businessDate.getDate(), businessDateData.get(0).getDate());
        assertEquals(businessDate.getType().getDescription(), businessDateData.get(0).getDescription());
        assertEquals(businessDate.getType(), businessDateData.get(0).getType());
    }
}
