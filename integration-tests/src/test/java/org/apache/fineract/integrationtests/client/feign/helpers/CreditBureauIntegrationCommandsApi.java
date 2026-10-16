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

import com.fasterxml.jackson.annotation.JsonProperty;
import feign.Headers;
import feign.RequestLine;
import org.apache.fineract.client.models.CommandProcessingResult;

/**
 * The credit report request names the borrower's national id {@code NRC}. In {@code integration-tests} the generated
 * {@code PostCreditReportRequest} resolves to the Retrofit model, which carries that name only in a Gson annotation, so
 * Jackson writes the field as {@code nrc}; the server then reads no national id and calls the bureau's search URL
 * without one.
 */
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface CreditBureauIntegrationCommandsApi {

    @RequestLine("POST /v1/creditBureauIntegration/creditReport")
    CommandProcessingResult fetchCreditReport(CreditReportRequest request);

    record CreditReportRequest(@JsonProperty("creditBureauID") String creditBureauId, @JsonProperty("NRC") String nationalId) {
    }
}
