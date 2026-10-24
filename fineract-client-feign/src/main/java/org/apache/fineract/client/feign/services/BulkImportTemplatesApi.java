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
package org.apache.fineract.client.feign.services;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import feign.Response;
import java.util.Map;
import org.apache.fineract.client.feign.FineractMultipartEncoder.MultipartData;
import org.apache.fineract.client.models.ApiResponse;

/**
 * Client API (Feign) for the bulk import workbooks: {@code /{entity}/downloadtemplate},
 * {@code /{entity}/uploadtemplate} and {@code /imports/downloadOutputTemplate}.
 *
 * This interface is hand-written, like {@link DocumentsApiFixed} and {@link ImagesApi}, because the generated
 * operations cannot carry a workbook (see
 * <a href="https://issues.apache.org/jira/browse/FINERACT-1227">FINERACT-1227</a>): the {@code downloadtemplate} and
 * {@code downloadOutputTemplate} operations are generated with a {@code void} return type, so the downloaded workbook
 * is discarded, and the {@code uploadtemplate} operations pass their multipart form fields as parameters that the
 * encoder sends as a JSON body.
 *
 * The {@code entityPath} is the resource path below {@code /v1}, for example {@code offices}, {@code loans} or
 * {@code savingsaccounts}.
 */
public interface BulkImportTemplatesApi {

    @RequestLine("GET /v1/{entityPath}/downloadtemplate")
    @Headers("Accept: application/vnd.ms-excel")
    Response downloadTemplate(@Param("entityPath") String entityPath, @QueryMap Map<String, Object> queryParams);

    @RequestLine("POST /v1/{entityPath}/uploadtemplate")
    ApiResponse<Long> uploadTemplateWithHttpInfo(@Param("entityPath") String entityPath, @QueryMap Map<String, Object> queryParams,
            MultipartData multipartData);

    @RequestLine("GET /v1/imports/downloadOutputTemplate?importDocumentId={importDocumentId}")
    @Headers("Accept: application/vnd.ms-excel")
    Response downloadOutputTemplate(@Param("importDocumentId") Long importDocumentId);
}
