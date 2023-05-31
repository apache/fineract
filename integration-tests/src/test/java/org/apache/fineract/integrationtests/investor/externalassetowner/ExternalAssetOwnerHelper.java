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
package org.apache.fineract.integrationtests.investor.externalassetowner;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.Utils;

public class ExternalAssetOwnerHelper extends IntegrationTest {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public ExternalAssetOwnerHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public String initiateTransferByLoanId(Long loanId, String command, String json) {
        final String INITIATE_TRANSFER_URL = "/fineract-provider/api/v1/external-asset-owners/transfers/loans/" + loanId + "?"
                + Utils.TENANT_IDENTIFIER + "&command=" + command;
        return Utils.performServerPost(requestSpec, responseSpec, INITIATE_TRANSFER_URL, json);
    }

    public String initiateTransferByLoanExternalId(String loanExternalId, String command, String json) {
        final String INITIATE_TRANSFER_URL = "/fineract-provider/api/v1/external-asset-owners/transfers/loans/external-id/" + loanExternalId
                + "?" + Utils.TENANT_IDENTIFIER + "&command=" + command;
        return Utils.performServerPost(requestSpec, responseSpec, INITIATE_TRANSFER_URL, json);
    }

    public String retrieveTransferByTransferExternalId(String transferExternalId) {
        final String RETRIEVE_TRANSFER_URL = "/fineract-provider/api/v1/external-asset-owners/transfers?" + Utils.TENANT_IDENTIFIER
                + "&transferExternalId=" + transferExternalId;
        return Utils.performServerGet(requestSpec, responseSpec, RETRIEVE_TRANSFER_URL);
    }

    public String retrieveTransferByLoanId(Long loanId) {
        final String RETRIEVE_TRANSFER_URL = "/fineract-provider/api/v1/external-asset-owners/transfers?" + Utils.TENANT_IDENTIFIER
                + "&loanId=" + loanId;
        return Utils.performServerGet(requestSpec, responseSpec, RETRIEVE_TRANSFER_URL);
    }
}
