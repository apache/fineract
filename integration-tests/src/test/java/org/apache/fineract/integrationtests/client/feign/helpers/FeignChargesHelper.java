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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.DeleteChargesChargeIdResponse;
import org.apache.fineract.client.models.DeleteClientsClientIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetChargesResponse;
import org.apache.fineract.client.models.GetClientsClientIdChargesResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsClientIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostClientsClientIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostClientsClientIdChargesRequest;
import org.apache.fineract.client.models.PostClientsClientIdChargesResponse;
import org.apache.fineract.client.models.PutChargesChargeIdResponse;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;

public class FeignChargesHelper {

    private static final String PAY_COMMAND = "paycharge";

    private final FineractFeignClient fineractClient;

    public FeignChargesHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostChargesResponse createCharge(ChargeRequest request) {
        return ok(() -> fineractClient.charges().createCharge(request));
    }

    public GetChargesResponse getCharge(Long chargeId) {
        return ok(() -> fineractClient.charges().retrieveOneCharge(chargeId));
    }

    public PutChargesChargeIdResponse updateCharge(Long chargeId, ChargeRequest request) {
        return ok(() -> fineractClient.charges().updateCharge(chargeId, request));
    }

    public DeleteChargesChargeIdResponse deleteCharge(Long chargeId) {
        return ok(() -> fineractClient.charges().deleteCharge(chargeId));
    }

    public CallFailedRuntimeException getChargeExpectingError(Long chargeId) {
        return fail(() -> fineractClient.charges().retrieveOneCharge(chargeId));
    }

    public PostChargesResponse createLoanSpecifiedDueDateCharge(double amount) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(amount));
    }

    public PostChargesResponse createLoanSpecifiedDueDateCharge(double amount, String currencyCode) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(amount, currencyCode));
    }

    public PostChargesResponse createLoanDisbursementCharge(double amount) {
        return createCharge(ChargeRequestBuilders.loanDisbursementFee(amount));
    }

    public PostChargesResponse createLoanSpecifiedDueDatePenalty(double amount) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDatePenalty(amount));
    }

    public PostChargesResponse createLoanSpecifiedDueDatePercentageOfInterestFee(double percentage) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDatePercentageOfInterestFee(percentage));
    }

    public PostChargesResponse createLoanOverdueFeePercentageOfAmountAndInterest(double percentage) {
        return createCharge(ChargeRequestBuilders.loanOverdueFeePercentageOfAmountAndInterest(percentage));
    }

    public PostChargesResponse createLoanDisbursementCharge(ChargeCalculationType chargeCalculationType, double amount) {
        return createCharge(ChargeRequestBuilders.loanDisbursementCharge(chargeCalculationType, amount));
    }

    public PostChargesResponse createLoanSpecifiedDueDateCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDateCharge(chargeCalculationType, amount, penalty));
    }

    public PostChargesResponse createLoanInstallmentCharge(ChargeCalculationType chargeCalculationType, double amount, boolean penalty) {
        return createCharge(ChargeRequestBuilders.loanInstallmentCharge(chargeCalculationType, amount, penalty));
    }

    public PostChargesResponse createLoanSpecifiedDueDateAccountTransferCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDateAccountTransferCharge(chargeCalculationType, amount, penalty));
    }

    public PostChargesResponse createLoanInstallmentAccountTransferCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return createCharge(ChargeRequestBuilders.loanInstallmentAccountTransferCharge(chargeCalculationType, amount, penalty));
    }

    public PostChargesResponse createClientSpecifiedDueDateCharge(double amount) {
        return createCharge(ChargeRequestBuilders.clientSpecifiedDueDateFee(amount));
    }

    public PostClientsClientIdChargesResponse addClientCharge(Long clientId, PostClientsClientIdChargesRequest request) {
        return ok(() -> fineractClient.clientCharges().createClientCharge(clientId, request));
    }

    public PostClientsClientIdChargesChargeIdResponse payClientCharge(Long clientId, Long chargeId,
            PostClientsClientIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.clientCharges().payOrWaiveClientCharge(clientId, chargeId, request, PAY_COMMAND));
    }

    public GetClientsClientIdChargesResponse getClientCharges(Long clientId) {
        return ok(() -> fineractClient.clientCharges().retrieveAllClientCharges(clientId, null, null, null, null));
    }

    public DeleteClientsClientIdChargesChargeIdResponse deleteClientCharge(Long clientId, Long chargeId) {
        return ok(() -> fineractClient.clientCharges().deleteClientCharge(clientId, chargeId));
    }
}
