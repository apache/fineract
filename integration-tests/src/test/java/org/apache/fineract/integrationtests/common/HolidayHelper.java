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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.client.models.GetHolidaysResponse;
import org.apache.fineract.client.models.PostHolidaysRequest;
import org.apache.fineract.client.models.PostHolidaysRequestOffices;

public final class HolidayHelper {

    private HolidayHelper() {

    }

    private static final Long OFFICE_ID = 1L;

    public static PostHolidaysRequest getCreateHolidayRequest() {
        return new PostHolidaysRequest().offices(List.of(new PostHolidaysRequestOffices().officeId(OFFICE_ID))).locale("en")
                .dateFormat("yyyy-MM-dd").name(Utils.uniqueRandomStringGenerator("HOLIDAY_", 5)).fromDate(LocalDate.of(2013, 4, 1))
                .toDate(LocalDate.of(2013, 4, 1)).repaymentsRescheduledTo(LocalDate.of(2013, 4, 8)).reschedulingType(2);
    }

    public static PostHolidaysRequest getCreateType1HolidayRequest() {
        return new PostHolidaysRequest().offices(List.of(new PostHolidaysRequestOffices().officeId(OFFICE_ID))).locale("en")
                .dateFormat("yyyy-MM-dd").name(Utils.uniqueRandomStringGenerator("HOLIDAY_", 5)).fromDate(LocalDate.of(2024, 4, 4))
                .toDate(LocalDate.of(2024, 4, 4)).reschedulingType(1);
    }

    public static Long createHolidays() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().holidays().createHoliday(getCreateHolidayRequest()))
                .getResourceId();
    }

    public static Long createTyoe1Holidays() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().holidays().createHoliday(getCreateType1HolidayRequest()))
                .getResourceId();
    }

    public static Long activateHolidays(final Long holidayId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().holidays().handleCommandsHoliday(holidayId,
                Collections.emptyMap(), "activate")).getResourceId();
    }

    public static GetHolidaysResponse getHolidayById(final Long holidayId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().holidays().retrieveOneHoliday(holidayId));
    }

    public static Long deleteHoliday(final Long holidayId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().holidays().deleteHoliday(holidayId)).getResourceId();
    }

}
