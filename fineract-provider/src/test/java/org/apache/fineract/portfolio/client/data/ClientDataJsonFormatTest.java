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
package org.apache.fineract.portfolio.client.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.jersey.JacksonWireFormatCharacterizationTest;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.junit.jupiter.api.Test;

/**
 * The client resources used to write these objects with GSON. They now return them typed, so Jersey's Jackson mapper
 * writes them. Each test compares the two, so the JSON the clients get stays the same.
 */
class ClientDataJsonFormatTest {

    private static final LocalDate SUBMITTED = LocalDate.of(2026, 1, 5);
    private static final LocalDate ACTIVATED = LocalDate.of(2026, 2, 6);

    private final ObjectMapper jackson = JacksonWireFormatCharacterizationTest.webObjectMapper();
    private final Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

    @Test
    void clientWithoutExternalIdMatchesGson() throws Exception {
        final ClientData client = client(ExternalId.empty());

        final JsonNode written = json(client);

        assertThat(written.has("externalId")).isFalse();
        assertThat(written.get("activationDate").toString()).isEqualTo("[2026,2,6]");
        assertThat(written.get("timeline").get("submittedOnDate").toString()).isEqualTo("[2026,1,5]");
        assertThat(written.get("clientNonPersonDetails").get("incorpValidityTillDate").toString()).isEqualTo("[2027,3,7]");
        assertSameAsGson(client);
    }

    @Test
    void clientWithExternalIdMatchesGson() throws Exception {
        final ClientData client = client(new ExternalId("ext-1"));

        assertThat(json(client).get("externalId").asText()).isEqualTo("ext-1");
        assertSameAsGson(client);
    }

    @Test
    void templateDatatablesMatchGson() throws Exception {
        final ResultsetColumnHeaderData column = ResultsetColumnHeaderData.basic("note", "VARCHAR", DatabaseType.POSTGRESQL);
        final ClientData template = ClientData.template(1L, SUBMITTED, null, null, null, null, null, null, null, null, null, null, null,
                null, Boolean.FALSE, List.of(DatatableData.create("m_client", "dt_client_extra", null, List.of(column))));

        final JsonNode datatable = json(template).get("datatables").get(0);

        assertThat(datatable.get("applicationTableName").asText()).isEqualTo("m_client");
        assertThat(datatable.get("columnHeaderData").get(0).get("columnName").asText()).isEqualTo("note");
        assertSameAsGson(template);
    }

    @Test
    void chargeMatchesGson() throws Exception {
        final ClientChargeData charge = ClientChargeData.instance(1L, 2L, 3L, "Fee",
                new EnumOptionData(2L, "chargeTimeType.specifiedDueDate", "Specified due date"), SUBMITTED, null, null, BigDecimal.TEN,
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(9), false, false, false, true, ACTIVATED, null);

        assertThat(json(charge).get("dueDate").toString()).isEqualTo("[2026,1,5]");
        assertSameAsGson(charge);
    }

    @Test
    void transactionMatchesGson() throws Exception {
        final ClientTransactionData transaction = ClientTransactionData.create(1L, 2L, "Head Office",
                new EnumOptionData(1L, "clientTransactionType.payCharge", "Pay Charge"), SUBMITTED, null, null, BigDecimal.TEN, null,
                ACTIVATED, false, 3L);

        assertThat(json(transaction).get("date").toString()).isEqualTo("[2026,1,5]");
        assertSameAsGson(transaction);
    }

    private static ClientData client(final ExternalId externalId) {
        final ClientTimelineData timeline = new ClientTimelineData(SUBMITTED, "mifos", "App", "Administrator", ACTIVATED, "mifos", "App",
                "Administrator", null, null, null, null);
        final ClientNonPersonData nonPerson = new ClientNonPersonData(CodeValueData.instance(4L, "Company"), "INC-1",
                LocalDate.of(2027, 3, 7), null, null);
        return ClientData.instance("000000001", new EnumOptionData(300L, "clientStatusType.active", "Active"), null, 1L, "Head Office",
                null, null, 7L, null, null, null, "Acme Ltd", "Acme Ltd", externalId, null, null, null, null, ACTIVATED, null, null, null,
                timeline, null, null, null, null, null, new EnumOptionData(2L, "legalFormType.entity", "ENTITY"), nonPerson, false);
    }

    /** The response body as Jersey writes it, read back as a tree. */
    private JsonNode json(final Object value) throws Exception {
        return jackson.readTree(jackson.writeValueAsString(value));
    }

    private void assertSameAsGson(final Object value) throws Exception {
        final JsonNode fromGson = jackson.readTree(gson.toJson(value));
        final JsonNode fromJackson = json(value);
        assertEquals(fromGson, fromJackson, () -> "GSON wrote " + fromGson + ", Jackson writes " + fromJackson);
    }
}
