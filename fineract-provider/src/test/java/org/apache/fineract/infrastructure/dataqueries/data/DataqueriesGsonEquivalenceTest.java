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
package org.apache.fineract.infrastructure.dataqueries.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.infrastructure.core.jersey.JerseyJacksonConverterConfig;
import org.apache.fineract.infrastructure.core.serialization.ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.junit.jupiter.api.Test;

/**
 * Pins that the typed datatable and entity datatable check responses serialize to the same JSON tree as the legacy Gson
 * serializer did.
 */
class DataqueriesGsonEquivalenceTest {

    private final ObjectMapper mapper = new JerseyJacksonConverterConfig().objectMapper(List.of(), List.of(), List.of());
    private final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson gson = new ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson();

    private JsonNode viaGson(Object o) throws Exception {
        return mapper.readTree(gson.serialize(o));
    }

    private JsonNode viaJackson(Object o) throws Exception {
        return mapper.readTree(mapper.writeValueAsString(o));
    }

    private static ResultsetColumnHeaderData fullHeader() {
        return ResultsetColumnHeaderData.detailed("status_cd", "int", 11L, true, false,
                List.of(new ResultsetColumnValueData(1, "Active"), new ResultsetColumnValueData(2, "Closed", 5)), "status_code", true, true,
                DatabaseType.POSTGRESQL);
    }

    private static List<ResultsetColumnHeaderData> headers() {
        return List.of(ResultsetColumnHeaderData.basic("id", "int", DatabaseType.POSTGRESQL), ResultsetColumnHeaderData.detailed("name",
                "varchar", 100L, false, false, List.of(), null, false, false, DatabaseType.POSTGRESQL), fullHeader());
    }

    @Test
    void datatableDataMatchesGson() throws Exception {
        DatatableData data = DatatableData.create("m_client", "extra_client_details", null, headers());
        assertThat(viaJackson(data)).isEqualTo(viaGson(data));
    }

    @Test
    void datatableDataWithSubTypeMatchesGson() throws Exception {
        DatatableData data = DatatableData.create("m_client", "extra_client_details", "PERSON", headers());
        assertThat(viaJackson(data)).isEqualTo(viaGson(data));
    }

    @Test
    void listOfDatatableDataMatchesGson() throws Exception {
        List<DatatableData> data = List.of(DatatableData.create("m_client", "dt_a", "PERSON", headers()),
                DatatableData.create("m_loan", "dt_b", null, List.of()));
        assertThat(viaJackson(data)).isEqualTo(viaGson(data));
    }

    @Test
    void columnHeaderWritesOnlyTheGsonFields() throws Exception {
        JsonNode header = viaJackson(fullHeader());
        assertThat(header.fieldNames()).toIterable().containsExactlyInAnyOrder("columnName", "columnType", "columnLength",
                "columnDisplayType", "isColumnNullable", "isColumnPrimaryKey", "isColumnUnique", "isColumnIndexed", "columnValues",
                "columnCode");
        assertThat(header).isEqualTo(viaGson(fullHeader()));
    }

    @Test
    void columnValueWritesOnlyTheGsonFields() throws Exception {
        JsonNode withScore = viaJackson(new ResultsetColumnValueData(2, "Closed", 5));
        assertThat(withScore.fieldNames()).toIterable().containsExactlyInAnyOrder("id", "value", "score");
        assertThat(withScore).isEqualTo(viaGson(new ResultsetColumnValueData(2, "Closed", 5)));
        assertThat(viaJackson(new ResultsetColumnValueData(1, "Active"))).isEqualTo(viaGson(new ResultsetColumnValueData(1, "Active")));
    }

    @Test
    void pageOfEntityDatatableChecksMatchesGson() throws Exception {
        EntityDataTableChecksData withProduct = new EntityDataTableChecksData(1L, "m_loan",
                new StringEnumOptionData("m_loan", "m_loan", "Loan"),
                new EnumOptionData(100L, "loanStatusType.submitted.and.pending.approval", "Submitted and pending approval"), "dt_loan",
                true, 7L, "Product A");
        EntityDataTableChecksData withoutProduct = new EntityDataTableChecksData(2L, "m_client",
                new StringEnumOptionData("m_client", "m_client", "Client"), new EnumOptionData(100L, "clientStatusType.pending", "Pending"),
                "dt_client", false, null, null);
        Page<EntityDataTableChecksData> page = new Page<>(List.of(withProduct, withoutProduct), 2);
        assertThat(viaJackson(page)).isEqualTo(viaGson(page));
    }
}
