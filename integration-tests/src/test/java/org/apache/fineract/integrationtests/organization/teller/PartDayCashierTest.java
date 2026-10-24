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
package org.apache.fineract.integrationtests.organization.teller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.CashierData;
import org.apache.fineract.client.models.PostTellersRequest;
import org.apache.fineract.client.models.PostTellersTellerIdCashiersRequest;
import org.apache.fineract.client.models.PutTellersTellerIdCashiersCashierIdRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Part-day cashiers (FINERACT-1821): their times used to be compared with MySQL's TIME() in SQL, so creating one failed
 * on PostgreSQL, and the overlap check let a shift inside another one through.
 */
public class PartDayCashierTest extends FeignIntegrationTest {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String ALREADY_ALLOCATED = "cashier.already.allocated.for.given.data.and.time.exception";
    private static final String END_BEFORE_START = "validation.msg.teller.hourEndTime.must.be.after.start.time";
    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 12, 31);

    private Long tellerId;
    private Long staffId;

    @BeforeEach
    public void setup() {
        staffId = new FeignStaffHelper(fineractClient()).createStaff().getResourceId();
        tellerId = ok(() -> fineractClient().tellerCashManagement()
                .createTeller(new PostTellersRequest().officeId(1L).name(Utils.uniqueRandomStringGenerator("Teller ", 6))
                        .description("Part-day cashier tests").status(300).startDate(START).dateFormat(DATE_FORMAT).locale("en")))
                .getResourceId();
    }

    private PostTellersTellerIdCashiersRequest partDay(final long startHour, final long endHour) {
        return new PostTellersTellerIdCashiersRequest().staffId(staffId).description(Utils.uniqueRandomStringGenerator("Part day ", 4))
                .isFullDay(false).hourStartTime(startHour).minStartTime(0L).hourEndTime(endHour).minEndTime(0L).startDate(START)
                .endDate(END).dateFormat(DATE_FORMAT).locale("en");
    }

    private void create(final PostTellersTellerIdCashiersRequest request) {
        ok(() -> fineractClient().tellerCashManagement().createCashierForTeller(tellerId, request));
    }

    private void assertAlreadyAllocated(final CallFailedRuntimeException failure) {
        assertEquals(403, failure.getStatus());
        // The top-level code is the generic domain rule violation; the specific one is in the error body
        assertTrue(failure.getMessage().contains(ALREADY_ALLOCATED), failure.getMessage());
    }

    private List<CashierData> cashiers() {
        return ok(() -> fineractClient().tellerCashManagement().retrieveAllCashiersForTeller(tellerId, (String) null, (String) null))
                .getCashiers();
    }

    @Test
    public void partDayCashierIsCreated() {
        create(partDay(9, 13));
        assertEquals(1, cashiers().size());
        assertEquals(Boolean.FALSE, cashiers().get(0).getIsFullDay());
    }

    @Test
    public void overlappingPartDayCashierIsRejectedAndSeparateShiftIsCreated() {
        create(partDay(9, 13));
        assertAlreadyAllocated(
                FeignCalls.fail(() -> fineractClient().tellerCashManagement().createCashierForTeller(tellerId, partDay(12, 17))));
        create(partDay(14, 17));
        assertEquals(2, cashiers().size());
    }

    @Test
    public void shiftInsideAnotherShiftIsRejected() {
        create(partDay(8, 18));
        assertAlreadyAllocated(
                FeignCalls.fail(() -> fineractClient().tellerCashManagement().createCashierForTeller(tellerId, partDay(10, 12))));
        assertEquals(1, cashiers().size());
    }

    @Test
    public void shiftEndingBeforeItStartsIsRejected() {
        // Shifts past midnight are not supported
        final CallFailedRuntimeException failure = FeignCalls
                .fail(() -> fineractClient().tellerCashManagement().createCashierForTeller(tellerId, partDay(22, 2)));
        assertEquals(400, failure.getStatus());
        assertTrue(failure.getMessage().contains(END_BEFORE_START), failure.getMessage());
    }

    @Test
    public void updateIntoAnOverlappingShiftIsRejected() {
        create(partDay(9, 13));
        create(partDay(14, 17));
        final Long afternoonId = cashiers().stream().filter(c -> "14:00".equals(c.getStartTime())).findFirst().orElseThrow().getId();

        // Moving the afternoon shift to 12:00-17:00 would overlap the morning one
        assertAlreadyAllocated(FeignCalls
                .fail(() -> fineractClient().tellerCashManagement().updateCashierForTeller(tellerId, afternoonId, partDayUpdate(12, 17))));
        // A change that creates no overlap still works, and is not compared with itself
        ok(() -> fineractClient().tellerCashManagement().updateCashierForTeller(tellerId, afternoonId, partDayUpdate(15, 17)));
    }

    private PutTellersTellerIdCashiersCashierIdRequest partDayUpdate(final long startHour, final long endHour) {
        return new PutTellersTellerIdCashiersCashierIdRequest().staffId(staffId).description("Afternoon").isFullDay(false)
                .hourStartTime(startHour).minStartTime(0L).hourEndTime(endHour).minEndTime(0L).startDate(START).endDate(END)
                .dateFormat(DATE_FORMAT).locale("en");
    }
}
