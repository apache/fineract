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

import com.fasterxml.jackson.annotation.JsonInclude;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdResponse;

/**
 * Loan application modifications whose bodies the generated model cannot produce: an explicit JSON null (dropped by the
 * mapper's global {@code NON_NULL}) or a blank frequency type (not a value of the generated enum).
 */
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface WorkingCapitalLoanCommandsApi {

    @RequestLine("PUT /v1/working-capital-loans/{loanId}")
    // Object: the Jackson encoder serialises the declared type and would drop the subclass fields
    PutWorkingCapitalLoansLoanIdResponse modifyApplication(@Param("loanId") Long loanId, Object request);

    abstract class LocalizedRequest {

        public String getLocale() {
            return "en";
        }

        public String getDateFormat() {
            return "dd MMMM yyyy";
        }
    }

    /** Serialises to {@code {"repaymentEvery":null,"repaymentFrequencyType":"DAYS",...}}. */
    class NullRepaymentEveryRequest extends LocalizedRequest {

        @JsonInclude(JsonInclude.Include.ALWAYS)
        private final Integer repaymentEvery = null;

        public Integer getRepaymentEvery() {
            return repaymentEvery;
        }

        public String getRepaymentFrequencyType() {
            return "DAYS";
        }
    }

    /** Serialises to {@code {"repaymentEvery":1,"repaymentFrequencyType":"",...}}. */
    class BlankRepaymentFrequencyTypeRequest extends LocalizedRequest {

        public Integer getRepaymentEvery() {
            return 1;
        }

        public String getRepaymentFrequencyType() {
            return "";
        }
    }
}
