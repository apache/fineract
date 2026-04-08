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
package org.apache.fineract.batch.command.internal;

import static org.apache.fineract.batch.command.CommandStrategyUtils.relativeUrlWithoutVersion;

import jakarta.ws.rs.core.UriInfo;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.command.CommandStrategyUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.api.MutableUriInfo;
import org.apache.fineract.infrastructure.dataqueries.api.DatatablesApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} and get datatable by appTableId. It passes the contents of the body from the
 * BatchRequest to {@link DatatablesApiResource} and gets back the response. This class will also catch any errors
 * raised by {@link DatatablesApiResource} and map those errors to appropriate status codes in BatchResponse.
 */
@Component
@RequiredArgsConstructor
public class GetDatatableEntryByAppTableIdCommandStrategy implements CommandStrategy {

    /**
     * Data table api resource {@link DatatablesApiResource}.
     */
    private final DatatablesApiResource dataTablesApiResource;

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {
        final MutableUriInfo parameterizedUriInfo = new MutableUriInfo(uriInfo);
        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);
        final String relativeUrlSubString = StringUtils.substringAfter(relativeUrl, "/");

        // uriInfo will contain the query parameter value(s) that are sent in the actual batch uri.
        // for example: batches?enclosingTransaction=true
        // But the query parameters that are sent in the batch relative url has to be sent to
        // datatablesApiResource.getDatatable
        // To use the relative url query parameters
        // - Parse and fetch the query parameters sent in the relative url
        // (datatables/dt_123/1?genericResultSet=true)
        // - Add them to the MutableUriInfo query parameters list
        // - Call datatablesApiResource.getDatatable(dataTable, appTableId, null, uriInfo)
        long appTableId;
        if (relativeUrl.indexOf('?') > 0) {
            appTableId = Long.parseLong(StringUtils.substringBetween(relativeUrlSubString, "/", "?"));
            Map<String, String> queryParameters = CommandStrategyUtils.getQueryParameters(relativeUrl);

            // Add the query parameters sent in the relative URL to MutableUriInfo
            CommandStrategyUtils.addQueryParametersToUriInfo(parameterizedUriInfo, queryParameters);
        } else {
            appTableId = Long.parseLong(StringUtils.substringAfter(relativeUrlSubString, "/"));
        }

        String dataTableName = relativeUrlSubString.substring(0, relativeUrlSubString.indexOf("/"));

        // Calls 'getDatatable' function from 'DatatablesApiResource' to
        // get the datatable details based on the appTableId
        responseBody = dataTablesApiResource.getDatatable(dataTableName, appTableId, null, parameterizedUriInfo);

        response.setStatusCode(HttpStatus.SC_OK);

        // Sets the response after retrieving the datatable
        response.setBody(responseBody);

        return response;
    }
}
