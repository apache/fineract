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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DeleteDataTablesDatatableAppTableIdResponse;
import org.apache.fineract.client.models.DeleteDataTablesResponse;
import org.apache.fineract.client.models.PostDataTablesAppTableIdResponse;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;

public class FeignDatatableHelper {

    private final FineractFeignClient fineractClient;

    public FeignDatatableHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostDataTablesResponse createDatatable(PostDataTablesRequest request) {
        return ok(() -> fineractClient.dataTables().createDatatable(request));
    }

    /**
     * Creates one entry in the given datatable for the given application-table row. The entry columns are datatable
     * specific, so the payload is passed as raw JSON.
     */
    public PostDataTablesAppTableIdResponse createDatatableEntry(String datatableName, Long apptableId, String entryJson) {
        return ok(() -> fineractClient.dataTables().createDatatableEntry(datatableName, apptableId, entryJson));
    }

    /** Deletes every entry the given datatable holds for the given application-table row. */
    public DeleteDataTablesDatatableAppTableIdResponse deleteDatatableEntries(String datatableName, Long apptableId) {
        return ok(() -> fineractClient.dataTables().deleteDatatableEntries(datatableName, apptableId));
    }

    /** Drops the datatable itself. The server rejects this while the datatable still holds entries. */
    public DeleteDataTablesResponse deleteDatatable(String datatableName) {
        return ok(() -> fineractClient.dataTables().deleteDatatable(datatableName));
    }
}
