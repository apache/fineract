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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.security.SecureRandom;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.WorkingDaysData;
import org.apache.fineract.client.models.WorkingDaysUpdateRequest;
import org.apache.fineract.client.models.WorkingDaysUpdateResponse;

public final class WorkingDaysHelper {

    private WorkingDaysHelper() {

    }

    private static final SecureRandom random = new SecureRandom();

    public static WorkingDaysUpdateResponse updateWorkingDays() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingDays().updateWorkingDay(getUpdateWorkingDaysRequest()));
    }

    public static CallFailedRuntimeException updateWorkingDaysWithWrongRecurrence() {
        return fail(() -> FineractFeignClientHelper.getFineractFeignClient().workingDays()
                .updateWorkingDay(getUpdateWorkingDaysWithWrongRecurrenceRequest()));
    }

    public static WorkingDaysUpdateResponse updateWorkingDaysWeekDays() {
        return ok(
                () -> FineractFeignClientHelper.getFineractFeignClient().workingDays().updateWorkingDay(getUpdateWorkingWeekDaysRequest()));
    }

    public static WorkingDaysUpdateRequest getUpdateWorkingWeekDaysRequest() {
        return new WorkingDaysUpdateRequest().recurrence("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR").repaymentRescheduleType(2)
                .extendTermForDailyRepayments(false);
    }

    public static WorkingDaysUpdateRequest getUpdateWorkingDaysRequest() {
        return new WorkingDaysUpdateRequest().recurrence("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU")
                .repaymentRescheduleType(random.nextInt(4) + 1).extendTermForDailyRepayments(false);
    }

    public static WorkingDaysUpdateRequest getUpdateWorkingDaysWithWrongRecurrenceRequest() {
        return new WorkingDaysUpdateRequest().recurrence("FREQ=WEEKLY;INTERVAL=1;BYDAY=MP,TI,TE,TH")
                .repaymentRescheduleType(random.nextInt(4) + 1).extendTermForDailyRepayments(false);
    }

    public static int workingDaysId() {
        return Math.toIntExact(getAllWorkingDays().getId());
    }

    public static WorkingDaysData getAllWorkingDays() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingDays().retrieveAllWorkingDays());
    }

}
