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
package org.apache.fineract.investor.service;

import org.apache.fineract.investor.data.ExternalAssetOwnerCreateResponse;
import org.apache.fineract.investor.data.ExternalAssetOwnerTransferResponse;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerBuybackRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerCancelRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerCreateRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerIntermediarySaleRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerSaleRequest;

public interface ExternalAssetOwnersWriteService {

    ExternalAssetOwnerTransferResponse saleLoan(ExternalAssetOwnerSaleRequest request);

    ExternalAssetOwnerTransferResponse intermediarySaleLoan(ExternalAssetOwnerIntermediarySaleRequest request);

    ExternalAssetOwnerTransferResponse buybackLoan(ExternalAssetOwnerBuybackRequest request);

    ExternalAssetOwnerTransferResponse cancelTransfer(ExternalAssetOwnerCancelRequest request);

    ExternalAssetOwnerCreateResponse createOwner(ExternalAssetOwnerCreateRequest request);
}
