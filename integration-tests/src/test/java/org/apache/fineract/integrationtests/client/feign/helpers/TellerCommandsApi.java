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
import org.apache.fineract.client.models.PostTellersTellerIdCashiersCashierIdAllocateResponse;

/**
 * The allocate-cash validation tests send bodies the generated model cannot: a {@code txnAmount} that is not a number,
 * which {@code PostTellersTellerIdCashiersCashierIdAllocateRequest} types as {@code BigDecimal}, and a body that is not
 * valid JSON at all.
 */
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface TellerCommandsApi {

    @RequestLine("POST /v1/tellers/{tellerId}/cashiers/{cashierId}/allocate")
    PostTellersTellerIdCashiersCashierIdAllocateResponse allocateCashWithTextAmount(@Param("tellerId") Long tellerId,
            @Param("cashierId") Long cashierId, TextAmountAllocation request);

    @RequestLine("POST /v1/tellers/{tellerId}/cashiers/{cashierId}/allocate")
    PostTellersTellerIdCashiersCashierIdAllocateResponse allocateCashWithBody(@Param("tellerId") Long tellerId,
            @Param("cashierId") Long cashierId, String body);

    record TextAmountAllocation(String locale, String dateFormat, String txnDate, String currencyCode, String txnAmount, String txnNote) {
    }
}
