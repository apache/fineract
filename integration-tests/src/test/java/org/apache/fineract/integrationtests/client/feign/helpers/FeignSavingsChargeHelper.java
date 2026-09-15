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
import org.apache.fineract.client.models.DeleteSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesRequest;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesResponse;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse;
import org.apache.fineract.client.models.PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest;
import org.apache.fineract.client.models.PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;

public class FeignSavingsChargeHelper {

    private final FineractFeignClient fineractClient;

    public FeignSavingsChargeHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostChargesResponse createWithdrawalFeeCharge() {
        return createCharge(SavingsRequestBuilders.savingsWithdrawalFeeCharge());
    }

    public PostChargesResponse createCharge(ChargeRequest request) {
        return ok(() -> fineractClient.charges().createCharge(request));
    }

    public PostSavingsAccountsSavingsAccountIdChargesResponse addChargeToSavings(Long savingsId,
            PostSavingsAccountsSavingsAccountIdChargesRequest request) {
        return ok(() -> fineractClient.savingsCharges().createSavingsAccountCharge(savingsId, request));
    }

    public PostSavingsAccountsSavingsAccountIdChargesResponse addChargeToSavings(Long savingsId, Long chargeId, Float amount) {
        return addChargeToSavings(savingsId, SavingsRequestBuilders.savingsAccountCharge(chargeId, amount));
    }

    /** Pays a charge; the command takes the amount and the due date it is paid against. */
    public PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse payCharge(Long savingsId, Long savingsChargeId,
            String amount, String dueDate) {
        PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest request = new PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest()//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .amount(Float.valueOf(amount))//
                .dueDate(dueDate);
        return ok(
                () -> fineractClient.savingsCharges().handleCommandsSavingsAccountCharge(savingsId, savingsChargeId, request, "paycharge"));
    }

    public PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse waiveCharge(Long savingsId, Long savingsChargeId) {
        return chargeCommand(savingsId, savingsChargeId, "waive");
    }

    public PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse inactivateCharge(Long savingsId, Long savingsChargeId) {
        return chargeCommand(savingsId, savingsChargeId, "inactivate");
    }

    public CallFailedRuntimeException inactivateChargeExpectingError(Long savingsId, Long savingsChargeId) {
        PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest request = new PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest()//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN);
        return fail(() -> fineractClient.savingsCharges().handleCommandsSavingsAccountCharge(savingsId, savingsChargeId, request,
                "inactivate"));
    }

    public CallFailedRuntimeException payChargeExpectingError(Long savingsId, Long savingsChargeId, String amount, String dueDate) {
        PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest request = new PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest()//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .amount(Float.valueOf(amount))//
                .dueDate(dueDate);
        return fail(
                () -> fineractClient.savingsCharges().handleCommandsSavingsAccountCharge(savingsId, savingsChargeId, request, "paycharge"));
    }

    public DeleteSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse deleteCharge(Long savingsId, Long savingsChargeId) {
        return ok(() -> fineractClient.savingsCharges().deleteSavingsAccountCharge(savingsId, savingsChargeId));
    }

    public PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse updateCharge(Long savingsId, Long savingsChargeId,
            String amount) {
        PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest request = new PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest()//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .amount(Float.valueOf(amount));
        return ok(() -> fineractClient.savingsCharges().updateSavingsAccountCharge(savingsId, savingsChargeId, request));
    }

    /** Attaches a charge that recurs on a fixed day, which needs the month-day pair alongside the due date. */
    public PostSavingsAccountsSavingsAccountIdChargesResponse addChargeWithDueDateAndFeeOnMonthDay(Long savingsId, Long chargeId,
            String dueDate, String amount, String feeOnMonthDay) {
        PostSavingsAccountsSavingsAccountIdChargesRequest request = new PostSavingsAccountsSavingsAccountIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(Float.valueOf(amount))//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .dueDate(dueDate)//
                .monthDayFormat(DepositTestData.MONTH_DAY_FORMAT)//
                .feeOnMonthDay(feeOnMonthDay);
        return ok(() -> fineractClient.savingsCharges().createSavingsAccountCharge(savingsId, request));
    }

    /** The same recurring charge without a due date; the server then derives the first one from the month-day pair. */
    public PostSavingsAccountsSavingsAccountIdChargesResponse addChargeWithFeeOnMonthDay(Long savingsId, Long chargeId, String amount,
            String feeOnMonthDay) {
        PostSavingsAccountsSavingsAccountIdChargesRequest request = new PostSavingsAccountsSavingsAccountIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(Float.valueOf(amount))//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .monthDayFormat(DepositTestData.MONTH_DAY_FORMAT)//
                .feeOnMonthDay(feeOnMonthDay);
        return ok(() -> fineractClient.savingsCharges().createSavingsAccountCharge(savingsId, request));
    }

    public PostSavingsAccountsSavingsAccountIdChargesResponse addChargeWithDueDate(Long savingsId, Long chargeId, String dueDate,
            String amount) {
        PostSavingsAccountsSavingsAccountIdChargesRequest request = new PostSavingsAccountsSavingsAccountIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(Float.valueOf(amount))//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .dueDate(dueDate);
        return ok(() -> fineractClient.savingsCharges().createSavingsAccountCharge(savingsId, request));
    }

    private PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse chargeCommand(Long savingsId, Long savingsChargeId,
            String command) {
        PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest request = new PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest()//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN);
        return ok(() -> fineractClient.savingsCharges().handleCommandsSavingsAccountCharge(savingsId, savingsChargeId, request, command));
    }
}
