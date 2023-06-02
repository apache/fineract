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
package org.apache.fineract.integrationtests.common;

import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostInitiateTransferRequest;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.integrationtests.client.IntegrationTest;

public class ExternalAssetOwnerHelper extends IntegrationTest {

    public ExternalAssetOwnerHelper() {}

    public PostInitiateTransferResponse initiateTransferByLoanId(Long loanId, String command, PostInitiateTransferRequest request) {
        return ok(fineract().externalAssetOwners.transferRequestWithLoanId(loanId, request, command));
    }

    public PageExternalTransferData retrieveTransferByTransferExternalId(String transferExternalId) {
        return ok(fineract().externalAssetOwners.getTransfer1(transferExternalId, null, null, 0, 100));
    }

    public PageExternalTransferData retrieveTransferByLoanExternalId(String loanExternalId) {
        return ok(fineract().externalAssetOwners.getTransfer1(null, null, loanExternalId, 0, 100));
    }

    public PageExternalTransferData retrieveTransferByLoanId(Long loanId) {
        return ok(fineract().externalAssetOwners.getTransfer1(null, loanId, null, 0, 100));
    }

    public PageExternalTransferData retrieveTransferByLoanId(Long loanId, int offset, int limit) {
        return ok(fineract().externalAssetOwners.getTransfer1(null, loanId, null, offset, limit));
    }
}
