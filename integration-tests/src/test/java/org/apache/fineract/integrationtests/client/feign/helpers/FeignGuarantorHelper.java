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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.GuarantorData;
import org.apache.fineract.client.models.GuarantorsRequest;

/**
 * Feign counterpart of the legacy {@code GuarantorHelper}: the guarantors attached to a loan account.
 */
public class FeignGuarantorHelper {

    private final FineractFeignClient fineractClient;

    public FeignGuarantorHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long createGuarantor(Long loanId, GuarantorsRequest request) {
        return ok(() -> fineractClient.guarantors().createGuarantor(loanId, request)).getResourceId();
    }

    public List<GuarantorData> getAllGuarantors(Long loanId) {
        return ok(() -> fineractClient.guarantors().retrieveGuarantorDetails(loanId));
    }

    public GuarantorData getGuarantor(Long loanId, Long guarantorId) {
        return ok(() -> fineractClient.guarantors().retrieveGuarantorDetails1(loanId, guarantorId));
    }

    public CommandProcessingResult deleteGuarantor(Long loanId, Long guarantorId) {
        return ok(() -> fineractClient.guarantors().deleteGuarantorUniversal(loanId, guarantorId, Map.of()));
    }

    /** Removes a single guarantee (funding) from a guarantor, leaving the guarantor itself in place. */
    public CommandProcessingResult deleteGuarantor(Long loanId, Long guarantorId, Long guarantorFundingId) {
        return ok(() -> fineractClient.guarantors().deleteGuarantor(loanId, guarantorId, guarantorFundingId));
    }
}
