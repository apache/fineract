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

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.TellerCashManagementApi.RetrieveAllCashiersForTellerQueryParams;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CashierData;
import org.apache.fineract.client.models.GetTellersTellerIdCashiersCashiersIdTransactionsResponse;
import org.apache.fineract.client.models.PostTellersRequest;
import org.apache.fineract.client.models.PostTellersResponse;
import org.apache.fineract.client.models.PostTellersTellerIdCashiersCashierIdAllocateRequest;
import org.apache.fineract.client.models.PostTellersTellerIdCashiersCashierIdAllocateResponse;
import org.apache.fineract.client.models.PostTellersTellerIdCashiersRequest;
import org.apache.fineract.integrationtests.client.feign.modules.FeignTestConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.organisation.teller.domain.TellerStatus;

public class FeignTellerHelper {

    private static final LocalDate TELLER_START_DATE = LocalDate.of(2011, 9, 20);
    private static final LocalDate CASHIER_START_DATE = LocalDate.of(2023, 1, 1);
    private static final LocalDate CASHIER_END_DATE = LocalDate.of(2023, 12, 31);
    public static final LocalDate ALLOCATION_DATE = LocalDate.of(2023, 1, 1);
    public static final String ALLOCATION_CURRENCY = "USD";

    private final FineractFeignClient fineractClient;
    private final TellerCommandsApi tellerCommandsApi;

    public FeignTellerHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.tellerCommandsApi = fineractClient.create(TellerCommandsApi.class);
    }

    public PostTellersResponse createTeller() {
        PostTellersRequest request = new PostTellersRequest()//
                .officeId(FeignOfficeHelper.HEAD_OFFICE_ID)//
                .name(Utils.uniqueRandomStringGenerator("Teller 1", 5))//
                .description(Utils.uniqueRandomStringGenerator("Teller For Testing", 4))//
                .status(TellerStatus.ACTIVE.getValue())//
                .startDate(TELLER_START_DATE)//
                .dateFormat(FeignTestConstants.ISO_DATE_PATTERN)//
                .locale(FeignTestConstants.LOCALE);
        return ok(() -> fineractClient.tellerCashManagement().createTeller(request));
    }

    /**
     * Creates a full-day cashier and answers its id. The create response names only the teller, so the new cashier is
     * found among the teller's cashiers by its unique description.
     */
    public Long createCashier(Long tellerId, Long staffId) {
        String description = Utils.uniqueRandomStringGenerator("test__", 4);
        PostTellersTellerIdCashiersRequest request = new PostTellersTellerIdCashiersRequest()//
                .staffId(staffId)//
                .description(description)//
                .isFullDay(true)//
                .startDate(CASHIER_START_DATE)//
                .endDate(CASHIER_END_DATE)//
                .dateFormat(FeignTestConstants.ISO_DATE_PATTERN)//
                .locale(FeignTestConstants.LOCALE);
        ok(() -> fineractClient.tellerCashManagement().createCashierForTeller(tellerId, request));
        return ok(() -> fineractClient.tellerCashManagement().retrieveAllCashiersForTeller(tellerId,
                new RetrieveAllCashiersForTellerQueryParams())).getCashiers().stream()
                .filter(cashier -> description.equals(cashier.getDescription())).findFirst().map(CashierData::getId)
                .orElseThrow(() -> new IllegalStateException("No cashier with description " + description + " on teller " + tellerId));
    }

    public static PostTellersTellerIdCashiersCashierIdAllocateRequest allocateCashRequest(BigDecimal amount) {
        return new PostTellersTellerIdCashiersCashierIdAllocateRequest()//
                .txnDate(ALLOCATION_DATE)//
                .currencyCode(ALLOCATION_CURRENCY)//
                .txnAmount(amount)//
                .txnNote(Utils.uniqueRandomStringGenerator("Allocate cash ", 4))//
                .dateFormat(FeignTestConstants.ISO_DATE_PATTERN)//
                .locale(FeignTestConstants.LOCALE);
    }

    public PostTellersTellerIdCashiersCashierIdAllocateResponse allocateCash(Long tellerId, Long cashierId,
            PostTellersTellerIdCashiersCashierIdAllocateRequest request) {
        return ok(() -> fineractClient.tellerCashManagement().allocateCashToCashier(tellerId, cashierId, request));
    }

    public CallFailedRuntimeException allocateCashWithTextAmountExpectingError(Long tellerId, Long cashierId, String txnAmount) {
        TellerCommandsApi.TextAmountAllocation request = new TellerCommandsApi.TextAmountAllocation(FeignTestConstants.LOCALE,
                FeignTestConstants.ISO_DATE_PATTERN, ALLOCATION_DATE.toString(), ALLOCATION_CURRENCY, txnAmount,
                Utils.uniqueRandomStringGenerator("Allocate cash ", 4));
        return fail(() -> tellerCommandsApi.allocateCashWithTextAmount(tellerId, cashierId, request));
    }

    public CallFailedRuntimeException allocateCashWithBodyExpectingError(Long tellerId, Long cashierId, String body) {
        return fail(() -> tellerCommandsApi.allocateCashWithBody(tellerId, cashierId, body));
    }

    public GetTellersTellerIdCashiersCashiersIdTransactionsResponse retrieveCashierTransactions(Long tellerId, Long cashierId,
            String currencyCode, int offset, int limit) {
        return ok(() -> fineractClient.tellerCashManagement().retrieveCashierTransactions(tellerId, cashierId, currencyCode, offset, limit,
                null, null));
    }
}
