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
package org.apache.fineract.integrationtests.common.xbrl;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.fineract.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class XBRLIntegrationTestHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String GET_TAXONOMY_LIST_URL = "/fineract-provider/api/v1/mixtaxonomy?" + Utils.TENANT_IDENTIFIER;
    private static final String TAXONOMY_MAPPING_URL = "/fineract-provider/api/v1/mixmapping?" + Utils.TENANT_IDENTIFIER;

    public XBRLIntegrationTestHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public ArrayList getTaxonomyList() {
        final ArrayList response = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_TAXONOMY_LIST_URL, "");

        return response;
    }

    public HashMap getTaxonomyMapping() {
        final HashMap response = Utils.performServerGet(this.requestSpec, this.responseSpec, TAXONOMY_MAPPING_URL, "config");
        return response;
    }

}
