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
import org.apache.fineract.client.models.PostAccountsTypeAccountIdResponse;

/**
 * Feign interface for the one share-account command whose body the generated client cannot express. It binds to the
 * generated response model, so the call stays typed end to end and does not build a JSON body by hand.
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
     * would break the approve path instead, so this one operation gets its own request model.
     */
    @RequestLine("POST /v1/accounts/{type}/{accountId}?command=redeemshares")
    PostAccountsTypeAccountIdResponse redeemShares(@Param("type") String type, @Param("accountId") Long accountId,
            RedeemSharesRequest request);

    /** Request body for {@link #redeemShares}, with {@code requestedShares} as the share count the command reads. */
    class RedeemSharesRequest {

        private String requestedDate;
        private String dateFormat;
        private String locale;
        private Long requestedShares;

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
    }
}
