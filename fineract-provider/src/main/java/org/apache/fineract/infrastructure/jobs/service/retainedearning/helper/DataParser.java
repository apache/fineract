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
package org.apache.fineract.infrastructure.jobs.service.retainedearning.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.model.AccountGLJournalEntryAnnualSummaryRecord;
import org.springframework.stereotype.Component;

@Component
public class DataParser {

    /**
     * Object mapper for JSON parsing.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse the JSON string into a list of AccountGLJournalEntryAnnualSummaryRecord.
     *
     * @param json
     * @return
     * @throws Exception
     */
    public List<AccountGLJournalEntryAnnualSummaryRecord> parse(final String json) throws Exception {
        final JsonNode root = objectMapper.readTree(json);

        // Get column names in order
        final List<String> columns = new ArrayList<>();
        columns.addAll(StreamSupport.stream(root.path("columnHeaders").spliterator(), false)
                .map(header -> header.path("columnName").asText()).collect(Collectors.toList()));

        final List<AccountGLJournalEntryAnnualSummaryRecord> records = StreamSupport.stream(root.path("data").spliterator(), false)
                .map(data -> {
                    JsonNode row = data.path("row");

                    // Create row dataMap Map<columnName, value>
                    Map<String, String> rowData = IntStream.range(0, Math.min(columns.size(), row.size())).boxed()
                            .collect(Collectors.toMap(i -> columns.get(i), i -> row.get(i).asText()));

                    // Build record
                    return AccountGLJournalEntryAnnualSummaryRecord.builder().postingDate(rowData.get("postingdate"))
                            .product(rowData.get("product")).glAcct(rowData.get("glacct"))
                            .assetOwner(ExternalIdFactory.produce(rowData.get("assetowner")))
                            .endingBalance(new BigDecimal(rowData.getOrDefault("endingbalance", "0"))).build();
                }).collect(Collectors.toList());

        return records;
    }
}
