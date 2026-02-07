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
package org.apache.fineract.integrationtests.investor;

import java.util.List;
import org.apache.fineract.client.models.ExternalTransferOwnerData;
import org.apache.fineract.client.models.PostExternalAssetOwnerRequest;
import org.apache.fineract.client.models.PostExternalAssetOwnerResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.FineractClientHelper;

public final class InvestorHelper {

    private InvestorHelper() {

    }

    public static List<ExternalTransferOwnerData> retrieveExternalAssetOwners() {
        return Calls.ok(FineractClientHelper.getFineractClient().externalAssetOwners.retrieveExternalAssetOwners());
    }

    public static PostExternalAssetOwnerResponse createExternalAssetOwner(final PostExternalAssetOwnerRequest request) {
        return Calls.ok(FineractClientHelper.getFineractClient().externalAssetOwners.createExternalAssetOwner(request));
    }
}
