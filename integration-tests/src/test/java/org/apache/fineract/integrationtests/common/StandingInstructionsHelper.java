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
package org.apache.fineract.integrationtests.common;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.Set;
import org.apache.fineract.client.feign.services.StandingInstructionsHistoryApi.RetrieveAllStandingInstructionHistoryQueryParams;
import org.apache.fineract.client.models.GetStandingInstructionHistoryPageItemsResponse;
import org.apache.fineract.client.models.GetStandingInstructionRunHistoryResponse;
import org.apache.fineract.client.models.GetStandingInstructionsStandingInstructionIdResponse;
import org.apache.fineract.client.models.StandingInstructionCreateResponse;
import org.apache.fineract.client.models.StandingInstructionCreationRequest;

public class StandingInstructionsHelper {

    private static final String LOCALE = "en_GB";
    private static final Long OFFICE_ID = 1L;
    private static final Integer INSTRUCTION_TYPE_FIXED = 1;
    private static final Integer PRIORITY_URGENT = 1;
    private static final Integer RECURRENCE_FREQUENCY_WEEKS = 1;
    private static final Integer RECURRENCE_TYPE_PERIODIC = 1;
    private static final Integer STATUS_ACTIVE = 1;
    private static final Integer TRANSFER_TYPE_ACCOUNT_TRANSFER = 1;

    private Long officeId = OFFICE_ID;

    public StandingInstructionsHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {}

    public StandingInstructionCreationRequest build(final Long clientId, final Long fromAccountId, final Long toAccountId,
            final Integer fromAccountType, final Integer toAccountType, final String validFrom, final String validTo,
            final String monthDay) {
        return new StandingInstructionCreationRequest().name(Utils.uniqueRandomStringGenerator("STANDING_INSTRUCTION_", 5))
                .dateFormat("dd MMMM yyyy").monthDayFormat("dd MMMM").locale(LOCALE).fromClientId(clientId).fromAccountId(fromAccountId)
                .fromAccountType(fromAccountType).fromOfficeId(this.officeId).toClientId(clientId).toAccountId(toAccountId)
                .toAccountType(toAccountType).toOfficeId(this.officeId).amount(BigDecimal.valueOf(500))
                .transferType(TRANSFER_TYPE_ACCOUNT_TRANSFER).priority(PRIORITY_URGENT).status(STATUS_ACTIVE)
                .instructionType(INSTRUCTION_TYPE_FIXED).validFrom(validFrom).validTill(validTo).recurrenceType(RECURRENCE_TYPE_PERIODIC)
                .recurrenceInterval(1).recurrenceFrequency(RECURRENCE_FREQUENCY_WEEKS).recurrenceOnMonthDay(monthDay);
    }

    public Long createStandingInstruction(final Long clientId, final Long fromAccountId, final Long toAccountId,
            final Integer fromAccountType, final Integer toAccountType, final String validFrom, final String validTo,
            final String monthDay) {
        StandingInstructionCreateResponse response = ok(
                () -> FineractFeignClientHelper.getFineractFeignClient().standingInstructions().createStandingInstruction(
                        build(clientId, fromAccountId, toAccountId, fromAccountType, toAccountType, validFrom, validTo, monthDay)));
        return response.getResourceId();
    }

    public GetStandingInstructionsStandingInstructionIdResponse getStandingInstructionById(final Long standingInstructionId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().standingInstructions()
                .retrieveOneStandingInstruction(standingInstructionId, null, null, null, null, null));
    }

    public Set<GetStandingInstructionHistoryPageItemsResponse> getStandingInstructionHistory(Integer fromSavingsId, Integer fromAccountType,
            Integer fromClientId, Integer transferType) {
        RetrieveAllStandingInstructionHistoryQueryParams queryParams = new RetrieveAllStandingInstructionHistoryQueryParams()
                .fromAccountId(fromSavingsId.longValue()).fromAccountType(fromAccountType).clientId(fromClientId.longValue())
                .transferType(transferType);
        GetStandingInstructionRunHistoryResponse response = ok(() -> FineractFeignClientHelper.getFineractFeignClient()
                .standingInstructionsHistory().retrieveAllStandingInstructionHistory(queryParams));
        return response.getPageItems();
    }
}
