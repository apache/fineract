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

import com.fasterxml.jackson.annotation.JsonInclude;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.apache.fineract.client.models.PutClientsClientIdIdentifiersIdentifierIdResponse;

/**
 * Clearing a client identifier's issuance and expiry dates needs a body that carries explicit JSON {@code null}s:
 * {@code ClientIdentifierCommandFromApiJsonDeserializer} records whether each date key is <em>present</em>, and
 * {@code ClientIdentifierCommand.validateForUpdate} rejects an update in which no parameter is, with
 * {@code validation.msg.clientIdentifier.no.parameters.for.update}.
 *
 * <p>
 * The generated {@code ClientIdentifierRequest} cannot express that here: the mapper's global {@code NON_NULL} drops a
 * null property, so {@code .issuanceDate(null).expiryDate(null)} reaches the server as <code>{}</code>. Declaring the
 * dates {@code nullable} in the spec would model them as {@code JsonNullable<String>} for SDK consumers, but
 * {@code fineract-client} (Retrofit/Gson) and {@code fineract-client-feign} (Jackson) both generate into
 * {@code org.apache.fineract.client.models}, and {@code integration-tests} puts the Retrofit artifact first on the test
 * classpath, so the Retrofit model wins the name clash and the Jackson annotations never reach the wire. Measured: the
 * Feign SDK's own mapper writes <code>{"expiryDate":null,"issuanceDate":null}</code>, while the same call from the test
 * JVM is rejected as having no parameters.
 *
 * <p>
 * Same situation, same answer as {@link LoanProductCommandsApi}: the call stays typed end to end and binds the response
 * to the generated model.
 */
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface ClientIdentifierCommandsApi {

    @RequestLine("PUT /v1/clients/{clientId}/identifiers/{identifierId}")
    PutClientsClientIdIdentifiersIdentifierIdResponse clearIdentifierDates(@Param("clientId") Long clientId,
            @Param("identifierId") Long identifierId, ClearIdentifierDatesRequest request);

    /**
     * Body for {@link #clearIdentifierDates}: serialises to <code>{"issuanceDate":null,"expiryDate":null}</code>. The
     * property-level {@code @JsonInclude(ALWAYS)} is what overrides the mapper's global {@code NON_NULL}.
     */
    class ClearIdentifierDatesRequest {

        @JsonInclude(JsonInclude.Include.ALWAYS)
        private final String issuanceDate = null;
        @JsonInclude(JsonInclude.Include.ALWAYS)
        private final String expiryDate = null;

        public String getIssuanceDate() {
            return issuanceDate;
        }

        public String getExpiryDate() {
            return expiryDate;
        }
    }
}
