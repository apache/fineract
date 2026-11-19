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

import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeCreateRequest;
import org.apache.fineract.client.models.ChargeCreateResponse;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeDeleteResponse;
import org.apache.fineract.client.models.ChargeUpdateRequest;
import org.apache.fineract.client.models.ChargeUpdateResponse;
import org.apache.fineract.client.models.DeleteClientsClientIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetClientsChargesPageItems;
import org.apache.fineract.client.models.GetClientsClientIdChargesResponse;
import org.apache.fineract.client.models.PostClientsClientIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostClientsClientIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostClientsClientIdChargesRequest;
import org.apache.fineract.client.models.PostClientsClientIdChargesResponse;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;

public class FeignChargesHelper {

    private static final String PAY_COMMAND = "paycharge";
    private static final String WAIVE_COMMAND = "waive";

    private final FineractFeignClient fineractClient;
    private final ChargeTemplateApi chargeTemplateApi;

    public FeignChargesHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.chargeTemplateApi = fineractClient.create(ChargeTemplateApi.class);
    }

    public ChargeCreateResponse createCharge(ChargeCreateRequest request) {
        return ok(() -> fineractClient.charges().createCharge(request));
    }

    public List<ChargeData> getAllCharges() {
        return ok(() -> fineractClient.charges().retrieveAllCharges());
    }

    public ChargeData getCharge(Long chargeId) {
        return ok(() -> fineractClient.charges().retrieveOneCharge(chargeId, (Boolean) null));
    }

    public ChargeUpdateResponse updateCharge(Long chargeId, ChargeUpdateRequest request) {
        return ok(() -> fineractClient.charges().updateCharge(chargeId, request));
    }

    public ChargeDeleteResponse deleteCharge(Long chargeId) {
        return ok(() -> fineractClient.charges().deleteCharge(chargeId));
    }

    public CallFailedRuntimeException getChargeExpectingError(Long chargeId) {
        return fail(() -> fineractClient.charges().retrieveOneCharge(chargeId, (Boolean) null));
    }

    public CallFailedRuntimeException createChargeExpectingError(ChargeCreateRequest request) {
        return fail(() -> fineractClient.charges().createCharge(request));
    }

    public CallFailedRuntimeException updateChargeExpectingError(Long chargeId, ChargeUpdateRequest request) {
        return fail(() -> fineractClient.charges().updateCharge(chargeId, request));
    }

    public ChargeData getChargeTemplate(Long chargeAppliesTo, Long chargeTimeType) {
        return ok(() -> fineractClient.charges().retrieveTemplateCharge(chargeAppliesTo, chargeTimeType));
    }

    public ChargeData getChargeWithTemplate(Long chargeId) {
        return ok(() -> chargeTemplateApi.retrieveChargeWithTemplate(chargeId));
    }

    public ChargeCreateResponse createLoanSpecifiedDueDateCharge(double amount) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(amount));
    }

    public ChargeCreateResponse createLoanSpecifiedDueDateCharge(double amount, String currencyCode) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(amount, currencyCode));
    }

    public ChargeCreateResponse createLoanDisbursementCharge(double amount) {
        return createCharge(ChargeRequestBuilders.loanDisbursementFee(amount));
    }

    public ChargeCreateResponse createLoanSpecifiedDueDatePenalty(double amount) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDatePenalty(amount));
    }

    public ChargeCreateResponse createLoanSpecifiedDueDatePercentageOfInterestFee(double percentage) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDatePercentageOfInterestFee(percentage));
    }

    public ChargeCreateResponse createLoanOverdueFeePercentageOfAmountAndInterest(double percentage) {
        return createCharge(ChargeRequestBuilders.loanOverdueFeePercentageOfAmountAndInterest(percentage));
    }

    public ChargeCreateResponse createLoanDisbursementCharge(ChargeCalculationType chargeCalculationType, double amount) {
        return createCharge(ChargeRequestBuilders.loanDisbursementCharge(chargeCalculationType, amount));
    }

    public ChargeCreateResponse createLoanSpecifiedDueDateCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDateCharge(chargeCalculationType, amount, penalty));
    }

    public ChargeCreateResponse createLoanInstallmentCharge(ChargeCalculationType chargeCalculationType, double amount, boolean penalty) {
        return createCharge(ChargeRequestBuilders.loanInstallmentCharge(chargeCalculationType, amount, penalty));
    }

    public ChargeCreateResponse createLoanSpecifiedDueDateAccountTransferCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDateAccountTransferCharge(chargeCalculationType, amount, penalty));
    }

    public ChargeCreateResponse createLoanInstallmentAccountTransferCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return createCharge(ChargeRequestBuilders.loanInstallmentAccountTransferCharge(chargeCalculationType, amount, penalty));
    }

    public ChargeCreateResponse createClientSpecifiedDueDateCharge(double amount) {
        return createCharge(ChargeRequestBuilders.clientSpecifiedDueDateFee(amount));
    }

    public PostClientsClientIdChargesResponse addClientCharge(Long clientId, PostClientsClientIdChargesRequest request) {
        return ok(() -> fineractClient.clientCharges().createClientCharge(clientId, request));
    }

    public PostClientsClientIdChargesChargeIdResponse payClientCharge(Long clientId, Long chargeId,
            PostClientsClientIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.clientCharges().payOrWaiveClientCharge(clientId, chargeId, request, PAY_COMMAND));
    }

    public CallFailedRuntimeException addClientChargeExpectingError(Long clientId, PostClientsClientIdChargesRequest request) {
        return fail(() -> fineractClient.clientCharges().createClientCharge(clientId, request));
    }

    public CallFailedRuntimeException payClientChargeExpectingError(Long clientId, Long chargeId,
            PostClientsClientIdChargesChargeIdRequest request) {
        return fail(() -> fineractClient.clientCharges().payOrWaiveClientCharge(clientId, chargeId, request, PAY_COMMAND));
    }

    public PostClientsClientIdChargesChargeIdResponse waiveClientCharge(Long clientId, Long chargeId,
            PostClientsClientIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.clientCharges().payOrWaiveClientCharge(clientId, chargeId, request, WAIVE_COMMAND));
    }

    public GetClientsChargesPageItems getClientCharge(Long clientId, Long chargeId) {
        return ok(() -> fineractClient.clientCharges().retrieveOneClientCharge(clientId, chargeId));
    }

    public GetClientsClientIdChargesResponse getClientCharges(Long clientId) {
        return ok(() -> fineractClient.clientCharges().retrieveAllClientCharges(clientId, null, null, null, null));
    }

    public DeleteClientsClientIdChargesChargeIdResponse deleteClientCharge(Long clientId, Long chargeId) {
        return ok(() -> fineractClient.clientCharges().deleteClientCharge(clientId, chargeId));
    }
}
