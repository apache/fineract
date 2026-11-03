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

package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesAppTableIdResponse;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignShareAccountHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

public class ShareProductDatatableIntegrationTest {

    private static final String SHARES_APP_TABLE_NAME = "m_share_product";
    private final FeignDatatableHelper datatableHelper = new FeignDatatableHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignShareAccountHelper shareAccountHelper = new FeignShareAccountHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testDatatableCreationForShareProduct() {
        // create dataTable
        String datatableName = Utils.uniqueRandomStringGenerator("dt_" + SHARES_APP_TABLE_NAME + "_", 5).toLowerCase().toLowerCase();
        String column1Name = "aNumber";
        String column2Name = "aString";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(SHARES_APP_TABLE_NAME);
        request.setMultiRow(false);

        PostColumnHeaderData column1HeaderRequestData = new PostColumnHeaderData();
        column1HeaderRequestData.setName(column1Name);
        column1HeaderRequestData.setType("Number");
        column1HeaderRequestData.setMandatory(false);
        column1HeaderRequestData.setLength(10L);
        column1HeaderRequestData.setCode("");
        column1HeaderRequestData.setUnique(false);
        column1HeaderRequestData.setIndexed(false);

        request.addColumnsItem(column1HeaderRequestData);

        PostColumnHeaderData column2HeaderRequestData = new PostColumnHeaderData();
        column2HeaderRequestData.setName(column2Name);
        column2HeaderRequestData.setType("String");
        column2HeaderRequestData.setMandatory(false);
        column2HeaderRequestData.setLength(10L);
        column2HeaderRequestData.setCode("");
        column2HeaderRequestData.setUnique(false);
        column2HeaderRequestData.setIndexed(false);

        request.addColumnsItem(column2HeaderRequestData);

        PostDataTablesResponse response = datatableHelper.createDatatable(request);

        assertNotNull(response.getResourceIdentifier());

        // verify Datatable got created
        GetDataTablesResponse dataTable = datatableHelper.getDatatable(datatableName);

        // verfify columns
        List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertNotNull(columnHeaderData);

        // two columns with 1 primary key and 2 audit columns created
        assertEquals(columnHeaderData.size(), 5);
    }

    @Test
    public void testDatatableEntryForShareProduct() {
        // create Shares Product
        final Long shareProductId = shareAccountHelper.createShareProduct(FeignShareAccountHelper.defaultShareProduct());

        assertNotNull(shareProductId);

        // create dataTable
        String datatableName = Utils.uniqueRandomStringGenerator("dt_" + SHARES_APP_TABLE_NAME + "_", 5).toLowerCase().toLowerCase();
        String column1Name = "aNumber";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(SHARES_APP_TABLE_NAME);
        request.setMultiRow(true);

        PostColumnHeaderData column1HeaderRequestData = new PostColumnHeaderData();
        column1HeaderRequestData.setName(column1Name);
        column1HeaderRequestData.setType("Number");
        column1HeaderRequestData.setMandatory(false);
        column1HeaderRequestData.setLength(10L);
        column1HeaderRequestData.setCode("");
        column1HeaderRequestData.setUnique(false);
        column1HeaderRequestData.setIndexed(false);

        request.addColumnsItem(column1HeaderRequestData);

        PostDataTablesResponse response = datatableHelper.createDatatable(request);

        assertNotNull(response);

        String datatableId = response.getResourceIdentifier();

        // add entries
        final HashMap<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put(column1Name, Utils.randomNumberGenerator(5));
        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        PostDataTablesAppTableIdResponse datatableEntryResponseFirst = datatableHelper.createDatatableEntry(datatableId, shareProductId,
                datatableEntryMap);
        PostDataTablesAppTableIdResponse datatableEntryResponseSecond = datatableHelper.createDatatableEntry(datatableId, shareProductId,
                datatableEntryMap);

        assertNotNull(datatableEntryResponseFirst.getResourceId());
        assertNotNull(datatableEntryResponseSecond.getResourceId());

        // Read the Datatable entry generated with genericResultSet
        JsonNode items = datatableHelper.getDatatableEntries(datatableId, shareProductId);
        assertNotNull(items);
        assertEquals(2, items.get("data").size());
    }
}
