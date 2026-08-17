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
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;

/**
 * Feign interface for the one loan-charge request shape the generated client cannot express. It binds to the generated
 * response model, so the call stays typed end to end and does not build a JSON body by hand.
 *
 * <p>
 * This is the only loan-charge call that needs it - everything else goes through the generated
 * {@code fineractClient.loanCharges()} API.
 */
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface LoanChargeCommandsApi {

    /**
     * Adds a loan charge whose {@code amount} is a locale-formatted decimal <em>string</em> (the German
     * {@code "50,05"}), which is what the server is being asked to parse here.
     *
     * <p>
     * {@code PostLoansLoanIdChargesRequest.amount} is a JSON number, so the generated model cannot carry a decimal
     * comma. Retyping it as a String at source is not the answer: that model is used by 118 call sites across
     * integration-tests, fineract-e2e-tests-core and fineract-e2e-tests-runner, all of which pass a number - the spec
     * would become a lie for every one of them to serve this single case. One schema cannot declare both "number" and
     * "locale-formatted string", so this operation gets its own request model.
     */
    @RequestLine("POST /v1/loans/{loanId}/charges")
    PostLoansLoanIdChargesResponse createLoanChargeWithLocaleFormattedAmount(@Param("loanId") Long loanId,
            LocaleFormattedAmountLoanChargeRequest request);

    /**
     * Request body for {@link #createLoanChargeWithLocaleFormattedAmount}. Mirrors the wire shape the pre-migration
     * test sent: both {@code amount} and {@code chargeId} are JSON strings, the former formatted for {@code locale}.
     */
    class LocaleFormattedAmountLoanChargeRequest {

        private String chargeId;
        private String amount;
        private String locale;
        private String dateFormat;

        public String getChargeId() {
            return chargeId;
        }

        public LocaleFormattedAmountLoanChargeRequest chargeId(String chargeId) {
            this.chargeId = chargeId;
            return this;
        }

        public String getAmount() {
            return amount;
        }

        public LocaleFormattedAmountLoanChargeRequest amount(String amount) {
            this.amount = amount;
            return this;
        }

        public String getLocale() {
            return locale;
        }

        public LocaleFormattedAmountLoanChargeRequest locale(String locale) {
            this.locale = locale;
            return this;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        public LocaleFormattedAmountLoanChargeRequest dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }
    }
}
