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
package org.apache.fineract.infrastructure.businessdate.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.ZoneId;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.junit.jupiter.api.Test;

class BusinessDateSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Test
    void serializeBusinessDateData() throws JsonProcessingException {
        var now = LocalDate.now(ZoneId.systemDefault());
        var businessDateResponse = BusinessDateResponse.builder().type(BusinessDateType.BUSINESS_DATE)
                .description(BusinessDateType.BUSINESS_DATE.getDescription()).date(now).build();

        var result = mapper.writeValueAsString(businessDateResponse);

        assertEquals("{\"description\":\"Business Date\",\"type\":\"BUSINESS_DATE\",\"date\":[" + now.getYear() + "," + now.getMonthValue()
                + "," + now.getDayOfMonth() + "]}", result);
    }

    @Test
    void serializeBusinessDateData_COB() throws JsonProcessingException {
        var now = LocalDate.now(ZoneId.systemDefault());
        var businessDateResponse = BusinessDateResponse.builder().type(BusinessDateType.COB_DATE)
                .description(BusinessDateType.COB_DATE.getDescription()).date(now).build();

        var result = mapper.writeValueAsString(businessDateResponse);

        assertEquals("{\"description\":\"Close of Business Date\",\"type\":\"COB_DATE\",\"date\":[" + now.getYear() + ","
                + now.getMonthValue() + "," + now.getDayOfMonth() + "]}", result);
    }
}
