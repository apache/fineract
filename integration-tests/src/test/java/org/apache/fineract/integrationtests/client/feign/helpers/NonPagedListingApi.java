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

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import org.apache.fineract.client.models.GetCentersPageItems;
import org.apache.fineract.client.models.GetGroupsPageItems;

/**
 * Feign interface for the non-paged variants of {@code GET /centers} and {@code GET /groups}, which answer a bare JSON
 * array although the endpoints declare the paged object as their only OpenAPI response schema. Asking for
 * {@code paged=true} to get the generated model instead is not equivalent: the paged branch ignores
 * {@code orphansOnly}. Check CentersApiResource#retrieveAll and GroupsApiResource#retrieveAll.
 */
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface NonPagedListingApi {

    @RequestLine("GET /v1/centers?limit=-1")
    List<GetCentersPageItems> listCenters();

    @RequestLine("GET /v1/centers?limit=-1&orderBy=id&sortOrder=asc")
    List<GetCentersPageItems> listCentersOrdered();

    @RequestLine("GET /v1/groups?officeId={officeId}&orphansOnly=true")
    List<GetGroupsPageItems> listOrphanGroups(@Param("officeId") Long officeId);
}
