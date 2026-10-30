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

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.Map;
import org.apache.fineract.client.models.PostAccountsTypeAccountIdResponse;
import org.apache.fineract.client.models.PutAccountsTypeAccountIdRequest;

/**
 * Feign interface for the share-account calls whose body the generated client cannot express or read. It binds to the
 * generated models wherever they fit, so no call builds a JSON body by hand.
 *
 * <p>
 * Every other share command goes through the generated {@code fineractClient.shareAccount()} API.
 */
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface ShareAccountCommandsApi {

    /**
     * Redeems shares from an active share account.
     *
     * <p>
     * All nine share commands share one request schema, {@code PostAccountsTypeAccountIdRequest}, and they disagree on
     * what {@code requestedShares} means. {@code ShareAccountDataSerializer.validateAndRedeemShares} reads it with
     * {@code extractLongNamed} - a share count - while {@code validateAndApproveAddtionalShares} reads the same key
     * with {@code extractJsonArrayNamed} and treats each entry as a purchased-share transaction id. The generated model
     * declares the array shape, so sending a count through it produces
     * {@code validation.msg.shareaccount.requestedshares.cannot.be.blank}. Retyping the field to a number at source
     * would break the approve path instead, so this operation and {@link #applyAdditionalShares} get their own request
     * model.
     */
    @RequestLine("POST /v1/accounts/{type}/{accountId}?command=redeemshares")
    PostAccountsTypeAccountIdResponse redeemShares(@Param("type") String type, @Param("accountId") Long accountId,
            RedeemSharesRequest request);

    /**
     * Applies for additional shares on an active share account. Like {@link #redeemShares}, the command reads
     * {@code requestedShares} as a share count, so it reuses {@link RedeemSharesRequest}.
     */
    @RequestLine("POST /v1/accounts/{type}/{accountId}?command=applyadditionalshares")
    PostAccountsTypeAccountIdResponse applyAdditionalShares(@Param("type") String type, @Param("accountId") Long accountId,
            RedeemSharesRequest request);

    /**
     * Modifies a share application. The generated {@code updateShareAccount} cannot read the response once
     * {@code requestedShares} changes: {@code ShareAccountDataSerializer.validateAndUpdate} reports that change as the
     * string {@code "Transaction"}, while the generated {@code PutAccountsChanges} declares an integer. This keeps the
     * generated request model and reads the response as a plain map.
     */
    @RequestLine("PUT /v1/accounts/{type}/{accountId}")
    Map<String, Object> updateShareAccount(@Param("type") String type, @Param("accountId") Long accountId,
            PutAccountsTypeAccountIdRequest request);

    /**
     * Request body for {@link #redeemShares} and {@link #applyAdditionalShares}, with {@code requestedShares} as the
     * share count the commands read, plus the optional payment detail fields.
     */
    class RedeemSharesRequest {

        private String requestedDate;
        private String dateFormat;
        private String locale;
        private Long requestedShares;
        private Long paymentTypeId;
        private String accountNumber;
        private String checkNumber;
        private String routingCode;
        private String receiptNumber;
        private String bankNumber;

        public String getRequestedDate() {
            return requestedDate;
        }

        public RedeemSharesRequest requestedDate(String requestedDate) {
            this.requestedDate = requestedDate;
            return this;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        public RedeemSharesRequest dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public String getLocale() {
            return locale;
        }

        public RedeemSharesRequest locale(String locale) {
            this.locale = locale;
            return this;
        }

        public Long getRequestedShares() {
            return requestedShares;
        }

        public RedeemSharesRequest requestedShares(Long requestedShares) {
            this.requestedShares = requestedShares;
            return this;
        }

        public Long getPaymentTypeId() {
            return paymentTypeId;
        }

        public RedeemSharesRequest paymentTypeId(Long paymentTypeId) {
            this.paymentTypeId = paymentTypeId;
            return this;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public RedeemSharesRequest accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public String getCheckNumber() {
            return checkNumber;
        }

        public RedeemSharesRequest checkNumber(String checkNumber) {
            this.checkNumber = checkNumber;
            return this;
        }

        public String getRoutingCode() {
            return routingCode;
        }

        public RedeemSharesRequest routingCode(String routingCode) {
            this.routingCode = routingCode;
            return this;
        }

        public String getReceiptNumber() {
            return receiptNumber;
        }

        public RedeemSharesRequest receiptNumber(String receiptNumber) {
            this.receiptNumber = receiptNumber;
            return this;
        }

        public String getBankNumber() {
            return bankNumber;
        }

        public RedeemSharesRequest bankNumber(String bankNumber) {
            this.bankNumber = bankNumber;
            return this;
        }
    }
}
