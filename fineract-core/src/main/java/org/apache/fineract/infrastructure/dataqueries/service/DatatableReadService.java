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
package org.apache.fineract.infrastructure.dataqueries.service;

import com.google.gson.JsonObject;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.service.PagedLocalRequest;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.portfolio.search.data.AdvancedQueryData;
import org.springframework.data.domain.Page;

public interface DatatableReadService {

    List<DatatableData> retrieveDatatableNames(String appTable);

    DatatableData retrieveDatatable(String datatable);

    List<JsonObject> queryDataTable(@NotNull String datatable, @NotNull String columnName, String columnValue,
            @NotNull String resultColumns);

    Page<JsonObject> queryDataTableAdvanced(@NotNull String datatable, @NotNull PagedLocalRequest<AdvancedQueryData> pagedRequest);

    boolean buildDataQueryEmbedded(@NotNull EntityTables entityTable, @NotNull String datatable, @NotNull AdvancedQueryData request,
            @NotNull List<String> selectColumns, @NotNull StringBuilder select, @NotNull StringBuilder from, @NotNull StringBuilder where,
            @NotNull List<Object> params, String mainAlias, String alias, String dateFormat, String dateTimeFormat, Locale locale);

    GenericResultsetData retrieveDataTableGenericResultSet(String datatable, Long appTableId, String order, Long id);

    Long countDatatableEntries(String datatableName, Long appTableId, String foreignKeyColumn);

    String getTableName(String Url);

    String getDataTableName(String Url);

}
