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

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

public class BusinessDateMapperTest {

    private BusinessDateMapper businessDateMapper = Mappers.getMapper(BusinessDateMapper.class);

    @Test
    public void testMapping() {
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        BusinessDate businessDate = BusinessDate.instance(BusinessDateType.BUSINESS_DATE, now);
        BusinessDateData businessDateData = businessDateMapper.map(businessDate);
        assertEquals(businessDate.getDate(), businessDateData.getDate());
        assertEquals(businessDate.getType().getDescription(), businessDateData.getDescription());
        assertEquals(businessDate.getType().getName(), businessDateData.getType());
    }

    @Test
    public void testMappingList() {
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        BusinessDate businessDate = BusinessDate.instance(BusinessDateType.BUSINESS_DATE, now);
        List<BusinessDateData> businessDateData = businessDateMapper.map(Collections.singletonList(businessDate));
        assertEquals(businessDate.getDate(), businessDateData.get(0).getDate());
        assertEquals(businessDate.getType().getDescription(), businessDateData.get(0).getDescription());
        assertEquals(businessDate.getType().getName(), businessDateData.get(0).getType());
    }
}
