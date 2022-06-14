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

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.serialization.CommandProcessingResultJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson;
import org.apache.fineract.infrastructure.core.serialization.ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.junit.Test;

public class BusinessDataSerialized {

    @Test
    public void serializeBusinessDateData() {
        DefaultToApiJsonSerializer<BusinessDateData> jsonSerializer = new DefaultToApiJsonSerializer<>(
                new ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson(),
                new ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson(), new CommandProcessingResultJsonSerializer(),
                new GoogleGsonSerializerHelper());

        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        BusinessDateData businessDateData = BusinessDateData.instance(BusinessDateType.BUSINESS_DATE, now);
        String result = jsonSerializer.serialize(businessDateData);
        assertEquals("{\"description\":\"Business Date\",\"type\":\"BUSINESS_DATE\",\"date\":[" + now.getYear() + "," + now.getMonthValue()
                + "," + now.getDayOfMonth() + "]}", result);
    }

    @Test
    public void serializeBusinessDateData_COB() {
        DefaultToApiJsonSerializer<BusinessDateData> jsonSerializer = new DefaultToApiJsonSerializer<>(
                new ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson(),
                new ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson(), new CommandProcessingResultJsonSerializer(),
                new GoogleGsonSerializerHelper());

        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        BusinessDateData businessDateData = BusinessDateData.instance(BusinessDateType.COB_DATE, now);
        String result = jsonSerializer.serialize(businessDateData);
        assertEquals("{\"description\":\"Close of Business Date\",\"type\":\"COB_DATE\",\"date\":[" + now.getYear() + ","
                + now.getMonthValue() + "," + now.getDayOfMonth() + "]}", result);
    }

}
